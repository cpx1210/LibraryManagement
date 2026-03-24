import request from '@/utils/request'

/**
 * 书单检测 API 接口
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

export function getTaskList(params) {
  return request({
    url: '/booklist-check/tasks',
    method: 'get',
    params
  })
}

export function getTaskDetail(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}`,
    method: 'get'
  })
}

export function getCheckDetails(taskId, riskLevel) {
  return request({
    url: `/booklist-check/tasks/${taskId}/details`,
    method: 'get',
    params: { riskLevel }
  })
}

export function getCheckDetailsPage(taskId, params) {
  return request({
    url: `/booklist-check/tasks/${taskId}/details/page`,
    method: 'get',
    params
  })
}

export function exportCheckResult(taskId) {
  return request({
    url: `/booklist-check/tasks/${taskId}/export`,
    method: 'get',
    responseType: 'blob'
  })
}

export function downloadTemplate() {
  return request({
    url: '/booklist-check/template',
    method: 'get',
    responseType: 'blob'
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
