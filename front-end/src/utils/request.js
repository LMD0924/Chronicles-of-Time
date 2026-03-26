// src/utils/request.js
import axios from 'axios';
import { ElMessage } from "element-plus";
import router from '@/router';

// Token 工具函数
const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_INFO_KEY = 'user_info'

// 获取 accessToken（优先 sessionStorage，其次 localStorage）
const getToken = () => {
  return sessionStorage.getItem(TOKEN_KEY) || localStorage.getItem(TOKEN_KEY)
}

// 获取 refreshToken（始终从 localStorage）
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

// 解析 Token 获取过期时间
const getTokenExpireTime = (token) => {
  if (!token) return null
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000
  } catch {
    return null
  }
}

// 检查 Token 是否过期
const isTokenExpired = (token) => {
  const expireTime = getTokenExpireTime(token)
  if (!expireTime) return true
  return Date.now() >= expireTime
}

// 是否正在刷新 Token
let isRefreshing = false
let pendingRequests = []

// 处理等待队列
const processQueue = (error, token = null) => {
  pendingRequests.forEach(callback => {
    if (error) {
      callback(error)
    } else {
      callback(null, token)
    }
  })
  pendingRequests = []
}

// 处理未授权
const handleUnauthorized = () => {
  clearToken()
  ElMessage.warning('登录已过期，请重新登录')
  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
}

// 刷新 Token
const refreshAccessToken = async () => {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    handleUnauthorized()
    throw new Error('No refresh token')
  }

  try {
    const response = await axios.post(
      'http://localhost:8500/api/auth/refresh',
      {},
      {
        headers: { Authorization: `Bearer ${refreshToken}` }
      }
    )

    if (response.data.code === 200 && response.data.data) {
      const { accessToken, refreshToken: newRefreshToken, userInfo } = response.data.data

      const isRememberMe = !!localStorage.getItem(TOKEN_KEY)
      setToken(accessToken, newRefreshToken || refreshToken, isRememberMe)

      if (userInfo) {
        localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
      }

      return accessToken
    }
    throw new Error('Refresh failed')
  } catch (error) {
    handleUnauthorized()
    throw error
  }
}

// 1. 创建 Axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8500/api/',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
});

// 2. 请求拦截器：添加 token
service.interceptors.request.use(
  (config) => {
    if (config.url === 'auth/refresh') {
      return config
    }

    const token = getToken()
    if (token) {
      if (isTokenExpired(token)) {
        console.log('Token 已过期，等待响应拦截器处理')
      }
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    ElMessage.error('请求参数异常，请检查！');
    return Promise.reject(error);
  }
);

// 3. 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code !== 200) {
      if (res.code === 401) {
        const originalRequest = response.config

        if (originalRequest._retry) {
          handleUnauthorized()
          return Promise.reject(res)
        }

        originalRequest._retry = true

        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            pendingRequests.push((err, token) => {
              if (err) {
                reject(err)
              } else {
                originalRequest.headers.Authorization = `Bearer ${token}`
                resolve(service(originalRequest))
              }
            })
          })
        }

        isRefreshing = true

        return refreshAccessToken()
          .then(newToken => {
            processQueue(null, newToken)
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            return service(originalRequest)
          })
          .catch(error => {
            processQueue(error, null)
            return Promise.reject(error)
          })
          .finally(() => {
            isRefreshing = false
          })
      }

      ElMessage.error(res.msg || res.message || '请求失败');
      return Promise.reject(res);
    }
    return res;
  },
  (error) => {
    if (error.response?.status === 401) {
      handleUnauthorized()
    } else {
      const errMsg = error.response
        ? `请求失败 [${error.response.status}]：${error.response.data?.msg || '服务器错误'}`
        : '网络异常，请检查网络连接！';
      ElMessage.error(errMsg);
    }
    return Promise.reject(error);
  }
);

// 4. 通用请求方法封装（支持文件上传）
const request = {
  // GET请求
  get: (url, params = {}, successCallback) => {
    return service.get(url, { params })
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      })
      .catch(err => Promise.reject(err));
  },

  // POST请求 - 自动判断是否为 FormData
  post: (url, data = {}, successCallback, customConfig = {}) => {
    // 判断是否为 FormData 对象
    const isFormData = data instanceof FormData

    const config = {
      ...customConfig,
      headers: {
        ...customConfig.headers,
        // 如果是 FormData，不设置 Content-Type（让浏览器自动设置 boundary）
        ...(isFormData ? {} : { 'Content-Type': 'application/json' })
      }
    }

    return service.post(url, data, config)
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      })
      .catch(err => Promise.reject(err));
  },

  // PUT请求 - 自动判断是否为 FormData
  put: (url, data = {}, successCallback, customConfig = {}) => {
    const isFormData = data instanceof FormData

    const config = {
      ...customConfig,
      headers: {
        ...customConfig.headers,
        ...(isFormData ? {} : { 'Content-Type': 'application/json' })
      }
    }

    return service.put(url, data, config)
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      })
      .catch(err => Promise.reject(err));
  },

  // DELETE请求
  delete: (url, params = {}, successCallback) => {
    return service.delete(url, { params })
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      })
      .catch(err => Promise.reject(err));
  },

  // ✅ 文件上传专用方法
  upload: (url, file, onProgress = null, successCallback = null) => {
    const formData = new FormData()
    formData.append('file', file)

    const config = {
      headers: { 'Content-Type': 'multipart/form-data' }
    }

    if (onProgress && typeof onProgress === 'function') {
      config.onUploadProgress = (progressEvent) => {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }

    return service.post(url, formData, config)
      .then(res => {
        successCallback && successCallback(res.msg, res.data)
        return res
      })
      .catch(err => Promise.reject(err))
  },

  // ✅ 多文件上传
  uploadMultiple: (url, files, onProgress = null, successCallback = null) => {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })

    const config = {
      headers: { 'Content-Type': 'multipart/form-data' }
    }

    if (onProgress && typeof onProgress === 'function') {
      config.onUploadProgress = (progressEvent) => {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }

    return service.post(url, formData, config)
      .then(res => {
        successCallback && successCallback(res.msg, res.data)
        return res
      })
      .catch(err => Promise.reject(err))
  },

  // 导出工具方法
  _utils: {
    getToken,
    setToken,
    clearToken,
    isTokenExpired
  }
};

export default request;
