<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { Bell, ChatDotRound, Document, EditPen, FolderOpened, Picture, Plus, Promotion, Refresh, Search, UserFilled } from '@element-plus/icons-vue'
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
const chatSocket = ref(null)
const socketStatus = ref('connecting')
const reconnectTimer = ref(null)
const messageListRef = ref(null)
const fileInputRef = ref(null)
const remarkEditorVisible = ref(false)
const remarkText = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)
const groupManageVisible = ref(false)
const groupMembers = ref([])
const groupMemberKeyword = ref('')
const groupMemberResults = ref([])
const emojiPickerVisible = ref(false)
const emojis = ['😀', '😁', '😂', '😍', '🤔', '👍', '👏', '🎉', '💪', '🔥', '📚', '✨']
let socketStopped = false
let reconnectAttempt = 0

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
const activeLevelText = computed(() => active.value?.conversationType === 'PRIVATE'
  ? `Lv.${active.value.level || 1}${active.value.levelName ? ` · ${active.value.levelName}` : ''}` : '')
const isGroupOwner = computed(() => active.value?.conversationType === 'GROUP' && active.value?.role === 'OWNER')
const isGroupManager = computed(() => active.value?.conversationType === 'GROUP'
  && ['OWNER', 'ADMIN'].includes(active.value?.role))
const activeMuted = computed(() => active.value?.conversationType === 'GROUP' && active.value?.role !== 'OWNER'
  && (active.value?.mutedAll || (active.value?.mutedUntil && new Date(active.value.mutedUntil) > new Date())))
const canSend = computed(() => !activeMuted.value)
const socketStatusText = computed(() => ({
  connected: '实时已连接',
  reconnecting: '正在重连',
  connecting: '正在连接',
  disconnected: '连接已断开',
}[socketStatus.value] || '正在连接'))

const conversationSignature = (item) => [
  item.conversationType,
  item.targetId,
  item.title,
  item.lastMessage,
  item.lastMessageAt,
  item.unreadCount,
].map(value => String(value ?? '')).join('|')

const hasCollectionChanges = (nextItems, currentItems, signature) => {
  if (nextItems.length !== currentItems.length) return true
  return nextItems.some((item, index) => signature(item) !== signature(currentItems[index] || {}))
}

const getAccessToken = () => sessionStorage.getItem('token') || localStorage.getItem('token')

const websocketUrl = () => {
  const apiOrigin = import.meta.env.VITE_GATEWAY_ORIGIN || 'http://localhost:8500'
  const wsOrigin = apiOrigin.replace(/^http/, 'ws').replace(/\/$/, '')
  return `${wsOrigin}/api/ws/chat?access_token=${encodeURIComponent(getAccessToken())}`
}

const isNearBottom = () => !messageListRef.value
  || messageListRef.value.scrollHeight - messageListRef.value.scrollTop - messageListRef.value.clientHeight < 80

const isMessageForActiveConversation = (message) => {
  if (!active.value || !message) return false
  if (message.conversationType === 'GROUP') {
    return String(message.groupId) === String(active.value.targetId)
  }
  const peerId = message.mine ? message.receiverId : message.senderId
  return active.value.conversationType === 'PRIVATE' && String(peerId) === String(active.value.targetId)
}

const appendRealtimeMessage = async (message) => {
  if (!message) return
  fetchConversations()
  if (!isMessageForActiveConversation(message)) return

  const existing = messages.value.findIndex(item => String(item.id) === String(message.id))
  if (existing >= 0) {
    Object.assign(messages.value[existing], message)
    return
  }

  const shouldStickToBottom = isNearBottom()
  messages.value = [...messages.value, message]
  messagesLoaded.value = true
  if (!message.mine) {
    markRead([message])
  }
  if (shouldStickToBottom) {
    await nextTick()
    scrollBottom()
  }
}

const handleSocketMessage = (event) => {
  try {
    const payload = JSON.parse(event.data)
    if (payload.type === 'CHAT_MESSAGE') {
      appendRealtimeMessage(payload.data)
    }
  } catch {
    // Ignore malformed websocket payloads and keep REST refresh available.
  }
}

const scheduleReconnect = () => {
  if (socketStopped || reconnectTimer.value) return
  socketStatus.value = 'reconnecting'
  const delay = Math.min(10000, 1000 * 2 ** reconnectAttempt)
  reconnectAttempt = Math.min(reconnectAttempt + 1, 4)
  reconnectTimer.value = window.setTimeout(() => {
    reconnectTimer.value = null
    connectChatSocket()
  }, delay)
}

