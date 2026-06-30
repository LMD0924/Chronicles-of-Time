import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { clearToken, getToken, setToken, USER_KEY } from '@/utils/auth'

const adminPermissions = [
  'sys:user:add', 'sys:user:edit', 'sys:user:delete', 'sys:role:assign', 'sys:menu:edit', 'sys:log:delete',
  'content:audit', 'content:delete', 'academic:edit', 'resource:delete', 'monitor:view',
]

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isLogin = computed(() => Boolean(token.value))
  const isAdmin = computed(() => userInfo.value?.roleCode === 'SUPER_ADMIN')
  const permissions = computed(() => userInfo.value?.permissions || [])

  const login = async ({ username, password, remember }) => {
    if (!username || !password) throw new Error('请输入账号和密码')
    const roleCode = username === 'guest' ? 'GUEST' : 'SUPER_ADMIN'
    const mockUser = {
      id: roleCode === 'SUPER_ADMIN' ? 1 : 2,
      username,
      nickname: roleCode === 'SUPER_ADMIN' ? '拾光记管理员' : '普通账号',
      roleName: roleCode === 'SUPER_ADMIN' ? '超级管理员' : '普通账号',
      roleCode,
      avatar: '',
      permissions: roleCode === 'SUPER_ADMIN' ? adminPermissions : [],
    }
    const nextToken = `mock-jwt-${Date.now()}`
    setToken(nextToken, remember)
    localStorage.setItem(USER_KEY, JSON.stringify(mockUser))
    token.value = nextToken
    userInfo.value = mockUser
    return mockUser
  }

  const logout = () => {
    clearToken()
    localStorage.removeItem(USER_KEY)
    token.value = ''
    userInfo.value = null
  }

  return { token, userInfo, isLogin, isAdmin, permissions, login, logout }
})
