<template>
  <div class="dashboard-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统首页</h2>
    </div>

    <!-- 欢迎卡片 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <div class="welcome-left">
          <div class="welcome-text">欢迎回来，{{ userStore.realName }}</div>
          <div class="welcome-time">{{ currentDate }}</div>
        </div>
        <div class="welcome-right">
          <el-button type="primary" @click="$router.push('/detection/check')">
            <el-icon><Search /></el-icon>
            书单检测
          </el-button>
          <el-button @click="$router.push('/sensitive-words')">
            敏感词库
          </el-button>
          <el-button @click="$router.push('/logs')">
            操作日志
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片区域 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="4">
        <el-card class="stat-card" shadow="never" @click="$router.push('/collection-books')">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409eff"><Reading /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.collectionBookCount) }}</div>
              <div class="stat-label">馆藏图书</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="never" @click="$router.push('/problem-books')">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#e6a23c"><WarningFilled /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.problemBookCount) }}</div>
              <div class="stat-label">问题书目</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="never" @click="$router.push('/sensitive-words')">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#f56c6c"><Lock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.sensitiveWordCount) }}</div>
              <div class="stat-label">敏感词</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="never" @click="$router.push('/publisher-whitelist')">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67c23a"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.publisherWhitelistCount) }}</div>
              <div class="stat-label">白名单出版社</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="never" @click="$router.push('/detection/history')">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#909399"><DataAnalysis /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.detectionTaskCount) }}</div>
              <div class="stat-label">检测任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="userStore.isAdmin" :span="4">
        <el-card class="stat-card" shadow="never" @click="$router.push('/users')">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409eff"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(stats.userCount) }}</div>
              <div class="stat-label">系统用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="charts-row">
      <el-col :span="10">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>敏感词分类分布</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>近7天检测趋势</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据列表区域 -->
    <el-row :gutter="16" class="lists-row">
      <el-col :span="12">
        <el-card class="list-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>最近检测任务</span>
              <el-button type="primary" link @click="$router.push('/detection/history')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="recentDetections" size="small" style="width: 100%">
            <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="totalBooks" label="图书数" width="80" align="center" />
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="命中" width="120" align="center">
              <template #default="{ row }">
                <span v-if="row.sensitiveHits > 0" class="hit-text danger">
                  敏感{{ row.sensitiveHits }}
                </span>
                <span v-if="row.problemBookHits > 0" class="hit-text warning">
                  问题{{ row.problemBookHits }}
                </span>
                <span v-if="row.sensitiveHits === 0 && row.problemBookHits === 0">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="时间" width="100">
              <template #default="{ row }">
                {{ formatDateTime(row.createdTime) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="recentDetections.length === 0" description="暂无检测任务" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="list-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>最近操作日志</span>
              <el-button type="primary" link @click="$router.push('/logs')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="recentLogs" size="small" style="width: 100%">
            <el-table-column prop="operatorName" label="操作人" width="80" />
            <el-table-column prop="operationTypeName" label="操作" width="60" align="center">
              <template #default="{ row }">
                <el-tag :type="getOperationTagType(row.operationType)" size="small">
                  {{ row.operationTypeName }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="moduleName" label="模块" min-width="100" />
            <el-table-column prop="operationTime" label="时间" width="100">
              <template #default="{ row }">
                {{ formatDateTime(row.operationTime) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="recentLogs.length === 0" description="暂无操作记录" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { 
  Search, Reading, WarningFilled, Lock, 
  CircleCheck, DataAnalysis, User
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { 
  getOverviewStats, 
  getSensitiveWordDistribution, 
  getDetectionTrend,
  getRecentDetections,
  getRecentLogs 
} from '@/api/statistics'

const userStore = useUserStore()

// 数据
const stats = ref({
  collectionBookCount: 0,
  problemBookCount: 0,
  sensitiveWordCount: 0,
  publisherWhitelistCount: 0,
  detectionTaskCount: 0,
  userCount: 0
})
const recentDetections = ref([])
const recentLogs = ref([])

// 图表引用
const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null

// 计算属性
const currentDate = computed(() => {
  const now = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  return now.toLocaleDateString('zh-CN', options)
})

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'pending': 'info',
    'processing': 'warning',
    'success': 'success',
    'failed': 'danger',
    'cancelled': 'info'
  }
  return map[status] || 'info'
}

// 获取操作类型 Tag
const getOperationTagType = (type) => {
  const map = {
    'create': 'success',
    'update': 'warning',
    'delete': 'danger',
    'import': 'primary',
    'export': 'info'
  }
  return map[type] || 'info'
}

// 初始化饼图
const initPieChart = (data) => {
  if (!pieChartRef.value) return
  
  pieChart = echarts.init(pieChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '10%',
      top: 'center'
    },
    color: ['#409eff', '#67c23a', '#e6a23c'],
    series: [
      {
        name: '敏感词分类',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  
  pieChart.setOption(option)
}

// 初始化折线图
const initLineChart = (data) => {
  if (!lineChartRef.value) return
  
  lineChart = echarts.init(lineChartRef.value)
  
  const dates = data.map(item => item.date)
  const counts = data.map(item => item.count)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '检测任务数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          color: '#409eff'
        },
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.1)'
        },
        data: counts
      }
    ]
  }
  
  lineChart.setOption(option)
}

// 加载数据
const loadData = async () => {
  try {
    // 加载概览数据
    const overviewRes = await getOverviewStats()
    if (overviewRes.code === 200) {
      stats.value = overviewRes.data
    }
    
    // 加载敏感词分布
    const distributionRes = await getSensitiveWordDistribution()
    if (distributionRes.code === 200) {
      initPieChart(distributionRes.data)
    }
    
    // 加载检测趋势
    const trendRes = await getDetectionTrend()
    if (trendRes.code === 200) {
      initLineChart(trendRes.data)
    }
    
    // 加载最近检测任务
    const detectionsRes = await getRecentDetections()
    if (detectionsRes.code === 200) {
      recentDetections.value = detectionsRes.data
    }
    
    // 加载最近操作日志
    const logsRes = await getRecentLogs()
    if (logsRes.code === 200) {
      recentLogs.value = logsRes.data
    }
  } catch (error) {
    console.error('加载 Dashboard 数据失败', error)
  }
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
}

/* 欢迎卡片 */
.welcome-card {
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.welcome-time {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.welcome-right {
  display: flex;
  gap: 10px;
}

/* 统计卡片区域 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: border-color 0.2s;
}

.stat-card:hover {
  border-color: #409eff;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 36px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 图表区域 */
.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.chart-container {
  height: 260px;
}

/* 列表区域 */
.lists-row {
  margin-bottom: 20px;
}

.list-card {
  height: 320px;
  display: flex;
  flex-direction: column;
}

.list-card :deep(.el-card__body) {
  padding: 0 20px 20px;
  flex: 1;
  overflow: auto;
}

.hit-text {
  font-size: 12px;
  margin-right: 4px;
}

.hit-text.danger {
  color: #f56c6c;
}

.hit-text.warning {
  color: #e6a23c;
}
</style>
