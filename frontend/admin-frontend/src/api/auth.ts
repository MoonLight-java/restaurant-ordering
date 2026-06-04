import request from '@/utils/request'

export function adminLogin(username: string, password: string) {
  return request.post('/admin/auth/login', { username, password })
}

export function adminLogout() {
  return request.post('/admin/auth/logout')
}
