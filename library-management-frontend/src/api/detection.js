import request from '@/utils/request'

/**
 * 书单检测 API 接口
 */

/**
 * 上传书单文件
 * @param {File} file - Excel 文件
 * @returns {Promise}
 */
export function uploadBooklist(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/booklist-check/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 分页查询检测任务列表
 * @param {Object} params - 查询参数
 * @param {string} params.taskName - 任务名称（模糊查询）
 * @param {string} params.status - 任务状态
 * @param {number} params.submittedBy - 提交人ID
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页大小
 * @returns {Promise}
 */
export function getTaskList(params) {
  return request({
    url: '/booklist-check/tasks',
    method: 'get',
    params
  })
}

/**
 * 查询检测任务详情
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function getTaskDetail(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}`,
    method: 'get'
  })
}

/**
 * 查询检测结果明细列表
 * @param {number} taskId - 任务ID
 * @param {string} riskLevel - 风险等级（可选）
 * @returns {Promise}
 */
export function getCheckDetails(taskId, riskLevel) {
  return request({
    url: `/booklist-check/tasks/${taskId}/details`,
    method: 'get',
    params: { riskLevel }
  })
}

/**
 * 导出检测结果
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function exportCheckResult(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}/export`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 下载检测模板
 * @returns {Promise}
 */
export function downloadTemplate() {
  return request({
    url: '/booklist-check/template',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 取消检测任务
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function cancelTask(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}/cancel`,
    method: 'post'
  })
}

/**
 * 删除检测任务
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function deleteTask(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}/delete`,
    method: 'post'
  })
}