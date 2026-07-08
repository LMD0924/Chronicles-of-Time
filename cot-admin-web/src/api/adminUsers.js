import request from '@/utils/request'

export const adminUserApi = {
  list(params) {
    return request.get('/admin/users', { params })
  },
  create(data) {
    return request.post('/admin/users', data)
  },
  update(id, data) {
    return request.put(`/admin/users/${id}`, data)
  },
  updateStatus(id, status) {
    return request.patch(`/admin/users/${id}/status`, { status })
  },
  resetPassword(id, password) {
    return request.patch(`/admin/users/${id}/password`, { password })
  },
  remove(id) {
    return request.delete(`/admin/users/${id}`)
  },
}
