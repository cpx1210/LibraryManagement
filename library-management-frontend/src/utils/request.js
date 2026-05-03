import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 创建 Axios 实例
 * - baseURL: API 基础路径（通过 Vite 代理转发到后端）
 * - timeout: 请求超时时间（默认60秒，文件上传等长时间操作可单独设置）
 */
const service = axios.create({
  baseURL: '/api',  // 所有请求都会加上 /api 前缀，由 Vite 代理转发
  timeout: 60000    // 请求超时时间 60 秒（增加到60秒以支持较大文件上传）
})

/**
 * 请求拦截器
 * 在发送请求前执行，用于添加统一的请求头
 */
service.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')

    // 如果 token 存在，添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 打印请求信息（开发环境）
    if (import.meta.env.DEV) {
      console.log('📤 请求:', config.method.toUpperCase(), config.url, config.data || config.params)
    }

    return config
  },
  error => {
    // 请求错误处理
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 在接收响应后执行，用于统一处理响应数据和错误
 */
service.interceptors.response.use(
  response => {
    // 打印响应信息（开发环境）
    if (import.meta.env.DEV) {
      console.log('📥 响应:', response.config.url, response.data)
    }

    // 特殊处理：如果是文件下载（responseType 为 blob），直接返回数据
    if (response.config.responseType === 'blob') {
      return response.data
    }

    const res = response.data

    /**
     * 后端返回的数据格式：
     * {
     *   code: 200,        // 状态码
     *   message: "操作成功",
     *   data: { ... }     // 实际数据
     * }
     */

    // 处理业务层面的未授权（token 过期或无效）
    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      // 清除 token 和用户信息
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      // 延迟跳转，确保消息能够显示
      setTimeout(() => {
        window.location.href = '/login'
      }, 1000)
      return Promise.reject(new Error('登录已过期'))
    }

    // 处理业务层面的权限不足（角色不具备访问权限）
    if (res.code === 403) {
      ElMessage.error(res.message || '权限不足，无法访问该资源')
      return Promise.reject(new Error(res.message || '权限不足'))
    }

    // 如果响应成功（code === 200）
    if (res.code === 200) {
      return res  // 返回完整的响应数据
    }

    // 如果响应失败（code !== 200）
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    // HTTP 错误处理
    console.error('响应错误:', error)

    // 处理不同的 HTTP 状态码
    if (error.response) {
      const status = error.response.status

      switch (status) {
        case 401:
          // 401 未授权（token 失效）
          ElMessage.error('登录已过期，请重新登录')
          // 清除 token 和用户信息
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          // 跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          // 403 拒绝访问（角色权限不足）
          ElMessage.error(error.response.data?.message || '权限不足，无法访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 413:
          // 413 请求实体过大（文件上传超过限制）
          ElMessage.error('上传文件过大，请确保文件不超过 50MB')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        case 503:
          ElMessage.error('服务不可用')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      // 其他错误
      ElMessage.error(error.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

/**
 * 导出封装好的 Axios 实例
 * 使用方式：
 * import request from '@/utils/request'
 * request.get('/user/info')
 * request.post('/auth/login', { username, password })
 */
export default service
