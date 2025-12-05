<template>
  <div class="operation-log-container">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="全部模块" clearable style="width: 150px">
            <el-option 
              v-for="item in moduleList" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="全部类型" clearable style="width: 120px">
            <el-option 
              v-for="item in operationTypeList" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="[new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59)]"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
          <el-button type="success" @click="handleExport" :icon="Download" :loading="exportLoading">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="logList" 
        border 
        stripe
        style="width: 100%"
      >
        <el-table-column prop="logId" label="日志ID" width="80" align="center" />
        <el-table-column prop="moduleName" label="操作模块" width="120" align="center">
          <template #default="{ row }">
            <el-tag>{{ row.moduleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationTypeName" label="操作类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTagType(row.operationType)">
              {{ row.operationTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="100" align="center" />
        <el-table-column prop="operatorName" label="操作人" width="100" align="center" />
        <el-table-column prop="operationTime" label="操作时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.operationTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="130" align="center" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="日志详情" 
      width="700px"
      destroy-on-close
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ currentLog.logId }}</el-descriptions-item>
        <el-descriptions-item label="目标ID">{{ currentLog.targetId }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.moduleName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTagType(currentLog.operationType)">
            {{ currentLog.operationTypeName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">
          {{ formatDateTime(currentLog.operationTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 变更内容 -->
      <div v-if="currentLog.oldValue || currentLog.newValue" class="change-content">
        <el-divider content-position="left">变更内容</el-divider>
        
        <!-- 美化的 JSON 展示 -->
        <el-row :gutter="20">
          <!-- 变更前 -->
          <el-col :span="currentLog.oldValue && currentLog.newValue ? 12 : 24" v-if="currentLog.oldValue">
            <div class="change-card old-value">
              <div class="change-header">
                <el-icon><Remove /></el-icon>
                <span>变更前</span>
              </div>
              <div class="change-body">
                <div 
                  v-for="(value, key) in parseJson(currentLog.oldValue)" 
                  :key="key" 
                  class="field-row"
                >
                  <span class="field-key">{{ getFieldLabel(key) }}</span>
                  <span class="field-value">{{ formatFieldValue(value) }}</span>
                </div>
              </div>
            </div>
          </el-col>
          
          <!-- 变更后 -->
          <el-col :span="currentLog.oldValue && currentLog.newValue ? 12 : 24" v-if="currentLog.newValue">
            <div class="change-card new-value">
              <div class="change-header">
                <el-icon><CirclePlus /></el-icon>
                <span>变更后</span>
              </div>
              <div class="change-body">
                <div 
                  v-for="(value, key) in parseJson(currentLog.newValue)" 
                  :key="key" 
                  class="field-row"
                  :class="{ 'field-changed': isFieldChanged(key) }"
                >
                  <span class="field-key">{{ getFieldLabel(key) }}</span>
                  <span class="field-value">{{ formatFieldValue(value) }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <!-- 原始 JSON 折叠展示 -->
        <el-collapse class="json-collapse">
          <el-collapse-item title="查看原始 JSON 数据" name="raw">
            <el-row :gutter="20">
              <el-col :span="12" v-if="currentLog.oldValue">
                <div class="raw-json-label">变更前 (JSON)</div>
                <pre class="raw-json">{{ formatJson(currentLog.oldValue) }}</pre>
              </el-col>
              <el-col :span="12" v-if="currentLog.newValue">
                <div class="raw-json-label">变更后 (JSON)</div>
                <pre class="raw-json">{{ formatJson(currentLog.newValue) }}</pre>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Refresh, Download, Remove, CirclePlus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getLogList, getModules, getOperationTypes, exportLogs } from '@/api/log'

// 字段名中文映射
const fieldLabelMap = {
  // 通用字段
  wordId: '词汇ID',
  keyword: '关键词',
  categoryId: '分类ID',
  categoryName: '分类名称',
  detectionType: '检测类型',
  alertMessage: '警报信息',
  matchType: '匹配类型',
  riskLevel: '风险等级',
  isActive: '是否启用',
  createdBy: '创建人',
  createTime: '创建时间',
  updatedBy: '更新人',
  updateTime: '更新时间',
  // 问题书目
  bookId: '书目ID',
  bookName: '书名',
  author: '作者',
  isbn: 'ISBN',
  publisher: '出版社',
  publishYear: '出版年份',
  problemType: '问题类型',
  source: '来源',
  // 出版社白名单
  publisherId: '出版社ID',
  publisherName: '出版社名称',
  years: '年份批次',
  // 用户
  userId: '用户ID',
  username: '用户名',
  realName: '真实姓名',
  role: '角色',
  department: '部门',
  employeeId: '工号',
  lastLoginTime: '最后登录时间',
  // 其他
  id: 'ID',
  name: '名称',
  status: '状态',
  description: '描述'
}

// 搜索表单
const searchForm = reactive({
  module: '',
  operationType: '',
  startTime: '',
  endTime: ''
})

// 日期范围
const dateRange = ref([])

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 数据
const loading = ref(false)
const exportLoading = ref(false)
const logList = ref([])
const moduleList = ref([])
const operationTypeList = ref([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentLog = ref({})

// 初始化
onMounted(() => {
  loadModules()
  loadOperationTypes()
  loadData()
})

// 加载模块列表
const loadModules = async () => {
  try {
    const res = await getModules()
    if (res.code === 200) {
      moduleList.value = res.data
    }
  } catch (error) {
    console.error('加载模块列表失败', error)
  }
}

// 加载操作类型列表
const loadOperationTypes = async () => {
  try {
    const res = await getOperationTypes()
    if (res.code === 200) {
      operationTypeList.value = res.data
    }
  } catch (error) {
    console.error('加载操作类型列表失败', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      searchForm.startTime = dateRange.value[0]
      searchForm.endTime = dateRange.value[1]
    } else {
      searchForm.startTime = ''
      searchForm.endTime = ''
    }

    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }

    const res = await getLogList(params)
    if (res.code === 200) {
      logList.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('加载日志列表失败', error)
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.module = ''
  searchForm.operationType = ''
  searchForm.startTime = ''
  searchForm.endTime = ''
  dateRange.value = []
  pagination.pageNum = 1
  loadData()
}

// 导出
const handleExport = async () => {
  exportLoading.value = true
  try {
    // 处理日期范围
    const params = { ...searchForm }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const res = await exportLogs(params)
    
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `操作日志_${new Date().toLocaleDateString()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 查看详情
const handleViewDetail = (row) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

// 分页大小变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadData()
}

// 页码变化
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadData()
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化 JSON
const formatJson = (jsonStr) => {
  if (!jsonStr) return ''
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch {
    return jsonStr
  }
}

// 解析 JSON 为对象
const parseJson = (jsonStr) => {
  if (!jsonStr) return {}
  try {
    return JSON.parse(jsonStr)
  } catch {
    return { '原始数据': jsonStr }
  }
}

// 获取字段中文标签
const getFieldLabel = (key) => {
  return fieldLabelMap[key] || key
}

// 格式化字段值
const formatFieldValue = (value) => {
  if (value === null || value === undefined) return '-'
  if (value === true) return '是'
  if (value === false) return '否'
  if (typeof value === 'object') return JSON.stringify(value)
  
  // 匹配类型
  if (typeof value === 'number') {
    // 可能是匹配类型枚举
    const matchTypeMap = { 0: '精确匹配', 1: '模糊匹配', 2: '正则匹配' }
    const riskLevelMap = { 1: '低', 2: '中', 3: '高' }
    // 这里简单返回值，因为不知道具体是哪个字段
  }
  
  return String(value)
}

// 判断字段是否变更
const isFieldChanged = (key) => {
  if (!currentLog.value.oldValue || !currentLog.value.newValue) return false
  try {
    const oldObj = JSON.parse(currentLog.value.oldValue)
    const newObj = JSON.parse(currentLog.value.newValue)
    return JSON.stringify(oldObj[key]) !== JSON.stringify(newObj[key])
  } catch {
    return false
  }
}

// 获取操作类型标签样式
const getOperationTypeTagType = (operationType) => {
  const typeMap = {
    'create': 'success',
    'update': 'warning',
    'delete': 'danger',
    'import': 'primary',
    'export': 'info',
    'login': 'success',
    'logout': 'info'
  }
  return typeMap[operationType] || 'info'
}
</script>

<style scoped>
.operation-log-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.change-content {
  margin-top: 20px;
}

/* 美化的变更卡片 */
.change-card {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.change-card .change-header {
  padding: 12px 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.change-card.old-value .change-header {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #f56c6c;
  border-bottom: 1px solid #fbc4c4;
}

.change-card.new-value .change-header {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67c23a;
  border-bottom: 1px solid #b3e19d;
}

.change-card .change-body {
  padding: 12px 16px;
  background: #fff;
  max-height: 350px;
  overflow-y: auto;
}

.field-row {
  display: flex;
  padding: 8px 12px;
  border-radius: 6px;
  margin-bottom: 4px;
  transition: background-color 0.2s;
}

.field-row:hover {
  background-color: #f5f7fa;
}

.field-row:nth-child(odd) {
  background-color: #fafafa;
}

.field-row:nth-child(odd):hover {
  background-color: #f0f2f5;
}

.field-row.field-changed {
  background-color: #fdf6ec !important;
  border-left: 3px solid #e6a23c;
}

.field-key {
  flex: 0 0 120px;
  color: #606266;
  font-weight: 500;
  font-size: 13px;
}

.field-value {
  flex: 1;
  color: #303133;
  font-size: 13px;
  word-break: break-all;
}

/* JSON 折叠面板 */
.json-collapse {
  margin-top: 16px;
}

.raw-json-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.raw-json {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.6;
  overflow-x: auto;
  margin: 0;
  max-height: 300px;
  overflow-y: auto;
}

/* 滚动条美化 */
.change-body::-webkit-scrollbar,
.raw-json::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.change-body::-webkit-scrollbar-thumb,
.raw-json::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.change-body::-webkit-scrollbar-track,
.raw-json::-webkit-scrollbar-track {
  background: #f5f7fa;
}
</style>

