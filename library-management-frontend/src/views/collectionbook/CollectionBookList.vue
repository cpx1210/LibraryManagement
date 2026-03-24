<template>
  <div class="collection-book-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>馆藏图书管理</h2>
      <p class="description">管理图书馆的馆藏图书信息，支持导入导出Excel</p>
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
        <el-form-item label="分馆">
          <el-input v-model="searchForm.branchLibrary" placeholder="请输入分馆" clearable />
        </el-form-item>
        <el-form-item label="批次">
          <el-input v-model="searchForm.batch" placeholder="请输入批次" clearable />
        </el-form-item>
        <el-form-item label="是否入库">
          <el-select v-model="searchForm.isStored" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="已入库" :value="1" />
            <el-option label="未入库" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button :disabled="loading" @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <div class="search-tip">
        大数据检索可能需要较长时间，检索期间页面会持续等待结果返回，不会因为前端 10 秒超时而中断。
      </div>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>馆藏图书列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
            <el-button type="success" @click="handleImport">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
            <el-button type="warning" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button @click="handleDownloadTemplate">
              <el-icon><Document /></el-icon>
              下载模板
            </el-button>
            <el-button type="info" @click="handleCheckBooks">
              <el-icon><Search /></el-icon>
              检测书单
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
        <el-table-column prop="price" label="单价" width="80">
          <template #default="{ row }">
            {{ row.price ? `¥${row.price}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="batch" label="批次" width="100" />
        <el-table-column prop="isStored" label="是否入库" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isStored === 1 ? 'success' : 'warning'">
              {{ row.isStored === 1 ? '已入库' : '未入库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="libraryLocation" label="馆藏院舍" width="100" show-overflow-tooltip />
        <el-table-column prop="shelfLocation" label="书架位置" width="100" show-overflow-tooltip />
        <el-table-column prop="duplicateFlag" label="重复标记" width="90">
          <template #default="{ row }">
            <el-tag :type="row.duplicateFlag === 1 ? 'danger' : 'info'" size="small">
              {{ row.duplicateFlag === 1 ? '重复' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="warning" link size="small" @click="handleMarkProblem(row)">
              标记问题
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增馆藏图书' : '编辑馆藏图书'"
      width="800px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="条码" prop="barcode">
              <el-input v-model="formData.barcode" placeholder="请输入条码" :disabled="dialogType === 'edit'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="题名" prop="bookName">
              <el-input v-model="formData.bookName" placeholder="请输入题名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="著者" prop="author">
              <el-input v-model="formData.author" placeholder="请输入著者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="formData.isbn" placeholder="请输入ISBN" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="formData.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版年" prop="publishYear">
              <el-input v-model="formData.publishYear" placeholder="请输入出版年" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分馆" prop="branchLibrary">
              <el-input v-model="formData.branchLibrary" placeholder="请输入分馆" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="索书号" prop="callNumber">
              <el-input v-model="formData.callNumber" placeholder="请输入索书号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="formData.price" :precision="2" :min="0" placeholder="请输入单价" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次" prop="batch">
              <el-input v-model="formData.batch" placeholder="请输入批次" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否入库" prop="isStored">
              <el-select v-model="formData.isStored" placeholder="请选择" style="width: 100%;">
                <el-option label="已入库" :value="1" />
                <el-option label="未入库" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重复标记" prop="duplicateFlag">
              <el-select v-model="formData.duplicateFlag" placeholder="请选择" style="width: 100%;">
                <el-option label="正常" :value="0" />
                <el-option label="重复" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="馆藏院舍" prop="libraryLocation">
              <el-input v-model="formData.libraryLocation" placeholder="请输入馆藏院舍" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书架位置" prop="shelfLocation">
              <el-input v-model="formData.shelfLocation" placeholder="请输入书架位置" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入馆藏图书" width="500px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 xlsx/xls 文件，请先下载模板填写后上传
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleImportSubmit">确定导入</el-button>
      </template>
    </el-dialog>

    <!-- 标记问题对话框 -->
    <el-dialog v-model="markProblemDialogVisible" title="标记为问题图书" width="500px">
      <el-form :model="markProblemForm" label-width="100px">
        <el-form-item label="问题类型">
          <el-input v-model="markProblemForm.problemType" placeholder="请输入问题类型" />
        </el-form-item>
        <el-form-item label="问题原因">
          <el-input v-model="markProblemForm.problemReason" type="textarea" :rows="4" placeholder="请输入问题原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="markProblemDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="markProblemLoading" @click="handleMarkProblemSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Upload, Download, Document, Delete, UploadFilled } from '@element-plus/icons-vue'
import {
  getCollectionBookList,
  createCollectionBook,
  updateCollectionBook,
  deleteCollectionBook,
  deleteBatchCollectionBooks,
  importCollectionBooks,
  exportCollectionBooks,
  downloadCollectionBookTemplate,
  markAsProblem,
  checkCollectionBooks
} from '@/api/collectionBook'
import { useRouter } from 'vue-router'

// 搜索表单
const searchForm = reactive({
  barcode: '',
  bookName: '',
  author: '',
  isbn: '',
  publisher: '',
  branchLibrary: '',
  batch: '',
  isStored: null,
  isProblem: 0  // 只查询正常馆藏（非问题图书）
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

// 对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const submitLoading = ref(false)

// 表单数据
const formData = reactive({
  barcode: '',
  bookName: '',
  author: '',
  isbn: '',
  publisher: '',
  publishYear: '',
  branchLibrary: '',
  callNumber: '',
  price: null,
  batch: '',
  isStored: 1,
  libraryLocation: '',
  shelfLocation: '',
  duplicateFlag: 0
})

// 表单校验规则
const formRules = {
  barcode: [
    { required: true, message: '请输入条码', trigger: 'blur' },
    { max: 50, message: '条码长度不能超过50个字符', trigger: 'blur' }
  ],
  bookName: [
    { required: true, message: '请输入题名', trigger: 'blur' },
    { max: 500, message: '题名长度不能超过500个字符', trigger: 'blur' }
  ]
}

// 导入对话框
const importDialogVisible = ref(false)
const importLoading = ref(false)
const uploadRef = ref(null)
const importFile = ref(null)

// 标记问题对话框
const markProblemDialogVisible = ref(false)
const markProblemLoading = ref(false)
const markProblemForm = reactive({
  barcode: '',
  problemType: '',
  problemReason: ''
})

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
    ElMessage.error(error.message || '加载数据失败，请稍后重试')
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
  searchForm.branchLibrary = ''
  searchForm.batch = ''
  searchForm.isStored = null
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

// 新增
const handleAdd = () => {
  dialogType.value = 'add'
  Object.keys(formData).forEach(key => {
    if (key === 'isStored') {
      formData[key] = 1
    } else if (key === 'duplicateFlag') {
      formData[key] = 0
    } else if (key === 'price') {
      formData[key] = null
    } else {
      formData[key] = ''
    }
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(formData).forEach(key => {
    formData[key] = row[key]
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await createCollectionBook(formData)
          ElMessage.success('新增成功')
        } else {
          await updateCollectionBook(formData)
          ElMessage.success('修改成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
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

// 导入
const handleImport = () => {
  importFile.value = null
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  importFile.value = file.raw
}

const handleImportSubmit = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    const res = await importCollectionBooks(formData)
    if (res.code === 200) {
      const { successCount, errorCount, errors } = res.data
      if (errorCount > 0) {
        ElMessage.warning(`导入完成：成功 ${successCount} 条，失败 ${errorCount} 条`)
        console.log('导入错误详情:', errors)
      } else {
        ElMessage.success(`导入成功：${successCount} 条`)
      }
      importDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

// 导出
const handleExport = async () => {
  try {
    const params = { ...searchForm }
    const res = await exportCollectionBooks(params)
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `馆藏图书_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 检测书单
const router = useRouter()
const handleCheckBooks = async () => {
  let loadingInstance = null
  try {
    await ElMessageBox.confirm(
      '确定要将当前筛选条件下的馆藏书目提交检测吗？',
      '检测确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 准备检测参数
    const checkParams = {
      taskName: `馆藏书目检测-${new Date().toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).replace(/\//g, '-')}`,
      ...searchForm
    }

    loadingInstance = ElMessage({
      message: '正在创建检测任务...',
      type: 'info',
      duration: 0
    })

    const response = await checkCollectionBooks(checkParams)
    
    loadingInstance.close()

    if (response.code === 200) {
      ElMessage.success(`检测任务创建成功！共检测 ${response.data.totalBooks} 本书目`)

      // 直接跳转到检测进度页，让用户第一时间看到实时任务进度。
      router.push({
        path: '/detection/check',
        query: { taskId: response.data.taskId }
      })
    }
  } catch (error) {
    loadingInstance?.close()
    if (error !== 'cancel') {
      console.error('创建检测任务失败:', error)
      ElMessage.error(error.message || '创建检测任务失败')
    }
  }
}

// 下载模板
const handleDownloadTemplate = async () => {
  try {
    const res = await downloadCollectionBookTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '馆藏图书导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败')
  }
}

// 标记为问题图书
const handleMarkProblem = (row) => {
  markProblemForm.barcode = row.barcode
  markProblemForm.problemType = ''
  markProblemForm.problemReason = ''
  markProblemDialogVisible.value = true
}

const handleMarkProblemSubmit = async () => {
  markProblemLoading.value = true
  try {
    await markAsProblem(markProblemForm.barcode, markProblemForm.problemType, markProblemForm.problemReason)
    ElMessage.success('已标记为问题图书')
    markProblemDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('标记失败:', error)
    ElMessage.error('标记失败')
  } finally {
    markProblemLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.collection-book-list {
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

.search-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
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