const connectChatSocket = () => {
  const token = getAccessToken()
  if (!token || socketStopped) {
    socketStatus.value = 'disconnected'
    return
  }

  socketStatus.value = reconnectAttempt ? 'reconnecting' : 'connecting'
  const socket = new WebSocket(websocketUrl())
  chatSocket.value = socket
  socket.onopen = () => {
    if (chatSocket.value !== socket) return
    reconnectAttempt = 0
    socketStatus.value = 'connected'
  }
  socket.onmessage = handleSocketMessage
  socket.onerror = () => socket.close()
  socket.onclose = () => {
    if (chatSocket.value !== socket) return
    chatSocket.value = null
    if (!socketStopped) scheduleReconnect()
  }
}
const fetchAll = async () => {
  await Promise.all([fetchConversations(), fetchFriends(), fetchGroups()])
}

const fetchConversations = async () => {
  const res = await request.get('/chat/conversations')
  const nextConversations = res.data || []
  if (hasCollectionChanges(nextConversations, conversations.value, conversationSignature)) {
    conversations.value = nextConversations
  }
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

const friendDisplayName = (friend) => friend?.remark || friend?.name || friend?.username || `用户${friend?.friendId || ''}`

const openFriend = (friend) => {
  openConversation({
    conversationType: 'PRIVATE',
    targetId: friend.friendId,
    title: friendDisplayName(friend),
    avatar: friend.avatar,
    remark: friend.remark,
    username: friend.username,
    name: friend.name,
    level: friend.level,
    levelName: friend.levelName,
  })
}

const openGroup = (group) => {
  openConversation({
    ...group,
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
      || String(message.id) !== String(current.id)
      || message.content !== current.content
      || message.senderId !== current.senderId
      || message.createdAt !== current.createdAt
  })
}

const syncMessageReadState = (nextMessages) => {
  const nextById = new Map(nextMessages.map(message => [String(message.id), message]))
  messages.value.forEach((message) => {
    const next = nextById.get(String(message.id))
    if (!next) return
    message.readCount = next.readCount
    message.unreadCount = next.unreadCount
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
    if (changed) {
      messages.value = nextMessages
    } else {
      syncMessageReadState(nextMessages)
    }
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
const sendChatPayload = async ({ contentType, content }, conversation = active.value) => {
  if (!conversation) return
  const res = await request.post('/chat/messages', {
    conversationType: conversation.conversationType,
    groupId: conversation.conversationType === 'GROUP' ? conversation.targetId : null,
    receiverId: conversation.conversationType === 'PRIVATE' ? conversation.targetId : null,
    contentType,
    content,
  })
  await appendRealtimeMessage(res.data)
}

const sendMessage = async () => {
  if (!active.value) return ElMessage.warning('请先选择会话')
  const content = messageText.value.trim()
  if (!content) return
  await sendChatPayload({ contentType: 'TEXT', content })
  messageText.value = ''
}

const attachmentData = (message) => {
  try {
    const data = JSON.parse(message.content)
    return data && data.url ? data : { name: '附件', url: message.content, size: 0 }
  } catch {
    return { name: '附件', url: message.content, size: 0 }
  }
}

const formatFileSize = (size) => {
  if (!size) return ''
  if (size < 1024 * 1024) return `${Math.max(1, Math.round(size / 1024))} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

const chooseFile = () => fileInputRef.value?.click()

const uploadAttachment = async (event) => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || !active.value || uploading.value) return

  const conversation = { ...active.value }
  const isImage = file.type.startsWith('image/')
  uploading.value = true
  uploadProgress.value = 0
  try {
    const uploadRes = await request.upload(isImage ? '/upload/image' : '/upload/file', file, (progress) => {
      uploadProgress.value = progress
    })
    const data = uploadRes.data
    await sendChatPayload({
      contentType: isImage ? 'IMAGE' : 'FILE',
      content: JSON.stringify({
        name: data.originalFilename || file.name,
        url: data.url,
        size: data.fileSize || file.size,
        thumbnailUrl: data.thumbnailUrl || '',
      }),
    }, conversation)
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

const openRemarkEditor = () => {
  if (!active.value || active.value.conversationType !== 'PRIVATE') return
  remarkText.value = active.value.remark || ''
  remarkEditorVisible.value = true
}

const saveRemark = async () => {
  if (!active.value) return
  const friendId = active.value.targetId
  const res = await request.put(`/chat/friends/${friendId}/remark`, { remark: remarkText.value })
  const updated = res.data
  const title = friendDisplayName(updated)
  friends.value = friends.value.map(item => String(item.friendId) === String(friendId) ? { ...item, ...updated } : item)
  conversations.value = conversations.value.map(item => item.conversationType === 'PRIVATE' && String(item.targetId) === String(friendId)
    ? { ...item, title, remark: updated.remark, level: updated.level, levelName: updated.levelName, avatar: updated.avatar }
    : item)
  active.value = { ...active.value, title, remark: updated.remark, level: updated.level, levelName: updated.levelName, avatar: updated.avatar }
  remarkEditorVisible.value = false
  ElMessage.success('备注已更新')
}

const fetchGroupMembers = async () => {
  if (!active.value || active.value.conversationType !== 'GROUP') return
  const res = await request.get(`/chat/groups/${active.value.targetId}/members`)
  groupMembers.value = res.data || []
}

const openGroupManage = async () => {
  if (!isGroupManager.value) return
  groupManageVisible.value = true
  groupMemberKeyword.value = ''
  groupMemberResults.value = []
  await fetchGroupMembers()
}

const searchGroupMemberCandidates = async () => {
  const keyword = groupMemberKeyword.value.trim()
  if (!keyword) {
    groupMemberResults.value = []
    return
  }
  const res = await request.get('/chat/users/search', { keyword })
  const memberIds = new Set(groupMembers.value.map(member => String(member.userId)))
  groupMemberResults.value = (res.data || []).filter(user => !memberIds.has(String(user.id)))
}

const syncGroupState = async (group) => {
  if (!group) return
  groups.value = groups.value.map(item => String(item.id) === String(group.id) ? { ...item, ...group } : item)
  conversations.value = conversations.value.map(item => item.conversationType === 'GROUP' && String(item.targetId) === String(group.id)
    ? { ...item, ...group, targetId: group.id, title: group.name }
    : item)
  if (active.value?.conversationType === 'GROUP' && String(active.value.targetId) === String(group.id)) {
    active.value = {
      ...active.value,
      ...group,
      targetId: group.id,
      title: group.name,
      pinnedMessageId: group.pinnedMessageId || null,
      pinnedMessage: group.pinnedMessage || null,
      pinnedMessageSenderName: group.pinnedMessageSenderName || null,
    }
  }
}

const inviteGroupMember = async (user) => {
  const res = await request.post(`/chat/groups/${active.value.targetId}/members`, { userId: user.id })
  await syncGroupState(res.data)
  await fetchGroupMembers()
  groupMemberResults.value = []
  groupMemberKeyword.value = ''
  ElMessage.success('已邀请成员入群')
}

const removeGroupMember = async (member) => {
  await request.delete(`/chat/groups/${active.value.targetId}/members/${member.userId}`)
  await fetchGroupMembers()
  await fetchAll()
  ElMessage.success('成员已移出群聊')
}

const muteGroupMember = async (member, minutes) => {
  await request.put(`/chat/groups/${active.value.targetId}/members/${member.userId}/mute`, { muteMinutes: minutes })
  await fetchGroupMembers()
  ElMessage.success(minutes ? `已禁言 ${minutes} 分钟` : '已解除禁言')
}

const toggleGroupMuteAll = async () => {
  const res = await request.put(`/chat/groups/${active.value.targetId}/mute-all`, { enabled: !active.value.mutedAll })
  await syncGroupState(res.data)
  ElMessage.success(res.data.mutedAll ? '已开启全员禁言' : '已解除全员禁言')
}

const pinGroupMessage = async (messageId) => {
  const res = await request.put(`/chat/groups/${active.value.targetId}/pinned-message`, { messageId })
  await syncGroupState(res.data)
  ElMessage.success('消息已置顶')
}

const unpinGroupMessage = async () => {
  const res = await request.put(`/chat/groups/${active.value.targetId}/pinned-message`, { messageId: null })
  await syncGroupState(res.data)
  ElMessage.success('已取消置顶')
}

const isMemberMuted = member => member?.mutedUntil && new Date(member.mutedUntil) > new Date()
const canManageMember = member => isGroupOwner.value || (isGroupManager.value && member.role === 'MEMBER')

const updateGroupMemberRole = async (member, role) => {
  await request.put(`/chat/groups/${active.value.targetId}/members/${member.userId}/role`, { role })
  await fetchGroupMembers()
  ElMessage.success(role === 'ADMIN' ? '已设为管理员' : '已取消管理员')
}

const sendEmoji = async (emoji) => {
  if (!active.value || !canSend.value) return
  await sendChatPayload({ contentType: 'EMOJI', content: emoji })
  emojiPickerVisible.value = false
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
  socketStopped = false
  fetchAll()
  connectChatSocket()
})

onUnmounted(() => {
  socketStopped = true
  if (reconnectTimer.value) {
    clearTimeout(reconnectTimer.value)
  }
  reconnectTimer.value = null
  chatSocket.value?.close()
  chatSocket.value = null
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
          <span class="socket-status" :class="`is-${socketStatus}`"><i></i>{{ socketStatusText }}</span>
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
              <div class="avatar">
                <ChatDotRound v-if="item.conversationType === 'GROUP'" />
                <img v-else-if="item.avatar" :src="item.avatar" :alt="item.title" />
                <UserFilled v-else />
              </div>
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
            <button v-for="friend in friends" :key="friend.friendId" class="plain-item friend-item" @click="openFriend(friend)">
              <span class="avatar small-avatar">
                <img v-if="friend.avatar" :src="friend.avatar" :alt="friendDisplayName(friend)">
                <UserFilled v-else />
              </span>
              <span class="friend-copy">
                <strong>{{ friendDisplayName(friend) }}</strong>
                <small>@{{ friend.username || friend.friendId }} · Lv.{{ friend.level || 1 }}{{ friend.levelName ? ` ${friend.levelName}` : '' }}</small>
              </span>
              <small v-if="friend.unreadCount" class="friend-unread">{{ friend.unreadCount }}</small>
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
            <div class="active-profile">
              <div v-if="active" class="avatar active-avatar">
                <ChatDotRound v-if="active.conversationType === 'GROUP'" />
                <img v-else-if="active.avatar" :src="active.avatar" :alt="activeTitle">
                <UserFilled v-else />
              </div>
              <div>
                <div class="active-name-row">
                  <h2>{{ activeTitle }}</h2>
                  <span v-if="activeLevelText" class="level-badge">{{ activeLevelText }}</span>
                </div>
                <p>{{ activeSubTitle }}</p>
              </div>
            </div>
            <div class="head-actions">
              <button v-if="isGroupManager" class="icon-refresh" type="button" title="群管理" aria-label="群管理" @click="openGroupManage"><UserFilled /></button>
              <button v-if="active?.conversationType === 'PRIVATE'" class="icon-refresh" type="button" title="修改好友备注" aria-label="修改好友备注" @click="openRemarkEditor"><EditPen /></button>
              <button v-if="active" class="icon-refresh" type="button" title="刷新消息" aria-label="刷新消息" @click="fetchMessages"><Refresh /></button>
            </div>
          </header>

          <div v-if="active?.conversationType === 'GROUP' && active.pinnedMessage" class="pinned-message">
            <strong>置顶</strong>
            <span>{{ active.pinnedMessageSenderName ? `${active.pinnedMessageSenderName}: ` : '' }}{{ active.pinnedMessage }}</span>
            <button v-if="isGroupOwner" type="button" title="取消置顶" @click="unpinGroupMessage">取消</button>
          </div>
          <div v-if="activeMuted" class="mute-notice">当前群聊已限制发言</div>
          <div ref="messageListRef" class="message-list">
            <div v-if="!active" class="empty-state"><ChatDotRound /><strong>选择一个会话</strong><span>从好友或群聊中开始一段交流</span></div>
            <div v-else-if="loadingMessages" class="empty-state">消息加载中...</div>
            <div v-else-if="!messages.length" class="empty-state">还没有消息</div>
            <template v-else>
              <div v-for="msg in messages" :key="msg.id" class="message-row" :class="{ mine: msg.mine }">
                <div class="message-avatar avatar">
                  <img v-if="msg.senderAvatar" :src="msg.senderAvatar" :alt="msg.senderName || '用户头像'">
                  <UserFilled v-else />
                </div>
                <div class="message-bubble">
                  <div class="message-meta">
                    <span>{{ msg.senderName || `用户${msg.senderId}` }} <em v-if="msg.senderRole === 'OWNER'" class="owner-badge">群主</em><em v-else-if="msg.senderRole === 'ADMIN'" class="admin-badge">管理员</em></span>
                    <small>{{ formatTime(msg.createdAt) }}</small>
                  </div>
                  <p v-if="msg.contentType === 'TEXT'">{{ msg.content }}</p>
                  <p v-else-if="msg.contentType === 'EMOJI'" class="message-emoji">{{ msg.content }}</p>
                  <a v-else-if="msg.contentType === 'IMAGE'" class="message-image" :href="attachmentData(msg).url" target="_blank" rel="noopener">
                    <img :src="attachmentData(msg).thumbnailUrl || attachmentData(msg).url" :alt="attachmentData(msg).name">
                  </a>
                  <a v-else class="message-file" :href="attachmentData(msg).url" target="_blank" rel="noopener">
                    <Document />
                    <span><strong>{{ attachmentData(msg).name }}</strong><small>{{ formatFileSize(attachmentData(msg).size) || '点击下载' }}</small></span>
                  </a>
                  <div v-if="isGroupOwner && active?.conversationType === 'GROUP'" class="message-admin-action">
                    <button type="button" title="置顶此消息" @click="pinGroupMessage(msg.id)">置顶</button>
                  </div>
                  <div class="read-line">
                    <span v-if="active.conversationType === 'GROUP'">已读 {{ msg.readCount || 0 }} / 未读 {{ msg.unreadCount || 0 }}</span>
                    <span v-else>{{ msg.unreadCount === 0 ? '对方已读' : '送达' }}</span>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <footer class="chat-input">
            <textarea v-model="messageText" rows="3" :disabled="uploading || !canSend" :placeholder="canSend ? '输入消息，Enter 发送，Shift+Enter 换行' : '当前无法发言'" @keydown.enter.exact.prevent="sendMessage"></textarea>
            <div class="input-tools">
              <input ref="fileInputRef" class="hidden-file-input" type="file" accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar" @change="uploadAttachment">
              <div class="emoji-picker-wrap">
                <button class="emoji-toggle" type="button" :disabled="!active || uploading || !canSend" title="发送表情" aria-label="发送表情" @click="emojiPickerVisible = !emojiPickerVisible">☺</button>
                <div v-if="emojiPickerVisible" class="emoji-picker">
                  <button v-for="emoji in emojis" :key="emoji" type="button" @click="sendEmoji(emoji)">{{ emoji }}</button>
                </div>
              </div>
              <button class="attachment-button" type="button" :disabled="!active || uploading || !canSend" title="发送图片或文件" aria-label="发送图片或文件" @click="chooseFile">
                <Picture v-if="!uploading" /><FolderOpened v-else />
                <span>{{ uploading ? `上传中 ${uploadProgress}%` : '附件' }}</span>
              </button>
              <button type="button" :disabled="!active || uploading || !canSend" title="发送消息" @click="sendMessage"><Promotion /><span>发送</span></button>
            </div>
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

    <el-dialog v-model="groupManageVisible" title="群管理" width="620px">
      <div class="group-manage-panel">
        <div class="group-manage-top">
          <div><strong>{{ active?.title }}</strong><small>群主：{{ active?.ownerName || active?.ownerId }}</small></div>
          <button v-if="isGroupOwner" class="dialog-secondary" type="button" @click="toggleGroupMuteAll">{{ active?.mutedAll ? '解除全员禁言' : '开启全员禁言' }}</button>
        </div>
        <div class="member-invite">
          <input v-model="groupMemberKeyword" class="dialog-input" placeholder="搜索用户后邀请入群" @keyup.enter="searchGroupMemberCandidates">
          <button class="dialog-primary" type="button" @click="searchGroupMemberCandidates"><Search /> 搜索</button>
        </div>
        <div v-if="groupMemberResults.length" class="manage-result-list">
          <div v-for="user in groupMemberResults" :key="user.id" class="manage-member-row">
            <span>{{ user.name || user.username }} <small>@{{ user.username }}</small></span>
            <button class="dialog-secondary" type="button" @click="inviteGroupMember(user)"><Plus /> 邀请</button>
          </div>
        </div>
        <div class="manage-member-list">
          <div v-for="member in groupMembers" :key="member.userId" class="manage-member-row">
            <span class="avatar small-avatar"><img v-if="member.avatar" :src="member.avatar" :alt="member.name || member.username"><UserFilled v-else /></span>
            <span class="manage-member-name"><strong>{{ member.name || member.username || member.userId }}</strong><small>@{{ member.username || member.userId }} · Lv.{{ member.level || 1 }}{{ member.levelName ? ` ${member.levelName}` : '' }} · {{ member.role === 'OWNER' ? '群主' : member.role === 'ADMIN' ? '管理员' : isMemberMuted(member) ? '禁言中' : '成员' }}</small></span>
            <template v-if="canManageMember(member) && member.role !== 'OWNER'">
              <button v-if="isGroupOwner" class="dialog-secondary" type="button" @click="updateGroupMemberRole(member, member.role === 'ADMIN' ? 'MEMBER' : 'ADMIN')">{{ member.role === 'ADMIN' ? '取消管理员' : '设为管理员' }}</button>
              <button class="dialog-secondary" type="button" @click="muteGroupMember(member, isMemberMuted(member) ? 0 : 60)">{{ isMemberMuted(member) ? '解除禁言' : '禁言 1 小时' }}</button>
              <button class="danger-action" type="button" @click="removeGroupMember(member)">移出</button>
            </template>
          </div>
        </div>
      </div>
      <template #footer><button class="dialog-secondary" type="button" @click="groupManageVisible = false">关闭</button></template>
    </el-dialog>

    <el-dialog v-model="remarkEditorVisible" title="修改好友备注" width="420px">
      <label class="block">
        <span class="mb-1 block text-sm text-slate-500">备注名称</span>
        <input v-model="remarkText" class="dialog-input" maxlength="40" placeholder="留空则使用对方昵称">
      </label>
      <template #footer>
        <button class="dialog-secondary" type="button" @click="remarkEditorVisible = false">取消</button>
        <button class="dialog-primary" type="button" @click="saveRemark"><EditPen /> 保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.chat-toolbar,
.chat-title,
.toolbar-actions,
.socket-status,
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

.socket-status,
.unread-summary,
.create-group-button,
.dialog-primary,
.pinned-message,
.mute-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid var(--app-border);
  padding: 8px 18px;
  color: var(--app-text-secondary);
  font-size: 12px;
}

.pinned-message {
  background: rgb(var(--theme-primary-rgb) / 0.08);
}

.pinned-message strong,
.owner-badge,
.admin-badge {
  color: var(--theme-primary);
  font-size: 10px;
  font-style: normal;
  font-weight: 800;
}

.pinned-message span {
  overflow: hidden;
  flex: 1;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pinned-message button,
.message-admin-action button {
  border: 0;
  background: transparent;
  color: inherit;
  font-size: 11px;
}

.mute-notice {
  justify-content: center;
  background: rgb(var(--theme-secondary-rgb) / 0.1);
  color: var(--theme-secondary);
  font-weight: 700;
}

.message-admin-action {
  margin-top: 7px;
  text-align: right;
}

.message-admin-action button {
  position: relative;
  z-index: 1;
  border-radius: 6px;
  background: rgb(var(--theme-primary-rgb) / 0.12);
  padding: 4px 8px;
  color: var(--theme-primary);
}

.message-row.mine .message-admin-action button {
  background: rgb(255 255 255 / 0.18);
  color: white;
}

.message-emoji {
  margin-top: 4px;
  font-size: 32px;
  line-height: 1.2;
}

.group-manage-panel,
.manage-member-list,
.manage-result-list {
  display: grid;
  gap: 10px;
}

.group-manage-top,
.member-invite,
.manage-member-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.group-manage-top {
  justify-content: space-between;
  border-bottom: 1px solid var(--app-border);
  padding-bottom: 12px;
}

.group-manage-top div,
.manage-member-name {
  display: grid;
  min-width: 0;
  gap: 2px;
}

.group-manage-top small,
.manage-member-name small,
.manage-member-row small {
  color: var(--app-text-muted);
  font-size: 11px;
}

.member-invite .dialog-input {
  flex: 1;
}

.manage-member-list {
  max-height: 330px;
  overflow-y: auto;
}

.manage-member-row {
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 8px;
}

.manage-member-name {
  flex: 1;
}

.danger-action {
  border: 1px solid rgb(220 38 38 / 0.24);
  border-radius: 8px;
  background: rgb(220 38 38 / 0.08);
  padding: 7px 9px;
  color: #dc2626;
  font-size: 12px;
  font-weight: 700;
}

.dialog-secondary {
  min-height: 40px;
  border-radius: 8px;
  padding: 0 14px;
  font-size: 13px;
  font-weight: 750;
}

.socket-status {
  gap: 6px;
  border: 1px solid var(--app-border);
  background: var(--app-card-solid);
  color: var(--app-text-muted);
}

.socket-status i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--app-text-muted);
}

.socket-status.is-connected {
  color: var(--theme-primary);
}

.socket-status.is-connected i {
  background: var(--theme-primary);
}

.socket-status.is-reconnecting i,
.socket-status.is-connecting i {
  background: var(--theme-secondary);
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

.avatar img {
  width: 100%;
  height: 100%;
  border-radius: inherit;
  object-fit: cover;
}

.small-avatar {
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
}

.friend-item {
  align-items: center;
}

.friend-copy {
  display: grid;
  min-width: 0;
  flex: 1;
  gap: 2px;
}

.friend-copy strong,
.friend-copy small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.friend-unread {
  display: grid;
  min-width: 18px;
  height: 18px;
  place-items: center;
  border-radius: 6px;
  background: var(--theme-secondary);
  color: white !important;
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

.active-profile,
.active-name-row,
.head-actions,
.input-tools,
.message-file {
  display: flex;
  align-items: center;
}

.active-profile {
  min-width: 0;
  gap: 10px;
}

.active-avatar {
  width: 40px;
  height: 40px;
  flex-basis: 40px;
}

.active-name-row {
  gap: 8px;
}

.head-actions,
.input-tools {
  gap: 8px;
}

.level-badge {
  border-radius: 999px;
  background: rgb(var(--theme-primary-rgb) / 0.12);
  padding: 3px 7px;
  color: var(--theme-primary);
  font-size: 10px;
  font-weight: 800;
  white-space: nowrap;
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
  align-items: flex-end;
  gap: 8px;
  margin-bottom: 12px;
}

.message-row.mine {
  flex-direction: row-reverse;
  justify-content: flex-start;
}

.message-avatar {
  width: 32px;
  height: 32px;
  flex-basis: 32px;
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

.message-image {
  display: block;
  overflow: hidden;
  margin-top: 7px;
  border-radius: 7px;
}

.message-image img {
  display: block;
  max-width: min(360px, 100%);
  max-height: 280px;
  object-fit: cover;
}

.message-file {
  min-width: min(250px, 100%);
  max-width: 360px;
  gap: 9px;
  margin-top: 7px;
  border: 1px solid rgb(var(--theme-primary-rgb) / 0.22);
  border-radius: 7px;
  background: rgb(var(--theme-primary-rgb) / 0.08);
  padding: 9px;
  color: inherit;
}

.message-file svg {
  width: 23px;
  flex: 0 0 23px;
}

.message-file span {
  display: grid;
  min-width: 0;
}

.message-file strong,
.message-file small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-file small {
  margin-top: 2px;
  opacity: 0.72;
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

.hidden-file-input {
  display: none;
}

.emoji-picker-wrap {
  position: relative;
}

.emoji-toggle {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: rgb(var(--theme-primary-rgb) / 0.08);
  color: var(--theme-primary);
  font-size: 19px;
}

.emoji-picker {
  position: absolute;
  right: 0;
  bottom: 48px;
  z-index: 5;
  display: grid;
  width: 220px;
  grid-template-columns: repeat(4, 1fr);
  gap: 5px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-card-solid);
  padding: 8px;
  box-shadow: var(--app-elevation-float);
}

.emoji-picker button {
  min-height: 34px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  padding: 0;
  font-size: 19px;
}

.emoji-picker button:hover {
  background: rgb(var(--theme-primary-rgb) / 0.1);
  transform: none;
}

.attachment-button {
  justify-content: center;
  border: 1px solid var(--app-border) !important;
  background: rgb(var(--theme-primary-rgb) / 0.08) !important;
  color: var(--theme-primary) !important;
  box-shadow: none !important;
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

.chat-input .attachment-button {
  border: 1px solid var(--app-border) !important;
  background: rgb(var(--theme-primary-rgb) / 0.08) !important;
  color: var(--theme-primary) !important;
  box-shadow: none !important;
}

.chat-input .emoji-toggle {
  border: 1px solid var(--app-border);
  background: rgb(var(--theme-primary-rgb) / 0.08);
  padding: 0;
  color: var(--theme-primary);
  box-shadow: none;
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
