<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { Bell, ChatDotRound, Plus, Promotion, Refresh, Search, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request.js'
import { getStoredTheme, ThemeType } from '@/utils/theme.js'

const isDark = ref(getStoredTheme() === ThemeType.DARK)
const conversations = ref([])
const friends = ref([])
const groups = ref([])
const messages = ref([])
const active = ref(null)
const messageText = ref('')
const userKeyword = ref('')
const groupKeyword = ref('')
const userResults = ref([])
const groupResults = ref([])
const createGroupVisible = ref(false)
const groupForm = ref({ name: '', announcement: '', searchable: true })
const loadingMessages = ref(false)
const messagesLoaded = ref(false)
const refreshingMessages = ref(false)
const listTimer = ref(null)
const msgTimer = ref(null)
const messageListRef = ref(null)

const menuItems = [
  { key: 'chat', label: '在线聊天', icon: '💬', path: '/Chat' },
  { key: 'checkin', label: '每日打卡', icon: '✅', path: '/DailyCheckin' },
  { key: 'records', label: '记录拾光', icon: '📝', path: '/Records' },
]

const activeTitle = computed(() => active.value?.title || '选择会话')
const activeSubTitle = computed(() => {
  if (!active.value) return '选择好友或群聊后开始沟通'
  if (active.value.conversationType === 'GROUP') return `群号 ${active.value.groupNo || active.value.targetId}`
  return '好友私聊'
})

const totalUnread = computed(() => conversations.value.reduce((sum, item) => sum + (item.unreadCount || 0), 0))

const fetchAll = async () => {
  await Promise.all([fetchConversations(), fetchFriends(), fetchGroups()])
}

const fetchConversations = async () => {
  const res = await request.get('/chat/conversations')
  conversations.value = res.data || []
}

const fetchFriends = async () => {
  const res = await request.get('/chat/friends')
  friends.value = res.data || []
}

const fetchGroups = async () => {
  const res = await request.get('/chat/groups')
  groups.value = res.data || []
}

const searchUsers = async () => {
  if (!userKeyword.value.trim()) {
    userResults.value = []
    return
  }
  const res = await request.get('/chat/users/search', { keyword: userKeyword.value.trim() })
  userResults.value = res.data || []
}

const addFriend = async (user) => {
  await request.post(`/chat/friends/${user.id}`, {})
  ElMessage.success('好友已添加')
  userResults.value = []
  userKeyword.value = ''
  await fetchAll()
}

const searchGroups = async () => {
  if (!groupKeyword.value.trim()) {
    groupResults.value = []
    return
  }
  const res = await request.get('/chat/groups/search', { groupNo: groupKeyword.value.trim() })
  groupResults.value = res.data || []
}

const joinGroup = async (group) => {
  await request.post(`/chat/groups/join/${group.groupNo}`, {})
  ElMessage.success('已加入群聊')
  groupResults.value = []
  groupKeyword.value = ''
  await fetchAll()
}

const createGroup = async () => {
  if (!groupForm.value.name.trim()) return ElMessage.warning('请输入群名称')
  const res = await request.post('/chat/groups', groupForm.value)
  ElMessage.success(`群聊已创建，群号 ${res.data?.groupNo || ''}`)
  createGroupVisible.value = false
  groupForm.value = { name: '', announcement: '', searchable: true }
  await fetchAll()
}

const openConversation = async (item) => {
  active.value = item
  messages.value = []
  messagesLoaded.value = false
  await fetchMessages({ showLoading: true, scrollToLatest: true })
}

const openFriend = (friend) => {
  openConversation({
    conversationType: 'PRIVATE',
    targetId: friend.friendId,
    title: friend.name || friend.username || `用户${friend.friendId}`,
    avatar: friend.avatar,
  })
}

const openGroup = (group) => {
  openConversation({
    conversationType: 'GROUP',
    targetId: group.id,
    title: group.name,
    groupNo: group.groupNo,
  })
}

const hasMessageChanges = (nextMessages, currentMessages) => {
  if (nextMessages.length !== currentMessages.length) return true
  return nextMessages.some((message, index) => {
    const current = currentMessages[index]
    return !current
      || message.id !== current.id
      || message.content !== current.content
      || message.readCount !== current.readCount
      || message.unreadCount !== current.unreadCount
  })
}

const fetchMessages = async ({ showLoading = false, scrollToLatest = false } = {}) => {
  if (!active.value || refreshingMessages.value) return
  const conversation = { ...active.value }
  refreshingMessages.value = true
  if (showLoading) loadingMessages.value = true
  try {
    const res = await request.get('/chat/messages', {
      conversationType: conversation.conversationType,
      targetId: conversation.targetId,
      limit: 80,
    })
    if (!sameConversation(conversation)) return

    const nextMessages = res.data || []
    const changed = hasMessageChanges(nextMessages, messages.value)
    if (changed) messages.value = nextMessages
    messagesLoaded.value = true

    if (changed && nextMessages.some(message => !message.mine && message.unreadCount > 0)) {
      await markRead(nextMessages)
    }
    if (changed && scrollToLatest) {
      await nextTick()
      scrollBottom()
    }
  } finally {
    if (showLoading) loadingMessages.value = false
    refreshingMessages.value = false
  }
}
const sendMessage = async () => {
  if (!active.value) return ElMessage.warning('请先选择会话')
  const content = messageText.value.trim()
  if (!content) return
  await request.post('/chat/messages', {
    conversationType: active.value.conversationType,
    groupId: active.value.conversationType === 'GROUP' ? active.value.targetId : null,
    receiverId: active.value.conversationType === 'PRIVATE' ? active.value.targetId : null,
    contentType: 'TEXT',
    content,
  })
  messageText.value = ''
  await fetchMessages({ scrollToLatest: true })
  await fetchConversations()
}

const markRead = async (messageList) => {
  if (!active.value || !messageList.length) return
  await request.post('/chat/messages/read', {
    conversationType: active.value.conversationType,
    groupId: active.value.conversationType === 'GROUP' ? active.value.targetId : null,
    friendId: active.value.conversationType === 'PRIVATE' ? active.value.targetId : null,
    messageIds: messageList.map(item => item.id),
  })
  fetchConversations()
}

const scrollBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const sameConversation = (item) => active.value
  && item.conversationType === active.value.conversationType
  && item.targetId === active.value.targetId

const formatTime = (value) => value ? value.replace('T', ' ').slice(5, 16) : ''

onMounted(() => {
  fetchAll()
  listTimer.value = setInterval(fetchConversations, 10000)
  msgTimer.value = setInterval(() => {
    if (active.value) fetchMessages()
  }, 5000)
})

onUnmounted(() => {
  clearInterval(listTimer.value)
  clearInterval(msgTimer.value)
})
</script>

<template>
  <div class="app-shell app-page-bg min-h-screen" :class="{ dark: isDark }">
    <Nav :isDark="isDark" :menuItems="menuItems" />

    <main class="mx-auto grid h-screen max-w-7xl grid-rows-[auto_1fr] px-4 pb-5 pt-24 sm:px-6 lg:px-8">
      <section class="chat-toolbar">
        <div class="chat-title">
          <span class="chat-title-icon"><ChatDotRound /></span>
          <div>
            <span class="toolbar-kicker">Conversation</span>
            <h1>在线聊天</h1>
            <p>和好友保持联系，也可以通过群号找到学习伙伴。</p>
          </div>
        </div>
        <div class="toolbar-actions">
          <span class="unread-summary"><Bell /> 未读 {{ totalUnread }}</span>
          <button class="create-group-button" type="button" @click="createGroupVisible = true">
            <Plus />
            <span>创建群聊</span>
          </button>
        </div>
      </section>

      <section class="chat-shell">
        <aside class="chat-side">
          <div class="space-y-3 border-b border-slate-100 p-4 dark:border-white/10">
            <div class="flex gap-2">
              <input v-model="userKeyword" class="search-input" placeholder="搜索用户账号/昵称" @keyup.enter="searchUsers">
              <button class="search-btn" type="button" title="查找用户" aria-label="查找用户" @click="searchUsers"><Search /></button>
            </div>
            <div v-if="userResults.length" class="result-list">
              <button v-for="user in userResults" :key="user.id" class="result-item" @click="addFriend(user)">
                <span>{{ user.name || user.username }}</span>
                <small>@{{ user.username }}</small>
              </button>
            </div>

            <div class="flex gap-2">
              <input v-model="groupKeyword" class="search-input" placeholder="搜索群号加入" @keyup.enter="searchGroups">
              <button class="search-btn" type="button" title="搜索群聊" aria-label="搜索群聊" @click="searchGroups"><Search /></button>
            </div>
            <div v-if="groupResults.length" class="result-list">
              <button v-for="group in groupResults" :key="group.id" class="result-item" @click="joinGroup(group)">
                <span>{{ group.name }}</span>
                <small>{{ group.groupNo }} · {{ group.memberCount }}人</small>
              </button>
            </div>
          </div>

          <div class="side-section">
            <div class="side-title">最近会话</div>
            <button
              v-for="item in conversations"
              :key="`${item.conversationType}-${item.targetId}`"
              class="conversation-item"
              :class="{ active: sameConversation(item) }"
              @click="openConversation(item)"
            >
              <div class="avatar"><ChatDotRound v-if="item.conversationType === 'GROUP'" /><UserFilled v-else /></div>
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between gap-2">
                  <strong>{{ item.title }}</strong>
                  <small>{{ formatTime(item.lastMessageAt) }}</small>
                </div>
                <p>{{ item.lastMessage || '暂无消息' }}</p>
              </div>
              <span v-if="item.unreadCount" class="unread">{{ item.unreadCount }}</span>
            </button>
          </div>

          <div class="side-section">
            <div class="side-title">好友</div>
            <button v-for="friend in friends" :key="friend.friendId" class="plain-item" @click="openFriend(friend)">
              <span>{{ friend.name || friend.username || friend.friendId }}</span>
              <small v-if="friend.unreadCount">{{ friend.unreadCount }} 未读</small>
            </button>
          </div>

          <div class="side-section">
            <div class="side-title">我的群聊</div>
            <button v-for="group in groups" :key="group.id" class="plain-item" @click="openGroup(group)">
              <span>{{ group.name }}</span>
              <small>{{ group.groupNo }}</small>
            </button>
          </div>
        </aside>

        <section class="chat-main">
          <header class="chat-head">
            <div>
              <h2>{{ activeTitle }}</h2>
              <p>{{ activeSubTitle }}</p>
            </div>
            <button v-if="active" class="icon-refresh" type="button" title="刷新消息" aria-label="刷新消息" @click="fetchMessages"><Refresh /></button>
          </header>

          <div ref="messageListRef" class="message-list">
            <div v-if="!active" class="empty-state"><ChatDotRound /><strong>选择一个会话</strong><span>从好友或群聊中开始一段交流</span></div>
            <div v-else-if="loadingMessages" class="empty-state">消息加载中...</div>
            <div v-else-if="!messages.length" class="empty-state">还没有消息</div>
            <template v-else>
              <div v-for="msg in messages" :key="msg.id" class="message-row" :class="{ mine: msg.mine }">
                <div class="message-bubble">
                  <div class="message-meta">
                    <span>{{ msg.senderName || `用户${msg.senderId}` }}</span>
                    <small>{{ formatTime(msg.createdAt) }}</small>
                  </div>
                  <p>{{ msg.content }}</p>
                  <div class="read-line">
                    <span v-if="active.conversationType === 'GROUP'">已读 {{ msg.readCount || 0 }} / 未读 {{ msg.unreadCount || 0 }}</span>
                    <span v-else>{{ msg.unreadCount === 0 ? '对方已读' : '送达' }}</span>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <footer class="chat-input">
            <textarea v-model="messageText" rows="3" placeholder="输入消息，Enter 发送，Shift+Enter 换行" @keydown.enter.exact.prevent="sendMessage"></textarea>
            <button type="button" title="发送消息" @click="sendMessage"><Promotion /><span>发送</span></button>
          </footer>
        </section>
      </section>
    </main>

    <el-dialog v-model="createGroupVisible" title="创建群聊" width="520px">
      <div class="space-y-4">
        <label class="block">
          <span class="mb-1 block text-sm text-slate-500">群名称</span>
          <input v-model="groupForm.name" class="dialog-input" maxlength="80">
        </label>
        <label class="block">
          <span class="mb-1 block text-sm text-slate-500">群公告</span>
          <textarea v-model="groupForm.announcement" class="dialog-input" rows="3" maxlength="500"></textarea>
        </label>
        <label class="flex items-center gap-2">
          <input v-model="groupForm.searchable" type="checkbox">
          <span>允许通过群号搜索加入</span>
        </label>
      </div>
      <template #footer>
        <button class="dialog-secondary" type="button" @click="createGroupVisible = false">取消</button>
        <button class="dialog-primary" type="button" @click="createGroup"><Plus /> 创建</button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.chat-toolbar,
.chat-title,
.toolbar-actions,
.unread-summary,
.create-group-button,
.chat-head,
.message-meta,
.chat-input button,
.dialog-primary,
.conversation-item,
.plain-item,
.result-item {
  display: flex;
  align-items: center;
}

.chat-toolbar {
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
  animation: chat-rise 520ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.chat-title {
  gap: 12px;
}

.chat-title-icon {
  display: grid;
  width: 46px;
  height: 46px;
  flex: 0 0 46px;
  place-items: center;
  border-radius: 8px;
  background: var(--theme-primary);
  color: white;
  box-shadow: 0 14px 26px -18px rgb(var(--theme-primary-rgb) / 0.9);
}

.chat-title-icon svg {
  width: 24px;
  height: 24px;
}

.toolbar-kicker {
  color: var(--theme-primary);
  font-size: 11px;
  font-weight: 800;
  text-transform: uppercase;
}

.chat-title h1 {
  color: var(--app-text);
  font-size: 25px;
  font-weight: 800;
  line-height: 1.15;
}

.chat-title p {
  margin-top: 3px;
  color: var(--app-text-muted);
  font-size: 13px;
}

.toolbar-actions {
  gap: 10px;
}

.unread-summary,
.create-group-button,
.dialog-primary,
.dialog-secondary {
  min-height: 40px;
  border-radius: 8px;
  padding: 0 14px;
  font-size: 13px;
  font-weight: 750;
}

.unread-summary {
  gap: 6px;
  border: 1px solid var(--app-border);
  background: var(--app-card-solid);
  color: var(--app-text-secondary);
}

.unread-summary svg,
.create-group-button svg,
.dialog-primary svg {
  width: 16px;
  height: 16px;
}

.create-group-button,
.dialog-primary {
  gap: 7px;
  border: 1px solid var(--theme-primary);
  background: var(--theme-primary);
  color: white;
  box-shadow: 0 12px 24px -18px rgb(var(--theme-primary-rgb) / 0.9);
  transition: transform 280ms cubic-bezier(0.16, 1, 0.3, 1), box-shadow 280ms ease;
}

.create-group-button:hover,
.dialog-primary:hover {
  box-shadow: 0 16px 28px -18px rgb(var(--theme-primary-rgb) / 0.95);
  transform: translateY(-2px);
}

.chat-shell {
  display: grid;
  min-height: 0;
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-card-solid);
  box-shadow: var(--app-elevation-float);
  grid-template-columns: 340px minmax(0, 1fr);
  animation: chat-rise 620ms 70ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.chat-side {
  min-height: 0;
  overflow-y: auto;
  border-right: 1px solid var(--app-border);
  background: color-mix(in srgb, var(--app-card-solid) 94%, var(--theme-primary) 6%);
}

.search-input,
.dialog-input,
.chat-input textarea {
  width: 100%;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-card-solid);
  color: var(--app-text);
  outline: none;
  transition: border-color 200ms ease, box-shadow 240ms ease;
}

.search-input,
.dialog-input {
  padding: 9px 11px;
}

.search-input:focus,
.dialog-input:focus,
.chat-input textarea:focus {
  border-color: var(--theme-primary);
  box-shadow: 0 0 0 3px rgb(var(--theme-primary-rgb) / 0.12);
}

.search-btn,
.icon-refresh {
  display: grid;
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  place-items: center;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: rgb(var(--theme-primary-rgb) / 0.09);
  color: var(--theme-primary);
  transition: transform 260ms cubic-bezier(0.16, 1, 0.3, 1), background 200ms ease;
}

.search-btn:hover,
.icon-refresh:hover {
  background: rgb(var(--theme-primary-rgb) / 0.16);
  transform: translateY(-2px) rotate(-3deg);
}

.search-btn svg,
.icon-refresh svg {
  width: 17px;
  height: 17px;
}

.result-list,
.side-section,
.chat-input {
  display: grid;
  gap: 8px;
}

.result-item,
.plain-item,
.conversation-item {
  width: 100%;
  border-radius: 8px;
  text-align: left;
  transition: transform 280ms cubic-bezier(0.16, 1, 0.3, 1), background 200ms ease, box-shadow 280ms ease;
}

.result-item,
.plain-item {
  justify-content: space-between;
  gap: 10px;
  border: 1px solid transparent;
  background: rgb(var(--theme-primary-rgb) / 0.055);
  padding: 10px;
}

.result-item:hover,
.plain-item:hover {
  border-color: var(--app-border);
  background: rgb(var(--theme-primary-rgb) / 0.1);
  transform: translateX(3px);
}

.result-item small,
.plain-item small,
.conversation-item small {
  color: var(--app-text-muted);
  font-size: 11px;
}

.side-section {
  padding: 14px;
}

.side-title {
  color: var(--app-text-muted);
  font-size: 11px;
  font-weight: 800;
  text-transform: uppercase;
}

.conversation-item {
  gap: 10px;
  border: 1px solid transparent;
  padding: 10px;
}

.conversation-item:hover,
.conversation-item.active {
  border-color: var(--app-border);
  background: rgb(var(--theme-primary-rgb) / 0.1);
  box-shadow: 0 10px 20px -18px rgb(var(--theme-primary-rgb) / 0.7);
  transform: translateX(3px);
}

.avatar {
  display: grid;
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  place-items: center;
  border-radius: 8px;
  background: rgb(var(--theme-primary-rgb) / 0.13);
  color: var(--theme-primary);
}

.avatar svg {
  width: 18px;
  height: 18px;
}

.conversation-item strong {
  display: block;
  overflow: hidden;
  color: var(--app-text);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-item p {
  overflow: hidden;
  margin-top: 2px;
  color: var(--app-text-muted);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread {
  display: grid;
  min-width: 20px;
  height: 20px;
  place-items: center;
  border-radius: 7px;
  background: var(--theme-secondary);
  color: white;
  font-size: 11px;
  animation: unread-pop 420ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.chat-main {
  display: grid;
  min-height: 0;
  grid-template-rows: auto minmax(0, 1fr) auto;
}

.chat-head {
  justify-content: space-between;
  border-bottom: 1px solid var(--app-border);
  padding: 15px 18px;
}

.chat-head h2 {
  color: var(--app-text);
  font-size: 17px;
  font-weight: 800;
}

.chat-head p {
  margin-top: 2px;
  color: var(--app-text-muted);
  font-size: 12px;
}

.message-list {
  min-height: 0;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;
}

.empty-state {
  display: grid;
  height: 100%;
  min-height: 300px;
  place-items: center;
  align-content: center;
  gap: 8px;
  color: var(--app-text-muted);
}

.empty-state svg {
  width: 34px;
  color: var(--theme-primary);
}

.empty-state strong {
  color: var(--app-text-secondary);
}

.empty-state span {
  font-size: 12px;
}

.message-row {
  display: flex;
  margin-bottom: 12px;
  animation: message-arrive 360ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.message-row.mine {
  justify-content: flex-end;
}

.message-bubble {
  max-width: min(620px, 76%);
  border: 1px solid var(--app-border);
  border-radius: 8px 8px 8px 3px;
  background: color-mix(in srgb, var(--app-card-solid) 92%, var(--theme-primary) 8%);
  padding: 10px 12px;
  box-shadow: 0 10px 22px -20px rgb(15 23 42 / 0.4);
}

.message-row.mine .message-bubble {
  border-color: transparent;
  border-radius: 8px 8px 3px 8px;
  background: var(--theme-primary);
  color: white;
}

.message-meta {
  justify-content: space-between;
  gap: 14px;
  color: var(--app-text-muted);
  font-size: 11px;
}

.message-row.mine .message-meta,
.message-row.mine .read-line {
  color: rgb(255 255 255 / 0.72);
}

.message-bubble p {
  margin-top: 6px;
  white-space: pre-wrap;
  word-break: break-word;
}

.read-line {
  margin-top: 6px;
  color: var(--app-text-muted);
  font-size: 10px;
  text-align: right;
}

.chat-input {
  border-top: 1px solid var(--app-border);
  padding: 14px;
}

.chat-input textarea {
  resize: none;
  padding: 11px;
}

.chat-input button {
  justify-self: end;
  gap: 7px;
  min-height: 40px;
  border-radius: 8px;
  background: var(--theme-primary);
  padding: 0 18px;
  color: white;
  font-weight: 800;
  box-shadow: 0 12px 24px -18px rgb(var(--theme-primary-rgb) / 0.9);
  transition: transform 260ms cubic-bezier(0.16, 1, 0.3, 1), box-shadow 260ms ease;
}

.chat-input button:hover {
  box-shadow: 0 16px 28px -18px rgb(var(--theme-primary-rgb) / 0.95);
  transform: translateY(-2px);
}

.chat-input button:active {
  transform: scale(0.96);
}

.chat-input button svg {
  width: 17px;
  height: 17px;
}

.dialog-secondary {
  border: 1px solid var(--app-border);
  background: var(--app-card-solid);
  color: var(--app-text-secondary);
}

@keyframes chat-rise {
  from { opacity: 0; transform: translateY(14px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes message-arrive {
  from { opacity: 0; transform: translateY(8px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes unread-pop {
  0% { opacity: 0; transform: scale(0.62); }
  70% { transform: scale(1.08); }
  100% { opacity: 1; transform: scale(1); }
}

@media (max-width: 960px) {
  .chat-shell {
    grid-template-columns: 1fr;
  }

  .chat-side {
    max-height: 42vh;
    border-right: 0;
    border-bottom: 1px solid var(--app-border);
  }
}

@media (max-width: 640px) {
  .chat-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .toolbar-actions,
  .create-group-button {
    width: 100%;
  }

  .toolbar-actions {
    align-items: stretch;
  }

  .create-group-button {
    justify-content: center;
  }

  .unread-summary {
    white-space: nowrap;
  }

  .message-bubble {
    max-width: 88%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .chat-toolbar,
  .chat-shell,
  .message-row,
  .unread {
    animation: none;
  }

  .conversation-item:hover,
  .result-item:hover,
  .plain-item:hover,
  .create-group-button:hover,
  .chat-input button:hover {
    transform: none;
  }
}
</style>
