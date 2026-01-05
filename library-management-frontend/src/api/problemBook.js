import request from '@/utils/request'

/**
 * 问题书目管理相关 API
 */

/**
 * 分页查询问题书目列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 当前页码（从1开始）
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.bookName - 书名（可选，模糊查询）
 * @param {string} params.author - 作者（可选，模糊查询）
 * @param {string} params.isbn - ISBN编号（可选，精确查询）
 * @param {string} params.publisher - 出版社（可选，模糊查询）
 * @param {string} params.publishYear - 出版年份（可选，精确查询）
 * @param {string} params.problemType - 问题类型（可选，模糊查询）
 * @returns {Promise} 问题书目列表
 *
 * 响应数据格式：
 * {
 *   code: 200,
 *   message: "操作成功",
 *   data: {
 *     records: [...],    // 问题书目列表
 *     total: 100,        // 总记录数
 *     size: 10,          // 每页数量
 *     current: 1,        // 当前页码
 *     pages: 10          // 总页数
 *   }
 * }
 */
export function getProblemBookList(params) {
  return request({
    url: '/problem-books',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询问题书目详情
 * @param {number} bookId - 书目ID
 * @returns {Promise} 问题书目详情
 */
export function getProblemBookById(bookId) {
  return request({
    url: `/problem-books/${bookId}`,
    method: 'get'
  })
}

/**
 * 创建新问题书目
 * @param {Object} data - 问题书目信息
 * @param {string} data.bookName - 书名（必填，1-500字符）
 * @param {string} data.author - 作者（可选，最多200字符）
 * @param {string} data.isbn - ISBN编号（可选，最多20字符）
 * @param {string} data.publisher - 出版社（可选，最多200字符）
 * @param {string} data.publishYear - 出版年份（可选，格式：YYYY）
 * @param {string} data.problemType - 问题类型（可选，最多100字符）
 * @param {string} data.source - 来源（可选，最多500字符）
 * @returns {Promise} 创建结果
 */
export function createProblemBook(data) {
  return request({
    url: '/problem-books',
    method: 'post',
    data
  })
}

/**
 * 修改问题书目信息
 * @param {Object} data - 问题书目信息
 * @param {number} data.bookId - 书目ID（必填）
 * @param {string} data.bookName - 书名（可选，1-500字符）
 * @param {string} data.author - 作者（可选，最多200字符）
 * @param {string} data.isbn - ISBN编号（可选，最多20字符）
 * @param {string} data.publisher - 出版社（可选，最多200字符）
 * @param {string} data.publishYear - 出版年份（可选，格式：YYYY）
 * @param {string} data.problemType - 问题类型（可选，最多100字符）
 * @param {string} data.source - 来源（可选，最多500字符）
 * @returns {Promise} 修改结果
 */
export function updateProblemBook(data) {
  return request({
    url: '/problem-books/update',
    method: 'post',
    data
  })
}

/**
 * 删除问题书目
 * @param {number} bookId - 书目ID
 * @returns {Promise} 删除结果
 */
export function deleteProblemBook(bookId) {
  return request({
    url: `/problem-books/delete/${bookId}`,
    method: 'post'
  })
}

/**
 * 批量导入问题书目
 * @param {FormData} formData - 包含文件的表单数据
 * @returns {Promise} 导入结果统计
 */
export function importProblemBooks(formData) {
  return request({
    url: '/problem-books/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量导出问题书目
 * @param {Object} params - 查询条件（可选）
 * @returns {Promise} 文件下载
 */
export function exportProblemBooks(params) {
  return request({
    url: '/problem-books/export',
    method: 'get',
    params,
    responseType: 'blob'  // 重要：接收二进制数据
  })
}

/**
 * 下载问题书目导入模板
 * @returns {Promise} 模板文件下载
 */
export function downloadTemplate() {
  return request({
    url: '/problem-books/template',
    method: 'get',
    responseType: 'blob'  // 重要：接收二进制数据
  })
}

/**
 * 获取所有问题书目（带缓存）
 * @returns {Promise} 所有问题书目列表
 */
export function getAllProblemBooks() {
  return request({
    url: '/problem-books/all',
    method: 'get'
  })
}
