import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 用户状态管理 Store
 * 管理用户登录状态、用户信息、token 等
 */
export const useUserStore = defineStore('user', {
  /**
   * 状态数据
   */
  state: () => ({
    token: localStorage.getItem('token') || '',     // JWT token
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),  // 用户信息
    isLoggedIn: !!localStorage.getItem('token')     // 是否已登录
  }),

  /**
   * 计算属性（类似 Vue 的 computed）
   */
  getters: {
    // 获取用户名
    username: (state) => state.userInfo.username || '',

    // 获取真实姓名
    realName: (state) => state.userInfo.realName || '',

    // 获取用户角色
    role: (state) => state.userInfo.role || '',

    // 是否是管理员
    isAdmin: (state) => state.userInfo.role === 'admin'
  },

  /**
   * 方法（类似 Vue 的 methods）
   */
  actions: {
    /**
     * 登录
     * @param {Object} loginForm - 登录表单数据
     * @param {string} loginForm.username - 用户名
     * @param {string} loginForm.password - 密码
     */
    async login(loginForm) {
      try {
        // 调用登录接口
        const response = await loginApi({
          username: loginForm.username.trim(),
          password: loginForm.password
        })

        // 登录成功
        if (response.code === 200) {
          const { token, ...userInfo } = response.data

          // 保存 token 和用户信息到 state
          this.token = token
          this.userInfo = userInfo
          this.isLoggedIn = true

          // 持久化到 localStorage
          localStorage.setItem('token', token)
          localStorage.setItem('userInfo', JSON.stringify(userInfo))

          ElMessage.success('登录成功')

          // 跳转到原来要访问的页面，或后台首页
          const redirect = router.currentRoute.value.query.redirect || '/dashboard'
          router.push(redirect)

          return response
        }
      } catch (error) {
        console.error('登录失败:', error)
        throw error
      }
    },

    /**
     * 登出
     */
    async logout() {
      try {
        // 调用登出接口
        await logoutApi()
      } catch (error) {
        console.error('登出接口调用失败:', error)
      } finally {
        // 无论接口是否成功，都清除本地数据
        this.clearUserData()
        ElMessage.success('已退出登录')

        // 跳转到登录页
        router.push('/login')
      }
    },

    /**
     * 清除用户数据（退出登录或 token 过期时调用）
     */
    clearUserData() {
      this.token = ''
      this.userInfo = {}
      this.isLoggedIn = false
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    /**
     * 设置用户信息（用于更新用户资料）
     * @param {Object} userInfo - 用户信息
     */
    setUserInfo(userInfo) {
      this.userInfo = { ...this.userInfo, ...userInfo }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    }
  }
})
