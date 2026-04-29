import request from '@/utils/request'

/**
 * 操作日志相关 API
 */

/**
 * 分页查询操作日志列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 当前页码（从1开始）
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.module - 操作模块（可选）
 * @param {string} params.operationType - 操作类型（可选）
 * @param {number} params.operatedBy - 操作人ID（可选）
 * @param {string} params.startTime - 开始时间（可选）
 * @param {string} params.endTime - 结束时间（可选）
 * @returns {Promise} 日志列表
 */
export function getLogList(params) {
    return request({
        url: '/logs',
        method: 'get',
        params,
        timeout: 0
    })
}

/**
 * 根据ID查询日志详情
 * @param {number} logId - 日志ID
 * @returns {Promise} 日志详情
 */
export function getLogById(logId) {
    return request({
        url: `/logs/${logId}`,
        method: 'get'
    })
}

/**
 * 查询指定记录的操作历史
 * @param {string} module - 模块名称
 * @param {number} targetId - 目标记录ID
 * @returns {Promise} 操作历史列表
 */
export function getLogHistory(module, targetId) {
    return request({
        url: '/logs/history',
        method: 'get',
        params: { module, targetId }
    })
}

/**
 * 获取所有模块列表
 * @returns {Promise} 模块列表
 */
export function getModules() {
    return request({
        url: '/logs/modules',
        method: 'get'
    })
}

/**
 * 获取所有操作类型列表
 * @returns {Promise} 操作类型列表
 */
export function getOperationTypes() {
    return request({
        url: '/logs/operation-types',
        method: 'get'
    })
}

/**
 * 导出操作日志
 * @param {Object} params - 查询参数（同查询接口）
 * @returns {Promise} 文件流
 */
export function exportLogs(params) {
    return request({
        url: '/logs/export',
        method: 'get',
        params,
        responseType: 'blob',
        timeout: 0
    })
}

