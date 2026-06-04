const { request } = require('../../utils/request')

Page({
  data: {
    addresses: [],
    selectMode: false
  },

  onLoad(options) {
    this.setData({ selectMode: options.select === 'true' })
  },

  onShow() {
    this.loadAddresses()
  },

  async loadAddresses() {
    try {
      const res = await request({ url: '/user/addresses' })
      this.setData({ addresses: res.data || [] })
    } catch {}
  },

  selectAddress(e) {
    if (!this.data.selectMode) return
    const addr = e.currentTarget.dataset.address
    const pages = getCurrentPages()
    const prevPage = pages[pages.length - 2]
    if (prevPage) {
      prevPage.setData({ address: addr })
    }
    wx.navigateBack()
  },

  editAddress(e) {
    const id = String(e.currentTarget.dataset.id)
    wx.navigateTo({ url: `/pages/address-edit/index?id=${id}` })
  },

  addAddress() {
    wx.navigateTo({ url: '/pages/address-edit/index' })
  },

  async deleteAddress(e) {
    const id = String(e.currentTarget.dataset.id)
    const res = await new Promise(resolve => {
      wx.showModal({ title: '提示', content: '确定删除该地址吗？', success: resolve })
    })
    if (!res.confirm) return
    try {
      await request({ url: `/user/addresses/${id}`, method: 'DELETE' })
      wx.showToast({ title: '删除成功', icon: 'success' })
      this.loadAddresses()
    } catch {}
  }
})
