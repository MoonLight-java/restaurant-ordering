const { request } = require('../../utils/request')
const app = getApp()

Page({
  data: {
    avatar: '',
    nickname: '',
    phone: '',
    gender: 2
  },

  onLoad() {
    this.loadProfile()
  },

  async loadProfile() {
    try {
      const res = await request({ url: '/user/profile' })
      if (res.data) {
        this.setData({
          avatar: this.resolveAvatar(res.data.avatar),
          nickname: res.data.nickname || '',
          phone: res.data.phone || '',
          gender: res.data.gender !== null && res.data.gender !== undefined ? res.data.gender : 2
        })
      }
    } catch {}
  },

  resolveAvatar(avatar) {
    if (!avatar || avatar.indexOf('http') === 0 || avatar.indexOf('wxfile://') === 0) return avatar
    const serverBase = app.globalData.baseUrl.replace(/\/api$/, '')
    return serverBase + avatar
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  selectGender(e) {
    this.setData({ gender: Number(e.currentTarget.dataset.gender) })
  },

  changeAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]
        this.uploadAvatar(tempFilePath)
      }
    })
  },

  uploadAvatar(filePath) {
    wx.showLoading({ title: '上传中...' })
    wx.uploadFile({
      url: app.globalData.baseUrl + '/user/profile/avatar',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        wx.hideLoading()
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200 && data.data && data.data.url) {
            this.setData({ avatar: this.resolveAvatar(data.data.url) })
            wx.showToast({ title: '头像已更新', icon: 'success' })
          } else {
            wx.showToast({ title: data.message || '上传失败', icon: 'none' })
          }
        } catch (e) {
          wx.showToast({ title: '上传失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    })
  },

  async saveProfile() {
    const { nickname, phone, gender } = this.data
    if (!nickname.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }

    wx.showLoading({ title: '保存中...' })
    try {
      await request({
        url: '/user/profile',
        method: 'PUT',
        data: { nickname: nickname.trim(), phone: phone.trim(), gender }
      })
      wx.hideLoading()
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1500)
    } catch {
      wx.hideLoading()
    }
  }
})
