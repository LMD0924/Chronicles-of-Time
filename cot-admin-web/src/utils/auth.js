/**
 * 文件说明：拾光记后台管理系统通用工具脚本模块，封装通用工具相关的配置、状态、路由或工具逻辑。
 */
export const TOKEN_KEY = 'cot_admin_token'
export const USER_KEY = 'cot_admin_user'
export const SETTINGS_KEY = 'cot_admin_settings'

export const getToken = () => localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY)

export const setToken = (token, remember = true) => {
  const storage = remember ? localStorage : sessionStorage
  storage.setItem(TOKEN_KEY, token)
}

export const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
}
