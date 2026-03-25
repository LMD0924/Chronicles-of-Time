const ACCESS_TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_INFO_KEY = 'user_info'

/**
 * 获取 accessToken
 * 优先从 sessionStorage 读取，如果没有再从 localStorage 读取
 */
export const getToken = () => {
  return sessionStorage.getItem(ACCESS_TOKEN_KEY) || localStorage.getItem(ACCESS_TOKEN_KEY)
}

/**
 * 获取 refreshToken（始终从 localStorage 读取）
 */
export const getRefreshToken = () => {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * 设置 Token
 * @param {string} accessToken - 访问令牌
 * @param {string} refreshToken - 刷新令牌
 * @param {boolean} rememberMe - 是否记住我
 */
export const setToken = (accessToken, refreshToken, rememberMe = false) => {
  if (rememberMe) {
    // 记住我：都存 localStorage（关闭浏览器也不会丢失）
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  } else {
    // 普通登录：accessToken 存 sessionStorage（关闭标签页清除），refreshToken 存 localStorage
    sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }
}

/**
 * 清除所有 Token
 */
export const clearToken = () => {
  sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 */
export const setUserInfo = (userInfo) => {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 检查 Token 是否过期
 */
export const isTokenExpired = (token) => {
  if (!token) return true
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const exp = payload.exp * 1000
    return Date.now() >= exp
  } catch {
    return true
  }
}

/**
 * 获取 Token 过期时间戳
 */
export const getTokenExpireTime = (token) => {
  if (!token) return null
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000
  } catch {
    return null
  }
}
