<template>
  <div class="check-result-container">
    <el-card class="task-info-card">
      <template #header>
        <div class="card-header">
          <span>任务信息</span>
          <div class="header-actions">
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出 Excel 报告
            </el-button>
            <el-button @click="handleBack">
              <el-icon><Back /></el-icon>
              返回
            </el-button>
          </div>
        </div>
      </template>

      <el-descriptions v-if="taskInfo" :column="3" border>
        <el-descriptions-item label="任务名称">{{ taskInfo.taskName }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ taskInfo.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="getStatusType(taskInfo.status)">
            {{ taskInfo.statusText }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总书目数">{{ taskInfo.totalBooks }}</el-descriptions-item>
        <el-descriptions-item label="命中敏感词">
          <span class="danger-text">{{ taskInfo.sensitiveHits }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="命中问题书目">
          <span class="warning-text">{{ taskInfo.problemBookHits }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="非白名单出版社">
          <span class="info-text">{{ taskInfo.nonWhitelistPubs }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="总问题书目">
          <span class="primary-text">{{ taskInfo.totalProblemBooks }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="检测耗时">
          <span v-if="taskInfo.durationSeconds">{{ formatDuration(taskInfo.durationSeconds) }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card danger-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">命中敏感词</div>
              <div class="stat-value">{{ taskInfo?.sensitiveHits || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card warning-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><WarnTriangleFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">命中问题书目</div>
              <div class="stat-value">{{ taskInfo?.problemBookHits || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card info-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><InfoFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">非白名单出版社</div>
              <div class="stat-value">{{ taskInfo?.nonWhitelistPubs || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card primary-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总问题书目</div>
              <div class="stat-value">{{ taskInfo?.totalProblemBooks || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="result-list-card">
      <template #header>
        <div class="card-header result-header">
          <span>检测结果明细</span>
          <div class="filter-actions">
            <el-checkbox v-model="onlyIssue" @change="handleFilterChange">
              只看问题项
            </el-checkbox>
            <el-radio-group v-model="riskFilter" @change="handleFilterChange">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="high">高风险</el-radio-button>
              <el-radio-button label="medium">中风险</el-radio-button>
              <el-radio-button label="low">低风险</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <div class="table-tip">
        当前结果采用服务端分页加载，避免一次性返回超大数据导致页面卡顿。
      </div>

      <el-table
        v-loading="loading"
        :data="resultList"
        stripe
        :row-class-name="getRowClassName"
        style="width: 100%"
      >
        <el-table-column label="序号" width="80" align="center">
          <template #default="{ $index }">
            {{ (pagination.pageNum - 1) * pagination.pageSize + $index + 1 }}
          </template>
        </el-table-column>

        <el-table-column prop="bookName" label="书名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="140" show-overflow-tooltip />
        <el-table-column prop="publisher" label="出版社" width="180" show-overflow-tooltip />
        <el-table-column prop="isbn" label="ISBN" width="160" />

        <el-table-column label="检测结果" width="180" align="center">
          <template #default="{ row }">
            <div class="result-tags">
              <el-tag v-if="row.hitSensitive" type="danger" size="small">敏感词</el-tag>
              <el-tag v-if="row.hitProblemBook" type="warning" size="small">问题书目</el-tag>
              <el-tag v-if="!row.isWhitelistPublisher" type="info" size="small">非白名单</el-tag>
              <el-tag
                v-if="row.isWhitelistPublisher && !row.hitSensitive && !row.hitProblemBook"
                type="success"
                size="small"
              >
                正常
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="riskLevel" label="风险等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRiskLevelType(row.riskLevel)">
              {{ row.riskLevelText || '-' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sensitiveWords" label="敏感词详情" min-width="260">
          <template #default="{ row }">
            <div v-if="row.sensitiveHitDetails && row.sensitiveHitDetails.length > 0" class="sensitive-details">
              <el-tooltip
                v-for="(detail, index) in row.sensitiveHitDetails"
                :key="index"
                :content="formatSensitiveDetail(detail)"
                placement="top"
              >
                <el-tag type="danger" size="small" class="sensitive-tag">
                  {{ detail.fieldName }}: {{ detail.keyword }}
                </el-tag>
              </el-tooltip>
            </div>
            <span v-else-if="row.sensitiveWords" class="sensitive-words">
              {{ row.sensitiveWords }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="remark" label="备注" min-width="320" show-overflow-tooltip />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[20, 50, 100, 200]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Back,
  Document,
  Download,
  InfoFilled,
  Warning,
  WarnTriangleFilled
} from '@element-plus/icons-vue'
import {
  exportCheckResult,
  getCheckDetailsPage,
  getTaskDetail
} from '@/api/detection'

const router = useRouter()
const route = useRoute()

const taskId = ref(route.params.taskId)
const taskInfo = ref(null)
const resultList = ref([])
const loading = ref(false)
const riskFilter = ref('')
const onlyIssue = ref(true)

const pagination = ref({
  pageNum: 1,
  pageSize: 50,
  total: 0
})

const loadTaskDetail = async () => {
  try {
    const res = await getTaskDetail(taskId.value)
    if (res.code === 200) {
      taskInfo.value = res.data
    } else {
      ElMessage.error(res.message || '加载任务详情失败')
    }
  } catch (error) {
    console.error('加载任务详情失败:', error)
    ElMessage.error('加载任务详情失败，请重试')
  }
}

const loadCheckDetails = async () => {
  loading.value = true

  try {
    const res = await getCheckDetailsPage(taskId.value, {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      riskLevel: riskFilter.value || undefined,
      hasIssue: onlyIssue.value
    })

    if (res.code === 200) {
      resultList.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载检测结果失败')
    }
  } catch (error) {
    console.error('加载检测结果失败:', error)
    ElMessage.error('加载检测结果失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.value.pageNum = 1
  loadCheckDetails()
}

const handleSizeChange = () => {
  pagination.value.pageNum = 1
  loadCheckDetails()
}

const handlePageChange = () => {
  loadCheckDetails()
}

const handleExport = async () => {
  try {
    const blob = await exportCheckResult(taskId.value)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `检测结果_${taskId.value}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  }
}

const handleBack = () => {
  router.back()
}

const getRowClassName = ({ row }) => {
  if (row.riskLevel === 'high') {
    return 'high-risk-row'
  }
  if (row.riskLevel === 'medium') {
    return 'medium-risk-row'
  }
  return ''
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

const getRiskLevelType = (riskLevel) => {
  const typeMap = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return typeMap[riskLevel] || 'info'
}

const formatDuration = (seconds) => {
  if (seconds < 60) {
    return `${seconds} 秒`
  }
  if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${minutes} 分 ${secs} 秒`
  }
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return `${hours} 小时 ${minutes} 分`
}

const formatSensitiveDetail = (detail) => {
  if (!detail) {
    return ''
  }

  let text = `${detail.fieldName} 匹配到关键词: ${detail.keyword}`
  if (detail.alertMessage) {
    text += `（原因: ${detail.alertMessage}）`
  }
  return text
}

onMounted(() => {
  loadTaskDetail()
  loadCheckDetails()
})
</script>

<style scoped>
.check-result-container {
  padding: 20px;
}

.task-info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.danger-text {
  color: #f56c6c;
  font-weight: bold;
}

.warning-text {
  color: #e6a23c;
  font-weight: bold;
}

.info-text {
  color: #909399;
  font-weight: bold;
}

.primary-text {
  color: #409eff;
  font-weight: bold;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.8;
}

.stat-info {
  flex: 1;
}

.stat-label {
  margin-bottom: 8px;
  color: #909399;
  font-size: 14px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
}

.danger-card .stat-icon,
.danger-card .stat-value {
  color: #f56c6c;
}

.warning-card .stat-icon,
.warning-card .stat-value {
  color: #e6a23c;
}

.info-card .stat-icon,
.info-card .stat-value {
  color: #909399;
}

.primary-card .stat-icon,
.primary-card .stat-value {
  color: #409eff;
}

.result-list-card {
  margin-bottom: 20px;
}

.result-header {
  gap: 16px;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.table-tip {
  margin-bottom: 16px;
  color: #909399;
  font-size: 13px;
}

.result-tags {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: wrap;
}

.sensitive-words {
  color: #f56c6c;
  font-weight: 500;
}

.sensitive-details {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.sensitive-tag {
  margin: 2px;
  cursor: pointer;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

:deep(.high-risk-row),
:deep(.high-risk-row .el-table__cell) {
  background-color: #fef0f0 !important;
}

:deep(.medium-risk-row),
:deep(.medium-risk-row .el-table__cell) {
  background-color: #fdf6ec !important;
}

:deep(.high-risk-row:hover > .el-table__cell) {
  background-color: #fde2e2 !important;
}

:deep(.medium-risk-row:hover > .el-table__cell) {
  background-color: #faecd8 !important;
}
</style>
