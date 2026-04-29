import request from '@/utils/request'

/**
 * 馆藏图书管理相关 API
 */

/**
 * 分页查询馆藏图书列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 当前页码（从1开始）
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.barcode - 条码（可选，精确查询）
 * @param {string} params.bookName - 题名（可选，模糊查询）
 * @param {string} params.author - 著者（可选，模糊查询）
 * @param {string} params.isbn - ISBN（可选，精确查询）
 * @param {string} params.publisher - 出版社（可选，模糊查询）
 * @param {string} params.publishYear - 出版年（可选，精确查询）
 * @param {string} params.branchLibrary - 分馆（可选，精确查询）
 * @param {string} params.callNumber - 索书号（可选，模糊查询）
 * @param {string} params.batch - 批次（可选，精确查询）
 * @param {number} params.isStored - 是否入库（可选，0-否，1-是）
 * @param {string} params.libraryLocation - 馆藏院舍（可选，模糊查询）
 * @param {number} params.duplicateFlag - 重复标记（可选，0-否，1-是）
 * @param {number} params.isProblem - 是否问题图书（可选，0-正常，1-问题）
 * @param {string} params.problemType - 问题类型（可选，模糊查询）
 * @returns {Promise} 馆藏图书列表
 */
export function getCollectionBookList(params) {
    return request({
        url: '/collection-books',
        method: 'get',
        params,
        timeout: 0
    })
}

/**
 * 根据条码查询馆藏图书详情
 * @param {string} barcode - 条码
 * @returns {Promise} 馆藏图书详情
 */
export function getCollectionBookByBarcode(barcode) {
    return request({
        url: `/collection-books/${barcode}`,
        method: 'get'
    })
}

/**
 * 创建新馆藏图书
 * @param {Object} data - 馆藏图书信息
 * @param {string} data.barcode - 条码（必填）
 * @param {string} data.bookName - 题名（必填）
 * @param {string} data.author - 著者（可选）
 * @param {string} data.isbn - ISBN（可选）
 * @param {string} data.publisher - 出版社（可选）
 * @param {string} data.publishYear - 出版年（可选）
 * @param {string} data.branchLibrary - 分馆（可选）
 * @param {string} data.callNumber - 索书号（可选）
 * @param {number} data.price - 单价（可选）
 * @param {string} data.batch - 批次（可选）
 * @param {number} data.isStored - 是否入库（可选，默认1）
 * @param {string} data.libraryLocation - 馆藏院舍（可选）
 * @param {string} data.shelfLocation - 书架位置（可选）
 * @param {number} data.duplicateFlag - 重复标记（可选，默认0）
 * @param {number} data.isProblem - 是否问题图书（可选，默认0）
 * @param {string} data.problemType - 问题类型（可选）
 * @param {string} data.problemReason - 问题原因（可选）
 * @returns {Promise} 创建结果
 */
export function createCollectionBook(data) {
    return request({
        url: '/collection-books',
        method: 'post',
        data
    })
}

/**
 * 修改馆藏图书信息
 * @param {Object} data - 馆藏图书信息
 * @param {string} data.barcode - 条码（必填，用于定位记录）
 * @param {string} data.bookName - 题名（可选）
 * @param {string} data.author - 著者（可选）
 * @param {string} data.isbn - ISBN（可选）
 * @param {string} data.publisher - 出版社（可选）
 * @param {string} data.publishYear - 出版年（可选）
 * @param {string} data.branchLibrary - 分馆（可选）
 * @param {string} data.callNumber - 索书号（可选）
 * @param {number} data.price - 单价（可选）
 * @param {string} data.batch - 批次（可选）
 * @param {number} data.isStored - 是否入库（可选）
 * @param {string} data.libraryLocation - 馆藏院舍（可选）
 * @param {string} data.shelfLocation - 书架位置（可选）
 * @param {number} data.duplicateFlag - 重复标记（可选）
 * @param {number} data.isProblem - 是否问题图书（可选）
 * @param {string} data.problemType - 问题类型（可选）
 * @param {string} data.problemReason - 问题原因（可选）
 * @returns {Promise} 修改结果
 */
export function updateCollectionBook(data) {
    return request({
        url: '/collection-books/update',
        method: 'post',
        data
    })
}

/**
 * 删除馆藏图书
 * @param {string} barcode - 条码
 * @returns {Promise} 删除结果
 */
export function deleteCollectionBook(barcode) {
    return request({
        url: `/collection-books/delete/${barcode}`,
        method: 'post'
    })
}

/**
 * 批量删除馆藏图书
 * @param {Array<string>} barcodes - 条码列表
 * @returns {Promise} 删除结果
 */
export function deleteBatchCollectionBooks(barcodes) {
    return request({
        url: '/collection-books/delete/batch',
        method: 'post',
        data: barcodes
    })
}

/**
 * 批量导入馆藏图书
 * @param {FormData} formData - 包含文件的表单数据
 * @returns {Promise} 导入结果统计
 */
export function importCollectionBooks(formData) {
    return request({
        url: '/collection-books/import',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        timeout: 0
    })
}

/**
 * 批量导出馆藏图书
 * @param {Object} params - 查询条件（可选）
 * @returns {Promise} 文件下载
 */
export function exportCollectionBooks(params) {
    return request({
        url: '/collection-books/export',
        method: 'get',
        params,
        responseType: 'blob',
        timeout: 0
    })
}

/**
 * 下载馆藏图书导入模板
 * @returns {Promise} 模板文件下载
 */
export function downloadCollectionBookTemplate() {
    return request({
        url: '/collection-books/template',
        method: 'get',
        responseType: 'blob',
        timeout: 0
    })
}

/**
 * 获取所有正常馆藏图书
 * @returns {Promise} 正常馆藏图书列表
 */
export function getAllNormalBooks() {
    return request({
        url: '/collection-books/all/normal',
        method: 'get'
    })
}

/**
 * 获取所有问题图书
 * @returns {Promise} 问题图书列表
 */
export function getAllProblemBooks() {
    return request({
        url: '/collection-books/all/problem',
        method: 'get'
    })
}

/**
 * 将馆藏图书标记为问题图书
 * @param {string} barcode - 条码
 * @param {string} problemType - 问题类型
 * @param {string} problemReason - 问题原因
 * @returns {Promise} 更新结果
 */
export function markAsProblem(barcode, problemType, problemReason) {
    return request({
        url: `/collection-books/${barcode}/mark-problem`,
        method: 'post',
        params: { problemType, problemReason }
    })
}

/**
 * 将问题图书恢复为正常馆藏
 * @param {string} barcode - 条码
 * @returns {Promise} 更新结果
 */
export function markAsNormal(barcode) {
    return request({
        url: `/collection-books/${barcode}/mark-normal`,
        method: 'post'
    })
}

/**
 * 获取统计信息
 * @returns {Promise} 统计数据
 */
export function getCollectionBookStatistics() {
    return request({
        url: '/collection-books/statistics',
        method: 'get',
        timeout: 0
    })
}

/**
 * 从馆藏书目创建检测任务
 * @param {Object} params - 查询条件（可选）
 * @returns {Promise} 检测任务信息
 */
export function checkCollectionBooks(params) {
    return request({
        url: '/booklist-check/check-from-collection',
        method: 'post',
        data: params,
        timeout: 0
    })
}

