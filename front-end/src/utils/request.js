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
    // 记住我：都存 localStorage
    localStorage.setItem(TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  } else {
    // 普通登录：accessToken 存 sessionStorage，refreshToken 存 localStorage
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
// 等待队列
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

      // 判断是否是记住我模式（通过 localStorage 是否有 token 判断）
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
    // 排除刷新 Token 的请求，避免死循环
    if (config.url === 'auth/refresh') {
      return config
    }

    const token = getToken()
    if (token) {
      // 检查 Token 是否过期
      if (isTokenExpired(token)) {
        // Token 已过期，但不在这里处理，让响应拦截器处理 401
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

// 3. 响应拦截器：统一处理响应结果 + Token 自动刷新
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code !== 200) {
      // 401 未授权，尝试刷新 Token
      if (res.code === 401) {
        // 获取原始请求配置
        const originalRequest = response.config

        // 避免重复刷新
        if (originalRequest._retry) {
          handleUnauthorized()
          return Promise.reject(res)
        }

        originalRequest._retry = true

        // 如果已经在刷新中，加入等待队列
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
    // 处理网络错误/状态码错误
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

// 4. 通用请求方法封装
const request = {
  // GET请求
  get: (url, params = {}, successCallback) => {
    return service.get(url, { params })
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      })
      .catch(err => {
        return Promise.reject(err);
      });
  },

  // POST请求
  post: (url, data = {}, successCallback) => {
    return service.post(url, data)
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      })
      .catch(err => {
        return Promise.reject(err);
      });
  },

  // PUT请求
  put: (url, data = {}, successCallback) => {
    return service.put(url, data)
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      })
      .catch(err => {
        return Promise.reject(err);
      });
  },

  // DELETE请求
  delete: (url, params = {}, successCallback) => {
    return service.delete(url, { params })
      .then(res => {
        successCallback && successCallback(res.msg, res.data);
        return res;
      })
      .catch(err => {
        return Promise.reject(err);
      });
  },

  // 导出工具方法供外部使用
  _utils: {
    getToken,
    setToken,
    clearToken,
    isTokenExpired
  }
};

// 导出通用请求对象
export default request;
