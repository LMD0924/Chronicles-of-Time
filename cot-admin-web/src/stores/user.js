/**
 * 文件说明：拾光记后台管理系统全局状态脚本模块，封装全局状态相关的配置、状态、路由或工具逻辑。
 */
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { clearToken, getToken, setToken, USER_KEY } from '@/utils/auth'

// 本地开发模式下模拟超级管理员按钮权限；对接后端后可直接替换为登录接口返回值。
const adminPermissions = [
  'sys:user:add', 'sys:user:edit', 'sys:user:delete', 'sys:role:assign', 'sys:menu:edit', 'sys:log:delete',
  'content:audit', 'content:delete', 'academic:edit', 'resource:delete', 'monitor:view',
]

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isLogin = computed(() => Boolean(token.value))


  // 后台系统只允许超级管理员进入，普通账号会在路由守卫中跳转到 403。
  const isAdmin = computed(() => userInfo.value?.roleCode === 'SUPER_ADMIN')
  const permissions = computed(() => userInfo.value?.permissions || [])



  // 当前实现保留 mock 登录，字段结构与 JWT + RBAC 后端返回保持一致，方便无缝替换真实接口。
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



  // 登出时同时清理 token 和用户缓存，避免切换账号后残留旧权限。
  const logout = () => {
    clearToken()
    localStorage.removeItem(USER_KEY)
    token.value = ''
    userInfo.value = null
  }

  return { token, userInfo, isLogin, isAdmin, permissions, login, logout }
})
