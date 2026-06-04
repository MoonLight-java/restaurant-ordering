const { request } = require('../../utils/request')
const app = getApp()

Page({
  data: {
    items: [],
    selectedIds: [],
    selectAll: false,
    totalPrice: 0
  },

  onShow() {
    this.loadCartItems()
  },

  async loadCartItems() {
    try {
      const res = await request({ url: '/cart/items' })
      const items = res.data || []
      this.setData({ items })

      // Restore previously selected IDs (ensure string comparison)
      const savedIds = (app.globalData.selectedCartIds || []).map(id => String(id))
      const validIds = savedIds.filter(id => items.some(item => String(item.id) === id))
      this.setData({
        selectedIds: validIds,
        selectAll: validIds.length > 0 && validIds.length === items.length
      })
      this.computeTotal()
    } catch {}
  },

  toggleSelect(e) {
    const id = String(e.currentTarget.dataset.id)
    let selectedIds = [...this.data.selectedIds]
    const idx = selectedIds.indexOf(id)
    if (idx > -1) {
      selectedIds.splice(idx, 1)
    } else {
      selectedIds.push(id)
    }
    this.setData({
      selectedIds,
      selectAll: selectedIds.length === this.data.items.length
    })
    this.computeTotal()
  },

  toggleSelectAll() {
    const { items, selectAll } = this.data
    let selectedIds = []
    if (!selectAll) {
      selectedIds = items.map(item => String(item.id))
    }
    this.setData({
      selectedIds,
      selectAll: !selectAll
    })
    this.computeTotal()
  },

  async quantityChange(e) {
    const id = String(e.currentTarget.dataset.id)
    const newQty = Number(e.currentTarget.dataset.qty)

    if (newQty < 1) return

    try {
      await request({
        url: `/cart/items/${id}`,
        method: 'PUT',
        data: { quantity: newQty }
      })

      // Update local state
      const items = this.data.items.map(item => {
        if (String(item.id) === id) {
          return { ...item, quantity: newQty }
        }
        return item
      })
      this.setData({ items })
      this.computeTotal()
    } catch {}
  },

  async deleteItem(e) {
    const id = String(e.currentTarget.dataset.id)
    wx.showModal({
      title: '提示',
      content: '确定要删除该商品吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request({
              url: `/cart/items/${id}`,
              method: 'DELETE'
            })

            const items = this.data.items.filter(item => String(item.id) !== id)
            const selectedIds = this.data.selectedIds.filter(sid => String(sid) !== id)
            this.setData({
              items,
              selectedIds,
              selectAll: selectedIds.length > 0 && items.length > 0 && selectedIds.length === items.length
            })
            this.computeTotal()
            wx.showToast({ title: '已删除', icon: 'success' })
          } catch {}
        }
      }
    })
  },

  computeTotal() {
    const { items, selectedIds } = this.data
    let total = 0
    items.forEach(item => {
      if (selectedIds.indexOf(String(item.id)) > -1) {
        total += Number(item.unitPrice || 0) * Number(item.quantity || 0)
      }
    })
    this.setData({ totalPrice: total.toFixed(2) })
  },

  goCheckout() {
    const { selectedIds } = this.data
    if (selectedIds.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' })
      return
    }
    app.globalData.selectedCartIds = selectedIds
    wx.navigateTo({ url: '/pages/order-confirm/index' })
  },

  goOrder() {
    wx.switchTab({ url: '/pages/index/index' })
  }
})
