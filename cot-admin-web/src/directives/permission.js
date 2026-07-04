/**
 * 文件说明：拾光记后台管理系统拾光记项目脚本模块，封装拾光记项目相关的配置、状态、路由或工具逻辑。
 */
export const setupPermissionDirective = (app) => {
  app.directive('permission', {
    mounted(el, binding) {
      const user = JSON.parse(localStorage.getItem('cot_admin_user') || '{}')
      const permissions = user.permissions || []
      const required = Array.isArray(binding.value) ? binding.value : [binding.value]
      const allowed = user.roleCode === 'SUPER_ADMIN' || required.some((code) => permissions.includes(code))
      if (!allowed) el.parentNode?.removeChild(el)
    },
  })
}
