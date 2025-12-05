<template>
  <div class="check-result-container">
    <!-- 任务信息 -->
    <el-card class="task-info-card">
      <template #header>
        <div class="card-header">
          <span>任务信息</span>
          <div class="header-actions">
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出Excel报告
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

    <!-- 统计概览 -->
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

    <!-- 检测结果列表 -->
    <el-card class="result-list-card">
      <template #header>
        <div class="card-header">
          <span>检测结果明细</span>
          <div class="filter-actions">
            <el-radio-group v-model="riskFilter" @change="handleFilterChange">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="high">高风险</el-radio-button>
              <el-radio-button label="medium">中风险</el-radio-button>
              <el-radio-button label="low">低风险</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="resultList"
        stripe
        :row-class-name="getRowClassName"
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />

        <el-table-column prop="bookName" label="书名" min-width="200" show-overflow-tooltip />

        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />

        <el-table-column prop="publisher" label="出版社" width="150" show-overflow-tooltip />

        <el-table-column prop="isbn" label="ISBN" width="140" />

        <el-table-column label="检测结果" width="120" align="center">
          <template #default="{ row }">
            <div class="result-tags">
              <el-tag v-if="row.hitSensitive" type="danger" size="small">敏感词</el-tag>
              <el-tag v-if="row.hitProblemBook" type="warning" size="small">问题书目</el-tag>
              <el-tag v-if="!row.isWhitelistPublisher" type="info" size="small">非白名单</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="riskLevel" label="风险等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRiskLevelType(row.riskLevel)">
              {{ row.riskLevelText }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sensitiveWords" label="敏感词详情" min-width="250">
          <template #default="{ row }">
            <div v-if="row.sensitiveHitDetails && row.sensitiveHitDetails.length > 0" class="sensitive-details">
              <el-tooltip
                v-for="(detail, index) in row.sensitiveHitDetails"
                :key="index"
                :content="formatSensitiveDetail(detail)"
                placement="top"
              >
                <el-tag type="danger" size="small" class="sensitive-tag">
                  {{ detail.fieldName }}：{{ detail.keyword }}
                </el-tag>
              </el-tooltip>
            </div>
            <span v-else-if="row.sensitiveWords" class="sensitive-words">
              {{ row.sensitiveWords }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="remark" label="备注" min-width="300" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Download,
  Back,
  Warning,
  WarnTriangleFilled,
  InfoFilled,
  Document
} from '@element-plus/icons-vue'
import {
  getTaskDetail,
  getCheckDetails,
  exportCheckResult
} from '@/api/detection'

const router = useRouter()
const route = useRoute()

// 任务ID
const taskId = ref(route.params.taskId)

// 任务信息
const taskInfo = ref(null)

// 风险等级筛选
const riskFilter = ref('')

// 检测结果列表
const resultList = ref([])
const loading = ref(false)

/**
 * 加载任务详情
 */
const loadTaskDetail = async () => {
  try {
    const res = await getTaskDetail(taskId.value)

    if (res.code === 200) {
      taskInfo.value = res.data
    } else {
      ElMessage.error(res.message || '加载任务详情失败')
    }
  } catch (error) {
    console.error('加载任务详情失败：', error)
    ElMessage.error('加载任务详情失败，请重试')
  }
}

/**
 * 加载检测结果明细
 */
const loadCheckDetails = async () => {
  loading.value = true

  try {
    const res = await getCheckDetails(taskId.value, riskFilter.value)

    if (res.code === 200) {
      resultList.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载检测结果失败')
    }
  } catch (error) {
    console.error('加载检测结果失败：', error)
    ElMessage.error('加载检测结果失败，请重试')
  } finally {
    loading.value = false
  }
}

/**
 * 筛选变化
 */
const handleFilterChange = () => {
  loadCheckDetails()
}

/**
 * 导出结果
 */
const handleExport = async () => {
  try {
    const blob = await exportCheckResult(taskId.value)

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `检测结果_${taskId.value}.xlsx`
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
 * 返回
 */
const handleBack = () => {
  router.back()
}

/**
 * 获取行类名（根据风险等级）
 */
const getRowClassName = ({ row }) => {
  if (row.riskLevel === 'high') {
    return 'high-risk-row'
  } else if (row.riskLevel === 'medium') {
    return 'medium-risk-row'
  }
  return ''
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
 * 获取风险等级类型
 */
const getRiskLevelType = (riskLevel) => {
  const typeMap = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return typeMap[riskLevel] || 'info'
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
 * 格式化敏感词详情（用于 tooltip 显示）
 */
const formatSensitiveDetail = (detail) => {
  if (!detail) return ''
  let text = `${detail.fieldName}匹配到关键词：${detail.keyword}`
  if (detail.alertMessage) {
    text += `（原因：${detail.alertMessage}）`
  }
  return text
}

/**
 * 组件挂载
 */
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

.stat-card {
  cursor: default;
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
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
}

.danger-card .stat-icon {
  color: #f56c6c;
}

.danger-card .stat-value {
  color: #f56c6c;
}

.warning-card .stat-icon {
  color: #e6a23c;
}

.warning-card .stat-value {
  color: #e6a23c;
}

.info-card .stat-icon {
  color: #909399;
}

.info-card .stat-value {
  color: #909399;
}

.primary-card .stat-icon {
  color: #409eff;
}

.primary-card .stat-value {
  color: #409eff;
}

.result-list-card {
  margin-bottom: 20px;
}

.filter-actions {
  display: flex;
  gap: 10px;
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
  cursor: pointer;
  margin: 2px;
}

/* 表格行背景色 */
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
