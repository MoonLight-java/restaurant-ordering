const { request } = require('../../utils/request')

Page({
  data: {
    isEdit: false,
    editId: null,
    form: {
      contactName: '',
      contactPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: false
    }
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ isEdit: true, editId: options.id })
      this.loadAddress(options.id)
    }
  },

  async loadAddress(addressId) {
    try {
      const res = await request({ url: '/user/addresses' })
      const addr = (res.data || []).find(a => String(a.id) === String(addressId))
      if (addr) {
        this.setData({
          form: {
            contactName: addr.contactName || '',
            contactPhone: addr.contactPhone || '',
            province: addr.province || '',
            city: addr.city || '',
            district: addr.district || '',
            detailAddress: addr.detailAddress || '',
            isDefault: addr.isDefault === 1
          }
        })
      }
    } catch {}
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    this.setData({ [`form.${field}`]: value })
  },

  onRegionChange(e) {
    const [province, city, district] = e.detail.value
    this.setData({
      'form.province': province,
      'form.city': city,
      'form.district': district
    })
  },

  onSwitchChange(e) {
    this.setData({ 'form.isDefault': e.detail.value })
  },

  async saveAddress() {
    const { form, isEdit, editId } = this.data
    if (!form.contactName.trim()) { wx.showToast({ title: '请输入联系人', icon: 'none' }); return }
    if (!form.contactPhone.trim()) { wx.showToast({ title: '请输入联系电话', icon: 'none' }); return }
    if (!form.detailAddress.trim()) { wx.showToast({ title: '请输入详细地址', icon: 'none' }); return }

    const data = {
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      province: form.province,
      city: form.city,
      district: form.district,
      detailAddress: form.detailAddress,
      isDefault: form.isDefault ? 1 : 0
    }

    try {
      if (isEdit) {
        await request({ url: `/user/addresses/${editId}`, method: 'PUT', data })
      } else {
        await request({ url: '/user/addresses', method: 'POST', data })
      }
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1500)
    } catch {}
  }
})
