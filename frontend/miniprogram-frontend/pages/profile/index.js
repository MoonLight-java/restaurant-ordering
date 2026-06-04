const { request } = require('../../utils/request')

Page({
  data: {
    user: { nickname: '未登录', avatar: '', phone: '' },
    orderCounts: {}
  },

  onShow() {
    this.loadUserInfo()
    this.loadOrderCounts()
  },

  resolveAvatar(avatar) {
    if (!avatar || avatar.indexOf('http') === 0 || avatar.indexOf('wxfile://') === 0) return avatar
    const serverBase = getApp().globalData.baseUrl.replace(/\/api$/, '')
    return serverBase + avatar
  },

  async loadUserInfo() {
    try {
      const res = await request({ url: '/user/profile' })
      if (res.data) {
        if (res.data.avatar) {
          res.data.avatar = this.resolveAvatar(res.data.avatar)
        }
        this.setData({ user: res.data })
      }
    } catch {}
  },

  async loadOrderCounts() {
    try {
      const [pending, confirmed, completed] = await Promise.all([
        request({ url: '/order/list', data: { status: 0, page: 1, size: 1 } }),
        request({ url: '/order/list', data: { status: 1, page: 1, size: 1 } }),
        request({ url: '/order/list', data: { status: 3, page: 1, size: 1 } })
      ])
      this.setData({
        orderCounts: {
          pending: pending.data?.total || 0,
          confirmed: confirmed.data?.total || 0,
          completed: completed.data?.total || 0
        }
      })
    } catch {}
  },

  goOrders() { wx.switchTab({ url: '/pages/order-list/index' }) },
  goAddress() { wx.navigateTo({ url: '/pages/address-list/index' }) },
  goEditProfile() { wx.navigateTo({ url: '/pages/profile-edit/index' }) }
})
