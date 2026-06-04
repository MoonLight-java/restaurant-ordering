const { request } = require('../../utils/request')

Page({
  data: {
    categories: [],
    activeCategoryId: '',
    dishes: [],
    cartCount: 0,
    showSpecModal: false,
    currentDish: null,
    selectedSpecs: {},
    quantity: 1,
    totalPrice: 0
  },

  onLoad() {
    this.loadCategories()
  },

  onShow() {
    this.loadCartCount()
  },

  async loadCategories() {
    try {
      const res = await request({ url: '/menu/categories' })
      const categories = res.data || []
      this.setData({ categories })
      if (categories.length > 0) {
        this.setData({ activeCategoryId: categories[0].id })
        this.loadDishes()
      }
    } catch {}
  },

  async loadDishes() {
    try {
      const res = await request({
        url: '/menu/dishes',
        data: { categoryId: this.data.activeCategoryId }
      })
      this.setData({ dishes: res.data?.records || [] })
    } catch {}
  },

  switchCategory(e) {
    const id = String(e.currentTarget.dataset.id)
    this.setData({ activeCategoryId: id })
    this.loadDishes()
  },

  showDishDetail(e) {
    const dish = e.currentTarget.dataset.dish
    wx.navigateTo({ url: `/pages/dish-detail/index?id=${dish.id}` })
  },

  calcPrice(dish) {
    return Number(dish.price || 0).toFixed(2)
  },

  async addToCart(e) {
    const dish = e.currentTarget.dataset.dish
    try {
      await request({
        url: '/cart/items',
        method: 'POST',
        data: {
          dishId: String(dish.id),
          dishName: dish.name,
          dishImage: dish.image || '',
          specJson: '[]',
          quantity: 1,
          unitPrice: dish.price
        }
      })
      wx.showToast({ title: '已加入购物车', icon: 'success' })
      this.loadCartCount()
    } catch {}
  },

  async loadCartCount() {
    try {
      const res = await request({ url: '/cart/items' })
      const items = res.data || []
      this.setData({ cartCount: items.length })
    } catch {
      this.setData({ cartCount: 0 })
    }
  },

  goCart() {
    wx.switchTab({ url: '/pages/cart/index' })
  }
})
