import request from '@/utils/request'

export function getUsers(params: any) {
  return request.get('/admin/users', { params })
}

export function updateUserStatus(id: string, status: number) {
  return request.put(`/admin/users/${id}/status`, { status })
}
