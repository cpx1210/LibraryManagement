<template>
  <div class="simple-home">
    <div class="simple-home__card">
      <div class="simple-home__header">
        <h1>深圳职业技术大学</h1>
      </div>

      <div class="simple-home__toolbar">
        <el-button
          class="action-button action-button--download"
          size="large"
          :loading="downloadingTemplate"
          @click="handleDownloadTemplate"
        >
          <el-icon><Download /></el-icon>
          下载模板
        </el-button>

        <el-button class="action-button action-button--upload" size="large" @click="handleChooseFile">
          <el-icon><Upload /></el-icon>
          {{ selectedFile ? '重新选择文件' : '上传文件' }}
        </el-button>

        <input
          ref="fileInputRef"
          class="hidden-file-input"
          type="file"
          accept=".xls,.xlsx"
          @change="handleFilePicked"
        >
      </div>

      <div class="file-hint">
        <span class="file-hint__label">已选文件：</span>
        <span>{{ selectedFileName }}</span>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="simple-home__form"
      >
        <div class="form-grid">
          <el-form-item label="姓名" prop="submitterName">
            <el-input v-model="form.submitterName" placeholder="请输入姓名" clearable />
          </el-form-item>

          <el-form-item label="部门" prop="department">
            <el-input v-model="form.department" placeholder="请输入部门" clearable />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" clearable />
          </el-form-item>

          <el-form-item label="工号" prop="employeeNo">
            <el-input v-model="form.employeeNo" placeholder="请输入工号" clearable />
          </el-form-item>

          <el-form-item label="手机" prop="mobile" class="form-grid__full-width">
            <el-input v-model="form.mobile" placeholder="请输入手机号" clearable />
          </el-form-item>
        </div>

        <div class="submit-row">
          <el-button
            class="submit-button"
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '提交中...' : '提交' }}
          </el-button>
        </div>
      </el-form>

      <div class="notice-panel">
        <div class="notice-panel__title">提示信息：</div>
        <div class="notice-panel__content">
          <p>{{ noticeMessage }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { Download, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { downloadTemplate, uploadBooklist } from '@/api/detection'

const formRef = ref(null)
const fileInputRef = ref(null)
const selectedFile = ref(null)
const submitting = ref(false)
const downloadingTemplate = ref(false)
const noticeMessage = ref('请先下载模板，填写书单后上传 Excel 文件；再补全联系人信息并点击提交。')

const form = reactive({
  submitterName: '',
  department: '',
  email: '',
  employeeNo: '',
  mobile: ''
})

const rules = {
  submitterName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  department: [{ required: true, message: '请输入部门', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  employeeNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的 11 位手机号', trigger: ['blur', 'change'] }
  ]
}

const selectedFileName = computed(() => selectedFile.value?.name || '暂未选择 Excel 文件')

const resetForm = () => {
  form.submitterName = ''
  form.department = ''
  form.email = ''
  form.employeeNo = ''
  form.mobile = ''
  formRef.value?.clearValidate()
}

const handleChooseFile = () => {
  fileInputRef.value?.click()
}

const handleFilePicked = (event) => {
  const [file] = event.target.files || []
  if (!file) return

  const isExcelFile = /\.(xls|xlsx)$/i.test(file.name)
  if (!isExcelFile) {
    ElMessage.error('请上传 Excel 文件（.xls 或 .xlsx）')
    event.target.value = ''
    return
  }

  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    event.target.value = ''
    return
  }

  selectedFile.value = file
  noticeMessage.value = `已选择文件：${file.name}，请确认信息无误后点击提交。`
  event.target.value = ''
}

const handleDownloadTemplate = async () => {
  downloadingTemplate.value = true
  try {
    const blob = await downloadTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '书单检测模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
    noticeMessage.value = '模板已下载，请按模板格式填写后上传。'
  } catch (error) {
    console.error('模板下载失败：', error)
    ElMessage.error('模板下载失败，请稍后重试')
    noticeMessage.value = '模板下载失败，请稍后重试。'
  } finally {
    downloadingTemplate.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    ElMessage.warning('请先补全页面信息')
    return
  }

  if (!selectedFile.value) {
    ElMessage.warning('请先选择要上传的 Excel 文件')
    return
  }

  submitting.value = true
  try {
    const res = await uploadBooklist(selectedFile.value, {
      submitterName: form.submitterName.trim(),
      department: form.department.trim(),
      email: form.email.trim(),
      employeeNo: form.employeeNo.trim(),
      mobile: form.mobile.trim()
    })

    if (res.code !== 200) {
      ElMessage.error(res.message || '提交失败，请稍后重试')
      noticeMessage.value = '文件提交失败，请检查内容后重新上传。'
      return
    }

    ElMessage.success(res.data.message || '提交成功')
    noticeMessage.value = '提交成功，文件已进入后台处理流程，请等待工作人员后续处理。'
    selectedFile.value = null
    resetForm()
  } catch (error) {
    console.error('提交书单失败：', error)
    ElMessage.error(error.message || '提交失败，请稍后重试')
    noticeMessage.value = '文件提交失败，请检查网络或稍后再试。'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.simple-home {
  min-height: 100vh;
  padding: 32px 20px;
  background:
    linear-gradient(180deg, #f6fbff 0%, #eef5ff 100%);
}

.simple-home__card {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
  padding: 36px 56px 40px;
  background: rgba(255, 255, 255, 0.97);
  border: 1px solid rgba(24, 94, 190, 0.08);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(23, 74, 144, 0.12);
}

.simple-home__header {
  padding: 6px 0 28px;
  text-align: center;
  border-bottom: 1px solid #e6edf7;
}

.simple-home__header h1 {
  margin: 0;
  color: #0f4ea5;
  font-size: 56px;
  line-height: 1.15;
  font-weight: 800;
  letter-spacing: 4px;
}

.simple-home__toolbar {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin: 46px 0 18px;
}

.action-button {
  min-width: 280px;
  height: 72px;
  border: none;
  border-radius: 14px;
  font-size: 32px;
  font-weight: 700;
  box-shadow: 0 16px 30px rgba(15, 78, 165, 0.16);
}

.action-button :deep(.el-icon) {
  margin-right: 10px;
  font-size: 30px;
}

.action-button--download {
  background: linear-gradient(180deg, #2690ff 0%, #0d68d0 100%);
  color: #fff;
}

.action-button--upload {
  background: linear-gradient(180deg, #ffa51f 0%, #ff8c00 100%);
  color: #fff;
}

.file-hint {
  margin: 0 auto 34px;
  padding: 12px 18px;
  max-width: 584px;
  border-radius: 12px;
  text-align: center;
  color: #4a5a73;
  background: #f3f8ff;
}

.file-hint__label {
  color: #0f4ea5;
  font-weight: 600;
}

.hidden-file-input {
  display: none;
}

.simple-home__form {
  padding: 34px 0 18px;
  border-top: 1px solid #e6edf7;
  border-bottom: 1px solid #e6edf7;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 32px;
  max-width: 920px;
  margin: 0 auto;
}

.form-grid__full-width {
  grid-column: 1 / -1;
}

.form-grid :deep(.el-form-item__label) {
  font-size: 28px;
  font-weight: 700;
  color: #24324a;
  line-height: 1.4;
  padding-bottom: 10px;
}

.form-grid :deep(.el-input__wrapper) {
  min-height: 64px;
  padding: 0 18px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #d8e2f2 inset;
}

.form-grid :deep(.el-input__inner) {
  font-size: 24px;
}

.submit-row {
  display: flex;
  justify-content: center;
  padding-top: 20px;
}

.submit-button {
  min-width: 260px;
  height: 68px;
  border: none;
  border-radius: 14px;
  font-size: 34px;
  font-weight: 700;
  background: linear-gradient(180deg, #2690ff 0%, #0d68d0 100%);
  box-shadow: 0 16px 30px rgba(15, 78, 165, 0.16);
}

.notice-panel {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 8px 20px;
  margin-top: 34px;
  padding: 4px 0 0;
}

.notice-panel__title {
  color: #0f67bf;
  font-size: 30px;
  font-weight: 700;
}

.notice-panel__content {
  color: #607089;
  font-size: 24px;
  line-height: 1.8;
}

@media (max-width: 1200px) {
  .simple-home__card {
    padding: 24px 28px 32px;
  }

  .simple-home__header h1 {
    font-size: 46px;
  }

  .action-button {
    min-width: 240px;
    font-size: 26px;
  }

  .form-grid :deep(.el-form-item__label) {
    font-size: 22px;
  }

  .form-grid :deep(.el-input__inner) {
    font-size: 18px;
  }

  .submit-button {
    font-size: 28px;
  }

  .notice-panel__title,
  .notice-panel__content {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  .simple-home {
    padding: 16px;
  }

  .simple-home__card {
    padding: 18px 16px 24px;
    border-radius: 20px;
  }

  .simple-home__toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .simple-home__header h1 {
    font-size: 34px;
    letter-spacing: 1px;
  }

  .action-button,
  .submit-button {
    width: 100%;
    min-width: 0;
    height: 56px;
    font-size: 22px;
  }

  .action-button :deep(.el-icon) {
    font-size: 20px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .form-grid__full-width {
    grid-column: auto;
  }

  .form-grid :deep(.el-form-item__label) {
    font-size: 18px;
  }

  .form-grid :deep(.el-input__wrapper) {
    min-height: 48px;
  }

  .form-grid :deep(.el-input__inner) {
    font-size: 16px;
  }

  .notice-panel {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .notice-panel__title,
  .notice-panel__content {
    font-size: 16px;
  }
}
</style>
