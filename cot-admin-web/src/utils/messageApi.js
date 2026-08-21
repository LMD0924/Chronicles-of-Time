import { ElMessage } from 'element-plus'

const normalize = (content) => {
  if (content == null) return ''
  if (typeof content === 'string') return content
  return content.message || content.msg || String(content)
}

const buildCustomClass = (type, customClass) =>
  ['cot-message', `cot-message--${type}`, customClass].filter(Boolean).join(' ')

const show = (type, content, options = {}) => {
  const { customClass, ...restOptions } = options

  return ElMessage({
    message: normalize(content),
    type: type === 'loading' ? 'info' : type,
    duration: type === 'loading' ? 0 : 3200,
    grouping: true,
    showClose: type !== 'loading',
    ...restOptions,
    customClass: buildCustomClass(type, customClass),
  })
}

export const messageApi = {
  success: (content, options = {}) => show('success', content, options),
  warning: (content, options = {}) => show('warning', content, options),
  error: (content, options = {}) => show('error', content, options),
  info: (content, options = {}) => show('info', content, options),
  loading: (content, duration = 0) => show('loading', content, { duration }),
  closeAll: () => ElMessage.closeAll(),
}

export default messageApi
