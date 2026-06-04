import request from '@/utils/request'

export function getDishes(params: any) {
  return request.get('/menu/dishes', { params })
}

export function getDishDetail(id: string) {
  return request.get(`/menu/dishes/${id}`)
}

export function addDish(data: any) {
  return request.post('/admin/dishes', data)
}

export function updateDish(id: string, data: any) {
  return request.put(`/admin/dishes/${id}`, data)
}

export function deleteDish(id: string) {
  return request.delete(`/admin/dishes/${id}`)
}

export function updateDishStatus(id: string, status: number) {
  return request.put(`/admin/dishes/${id}/status`, { status })
}

export function uploadDishImage(id: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/admin/dishes/${id}/image`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function addSpecGroup(dishId: string, data: any) {
  return request.post(`/admin/dishes/${dishId}/specs`, data)
}

export function updateSpecGroup(id: string, data: any) {
  return request.put(`/admin/specs/${id}`, data)
}

export function deleteSpecGroup(id: string) {
  return request.delete(`/admin/specs/${id}`)
}

export function addSpecItem(groupId: string, data: any) {
  return request.post(`/admin/specs/${groupId}/items`, data)
}

export function updateSpecItem(id: string, data: any) {
  return request.put(`/admin/spec-items/${id}`, data)
}

export function deleteSpecItem(id: string) {
  return request.delete(`/admin/spec-items/${id}`)
}
