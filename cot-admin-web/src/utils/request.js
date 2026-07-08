/**
 * 文件说明：拾光记后台管理系统通用工具脚本模块，封装通用工具相关的配置、状态、路由或工具逻辑。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearToken } from '@/utils/auth'

// 所有后台接口统一走 /api 网关前缀，方便对接 Spring Cloud Gateway。
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})



// 请求发出前自动注入 JWT，后端网关会从 Authorization: Bearer 中解析登录用户。
service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})



// 响应拦截器兼容后端 RestBean/code 结构，同时保留普通 JSON 直出能力。
service.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data && typeof data === 'object' && 'code' in data) {
      if ([200, 0].includes(data.code)) return data.data ?? data
      ElMessage.error(data.msg || data.message || '请求处理失败')
      return Promise.reject(data)
    }
    return data
  },
  // 401 说明登录态已经失效，清理本地 token 后回到登录页重新认证。
  (error) => {
    if (error.response?.status === 401) {
      clearToken()
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  },
)

export default service
