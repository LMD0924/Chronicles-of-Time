import { ElMessage } from 'element-plus'

const normalize = (content) => {
  if (content == null) return ''
  if (typeof content === 'string') return content
  return content.message || content.msg || String(content)
}

export const messageApi = {
  success: (content, options = {}) => ElMessage.success({ message: normalize(content), ...options }),
  warning: (content, options = {}) => ElMessage.warning({ message: normalize(content), ...options }),
  error: (content, options = {}) => ElMessage.error({ message: normalize(content), ...options }),
  info: (content, options = {}) => ElMessage.info({ message: normalize(content), ...options }),
  loading: (content, duration = 0) => ElMessage({ message: normalize(content), type: 'info', duration }),
  closeAll: () => ElMessage.closeAll(),
}

export default messageApi
