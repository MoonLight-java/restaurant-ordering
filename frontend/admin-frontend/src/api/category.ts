import request from '@/utils/request'

export function getCategories() {
  return request.get('/menu/categories')
}

export function getAllCategories() {
  return request.get('/admin/categories')
}

export function addCategory(data: any) {
  return request.post('/admin/categories', data)
}

export function updateCategory(id: string, data: any) {
  return request.put(`/admin/categories/${id}`, data)
}

export function deleteCategory(id: string) {
  return request.delete(`/admin/categories/${id}`)
}
