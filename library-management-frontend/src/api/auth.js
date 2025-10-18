import request from '@/utils/request'

/**
 * 认证相关 API
 */

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} 登录响应
 *
 * 响应数据格式：
 * {
 *   code: 200,
 *   message: "操作成功",
 *   data: {
 *     token: "eyJhbGciOiJIUzUxMiJ9...",
 *     tokenType: "Bearer",
 *     expiresIn: 7200000,
 *     userId: 1,
 *     username: "admin",
 *     realName: "系统管理员",
 *     role: "admin",
 *     department: "系统管理部",
 *     isActive: 1
 *   }
 * }
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise} 登出响应
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise} 用户信息
 */
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 刷新 token
 * @returns {Promise} 新的 token
 */
export function refreshToken() {
  return request({
    url: '/auth/refresh',
    method: 'post'
  })
}
