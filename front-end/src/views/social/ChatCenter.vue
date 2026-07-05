<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
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
  return '单向好友聊天'
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
  await fetchMessages()
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

const fetchMessages = async () => {
  if (!active.value) return
  loadingMessages.value = true
  try {
    const res = await request.get('/chat/messages', {
      conversationType: active.value.conversationType,
      targetId: active.value.targetId,
      limit: 80,
    })
    messages.value = res.data || []
    await markRead()
    await nextTick()
    scrollBottom()
  } finally {
    loadingMessages.value = false
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
  await fetchMessages()
  await fetchConversations()
}

const markRead = async () => {
  if (!active.value || !messages.value.length) return
  await request.post('/chat/messages/read', {
    conversationType: active.value.conversationType,
    groupId: active.value.conversationType === 'GROUP' ? active.value.targetId : null,
    friendId: active.value.conversationType === 'PRIVATE' ? active.value.targetId : null,
    messageIds: messages.value.map(item => item.id),
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
  <div class="min-h-screen bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-white" :class="{ dark: isDark }">
    <Nav :isDark="isDark" :menuItems="menuItems" />

    <main class="mx-auto grid h-screen max-w-7xl grid-rows-[auto_1fr] px-4 pb-5 pt-24 sm:px-6 lg:px-8">
      <section class="mb-4 flex flex-col gap-3 rounded-2xl border border-white/70 bg-white/90 p-4 shadow-sm dark:border-white/10 dark:bg-slate-900/90 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 class="text-2xl font-bold">在线聊天</h1>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">支持群号入群、账号加好友、群聊已读和好友已读。</p>
        </div>
        <div class="flex items-center gap-3">
          <span class="rounded-full bg-rose-100 px-3 py-1 text-sm font-medium text-rose-700">未读 {{ totalUnread }}</span>
          <button class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-500" @click="createGroupVisible = true">创建群聊</button>
        </div>
      </section>

      <section class="chat-shell">
        <aside class="chat-side">
          <div class="space-y-3 border-b border-slate-100 p-4 dark:border-white/10">
            <div class="flex gap-2">
              <input v-model="userKeyword" class="search-input" placeholder="搜索用户账号/昵称" @keyup.enter="searchUsers">
              <button class="search-btn" @click="searchUsers">查找</button>
            </div>
            <div v-if="userResults.length" class="result-list">
              <button v-for="user in userResults" :key="user.id" class="result-item" @click="addFriend(user)">
                <span>{{ user.name || user.username }}</span>
                <small>@{{ user.username }}</small>
              </button>
            </div>

            <div class="flex gap-2">
              <input v-model="groupKeyword" class="search-input" placeholder="搜索群号加入" @keyup.enter="searchGroups">
              <button class="search-btn" @click="searchGroups">搜群</button>
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
              <div class="avatar">{{ item.conversationType === 'GROUP' ? '群' : (item.title || '友').slice(0, 1) }}</div>
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
            <button v-if="active" class="rounded-lg border border-slate-200 px-3 py-2 text-sm dark:border-white/10" @click="fetchMessages">刷新</button>
          </header>

          <div ref="messageListRef" class="message-list">
            <div v-if="!active" class="empty-state">选择一个好友或群聊开始聊天</div>
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
            <button @click="sendMessage">发送</button>
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
        <button class="rounded-lg border px-4 py-2" @click="createGroupVisible = false">取消</button>
        <button class="rounded-lg bg-blue-600 px-4 py-2 text-white" @click="createGroup">创建</button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.chat-shell {
  display: grid;
  min-height: 0;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 18px;
  background: white;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
  grid-template-columns: 360px minmax(0, 1fr);
}

.dark .chat-shell {
  border-color: rgba(255, 255, 255, 0.08);
  background: #0f172a;
}

.chat-side {
  min-height: 0;
  overflow-y: auto;
  border-right: 1px solid #e2e8f0;
}

.dark .chat-side {
  border-color: rgba(255, 255, 255, 0.08);
}

.search-input,
.dialog-input {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  background: white;
  padding: 9px 11px;
  outline: none;
}

.dark .search-input,
.dark .dialog-input {
  border-color: rgba(255, 255, 255, 0.14);
  background: #1e293b;
  color: white;
}

.search-btn {
  flex: 0 0 auto;
  border-radius: 10px;
  background: #2563eb;
  padding: 0 12px;
  color: white;
  font-size: 13px;
  font-weight: 700;
}

.result-list {
  display: grid;
  gap: 6px;
}

.result-item,
.plain-item,
.conversation-item {
  width: 100%;
  border-radius: 12px;
  padding: 10px;
  text-align: left;
  transition: background 0.18s ease;
}

.result-item,
.plain-item {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  background: #f8fafc;
}

.dark .result-item,
.dark .plain-item {
  background: #1e293b;
}

.result-item small,
.plain-item small,
.conversation-item small {
  color: #94a3b8;
  font-size: 12px;
}

.side-section {
  display: grid;
  gap: 8px;
  padding: 14px;
}

.side-title {
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.conversation-item:hover,
.conversation-item.active {
  background: #eff6ff;
}

.dark .conversation-item:hover,
.dark .conversation-item.active {
  background: #1e3a8a;
}

.avatar {
  display: grid;
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  place-items: center;
  border-radius: 12px;
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 800;
}

.conversation-item strong {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-item p {
  overflow: hidden;
  margin-top: 2px;
  color: #64748b;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread {
  display: grid;
  min-width: 22px;
  height: 22px;
  place-items: center;
  border-radius: 999px;
  background: #ef4444;
  color: white;
  font-size: 12px;
}

.chat-main {
  display: grid;
  min-height: 0;
  grid-template-rows: auto minmax(0, 1fr) auto;
}

.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
  padding: 16px 18px;
}

.dark .chat-head {
  border-color: rgba(255, 255, 255, 0.08);
}

.chat-head h2 {
  font-size: 18px;
  font-weight: 800;
}

.chat-head p {
  margin-top: 2px;
  color: #64748b;
  font-size: 13px;
}

.message-list {
  min-height: 0;
  overflow-y: auto;
  padding: 18px;
}

.empty-state {
  display: grid;
  height: 100%;
  min-height: 320px;
  place-items: center;
  color: #94a3b8;
}

.message-row {
  display: flex;
  margin-bottom: 12px;
}

.message-row.mine {
  justify-content: flex-end;
}

.message-bubble {
  max-width: min(620px, 76%);
  border-radius: 14px;
  background: #f1f5f9;
  padding: 10px 12px;
}

.message-row.mine .message-bubble {
  background: #dbeafe;
}

.dark .message-bubble {
  background: #1e293b;
}

.dark .message-row.mine .message-bubble {
  background: #1d4ed8;
}

.message-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  color: #64748b;
  font-size: 12px;
}

.message-bubble p {
  margin-top: 6px;
  white-space: pre-wrap;
  word-break: break-word;
}

.read-line {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 11px;
  text-align: right;
}

.chat-input {
  display: grid;
  gap: 10px;
  border-top: 1px solid #e2e8f0;
  padding: 14px;
}

.dark .chat-input {
  border-color: rgba(255, 255, 255, 0.08);
}

.chat-input textarea {
  width: 100%;
  resize: none;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 11px;
  outline: none;
}

.dark .chat-input textarea {
  border-color: rgba(255, 255, 255, 0.14);
  background: #1e293b;
  color: white;
}

.chat-input button {
  justify-self: end;
  border-radius: 10px;
  background: #2563eb;
  padding: 9px 22px;
  color: white;
  font-weight: 800;
}

@media (max-width: 960px) {
  .chat-shell {
    grid-template-columns: 1fr;
  }

  .chat-side {
    max-height: 42vh;
    border-right: 0;
    border-bottom: 1px solid #e2e8f0;
  }
}
</style>
