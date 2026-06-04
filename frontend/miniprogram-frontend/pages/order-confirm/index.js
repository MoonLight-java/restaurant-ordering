const { request } = require('../../utils/request')

Page({
  data: {
    address: null,
    items: [],
    totalAmount: 0,
    remark: ''
  },

  onLoad() {
    const app = getApp()
    this.selectedIds = app.globalData?.selectedCartIds || []
  },

  onShow() {
    this.loadDefaultAddress()
    this.loadCartItems()
  },

  async loadDefaultAddress() {
    try {
      const res = await request({ url: '/user/addresses' })
      const addresses = res.data || []
      const defaultAddr = addresses.find(a => a.isDefault === 1) || addresses[0]
      this.setData({ address: defaultAddr || null })
    } catch {}
  },

  async loadCartItems() {
    try {
      const res = await request({ url: '/cart/items' })
      const allItems = res.data || []
      let items = allItems
      if (this.selectedIds && this.selectedIds.length > 0) {
        const selectedIds = this.selectedIds.map(id => String(id))
        items = allItems.filter(item => selectedIds.includes(String(item.id)))
      }
      const totalAmount = items.reduce((sum, item) => sum + (item.unitPrice || 0) * (item.quantity || 0), 0)
      this.setData({ items, totalAmount: totalAmount.toFixed(2) })
    } catch {}
  },

  formatSpec(specJson) {
    try {
      const specs = typeof specJson === 'string' ? JSON.parse(specJson) : specJson
      if (Array.isArray(specs) && specs.length > 0) {
        return specs.map(s => s.itemName || s.name || '').join(', ')
      }
    } catch {}
    return ''
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value })
  },

  chooseAddress() {
    wx.navigateTo({ url: '/pages/address-list/index?select=true' })
  },

  async submitOrder() {
    if (!this.data.address) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' })
      return
    }
    const cartItemIds = this.data.items.map(item => String(item.id))
    try {
      const res = await request({
        url: '/order/create',
        method: 'POST',
        data: {
          addressId: String(this.data.address.id),
          remark: this.data.remark,
          cartItemIds: cartItemIds
        }
      })
      const { orderId, orderNo, payAmount } = res.data
      const app = getApp()
      app.globalData.pendingOrderDetailId = orderId
      wx.switchTab({ url: '/pages/order-list/index' })
    } catch {}
  }
})
