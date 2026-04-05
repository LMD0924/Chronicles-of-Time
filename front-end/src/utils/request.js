import axios from 'axios';
import { ElMessage } from "element-plus";
import router from '@/router';

// Token 工具函数
const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_INFO_KEY = 'user_info'

// 获取 accessToken
const getToken = () => {
  return sessionStorage.getItem(TOKEN_KEY) || localStorage.getItem(TOKEN_KEY)
}

// 获取 refreshToken
const getRefreshToken = () => {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

// 设置 Token
const setToken = (accessToken, refreshToken, rememberMe = false) => {
  if (rememberMe) {
    localStorage.setItem(TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  } else {
    sessionStorage.setItem(TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }
}

// 清除所有 Token
const clearToken = () => {
  sessionStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_INFO_KEY)
}

// 解析 Token
const getTokenExpireTime = (token) => {
  if (!token) return null
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000
  } catch {
    return null
  }
}

// 是否过期
const isTokenExpired = (token) => {
  const expireTime = getTokenExpireTime(token)
  if (!expireTime) return true
  return Date.now() >= expireTime
}

// 刷新队列
let isRefreshing = false
let pendingRequests = []

const processQueue = (error, token = null) => {
  pendingRequests.forEach(callback => {
    if (error) callback(error)
    else callback(null, token)
  })
  pendingRequests = []
}

// 统一登出
const handleUnauthorized = () => {
  clearToken()
  ElMessage.warning('登录已过期，请重新登录')
  router.push('/login')
}

// 刷新 Token
const refreshAccessToken = async () => {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    handleUnauthorized()
    throw new Error('无刷新令牌')
  }

  try {
    const res = await axios.post('http://localhost:8500/api/auth/refresh', {}, {
      headers: { Authorization: `Bearer ${refreshToken}` }
    })

    if (res.data.code === 200 && res.data.data) {
      const { accessToken, refreshToken: newRefreshToken } = res.data.data
      const remember = !!localStorage.getItem(TOKEN_KEY)
      setToken(accessToken, newRefreshToken || refreshToken, remember)
      return accessToken
    }
    throw new Error('刷新失败')
  } catch (e) {
    handleUnauthorized()
    throw e
  }
}

// ==========================
// 创建 axios 实例
// ==========================
const service = axios.create({
  baseURL: 'http://localhost:8500/api/',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// ==========================
// 请求拦截器（只写一次！）
// ==========================
service.interceptors.request.use(
  (config) => {
    // 刷新接口不处理
    if (config.url.includes('refresh')) return config

    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    ElMessage.error('请求异常')
    return Promise.reject(error)
  }
)

// ==========================
// 响应拦截器（只写一次！）
// ==========================
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      // 401 处理
      if (res.code === 401) {
        const original = response.config
        if (original._retry) {
          handleUnauthorized()
          return Promise.reject(res)
        }
        original._retry = true

        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            pendingRequests.push((err, token) => {
              if (err) reject(err)
              else {
                original.headers.Authorization = `Bearer ${token}`
                resolve(service(original))
              }
            })
          })
        }

        isRefreshing = true
        return refreshAccessToken()
          .then(newToken => {
            processQueue(null, newToken)
            original.headers.Authorization = `Bearer ${newToken}`
            return service(original)
          })
          .catch(err => {
            processQueue(err, null)
            return Promise.reject(err)
          })
          .finally(() => {
            isRefreshing = false
          })
      }

      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) handleUnauthorized()
    else ElMessage.error('网络或服务器异常')
    return Promise.reject(error)
  }
)

// ==========================
// 通用请求方法
// ==========================
const request = {
  get(url, params, cb) {
    return service.get(url, { params }).then(r => { cb && cb(r.msg, r.data); return r })
  },
  post(url, data, paramsOrCb, cbOrConfig, config = {}) {
    let params = null
    let cb = null
    let finalConfig = config
    
    // 处理参数重载
    if (typeof paramsOrCb === 'function') {
      cb = paramsOrCb
      if (typeof cbOrConfig === 'object') {
        finalConfig = cbOrConfig
      }
    } else if (typeof paramsOrCb === 'object') {
      params = paramsOrCb
      if (typeof cbOrConfig === 'function') {
        cb = cbOrConfig
      } else if (typeof cbOrConfig === 'object') {
        finalConfig = cbOrConfig
      }
    }
    
    const isForm = data instanceof FormData
    const axiosConfig = {
      ...finalConfig,
      headers: isForm ? {} : { 'Content-Type': 'application/json' }
    }
    
    if (params) {
      axiosConfig.params = params
    }
    
    return service.post(url, data, axiosConfig).then(r => { cb && cb(r.msg, r.data); return r })
  },
  put(url, data, cb, config = {}) {
    const isForm = data instanceof FormData
    return service.put(url, data, {
      ...config,
      headers: isForm ? {} : { 'Content-Type': 'application/json' }
    }).then(r => { cb && cb(r.msg, r.data); return r })
  },
  delete(url, params, cb) {
    return service.delete(url, { params }).then(r => { cb && cb(r.msg, r.data); return r })
  },
  upload(url, file, progress, cb) {
    const fd = new FormData()
    fd.append('file', file)
    const cfg = {}
    if (progress) cfg.onUploadProgress = e => progress(Math.round(e.loaded / e.total * 100))
    return service.post(url, fd, cfg).then(r => { cb && cb(r.msg, r.data); return r })
  },
  _utils: { getToken, setToken, clearToken, isTokenExpired }
}

export default request
