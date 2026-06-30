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
