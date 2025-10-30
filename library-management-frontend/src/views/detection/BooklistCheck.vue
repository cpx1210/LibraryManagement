<template>
  <div class="booklist-check-container">
    <!-- 页面标题 -->
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <h2>书单检测</h2>
          <p class="description">上传书单Excel文件，系统将自动检测敏感词、问题书目和白名单出版社</p>
        </div>
        <el-button type="primary" @click="handleDownloadTemplate">
          <el-icon><Download /></el-icon>
          下载检测模板
        </el-button>
      </div>
    </el-card>

    <!-- 上传区域 -->
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <span>上传书单</span>
        </div>
      </template>

      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :action="uploadAction"
        :auto-upload="false"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :file-list="fileList"
        :limit="1"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将Excel文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只支持 .xlsx 或 .xls 格式的 Excel 文件，文件大小不超过 50MB
          </div>
        </template>
      </el-upload>

      <div class="upload-actions">
        <el-button
          type="primary"
          size="large"
          :loading="uploading"
          :disabled="fileList.length === 0"
          @click="handleUpload"
        >
          <el-icon><Upload /></el-icon>
          开始检测
        </el-button>
        <el-button
          size="large"
          :disabled="fileList.length === 0"
          @click="handleClearFile"
        >
          清空文件
        </el-button>
      </div>
    </el-card>

    <!-- 检测进度 -->
    <el-card v-if="currentTask" class="progress-card">
      <template #header>
        <div class="card-header">
          <span>检测进度</span>
          <el-tag :type="getStatusType(currentTask.status)">
            {{ currentTask.statusText }}
          </el-tag>
        </div>
      </template>

      <div class="task-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentTask.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="总书目数">{{ currentTask.totalBooks }}</el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusType(currentTask.status)">
              {{ currentTask.statusText }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 进度条 -->
        <div v-if="currentTask.status === 'processing'" class="progress-bar">
          <el-progress
            :percentage="100"
            :indeterminate="true"
            :duration="3"
          />
          <p class="progress-text">正在检测中，请稍候...</p>
        </div>

        <!-- 检测结果 -->
        <div v-if="currentTask.status === 'success'" class="result-summary">
          <el-alert
            title="检测完成"
            type="success"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="result-stats">
                <div class="stat-item">
                  <span class="stat-label">命中敏感词：</span>
                  <span class="stat-value danger">{{ currentTask.sensitiveHits }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">命中问题书目：</span>
                  <span class="stat-value warning">{{ currentTask.problemBookHits }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">非白名单出版社：</span>
                  <span class="stat-value info">{{ currentTask.nonWhitelistPubs }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">总问题书目：</span>
                  <span class="stat-value primary">{{ currentTask.totalProblemBooks }}</span>
                </div>
              </div>
            </template>
          </el-alert>

          <div class="result-actions">
            <el-button type="primary" @click="handleViewResult(currentTask.taskId)">
              <el-icon><View /></el-icon>
              查看详细结果
            </el-button>
            <el-button type="success" @click="handleExport(currentTask.taskId)">
              <el-icon><Download /></el-icon>
              导出Excel报告
            </el-button>
          </div>
        </div>

        <!-- 错误信息 -->
        <div v-if="currentTask.status === 'failed'" class="error-info">
          <el-alert
            title="检测失败"
            type="error"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>{{ currentTask.errorMessage || '检测过程中发生错误，请重试' }}</p>
            </template>
          </el-alert>
        </div>
      </div>
    </el-card>

    <!-- 最近的检测任务 -->
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span>最近的检测任务</span>
          <el-button type="primary" link @click="handleViewAllHistory">
            查看全部历史 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>

      <el-table :data="recentTasks" stripe>
        <el-table-column prop="taskName" label="任务名称" min-width="200" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column prop="totalBooks" label="总书目数" width="100" align="center" />
        <el-table-column prop="totalProblemBooks" label="问题书目" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.totalProblemBooks > 0" type="danger">
              {{ row.totalProblemBooks }}
            </el-tag>
            <span v-else>0</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'success'"
              type="primary"
              link
              @click="handleViewResult(row.taskId)"
            >
              查看结果
            </el-button>
            <el-button
              v-if="row.status === 'success'"
              type="success"
              link
              @click="handleExport(row.taskId)"
            >
              导出
            </el-button>
            <el-button
              v-if="row.status === 'processing'"
              type="warning"
              link
              @click="handleCancelTask(row.taskId)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, UploadFilled, Upload, View, ArrowRight } from '@element-plus/icons-vue'
import {
  uploadBooklist,
  getTaskList,
  getTaskDetail,
  downloadTemplate,
  exportCheckResult,
  cancelTask
} from '@/api/detection'

const router = useRouter()

// 上传相关
const uploadRef = ref(null)
const fileList = ref([])
const uploading = ref(false)
const uploadAction = '' // 不使用action，手动上传

// 当前检测任务
const currentTask = ref(null)

// 最近的任务列表
const recentTasks = ref([])

// 轮询定时器
let pollingTimer = null

/**
 * 处理文件选择
 */
const handleFileChange = (file) => {
  fileList.value = [file]
}

/**
 * 处理文件移除
 */
const handleFileRemove = () => {
  fileList.value = []
}

/**
 * 清空文件
 */
const handleClearFile = () => {
  fileList.value = []
  uploadRef.value.clearFiles()
}

/**
 * 上传文件并开始检测
 */
const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择要上传的文件')
    return
  }

  const file = fileList.value[0].raw

  // 文件大小检查
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return
  }

  uploading.value = true

  try {
    const res = await uploadBooklist(file)

    if (res.code === 200) {
      ElMessage.success(res.data.message || '上传成功，正在检测中...')

      // 设置当前任务
      currentTask.value = {
        taskId: res.data.taskId,
        taskName: res.data.taskName,
        status: res.data.status,
        statusText: '处理中',
        totalBooks: res.data.totalBooks,
        submitTime: new Date().toLocaleString()
      }

      // 清空文件列表
      handleClearFile()

      // 开始轮询任务状态
      startPolling(res.data.taskId)

      // 刷新最近任务列表
      loadRecentTasks()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败：', error)
    ElMessage.error(error.message || '上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

/**
 * 开始轮询任务状态
 */
const startPolling = (taskId) => {
  // 清除现有定时器
  if (pollingTimer) {
    clearInterval(pollingTimer)
  }

  // 每3秒查询一次
  pollingTimer = setInterval(async () => {
    try {
      const res = await getTaskDetail(taskId)

      if (res.code === 200) {
        currentTask.value = res.data

        // 如果任务完成或失败，停止轮询
        if (res.data.status === 'success' || res.data.status === 'failed' || res.data.status === 'cancelled') {
          stopPolling()

          // 刷新最近任务列表
          loadRecentTasks()
        }
      }
    } catch (error) {
      console.error('查询任务状态失败：', error)
    }
  }, 3000)
}

/**
 * 停止轮询
 */
const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

/**
 * 加载最近的任务
 */
const loadRecentTasks = async () => {
  try {
    const res = await getTaskList({
      pageNum: 1,
      pageSize: 5
    })

    if (res.code === 200) {
      recentTasks.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载最近任务失败：', error)
  }
}

/**
 * 下载检测模板
 */
const handleDownloadTemplate = async () => {
  try {
    const blob = await downloadTemplate()

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '书单检测模板.xlsx'
    link.click()

    // 释放URL对象
    window.URL.revokeObjectURL(url)

    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败：', error)
    ElMessage.error('下载模板失败，请重试')
  }
}

/**
 * 查看检测结果
 */
const handleViewResult = (taskId) => {
  router.push(`/detection/result/${taskId}`)
}

/**
 * 导出检测结果
 */
const handleExport = async (taskId) => {
  try {
    const blob = await exportCheckResult(taskId)

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `检测结果_${taskId}.xlsx`
    link.click()

    // 释放URL对象
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败：', error)
    ElMessage.error('导出失败，请重试')
  }
}

/**
 * 取消检测任务
 */
const handleCancelTask = async (taskId) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个检测任务吗？',
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await cancelTask(taskId)

    if (res.code === 200) {
      ElMessage.success('任务已取消')

      // 如果是当前任务，停止轮询
      if (currentTask.value && currentTask.value.taskId === taskId) {
        stopPolling()
        currentTask.value.status = 'cancelled'
        currentTask.value.statusText = '已取消'
      }

      // 刷新任务列表
      loadRecentTasks()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消任务失败：', error)
      ElMessage.error('取消任务失败，请重试')
    }
  }
}

/**
 * 查看全部历史
 */
const handleViewAllHistory = () => {
  router.push('/detection/history')
}

/**
 * 获取状态类型
 */
const getStatusType = (status) => {
  const typeMap = {
    pending: 'info',
    processing: 'warning',
    success: 'success',
    failed: 'danger',
    cancelled: 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 组件挂载
 */
onMounted(() => {
  loadRecentTasks()
})

/**
 * 组件卸载
 */
onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.booklist-check-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.upload-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.upload-area {
  margin-bottom: 20px;
}

.upload-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.progress-card {
  margin-bottom: 20px;
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.progress-bar {
  margin-top: 20px;
}

.progress-text {
  text-align: center;
  color: #606266;
  margin-top: 10px;
}

.result-summary {
  margin-top: 20px;
}

.result-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 15px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: bold;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.info {
  color: #909399;
}

.stat-value.primary {
  color: #409eff;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.error-info {
  margin-top: 20px;
}

.history-card {
  margin-bottom: 20px;
}
</style>