import request from '@/utils/request'

/**
 * 出版社白名单管理相关 API
 */

/**
 * 分页查询出版社白名单列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 当前页码（从1开始）
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.publisherName - 出版社名称（可选，模糊查询）
 * @param {number} params.years - 年份批次（可选，精确查询）
 * @param {boolean} params.isActive - 是否启用（可选，精确查询）
 * @returns {Promise} 出版社白名单列表
 *
 * 响应数据格式：
 * {
 *   code: 200,
 *   message: "操作成功",
 *   data: {
 *     records: [...],    // 出版社白名单列表
 *     total: 100,        // 总记录数
 *     size: 10,          // 每页数量
 *     current: 1,        // 当前页码
 *     pages: 10          // 总页数
 *   }
 * }
 */
export function getPublisherWhitelistList(params) {
  return request({
    url: '/publisher-whitelist',
    method: 'get',
    params,
    timeout: 0
  })
}

/**
 * 根据ID查询出版社白名单详情
 * @param {number} publisherId - 出版社ID
 * @returns {Promise} 出版社白名单详情
 */
export function getPublisherWhitelistById(publisherId) {
  return request({
    url: `/publisher-whitelist/${publisherId}`,
    method: 'get'
  })
}

/**
 * 创建新出版社白名单
 * @param {Object} data - 出版社白名单信息
 * @param {string} data.publisherName - 出版社名称（必填，1-200字符）
 * @param {number} data.years - 年份批次（可选）
 * @param {boolean} data.isActive - 是否启用（可选，默认true）
 * @returns {Promise} 创建结果
 */
export function createPublisherWhitelist(data) {
  return request({
    url: '/publisher-whitelist',
    method: 'post',
    data
  })
}

/**
 * 修改出版社白名单信息
 * @param {Object} data - 出版社白名单信息
 * @param {number} data.publisherId - 出版社ID（必填）
 * @param {string} data.publisherName - 出版社名称（可选，1-200字符）
 * @param {number} data.years - 年份批次（可选）
 * @param {boolean} data.isActive - 是否启用（可选）
 * @returns {Promise} 修改结果
 */
export function updatePublisherWhitelist(data) {
  return request({
    url: '/publisher-whitelist/update',
    method: 'post',
    data
  })
}

/**
 * 删除出版社白名单
 * @param {number} publisherId - 出版社ID
 * @returns {Promise} 删除结果
 */
export function deletePublisherWhitelist(publisherId) {
  return request({
    url: `/publisher-whitelist/delete/${publisherId}`,
    method: 'post'
  })
}

/**
 * 批量导入出版社白名单
 * @param {FormData} formData - 包含文件的表单数据
 * @returns {Promise} 导入结果统计
 */
export function importPublisherWhitelists(formData) {
  return request({
    url: '/publisher-whitelist/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 0
  })
}

/**
 * 批量导出出版社白名单
 * @param {Object} params - 查询条件（可选）
 * @returns {Promise} 文件下载
 */
export function exportPublisherWhitelists(params) {
  return request({
    url: '/publisher-whitelist/export',
    method: 'get',
    params,
    responseType: 'blob',  // 重要：接收二进制数据
    timeout: 0
  })
}

/**
 * 下载出版社白名单导入模板
 * @returns {Promise} 模板文件下载
 */
export function downloadTemplate() {
  return request({
    url: '/publisher-whitelist/template',
    method: 'get',
    responseType: 'blob',  // 重要：接收二进制数据
    timeout: 0
  })
}

/**
 * 获取所有启用的出版社白名单（带缓存）
 * @returns {Promise} 所有启用的出版社白名单列表
 */
export function getAllActivePublisherWhitelists() {
  return request({
    url: '/publisher-whitelist/all',
    method: 'get',
    timeout: 0
  })
}
