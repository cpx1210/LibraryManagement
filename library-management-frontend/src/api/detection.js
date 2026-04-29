import request from '@/utils/request'

/**
 * 书单检测 API 接口
 */

export function uploadBooklist(file, extraData = {}) {
  const formData = new FormData()
  formData.append('file', file)
  Object.entries(extraData).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      formData.append(key, value)
    }
  })

  return request({
    url: '/booklist-check/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 0
  })
}

export function getTaskList(params) {
  return request({
    url: '/booklist-check/tasks',
    method: 'get',
    params,
    timeout: 0
  })
}

export function getTaskDetail(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}`,
    method: 'get',
    timeout: 0
  })
}

export function getCheckDetails(taskId, riskLevel) {
  return request({
    url: `/booklist-check/tasks/${taskId}/details`,
    method: 'get',
    params: { riskLevel },
    timeout: 0
  })
}

export function getCheckDetailsPage(taskId, params) {
  return request({
    url: `/booklist-check/tasks/${taskId}/details/page`,
    method: 'get',
    params,
    timeout: 0
  })
}

export function startExportCheckResult(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}/export`,
    method: 'post',
    timeout: 0
  })
}

export function downloadExportCheckResult(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}/export`,
    method: 'get',
    responseType: 'blob',
    timeout: 0
  })
}

export function downloadTemplate() {
  return request({
    url: '/booklist-check/template',
    method: 'get',
    responseType: 'blob',
    timeout: 0
  })
}

export function cancelTask(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}/cancel`,
    method: 'post'
  })
}

export function deleteTask(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}/delete`,
    method: 'post'
  })
}
