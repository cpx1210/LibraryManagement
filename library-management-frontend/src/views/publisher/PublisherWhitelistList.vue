<template>
  <div class="publisher-whitelist-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>出版社白名单管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="出版社名称">
          <el-input
            v-model="queryForm.publisherName"
            placeholder="请输入出版社名称"
            clearable
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item label="年份批次">
          <el-input
            v-model="queryForm.years"
            placeholder="请输入年份"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.isActive"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :icon="Search">
            查询
          </el-button>
          <el-button @click="handleReset" :icon="Refresh">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮和表格 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd" :icon="Plus">
          新增出版社
        </el-button>
        <el-button type="success" @click="handleImport" :icon="Upload">
          批量导入
        </el-button>
        <el-button type="warning" @click="handleExport" :icon="Download">
          批量导出
        </el-button>
        <el-button @click="handleDownloadTemplate" :icon="Document">
          下载模板
        </el-button>
      </div>

      <!-- 出版社白名单表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column label="序号" width="60" align="center">
          <template #default="{ $index }">
            {{ (pagination.pageNum - 1) * pagination.pageSize + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="出版社名称" min-width="300" show-overflow-tooltip />
        <el-table-column prop="years" label="年份批次" width="120" align="center">
          <template #default="{ row }">
            {{ row.years || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'">
              {{ row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="handleEdit(row)"
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              link
              @click="handleDelete(row)"
              :icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="出版社名称" prop="publisherName">
          <el-input
            v-model="formData.publisherName"
            placeholder="请输入出版社名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="年份批次" prop="years">
          <el-input
            v-model="formData.years"
            placeholder="请输入年份（如：2024）"
            type="number"
          />
        </el-form-item>
        <el-form-item label="是否启用" prop="isActive">
          <el-radio-group v-model="formData.isActive">
            <el-radio :value="true">启用</el-radio>
            <el-radio :value="false">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入出版社白名单"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        :on-exceed="handleExceed"
        :on-change="handleFileChange"
        accept=".xlsx,.xls"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 xlsx/xls 文件，建议先下载模板后填写
          </div>
        </template>
      </el-upload>

      <!-- 导入结果展示 -->
      <el-alert
        v-if="importResult"
        :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`"
        :type="importResult.failCount > 0 ? 'warning' : 'success'"
        style="margin-top: 20px"
        show-icon
        :closable="false"
      >
        <template v-if="importResult.errorMessages && importResult.errorMessages.length > 0">
          <div style="max-height: 200px; overflow-y: auto; margin-top: 10px">
            <div v-for="(msg, index) in importResult.errorMessages" :key="index" style="color: #E6A23C; font-size: 12px;">
              {{ msg }}
            </div>
          </div>
        </template>
      </el-alert>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmImport" :loading="importing">
          确定导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Upload,
  Download,
  Document,
  Edit,
  Delete,
  UploadFilled
} from '@element-plus/icons-vue'
import {
  getPublisherWhitelistList,
  createPublisherWhitelist,
  updatePublisherWhitelist,
  deletePublisherWhitelist,
  importPublisherWhitelists,
  exportPublisherWhitelists,
  downloadTemplate
} from '@/api/publisherWhitelist'

// 查询表单
const queryForm = reactive({
  publisherName: '',
  years: '',
  isActive: null
})

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const formData = reactive({
  publisherId: null,
  publisherName: '',
  years: null,
  isActive: true
})
const submitting = ref(false)

// 表单验证规则
const formRules = {
  publisherName: [
    { required: true, message: '请输入出版社名称', trigger: 'blur' },
    { min: 1, max: 200, message: '长度在 1 到 200 个字符', trigger: 'blur' }
  ]
}

// 导入对话框
const importDialogVisible = ref(false)
const uploadRef = ref(null)
const uploadFile = ref(null)
const importing = ref(false)
const importResult = ref(null)

/**
 * 查询出版社白名单列表
 */
const loadTableData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    const res = await getPublisherWhitelistList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('查询失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

/**
 * 查询按钮点击
 */
const handleQuery = () => {
  pagination.pageNum = 1
  loadTableData()
}

/**
 * 重置按钮点击
 */
const handleReset = () => {
  queryForm.publisherName = ''
  queryForm.years = ''
  queryForm.isActive = null
  pagination.pageNum = 1
  loadTableData()
}

/**
 * 分页大小改变
 */
const handleSizeChange = (newSize) => {
  pagination.pageSize = newSize
  pagination.pageNum = 1
  loadTableData()
}

/**
 * 当前页改变
 */
const handleCurrentChange = (newPage) => {
  pagination.pageNum = newPage
  loadTableData()
}

/**
 * 新增按钮点击
 */
const handleAdd = () => {
  dialogTitle.value = '新增出版社白名单'
  formData.publisherId = null
  formData.publisherName = ''
  formData.years = null
  formData.isActive = true
  dialogVisible.value = true
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

/**
 * 编辑按钮点击
 */
const handleEdit = (row) => {
  dialogTitle.value = '编辑出版社白名单'
  formData.publisherId = row.publisherId
  formData.publisherName = row.publisherName
  formData.years = row.years
  formData.isActive = row.isActive
  dialogVisible.value = true
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

/**
 * 表单提交
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const data = {
        publisherId: formData.publisherId,
        publisherName: formData.publisherName,
        years: formData.years ? parseInt(formData.years) : null,
        isActive: formData.isActive
      }

      if (formData.publisherId) {
        // 编辑
        await updatePublisherWhitelist(data)
        ElMessage.success('修改成功')
      } else {
        // 新增
        await createPublisherWhitelist(data)
        ElMessage.success('新增成功')
      }

      dialogVisible.value = false
      loadTableData()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

/**
 * 删除按钮点击
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除出版社「${row.publisherName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deletePublisherWhitelist(row.publisherId)
    ElMessage.success('删除成功')
    loadTableData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

/**
 * 批量导入按钮点击
 */
const handleImport = () => {
  importDialogVisible.value = true
  uploadFile.value = null
  importResult.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

/**
 * 文件选择
 */
const handleFileChange = (file) => {
  uploadFile.value = file.raw
}

/**
 * 文件超出限制
 */
const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

/**
 * 确认导入
 */
const handleConfirmImport = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)

    const res = await importPublisherWhitelists(formData)
    importResult.value = res.data
    ElMessage.success('导入完成')
    loadTableData()
  } catch (error) {
    ElMessage.error('导入失败：' + (error.message || '未知错误'))
  } finally {
    importing.value = false
  }
}

/**
 * 批量导出按钮点击
 */
const handleExport = async () => {
  try {
    const res = await exportPublisherWhitelists(queryForm)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `出版社白名单_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
  }
}

/**
 * 下载模板按钮点击
 */
const handleDownloadTemplate = async () => {
  try {
    const res = await downloadTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '出版社白名单导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('下载失败：' + (error.message || '未知错误'))
  }
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

// 组件挂载时加载数据
onMounted(() => {
  loadTableData()
})
</script>

<style scoped>
.publisher-whitelist-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.search-card {
  margin-bottom: 20px;
}

.table-header {
  margin-bottom: 20px;
}
</style>
