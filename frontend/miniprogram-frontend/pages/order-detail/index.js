const { request } = require('../../utils/request')

Page({
  data: {
    order: null,
    items: [],
    address: null,
    statusText: '',
    loading: true
  },

  onLoad(options) {
    this.orderId = options.id
    this.loadOrder()
  },

  async loadOrder() {
    this.setData({ loading: true })
    try {
      const res = await request({
        url: `/order/detail/${this.orderId}`
      })
      const data = res.data
      const order = data.order || data
      const items = data.items || data.orderItems || order.orderItems || []

      // Parse address
      let address = null
      try {
        if (order.addressJson) {
          address = typeof order.addressJson === 'string'
            ? JSON.parse(order.addressJson)
            : order.addressJson
        }
      } catch (e) {
        // ignore parse error
      }

      const statusMap = {
        0: '待支付',
        1: '已确认',
        2: '制作中',
        3: '已完成',
        4: '已取消'
      }

      this.setData({
        order,
        items,
        address,
        statusText: statusMap[order.status] || '未知状态',
        loading: false
      })
    } catch {
      this.setData({ loading: false })
    }
  },

  formatSpec(specJson) {
    try {
      const specs = typeof specJson === 'string' ? JSON.parse(specJson) : specJson
      if (Array.isArray(specs) && specs.length > 0) {
        return specs.map(s => s.itemName || s.name || '').join(', ')
      }
    } catch (e) {
      // ignore parse error
    }
    return ''
  },

  async payOrder() {
    wx.showLoading({ title: '支付中...' })
    try {
      // Step 1: Create payment (userId from JWT header, not body)
      await request({
        url: '/payment/create',
        method: 'POST',
        data: {
          orderId: String(this.orderId),
          payMethod: 1
        }
      })

      // Step 2: Mock pay (userId from JWT header)
      await request({
        url: `/payment/mock-pay/${this.orderId}`,
        method: 'POST'
      })

      wx.hideLoading()
      wx.showToast({ title: '支付成功', icon: 'success' })
      this.loadOrder()
    } catch {
      wx.hideLoading()
      wx.showToast({ title: '支付失败', icon: 'none' })
    }
  },

  async cancelOrder() {
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: async (modalRes) => {
        if (modalRes.confirm) {
          try {
            await request({
              url: `/order/cancel/${this.orderId}`,
              method: 'PUT'
            })
            wx.showToast({ title: '订单已取消', icon: 'success' })
            this.loadOrder()
          } catch {}
        }
      }
    })
  },

  reOrder() {
    wx.switchTab({
      url: '/pages/index/index'
    })
  }
})
