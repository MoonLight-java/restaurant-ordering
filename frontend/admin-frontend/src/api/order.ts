import request from '@/utils/request'

export function getOrders(params: any) {
  return request.get('/admin/orders', { params })
}

export function getOrderDetail(id: string) {
  return request.get(`/admin/orders/${id}`)
}

export function changeOrderStatus(id: string, data: any) {
  return request.put(`/admin/orders/${id}/status`, data)
}

export function getDashboardStats() {
  return request.get('/admin/dashboard/statistics')
}
