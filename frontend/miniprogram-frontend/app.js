App({
  onLaunch() {
    this.checkLogin()
  },

  checkLogin() {
    const token = wx.getStorageSync('token')
    if (!token) {
      this.wxLogin()
    } else {
      this.globalData.token = token
    }
  },

  wxLogin() {
    wx.login({
      success: (res) => {
        if (res.code) {
          wx.request({
            url: this.globalData.baseUrl + '/auth/login/wechat',
            method: 'POST',
            data: { code: res.code },
            success: (result) => {
              if (result.data.code === 200) {
                const { token, userId, nickname, avatar } = result.data.data
                wx.setStorageSync('token', token)
                wx.setStorageSync('userId', String(userId))
                wx.setStorageSync('nickname', nickname)
                wx.setStorageSync('avatar', avatar)
                this.globalData.token = token
                this.globalData.userId = String(userId)
              }
            },
            fail: () => {
              console.log('Login failed, retrying...')
            }
          })
        }
      }
    })
  },

  globalData: {
    token: '',
    userId: 0,
    baseUrl: 'http://localhost:8080/api' // 部署时修改为实际服务器地址
  }
})
