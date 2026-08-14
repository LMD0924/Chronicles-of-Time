import messageApi from '@/utils/messageApi'

const listeners = new Map()
let socket = null
let reconnectTimer = null
let reconnectAttempt = 0
let stopped = true
let installed = false
let connectionStatus = 'disconnected'

const token = () => sessionStorage.getItem('token') || localStorage.getItem('token')

const websocketUrl = () => {
  const apiOrigin = import.meta.env.VITE_GATEWAY_ORIGIN || 'http://localhost:8500'
  return `${apiOrigin.replace(/^http/, 'ws').replace(/\/$/, '')}/api/ws/chat?access_token=${encodeURIComponent(token())}`
}

const emit = (type, data) => {
  if (type === 'CONNECTION_STATUS') connectionStatus = data
  listeners.get(type)?.forEach((listener) => listener(data))
  listeners.get('*')?.forEach((listener) => listener({ type, data }))
}

const scheduleReconnect = () => {
  if (stopped || reconnectTimer || !token()) return
  const delay = Math.min(10000, 1000 * 2 ** reconnectAttempt)
  reconnectAttempt = Math.min(reconnectAttempt + 1, 4)
  reconnectTimer = window.setTimeout(() => {
    reconnectTimer = null
    connectRealtime()
  }, delay)
}

export const connectRealtime = () => {
  if (!token()) return
  stopped = false
  if (socket && [WebSocket.OPEN, WebSocket.CONNECTING].includes(socket.readyState)) return

  emit('CONNECTION_STATUS', reconnectAttempt ? 'reconnecting' : 'connecting')
  const current = new WebSocket(websocketUrl())
  socket = current
  current.onopen = () => {
    if (socket !== current) return
    reconnectAttempt = 0
    emit('CONNECTION_STATUS', 'connected')
  }
  current.onmessage = (event) => {
    try {
      const payload = JSON.parse(event.data)
      if (payload?.type) emit(payload.type, payload.data)
    } catch {
      // Ignore malformed events without breaking future delivery.
    }
  }
  current.onerror = () => current.close()
  current.onclose = () => {
    if (socket !== current) return
    socket = null
    emit('CONNECTION_STATUS', 'reconnecting')
    scheduleReconnect()
  }
}

export const disconnectRealtime = () => {
  stopped = true
  reconnectAttempt = 0
  if (reconnectTimer) window.clearTimeout(reconnectTimer)
  reconnectTimer = null
  socket?.close()
  socket = null
  emit('CONNECTION_STATUS', 'disconnected')
}

export const subscribeRealtime = (type, listener) => {
  if (!listeners.has(type)) listeners.set(type, new Set())
  listeners.get(type).add(listener)
  if (type === 'CONNECTION_STATUS') listener(connectionStatus)
  return () => listeners.get(type)?.delete(listener)
}

export const installRealtimeNotifications = (router) => {
  if (installed) return
  installed = true

  subscribeRealtime('SYSTEM_NOTIFICATION', (notice) => {
    window.dispatchEvent(new CustomEvent('cot:notification', { detail: notice }))
    messageApi.info(notice?.title || '收到一条新通知', {
      duration: 5000,
      onClick: () => router.push(notice?.actionPath || '/GrowthPlanner?view=notifications'),
    })
  })
  subscribeRealtime('CHAT_MESSAGE', (chatMessage) => {
    window.dispatchEvent(new CustomEvent('cot:chat-message', { detail: chatMessage }))
    if (!chatMessage?.mine && router.currentRoute.value.path !== '/Chat') {
      const content = chatMessage?.contentType === 'TEXT' ? chatMessage?.content : `[${chatMessage?.contentType || '消息'}]`
      messageApi.info(`${chatMessage?.senderName || '新消息'}：${content || ''}`, {
        duration: 5000,
        onClick: () => router.push('/Chat'),
      })
    }
  })

  router.afterEach((to) => {
    if (to.path === '/login' || !token()) disconnectRealtime()
    else connectRealtime()
  })
  connectRealtime()
}
