import request from '@/utils/request'

/**
 * 敏感词管理相关 API
 */

/**
 * 分页查询敏感词列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 当前页码（从1开始）
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.keyword - 敏感词内容（可选）
 * @param {string} params.category - 敏感词类别（可选）
 * @param {string} params.createdBy - 创建人（可选）
 * @returns {Promise} 敏感词列表
 *
 * 响应数据格式：
 * {
 *   code: 200,
 *   message: "操作成功",
 *   data: {
 *     records: [...],    // 敏感词列表
 *     total: 100,        // 总记录数
 *     size: 10,          // 每页数量
 *     current: 1,        // 当前页码
 *     pages: 10          // 总页数
 *   }
 * }
 */
export function getSensitiveWordList(params) {
  return request({
    url: '/sensitive-words',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询敏感词详情
 * @param {number} wordId - 敏感词ID
 * @returns {Promise} 敏感词详情
 */
export function getSensitiveWordById(wordId) {
  return request({
    url: `/sensitive-words/${wordId}`,
    method: 'get'
  })
}

/**
 * 创建新敏感词
 * @param {Object} data - 敏感词信息
 * @param {string} data.keyword - 敏感词内容（必填，1-100字符）
 * @param {string} data.category - 敏感词类别（必填，最多50字符）
 * @returns {Promise} 创建结果
 */
export function createSensitiveWord(data) {
  return request({
    url: '/sensitive-words',
    method: 'post',
    data
  })
}

/**
 * 修改敏感词信息
 * @param {Object} data - 敏感词信息
 * @param {number} data.wordId - 敏感词ID（必填）
 * @param {string} data.keyword - 敏感词内容（可选，1-100字符）
 * @param {string} data.category - 敏感词类别（可选，最多50字符）
 * @returns {Promise} 修改结果
 */
export function updateSensitiveWord(data) {
  return request({
    url: '/sensitive-words',
    method: 'put',
    data
  })
}

/**
 * 删除敏感词
 * @param {number} wordId - 敏感词ID
 * @returns {Promise} 删除结果
 */
export function deleteSensitiveWord(wordId) {
  return request({
    url: `/sensitive-words/${wordId}`,
    method: 'delete'
  })
}

/**
 * 批量导入敏感词
 * @param {FormData} formData - 包含文件的表单数据
 * @returns {Promise} 导入结果统计
 */
export function importSensitiveWords(formData) {
  return request({
    url: '/sensitive-words/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量导出敏感词
 * @param {Object} params - 查询条件（可选）
 * @returns {Promise} 文件下载
 */
export function exportSensitiveWords(params) {
  return request({
    url: '/sensitive-words/export',
    method: 'get',
    params,
    responseType: 'blob'  // 重要：接收二进制数据
  })
}

/**
 * 下载敏感词导入模板
 * @returns {Promise} 模板文件下载
 */
export function downloadTemplate() {
  return request({
    url: '/sensitive-words/template',
    method: 'get',
    responseType: 'blob'  // 重要：接收二进制数据
  })
}

/**
 * 获取所有敏感词（带缓存）
 * @returns {Promise} 所有敏感词列表
 */
export function getAllSensitiveWords() {
  return request({
    url: '/sensitive-words/all',
    method: 'get'
  })
}
