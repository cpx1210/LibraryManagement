<template>
  <div class="check-history-container">
    <!-- 页面标题 -->
    <el-card class="header-card">
      <h2>检测历史记录</h2>
      <p class="description">查看所有书单检测任务的历史记录</p>
    </el-card>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="任务名称">
          <el-input
            v-model="searchForm.taskName"
            placeholder="请输入任务名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="任务状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="taskList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="taskName" label="任务名称" min-width="200" show-overflow-tooltip />

        <el-table-column prop="submitTime" label="提交时间" width="180" />

        <el-table-column prop="totalBooks" label="总书目数" width="100" align="center" />

        <el-table-column label="检测结果" width="300" align="center">
          <template #default="{ row }">
            <div v-if="row.status === 'success'" class="result-tags">
              <el-tag type="danger" size="small">敏感词: {{ row.sensitiveHits }}</el-tag>
              <el-tag type="warning" size="small">问题书目: {{ row.problemBookHits }}</el-tag>
              <el-tag type="info" size="small">非白名单: {{ row.nonWhitelistPubs }}</el-tag>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="totalProblemBooks" label="问题总数" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.totalProblemBooks > 0" type="danger">
              {{ row.totalProblemBooks }}
            </el-tag>
            <span v-else>0</span>
          </template>
        </el-table-column>

        <el-table-column label="耗时" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.durationSeconds">{{ formatDuration(row.durationSeconds) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="250" align="center" fixed="right">
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
              @click="handleCancelTask(row.taskId)"
            >
              取消
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row.taskId)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getTaskList,
  startExportCheckResult,
  downloadExportCheckResult,
  cancelTask,
  deleteTask
} from '@/api/detection'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  taskName: '',
  status: ''
})

// 分页
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const taskList = ref([])
const loading = ref(false)
const exportStartingTaskIds = ref({})
const pendingExportDownloadTaskIds = new Set()

let pollingTimer = null

/**
 * 加载任务列表
 */
const loadTaskList = async () => {
  loading.value = true

  try {
    const res = await getTaskList({
      ...searchForm.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    })

    if (res.code === 200) {
      taskList.value = res.data.records || []
      pagination.value.total = res.data.total || 0
      for (const task of taskList.value) {
        await tryAutoDownload(task)
      }
      if (taskList.value.some(isTaskActive) || pendingExportDownloadTaskIds.size > 0) {
        startPolling()
      } else {
        stopPolling()
      }
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    console.error('加载任务列表失败：', error)
    ElMessage.error('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.value.pageNum = 1
  loadTaskList()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.value = {
    taskName: '',
    status: ''
  }
  handleSearch()
}

/**
 * 分页大小改变
 */
const handleSizeChange = () => {
  loadTaskList()
}

/**
 * 页码改变
 */
const handlePageChange = () => {
  loadTaskList()
}

/**
 * 查看结果
 */
const handleViewResult = (taskId) => {
  router.push(`/detection/result/${taskId}`)
}

/**
 * 导出结果
 */
const handleExport = async (taskId) => {
  const targetTask = taskList.value.find(task => task.taskId === taskId)
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

    if (res.data.exportStatus === 'failed') {
      ElMessage.error(res.data.exportErrorMessage || '创建导出任务失败')
      return
    }

    pendingExportDownloadTaskIds.add(taskId)
    await loadTaskList()

    if (res.data.exportFileReady) {
      await downloadExportFile(taskId, false)
      return
    }

    startPolling()
    ElMessage.success(res.data.exportStatus === 'pending' ? '已加入导出队列，完成后会自动下载' : '后台导出已开始，完成后会自动下载')
  } catch (error) {
    console.error('导出失败：', error)
    ElMessage.error(error.message || '创建导出任务失败，请重试')
  } finally {
    setExportStarting(taskId, false)
  }
}

/**
 * 取消任务
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
      loadTaskList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消任务失败：', error)
      ElMessage.error('取消任务失败，请重试')
    }
  }
}

/**
 * 删除任务
 */
const handleDelete = async (taskId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个检测任务吗？删除后将无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteTask(taskId)

    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadTaskList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败：', error)
      ElMessage.error('删除任务失败，请重试')
    }
  }
}

/**
 * 格式化耗时
 */
const formatDuration = (seconds) => {
  if (seconds < 60) {
    return `${seconds}秒`
  } else if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${minutes}分${secs}秒`
  } else {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    return `${hours}时${minutes}分`
  }
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

const isExportRunning = (task) => ['pending', 'processing'].includes(task?.exportStatus)

const isTaskActive = (task) => isExportRunning(task)

const getExportButtonText = (task) => {
  if (!task) return '导出'
  if (task.exportFileReady) return '下载文件'
  if (task.exportStatus === 'pending') return '导出排队中'
  if (task.exportStatus === 'processing') return `导出中 ${task.exportProgressPercent || 0}%`
  if (task.exportStatus === 'failed') return '重新导出'
  return '导出'
}

const isExportButtonLoading = (task) => {
  if (!task) return false
  return Boolean(exportStartingTaskIds.value[task.taskId]) || isExportRunning(task)
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
    ElMessage.success(`任务 ${task.taskName} 导出完成，文件已开始下载`)
  } catch (error) {
    console.error('自动下载导出文件失败：', error)
    ElMessage.error('导出文件已生成，但下载失败，请手动重试')
  }
}

const startPolling = () => {
  if (pollingTimer) {
    return
  }
  pollingTimer = setInterval(() => {
    loadTaskList()
  }, 2000)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

/**
 * 组件挂载
 */
onMounted(() => {
  loadTaskList()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.check-history-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.result-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
