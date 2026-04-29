<template>
  <div class="booklist-check-container">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <h2>书单检测</h2>
          <p class="description">支持上传 Excel 书单检测，也支持从馆藏发起分批检测任务并实时查看执行进度。</p>
        </div>
        <el-button type="primary" @click="handleDownloadTemplate">
          <el-icon><Download /></el-icon>
          下载检测模板
        </el-button>
      </div>
    </el-card>

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
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将 Excel 文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">仅支持 `.xlsx/.xls` 文件，大小不超过 50MB</div>
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
        <el-button size="large" :disabled="fileList.length === 0" @click="handleClearFile">
          清空文件
        </el-button>
      </div>
    </el-card>

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
          <el-descriptions-item label="任务类型">{{ currentTask.taskType || '书单检测' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentTask.submitTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总书目数">{{ formatNumber(currentTask.totalBooks) }}</el-descriptions-item>
          <el-descriptions-item label="已处理">{{ formatNumber(currentTask.processedBooks) }}</el-descriptions-item>
          <el-descriptions-item label="当前批次">
            {{ formatBatch(currentTask.currentBatch, currentTask.totalBatches) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="progress-panel">
          <div class="progress-meta">
            <span>{{ getProgressText(currentTask) }}</span>
            <span>{{ currentTask.progressPercent || 0 }}%</span>
          </div>
          <el-progress
            :percentage="currentTask.progressPercent || 0"
            :status="getProgressStatus(currentTask.status)"
            :stroke-width="18"
          />
          <p v-if="currentTask.status === 'processing'" class="progress-tip">
            系统正在按批次执行检测，页面每 2 秒自动刷新一次进度。
          </p>
          <p v-if="currentTask.status === 'pending'" class="progress-tip">
            任务已创建，正在后台排队，页面会自动刷新任务状态。
          </p>
        </div>

        <div v-if="currentTask.status === 'processing'" class="running-stats">
          <div class="mini-stat">
            <span class="mini-label">敏感词命中</span>
            <span class="mini-value danger">{{ formatNumber(currentTask.sensitiveHits) }}</span>
          </div>
          <div class="mini-stat">
            <span class="mini-label">问题书目命中</span>
            <span class="mini-value warning">{{ formatNumber(currentTask.problemBookHits) }}</span>
          </div>
          <div class="mini-stat">
            <span class="mini-label">非白名单出版社</span>
            <span class="mini-value info">{{ formatNumber(currentTask.nonWhitelistPubs) }}</span>
          </div>
          <div class="mini-stat">
            <span class="mini-label">问题书目总数</span>
            <span class="mini-value primary">{{ formatNumber(currentTask.totalProblemBooks) }}</span>
          </div>
        </div>

        <div v-if="currentTask.status === 'success'" class="result-summary">
          <el-alert title="检测完成" type="success" :closable="false" show-icon>
            <template #default>
              <div class="result-stats">
                <div class="stat-item">
                  <span class="stat-label">敏感词命中</span>
                  <span class="stat-value danger">{{ formatNumber(currentTask.sensitiveHits) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">问题书目命中</span>
                  <span class="stat-value warning">{{ formatNumber(currentTask.problemBookHits) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">非白名单出版社</span>
                  <span class="stat-value info">{{ formatNumber(currentTask.nonWhitelistPubs) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">问题书目总数</span>
                  <span class="stat-value primary">{{ formatNumber(currentTask.totalProblemBooks) }}</span>
                </div>
              </div>
            </template>
          </el-alert>

          <div class="result-actions">
            <el-button type="primary" @click="handleViewResult(currentTask.taskId)">
              <el-icon><View /></el-icon>
              查看详细结果
            </el-button>
            <el-button
              type="success"
              :loading="isExportButtonLoading(currentTask)"
              @click="handleExport(currentTask.taskId)"
            >
              <el-icon><Download /></el-icon>
              {{ getExportButtonText(currentTask) }}
            </el-button>
          </div>
          <p
            v-if="currentTask.exportStatus === 'pending' || currentTask.exportStatus === 'processing'"
            class="progress-tip"
          >
            导出文件正在后台生成，完成后会自动下载。
          </p>
          <el-alert
            v-if="currentTask.exportStatus === 'failed'"
            title="导出失败"
            type="error"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>{{ currentTask.exportErrorMessage || '导出过程中发生错误，请重新尝试。' }}</p>
            </template>
          </el-alert>
        </div>

        <div v-if="currentTask.status === 'failed'" class="error-info">
          <el-alert title="检测失败" type="error" :closable="false" show-icon>
            <template #default>
              <p>{{ currentTask.errorMessage || '检测过程中发生错误，请稍后重试。' }}</p>
            </template>
          </el-alert>
        </div>
      </div>
    </el-card>

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
        <el-table-column prop="taskName" label="任务名称" min-width="220" />
        <el-table-column prop="submitterName" label="上传人" width="120" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="进度" min-width="220">
          <template #default="{ row }">
            <div class="table-progress">
              <el-progress
                :percentage="row.progressPercent || 0"
                :status="getProgressStatus(row.status)"
                :stroke-width="14"
              />
              <span class="table-progress-text">{{ getProgressText(row) }}</span>
            </div>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="220" align="center">
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
              :disabled="isExportButtonLoading(row)"
              @click="handleExport(row.taskId)"
            >
              {{ getExportButtonText(row) }}
            </el-button>
            <el-button
              v-if="row.status === 'processing'"
              type="warning"
              link
              @click="handleFocusTask(row.taskId)"
            >
              查看进度
            </el-button>
            <el-button
              v-if="row.status === 'processing'"
              type="danger"
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, UploadFilled, Upload, View, ArrowRight } from '@element-plus/icons-vue'
import {
  uploadBooklist,
  getTaskList,
  getTaskDetail,
  downloadTemplate,
  startExportCheckResult,
  downloadExportCheckResult,
  cancelTask
} from '@/api/detection'

const route = useRoute()
const router = useRouter()

const uploadRef = ref(null)
const fileList = ref([])
const uploading = ref(false)
const uploadAction = ''

const currentTask = ref(null)
const recentTasks = ref([])
const exportStartingTaskIds = ref({})
const pendingExportDownloadTaskIds = new Set()

let pollingTimer = null

const handleFileChange = (file) => {
  fileList.value = [file]
}

const handleFileRemove = () => {
  fileList.value = []
}

const handleClearFile = () => {
  fileList.value = []
  uploadRef.value?.clearFiles()
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择要上传的文件')
    return
  }

  const file = fileList.value[0].raw
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return
  }

  uploading.value = true
  try {
    const res = await uploadBooklist(file)
    if (res.code !== 200) {
      ElMessage.error(res.message || '上传失败')
      return
    }

    ElMessage.success(res.data.message || '上传成功，正在检测中...')
    currentTask.value = {
      taskId: res.data.taskId,
      taskName: res.data.taskName,
      taskType: '上传检测',
      status: res.data.status,
      statusText: '处理中',
      totalBooks: res.data.totalBooks,
      processedBooks: res.data.processedBooks || 0,
      currentBatch: res.data.currentBatch || 0,
      totalBatches: res.data.totalBatches || 1,
      progressPercent: 0,
      submitTime: new Date().toLocaleString()
    }
    handleClearFile()
    await loadRecentTasks()
    startPolling(res.data.taskId)
  } catch (error) {
    console.error('上传失败：', error)
    ElMessage.error(error.message || '上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

const loadTask = async (taskId, shouldPoll = true) => {
  const res = await getTaskDetail(taskId)
  if (res.code !== 200) {
    ElMessage.error(res.message || '获取任务详情失败')
    return
  }

  currentTask.value = res.data
  await tryAutoDownload(res.data)
  if (shouldPoll && isTaskActive(res.data)) {
    startPolling(taskId)
  } else if (!isTaskActive(res.data) && !recentTasks.value.some(isTaskActive)) {
    stopPolling()
  }
}

const startPolling = (taskId) => {
  stopPolling()
  pollingTimer = setInterval(async () => {
    try {
      await loadTask(taskId, false)
      await loadRecentTasks()
      if ((!currentTask.value || !isTaskActive(currentTask.value)) && !recentTasks.value.some(isTaskActive)) {
        stopPolling()
      }
    } catch (error) {
      console.error('查询任务状态失败：', error)
    }
  }, 2000)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

const loadRecentTasks = async () => {
  try {
    const res = await getTaskList({ pageNum: 1, pageSize: 5 })
    if (res.code === 200) {
      recentTasks.value = res.data.records || []
      for (const task of recentTasks.value) {
        await tryAutoDownload(task)
      }
    }
  } catch (error) {
    console.error('加载最近任务失败：', error)
  }
}

const handleDownloadTemplate = async () => {
  try {
    const blob = await downloadTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '书单检测模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败：', error)
    ElMessage.error('下载模板失败，请重试')
  }
}

const handleViewResult = (taskId) => {
  router.push(`/detection/result/${taskId}`)
}

const handleFocusTask = async (taskId) => {
  await loadTask(taskId, true)
  router.replace({
    path: '/detection/check',
    query: { taskId }
  })
}

const handleExport = async (taskId) => {
  const targetTask = currentTask.value?.taskId === taskId
    ? currentTask.value
    : recentTasks.value.find(task => task.taskId === taskId)

  if (targetTask?.exportFileReady) {
    await downloadExportFile(taskId, false)
    return
  }

  setExportStarting(taskId, true)
  try {
    const res = await startExportCheckResult(taskId)
    if (res.code !== 200) {
      ElMessage.error(res.message || '创建导出任务失败')
      return
    }

    currentTask.value = res.data
    if (res.data.exportStatus === 'failed') {
      ElMessage.error(res.data.exportErrorMessage || '创建导出任务失败')
      return
    }

    pendingExportDownloadTaskIds.add(taskId)
    await loadRecentTasks()
    await loadTask(taskId, true)

    if (res.data.exportFileReady) {
      await downloadExportFile(taskId, false)
      return
    }

    ElMessage.success(res.data.exportStatus === 'pending' ? '已加入导出队列，完成后会自动下载' : '后台导出已开始，完成后会自动下载')
  } catch (error) {
    console.error('导出失败：', error)
    ElMessage.error(error.message || '创建导出任务失败，请重试')
  } finally {
    setExportStarting(taskId, false)
  }
}

const handleCancelTask = async (taskId) => {
  try {
    await ElMessageBox.confirm('确定要取消这个检测任务吗？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await cancelTask(taskId)
    if (res.code !== 200) {
      ElMessage.error(res.message || '取消任务失败')
      return
    }

    ElMessage.success('任务已取消')
    if (currentTask.value?.taskId === taskId) {
      await loadTask(taskId, false).catch(() => {
        currentTask.value.status = 'cancelled'
        currentTask.value.statusText = '已取消'
      })
    }
    await loadRecentTasks()
    stopPolling()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消任务失败：', error)
      ElMessage.error('取消任务失败，请重试')
    }
  }
}

const handleViewAllHistory = () => {
  router.push('/detection/history')
}

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

const getProgressStatus = (status) => {
  if (status === 'success') return 'success'
  if (status === 'failed') return 'exception'
  return undefined
}

const getProgressText = (task) => {
  const processed = formatNumber(task?.processedBooks)
  const total = formatNumber(task?.totalBooks)
  const batch = formatBatch(task?.currentBatch, task?.totalBatches)
  if (!task) {
    return '-'
  }
  if (task.status === 'success') {
    return `已完成 ${processed}/${total}`
  }
  if (task.status === 'failed') {
    return `失败于 ${processed}/${total}`
  }
  if (task.status === 'cancelled') {
    return `已取消，停在 ${batch}`
  }
  if (task.status === 'processing') {
    return `已处理 ${processed}/${total}，当前 ${batch}`
  }
  return `等待执行，计划 ${batch}`
}

const isDetectionRunning = (status) => ['pending', 'processing'].includes(status)

const isExportRunning = (task) => ['pending', 'processing'].includes(task?.exportStatus)

const isTaskActive = (task) => isDetectionRunning(task?.status) || isExportRunning(task)

const isExportButtonLoading = (task) => {
  if (!task) return false
  return Boolean(exportStartingTaskIds.value[task.taskId]) || isExportRunning(task)
}

const getExportButtonText = (task) => {
  if (!task) return '导出 Excel 报告'
  if (task.exportFileReady) return '下载导出文件'
  if (task.exportStatus === 'pending') return '导出排队中'
  if (task.exportStatus === 'processing') return `导出中 ${task.exportProgressPercent || 0}%`
  if (task.exportStatus === 'failed') return '重新导出'
  return '导出 Excel 报告'
}

const setExportStarting = (taskId, loading) => {
  const nextState = { ...exportStartingTaskIds.value }
  if (loading) {
    nextState[taskId] = true
  } else {
    delete nextState[taskId]
  }
  exportStartingTaskIds.value = nextState
}

const downloadExportFile = async (taskId, silent = true) => {
  const blob = await downloadExportCheckResult(taskId)
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `检测结果_${taskId}.xlsx`
  link.click()
  window.URL.revokeObjectURL(url)
  pendingExportDownloadTaskIds.delete(taskId)
  if (!silent) {
    ElMessage.success('导出文件下载成功')
  }
}

const tryAutoDownload = async (task) => {
  if (!task || !pendingExportDownloadTaskIds.has(task.taskId) || !task.exportFileReady) {
    return
  }

  try {
    await downloadExportFile(task.taskId, true)
    ElMessage.success('导出完成，文件已开始下载')
  } catch (error) {
    console.error('自动下载导出文件失败：', error)
    ElMessage.error('导出文件已生成，但下载失败，请手动重试')
  }
}

const formatBatch = (currentBatch, totalBatches) => {
  const current = currentBatch || 0
  const total = totalBatches || 0
  return `${current}/${total}`
}

const formatNumber = (value) => {
  return Number(value || 0).toLocaleString('zh-CN')
}

onMounted(async () => {
  await loadRecentTasks()
  const taskId = Number(route.query.taskId)
  if (taskId) {
    await loadTask(taskId, true)
    return
  }

  const runningTask = recentTasks.value.find(task => isTaskActive(task))
  if (runningTask) {
    await loadTask(runningTask.taskId, true)
  }
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.booklist-check-container {
  padding: 20px;
}

.header-card,
.upload-card,
.progress-card,
.history-card {
  margin-bottom: 20px;
}

.header-content,
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
}

.description {
  margin: 0;
  color: #909399;
}

.upload-area {
  margin-bottom: 20px;
}

.upload-actions,
.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.progress-panel {
  padding: 18px;
  background: #f8fafc;
  border-radius: 12px;
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.progress-tip {
  margin: 10px 0 0;
  color: #909399;
  font-size: 13px;
}

.running-stats,
.result-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.mini-stat,
.stat-item {
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #ebeef5;
  text-align: center;
}

.mini-label,
.stat-label {
  display: block;
  color: #606266;
  font-size: 13px;
  margin-bottom: 8px;
}

.mini-value,
.stat-value {
  display: block;
  font-size: 26px;
  font-weight: 700;
}

.danger {
  color: #f56c6c;
}

.warning {
  color: #e6a23c;
}

.info {
  color: #909399;
}

.primary {
  color: #409eff;
}

.table-progress {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.table-progress-text {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 900px) {
  .header-content,
  .card-header,
  .upload-actions,
  .result-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .running-stats,
  .result-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
