import request from '@/utils/request'

/**
 * 用户管理相关 API
 */

/**
 * 分页查询用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 当前页码（从1开始）
 * @param {number} params.size - 每页数量
 * @param {string} params.username - 用户名（可选）
 * @param {string} params.realName - 真实姓名（可选）
 * @param {string} params.role - 角色（可选）
 * @param {string} params.department - 部门（可选）
 * @param {number} params.isActive - 状态（可选：0-禁用，1-启用）
 * @returns {Promise} 用户列表
 *
 * 响应数据格式：
 * {
 *   code: 200,
 *   message: "操作成功",
 *   data: {
 *     records: [...],    // 用户列表
 *     total: 100,        // 总记录数
 *     size: 10,          // 每页数量
 *     current: 1,        // 当前页码
 *     pages: 10          // 总页数
 *   }
 * }
 */
export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params: {
      ...params,
      _t: Date.now() // 添加时间戳防止缓存
    }
  })
}

/**
 * 根据ID查询用户详情
 * @param {number} userId - 用户ID
 * @returns {Promise} 用户详情
 */
export function getUserById(userId) {
  return request({
    url: `/users/${userId}`,
    method: 'get'
  })
}

/**
 * 创建新用户
 * @param {Object} data - 用户信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.realName - 真实姓名
 * @param {string} data.role - 角色（admin/user）
 * @param {string} data.department - 部门（可选）
 * @param {string} data.employeeId - 工号（可选）
 * @returns {Promise} 创建结果
 */
export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

/**
 * 修改用户信息
 * @param {Object} data - 用户信息
 * @param {number} data.userId - 用户ID（必填）
 * @param {string} data.realName - 真实姓名（可选）
 * @param {string} data.role - 角色（可选）
 * @param {string} data.department - 部门（可选）
 * @param {string} data.employeeId - 工号（可选）
 * @param {number} data.isActive - 状态（可选：0-禁用，1-启用）
 * @returns {Promise} 修改结果
 */
export function updateUser(data) {
  return request({
    url: '/users/update',
    method: 'post',
    data
  })
}

/**
 * 删除用户
 * @param {number} userId - 用户ID
 * @returns {Promise} 删除结果
 */
export function deleteUser(userId) {
  return request({
    url: `/users/delete/${userId}`,
    method: 'post'
  })
}

/**
 * 重置用户密码
 * @param {Object} data - 重置密码信息
 * @param {number} data.userId - 用户ID
 * @param {string} data.newPassword - 新密码
 * @returns {Promise} 重置结果
 */
export function resetPassword(data) {
  return request({
    url: '/users/reset-password',
    method: 'post',
    data
  })
}
