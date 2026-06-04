const { request } = require('../../utils/request')

Page({
  data: {
    tabs: ['全部', '待支付', '已确认', '制作中', '已完成'],
    activeTab: 0,
    orders: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    loadingMore: false,
    statusText: {
      0: '待支付',
      1: '已确认',
      2: '制作中',
      3: '已完成',
      4: '已取消'
    }
  },

  onLoad() {
    this.loadOrders()
  },

  onShow() {
    // Always reload to reflect latest order status
    this.setData({ page: 1, orders: [], hasMore: true })
    this.loadOrders()

    // 从下单确认页跳转过来时，自动打开订单详情
    const app = getApp()
    const pendingId = app.globalData.pendingOrderDetailId
    if (pendingId) {
      app.globalData.pendingOrderDetailId = null
      wx.navigateTo({
        url: `/pages/order-detail/index?id=${pendingId}`
      })
    }
  },

  onPullDownRefresh() {
    this.setData({ page: 1, orders: [], hasMore: true })
    this.loadOrders().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingMore) {
      this.loadMore()
    }
  },

  async loadOrders() {
    if (this.data.loading) return
    this.setData({ loading: true })

    try {
      const params = {
        page: this.data.page,
        size: this.data.size
      }
      // Tab 0 = all (no status filter), tabs 1-4 = status 0-3
      if (this.data.activeTab > 0) {
        params.status = this.data.activeTab - 1
      }

      const res = await request({
        url: '/order/list',
        data: params
      })

      const newOrders = res.data?.records || res.data || []
      const hasMore = newOrders.length >= this.data.size

      this.setData({
        orders: this.data.page === 1 ? newOrders : [...this.data.orders, ...newOrders],
        hasMore,
        loading: false
      })
    } catch {
      this.setData({ loading: false })
    }
  },

  loadMore() {
    this.setData({
      page: this.data.page + 1,
      loadingMore: true
    })
    this.loadOrders().then(() => {
      this.setData({ loadingMore: false })
    })
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index
    if (index === this.data.activeTab) return
    this.setData({
      activeTab: index,
      page: 1,
      orders: [],
      hasMore: true
    })
    this.loadOrders()
  },

  goDetail(e) {
    const id = String(e.currentTarget.dataset.id)
    wx.navigateTo({
      url: `/pages/order-detail/index?id=${id}`
    })
  },

  payOrder(e) {
    const id = String(e.currentTarget.dataset.id)
    wx.navigateTo({
      url: `/pages/order-detail/index?id=${id}`
    })
  },

  async cancelOrder(e) {
    const id = String(e.currentTarget.dataset.id)
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: async (modalRes) => {
        if (modalRes.confirm) {
          try {
            await request({
              url: `/order/cancel/${id}`,
              method: 'PUT'
            })
            wx.showToast({ title: '订单已取消', icon: 'success' })
            // Update local order status
            const orders = this.data.orders.map(order => {
              if (String(order.id) === id) {
                return { ...order, status: 4 }
              }
              return order
            })
            this.setData({ orders })
          } catch {}
        }
      }
    })
  },

  reOrder(e) {
    wx.switchTab({
      url: '/pages/index/index'
    })
  },

  goIndex() {
    wx.switchTab({
      url: '/pages/index/index'
    })
  }
})
