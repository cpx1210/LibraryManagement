<template>
  <div class="sensitive-word-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>敏感词管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="敏感词内容">
          <el-input
            v-model="queryForm.keyword"
            placeholder="请输入敏感词内容"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="敏感词类别">
          <el-input
            v-model="queryForm.category"
            placeholder="请输入敏感词类别"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="创建人">
          <el-input
            v-model="queryForm.createdBy"
            placeholder="请输入创建人"
            clearable
            style="width: 200px"
          />
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
          新增敏感词
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

      <!-- 敏感词表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="keyword" label="敏感词内容" min-width="200" />
        <el-table-column prop="category" label="敏感词类别" width="150" />
        <el-table-column prop="createdBy" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updatedBy" label="更新人" width="120" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
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
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 敏感词表单对话框 -->
    <SensitiveWordForm
      v-model:visible="dialogVisible"
      :form-data="currentWord"
      :is-edit="isEdit"
      @success="handleSuccess"
    />

    <!-- 批量导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入敏感词"
      width="500px"
    >
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        accept=".xlsx,.xls"
        drag
      >
        <el-icon class="el-icon--upload">
          <UploadFilled />
        </el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 xlsx/xls 文件，且不超过 10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmImport" :loading="importLoading">
          确定导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 导入结果对话框 -->
    <el-dialog
      v-model="importResultDialogVisible"
      title="导入结果"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="总数">
          {{ importResult.totalCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="成功">
          <span style="color: #67c23a">{{ importResult.successCount || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="失败">
          <span style="color: #f56c6c">{{ importResult.errorCount || 0 }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="importResult.errorList && importResult.errorList.length > 0" style="margin-top: 20px">
        <el-divider content-position="left">错误详情</el-divider>
        <el-scrollbar max-height="300px">
          <el-alert
            v-for="(error, index) in importResult.errorList"
            :key="index"
            :title="error"
            type="error"
            :closable="false"
            style="margin-bottom: 10px"
          />
        </el-scrollbar>
      </div>

      <template #footer>
        <el-button type="primary" @click="importResultDialogVisible = false">
          关闭
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
  Edit,
  Delete,
  Upload,
  Download,
  Document,
  UploadFilled
} from '@element-plus/icons-vue'
import {
  getSensitiveWordList,
  deleteSensitiveWord,
  importSensitiveWords,
  exportSensitiveWords,
  downloadTemplate
} from '@/api/sensitiveWord'
import SensitiveWordForm from './SensitiveWordForm.vue'

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  category: '',
  createdBy: ''
})

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentWord = ref({})

// 批量导入相关
const importDialogVisible = ref(false)
const importLoading = ref(false)
const uploadRef = ref(null)
const uploadFile = ref(null)

// 导入结果对话框
const importResultDialogVisible = ref(false)
const importResult = ref({})

/**
 * 加载敏感词列表
 */
const loadSensitiveWordList = async () => {
  loading.value = true
  try {
    const response = await getSensitiveWordList(queryForm)
    if (response.code === 200) {
      tableData.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('加载敏感词列表失败:', error)
    ElMessage.error('加载敏感词列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 查询按钮点击
 */
const handleQuery = () => {
  queryForm.pageNum = 1
  loadSensitiveWordList()
}

/**
 * 重置按钮点击
 */
const handleReset = () => {
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  queryForm.keyword = ''
  queryForm.category = ''
  queryForm.createdBy = ''
  loadSensitiveWordList()
}

/**
 * 新增敏感词
 */
const handleAdd = () => {
  isEdit.value = false
  currentWord.value = {}
  dialogVisible.value = true
}

/**
 * 编辑敏感词
 */
const handleEdit = (row) => {
  isEdit.value = true
  currentWord.value = { ...row }
  dialogVisible.value = true
}

/**
 * 删除敏感词
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除敏感词"${row.keyword}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await deleteSensitiveWord(row.wordId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadSensitiveWordList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除敏感词失败:', error)
    }
  }
}

/**
 * 批量导入
 */
const handleImport = () => {
  uploadFile.value = null
  importDialogVisible.value = true
}

/**
 * 文件选择变化
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
const confirmImport = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }

  // 检查文件大小（10MB）
  if (uploadFile.value.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }

  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)

    const response = await importSensitiveWords(formData)
    if (response.code === 200) {
      importResult.value = response.data
      importDialogVisible.value = false
      importResultDialogVisible.value = true

      // 刷新列表
      loadSensitiveWordList()

      // 清空上传组件
      uploadRef.value?.clearFiles()
    }
  } catch (error) {
    console.error('导入敏感词失败:', error)
    ElMessage.error('导入失败，请检查文件格式')
  } finally {
    importLoading.value = false
  }
}

/**
 * 批量导出
 */
const handleExport = async () => {
  try {
    ElMessage.info('正在导出，请稍候...')

    const response = await exportSensitiveWords(queryForm)

    // 创建下载链接
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `敏感词列表_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出敏感词失败:', error)
    ElMessage.error('导出失败')
  }
}

/**
 * 下载模板
 */
const handleDownloadTemplate = async () => {
  try {
    ElMessage.info('正在下载模板，请稍候...')

    const response = await downloadTemplate()

    // 创建下载链接
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '敏感词导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)

    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('模板下载失败')
  }
}

/**
 * 表单操作成功回调
 */
const handleSuccess = () => {
  dialogVisible.value = false
  loadSensitiveWordList()
}

/**
 * 每页数量变化
 */
const handleSizeChange = () => {
  loadSensitiveWordList()
}

/**
 * 当前页变化
 */
const handleCurrentChange = () => {
  loadSensitiveWordList()
}

// 页面加载时获取数据
onMounted(() => {
  loadSensitiveWordList()
})
</script>

<style scoped>
.sensitive-word-list-container {
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.el-icon--upload {
  font-size: 67px;
  color: #c0c4cc;
  margin: 40px 0 16px;
  line-height: 50px;
}

.el-upload__text {
  color: #606266;
  font-size: 14px;
  text-align: center;
}

.el-upload__text em {
  color: #409eff;
  font-style: normal;
}
</style>
