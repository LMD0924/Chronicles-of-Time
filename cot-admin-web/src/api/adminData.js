import request from '@/utils/request'

export const adminDataApi = {
  dashboard() {
    return request.get('/admin/data/dashboard')
  },
  list(moduleKey, params) {
    return request.get('/admin/data/modules', {
      params: { moduleKey, ...params },
    })
  },
  detail(moduleKey, id) {
    return request.get(`/admin/data/modules/${moduleKey}/${id}`)
  },
  create(moduleKey, data) {
    return request.post(`/admin/data/modules/${moduleKey}`, data)
  },
  update(moduleKey, id, data) {
    return request.put(`/admin/data/modules/${moduleKey}/${id}`, data)
  },
  remove(moduleKey, id) {
    return request.delete(`/admin/data/modules/${moduleKey}/${id}`)
  },
}
