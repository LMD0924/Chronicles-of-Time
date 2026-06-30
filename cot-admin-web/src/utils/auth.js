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
