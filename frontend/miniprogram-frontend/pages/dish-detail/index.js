const { request } = require('../../utils/request')

Page({
  data: {
    dish: {},
    selectedSpecs: {},
    quantity: 1,
    totalPrice: 0
  },

  onLoad(options) {
    const dishId = options.id
    if (dishId) {
      this.loadDishDetail(dishId)
    }
  },

  async loadDishDetail(id) {
    try {
      const res = await request({ url: `/menu/dishes/${id}` })
      const dish = res.data || {}
      this.setData({ dish })
      this.initSelectedSpecs(dish)
      this.computeTotal()
    } catch {}
  },

  initSelectedSpecs(dish) {
    const selectedSpecs = {}
    const groups = dish.specGroups || []
    groups.forEach(group => {
      if (group.selectType === 0) {
        // Single select: default to first item
        if (group.items && group.items.length > 0) {
          selectedSpecs[String(group.id)] = String(group.items[0].id)
        }
      } else if (group.selectType === 1) {
        // Multi select: empty array
        selectedSpecs[String(group.id)] = []
      }
    })
    this.setData({ selectedSpecs })
  },

  onSpecSelect(e) {
    const groupId = String(e.currentTarget.dataset.groupId)
    const itemId = String(e.currentTarget.dataset.itemId)
    const selectType = e.currentTarget.dataset.selectType
    const selectedSpecs = JSON.parse(JSON.stringify(this.data.selectedSpecs))

    if (selectType === 0) {
      // Single select: replace
      selectedSpecs[groupId] = itemId
    } else if (selectType === 1) {
      // Multi select: toggle
      let arr = selectedSpecs[groupId] || []
      const idx = arr.indexOf(itemId)
      if (idx > -1) {
        arr.splice(idx, 1)
      } else {
        arr.push(itemId)
      }
      selectedSpecs[groupId] = arr
    }

    this.setData({ selectedSpecs })
    this.computeTotal()
  },

  computeTotal() {
    const { dish, selectedSpecs, quantity } = this.data
    let basePrice = Number(dish.price || 0)

    // Sum all selected spec item price adjustments
    let specAdjust = 0
    const groups = dish.specGroups || []
    groups.forEach(group => {
      const selected = selectedSpecs[String(group.id)]
      if (group.items && group.items.length > 0) {
        if (group.selectType === 0 && selected) {
          const item = group.items.find(i => String(i.id) === selected)
          if (item) {
            specAdjust += Number(item.priceAdjust || 0)
          }
        } else if (group.selectType === 1 && Array.isArray(selected)) {
          selected.forEach(sid => {
            const item = group.items.find(i => String(i.id) === sid)
            if (item) {
              specAdjust += Number(item.priceAdjust || 0)
            }
          })
        }
      }
    })

    const totalPrice = ((basePrice + specAdjust) * quantity).toFixed(2)
    this.setData({ totalPrice })
  },

  decreaseQty() {
    if (this.data.quantity <= 1) return
    this.setData({ quantity: this.data.quantity - 1 })
    this.computeTotal()
  },

  increaseQty() {
    this.setData({ quantity: this.data.quantity + 1 })
    this.computeTotal()
  },

  async addToCart() {
    const { dish, selectedSpecs, quantity, totalPrice } = this.data

    // Build specJson: array of selected spec objects
    const specJson = []
    const groups = dish.specGroups || []
    groups.forEach(group => {
      const selected = selectedSpecs[String(group.id)]
      if (group.selectType === 0 && selected) {
        const item = group.items.find(i => String(i.id) === selected)
        if (item) {
          specJson.push({
            groupId: String(group.id),
            groupName: group.name,
            itemId: String(item.id),
            itemName: item.name,
            priceAdjust: Number(item.priceAdjust || 0)
          })
        }
      } else if (group.selectType === 1 && Array.isArray(selected) && selected.length > 0) {
        selected.forEach(sid => {
          const item = group.items.find(i => String(i.id) === sid)
          if (item) {
            specJson.push({
              groupId: String(group.id),
              groupName: group.name,
              itemId: String(item.id),
              itemName: item.name,
              priceAdjust: Number(item.priceAdjust || 0)
            })
          }
        })
      }
    })

    const unitPrice = (Number(totalPrice) / quantity).toFixed(2)

    try {
      await request({
        url: '/cart/items',
        method: 'POST',
        data: {
          dishId: String(dish.id),
          dishName: dish.name,
          dishImage: dish.image || '',
          specJson: JSON.stringify(specJson),
          quantity: quantity,
          unitPrice: unitPrice
        }
      })
      wx.showToast({ title: '已加入购物车', icon: 'success', duration: 1500 })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch {}
  }
})
