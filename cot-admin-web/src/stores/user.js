import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import request from '@/utils/request'
import { clearToken, getToken, setToken, USER_KEY } from '@/utils/auth'

const REFRESH_TOKEN_KEY = 'cot_admin_refresh_token'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isLogin = computed(() => Boolean(token.value))
  const isAdmin = computed(() => ['SUPER_ADMIN', 'ADMIN'].includes(userInfo.value?.roleCode))
  const permissions = computed(() => userInfo.value?.permissions || [])

  const login = async ({ username, password, remember }) => {
    if (!username || !password) throw new Error('请输入账号和密码')
    const data = await request.post('/auth/login', {
      username,
      password,
      rememberMe: remember,
    })
    const roles = data.roles || data.userInfo?.roles || []
    const roleCode = data.userInfo?.role || roles[0] || 'USER'
    const realUser = {
      ...(data.userInfo || {}),
      nickname: data.userInfo?.name || data.userInfo?.username || username,
      roleCode,
      roleName: roleCode,
      permissions: data.permissions || data.userInfo?.permissions || [],
    }

    setToken(data.accessToken, remember)
    if (data.refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(realUser))
    token.value = data.accessToken
    userInfo.value = realUser
    return realUser
  }

  const logout = () => {
    clearToken()
    localStorage.removeItem(USER_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    token.value = ''
    userInfo.value = null
  }

  return { token, userInfo, isLogin, isAdmin, permissions, login, logout }
})
