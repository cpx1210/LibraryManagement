<template>
  <div class="collection-problem-book-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>馆藏问题图书管理</h2>
      <p class="description">管理标记为问题的馆藏图书，可恢复为正常馆藏</p>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="条码">
          <el-input v-model="searchForm.barcode" placeholder="请输入条码" clearable />
        </el-form-item>
        <el-form-item label="题名">
          <el-input v-model="searchForm.bookName" placeholder="请输入题名" clearable />
        </el-form-item>
        <el-form-item label="著者">
          <el-input v-model="searchForm.author" placeholder="请输入著者" clearable />
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="searchForm.isbn" placeholder="请输入ISBN" clearable />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="searchForm.publisher" placeholder="请输入出版社" clearable />
        </el-form-item>
        <el-form-item label="问题类型">
          <el-input v-model="searchForm.problemType" placeholder="请输入问题类型" clearable />
        </el-form-item>
        <el-form-item label="分馆">
          <el-input v-model="searchForm.branchLibrary" placeholder="请输入分馆" clearable />
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

    <!-- 操作按钮 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>馆藏问题图书列表</span>
          <div class="header-actions">
            <el-button type="warning" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="barcode" label="条码" width="120" fixed />
        <el-table-column prop="bookName" label="题名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="author" label="著者" width="120" show-overflow-tooltip />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="publisher" label="出版社" width="140" show-overflow-tooltip />
        <el-table-column prop="publishYear" label="出版年" width="80" />
        <el-table-column prop="branchLibrary" label="分馆" width="100" />
        <el-table-column prop="callNumber" label="索书号" width="100" />
        <el-table-column prop="problemType" label="问题类型" width="120">
          <template #default="{ row }">
            <el-tag type="danger" v-if="row.problemType">{{ row.problemType }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="problemReason" label="问题原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="libraryLocation" label="馆藏院舍" width="100" show-overflow-tooltip />
        <el-table-column prop="shelfLocation" label="书架位置" width="100" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="标记时间" width="160">
          <template #default="{ row }">
            {{ row.updateTime ? formatDateTime(row.updateTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">
              详情
            </el-button>
            <el-button type="success" link size="small" @click="handleRestore(row)">
              恢复正常
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="问题图书详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="条码">{{ detailData.barcode }}</el-descriptions-item>
        <el-descriptions-item label="题名">{{ detailData.bookName }}</el-descriptions-item>
        <el-descriptions-item label="著者">{{ detailData.author || '-' }}</el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ detailData.isbn || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出版社">{{ detailData.publisher || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出版年">{{ detailData.publishYear || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分馆">{{ detailData.branchLibrary || '-' }}</el-descriptions-item>
        <el-descriptions-item label="索书号">{{ detailData.callNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="单价">{{ detailData.price ? `¥${detailData.price}` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="批次">{{ detailData.batch || '-' }}</el-descriptions-item>
        <el-descriptions-item label="馆藏院舍">{{ detailData.libraryLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="书架位置">{{ detailData.shelfLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题类型" :span="2">
          <el-tag type="danger" v-if="detailData.problemType">{{ detailData.problemType }}</el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="问题原因" :span="2">{{ detailData.problemReason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="handleRestoreFromDetail">恢复为正常馆藏</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download, Delete } from '@element-plus/icons-vue'
import {
  getCollectionBookList,
  deleteCollectionBook,
  deleteBatchCollectionBooks,
  exportCollectionBooks,
  markAsNormal
} from '@/api/collectionBook'

// 搜索表单
const searchForm = reactive({
  barcode: '',
  bookName: '',
  author: '',
  isbn: '',
  publisher: '',
  problemType: '',
  branchLibrary: '',
  isProblem: 1  // 只查询问题图书
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const selectedRows = ref([])

// 详情对话框
const detailDialogVisible = ref(false)
const detailData = ref({})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载列表数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await getCollectionBookList(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  searchForm.barcode = ''
  searchForm.bookName = ''
  searchForm.author = ''
  searchForm.isbn = ''
  searchForm.publisher = ''
  searchForm.problemType = ''
  searchForm.branchLibrary = ''
  handleSearch()
}

// 分页
const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadData()
}

// 多选
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 查看详情
const handleView = (row) => {
  detailData.value = { ...row }
  detailDialogVisible.value = true
}

// 恢复为正常馆藏
const handleRestore = (row) => {
  ElMessageBox.confirm(`确定要将条码为"${row.barcode}"的图书恢复为正常馆藏吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await markAsNormal(row.barcode)
      ElMessage.success('已恢复为正常馆藏')
      loadData()
    } catch (error) {
      console.error('恢复失败:', error)
      ElMessage.error('恢复失败')
    }
  }).catch(() => {})
}

// 从详情对话框恢复
const handleRestoreFromDetail = () => {
  handleRestore(detailData.value)
  detailDialogVisible.value = false
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除条码为"${row.barcode}"的图书吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCollectionBook(row.barcode)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的数据')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条数据吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const barcodes = selectedRows.value.map(item => item.barcode)
      await deleteBatchCollectionBooks(barcodes)
      ElMessage.success('批量删除成功')
      loadData()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

// 导出
const handleExport = async () => {
  try {
    const params = { ...searchForm }
    const res = await exportCollectionBooks(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `馆藏问题图书_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.collection-problem-book-list {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}

.page-header .description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

