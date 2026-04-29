import request from '@/utils/request'

/**
 * 统计数据相关 API
 */

/**
 * 获取 Dashboard 概览数据
 */
export function getOverviewStats() {
    return request({
        url: '/statistics/overview',
        method: 'get',
        timeout: 0
    })
}

/**
 * 获取敏感词分类分布
 */
export function getSensitiveWordDistribution() {
    return request({
        url: '/statistics/sensitive-word-distribution',
        method: 'get',
        timeout: 0
    })
}

/**
 * 获取近7天检测任务趋势
 */
export function getDetectionTrend() {
    return request({
        url: '/statistics/detection-trend',
        method: 'get',
        timeout: 0
    })
}

/**
 * 获取最近检测任务
 */
export function getRecentDetections() {
    return request({
        url: '/statistics/recent-detections',
        method: 'get',
        timeout: 0
    })
}

/**
 * 获取最近操作日志
 */
export function getRecentLogs() {
    return request({
        url: '/statistics/recent-logs',
        method: 'get',
        timeout: 0
    })
}

