<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑问题书目' : '新增问题书目'"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <!-- 书名 -->
      <el-form-item label="书名" prop="bookName">
        <el-input
          v-model="form.bookName"
          placeholder="请输入书名"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 作者 -->
      <el-form-item label="作者" prop="author">
        <el-input
          v-model="form.author"
          placeholder="请输入作者（多个作者用逗号分隔）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- ISBN -->
      <el-form-item label="ISBN" prop="isbn">
        <el-input
          v-model="form.isbn"
          placeholder="请输入ISBN编号"
          maxlength="20"
          show-word-limit
        />
      </el-form-item>

      <!-- 出版社 -->
      <el-form-item label="出版社" prop="publisher">
        <el-input
          v-model="form.publisher"
          placeholder="请输入出版社"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 出版年份 -->
      <el-form-item label="出版年份" prop="publishYear">
        <el-input
          v-model="form.publishYear"
          placeholder="请输入出版年份（格式：YYYY）"
          maxlength="4"
          show-word-limit
        />
      </el-form-item>

      <!-- 问题类型 -->
      <el-form-item label="问题类型" prop="problemType">
        <el-select
          v-model="form.problemType"
          placeholder="请选择问题类型"
          style="width: 100%"
          filterable
          allow-create
          clearable
        >
          <el-option label="政治问题" value="政治问题" />
          <el-option label="内容不当" value="内容不当" />
          <el-option label="盗版" value="盗版" />
          <el-option label="质量问题" value="质量问题" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>

      <!-- 来源 -->
      <el-form-item label="来源" prop="source">
        <el-input
          v-model="form.source"
          type="textarea"
          :rows="3"
          placeholder="请输入来源说明（如：教育部通报、出版社通知等）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { createProblemBook, updateProblemBook } from '@/api/problemBook'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  formData: {
    type: Object,
    default: () => ({})
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:visible', 'success'])

// 对话框显示状态
const dialogVisible = ref(props.visible)

// 表单引用
const formRef = ref(null)

// 表单数据
const form = reactive({
  bookId: null,
  bookName: '',
  author: '',
  isbn: '',
  publisher: '',
  publishYear: '',
  problemType: '',
  source: ''
})

// 提交加载状态
const submitLoading = ref(false)

// 表单验证规则
const rules = {
  bookName: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { min: 1, max: 500, message: '书名长度在1-500个字符', trigger: 'blur' }
  ],
  author: [
    { max: 200, message: '作者名称最多200个字符', trigger: 'blur' }
  ],
  isbn: [
    { max: 20, message: 'ISBN编号最多20个字符', trigger: 'blur' }
  ],
  publisher: [
    { max: 200, message: '出版社名称最多200个字符', trigger: 'blur' }
  ],
  publishYear: [
    { pattern: /^\d{4}$|^$/, message: '出版年份格式必须为4位数字（如：2023）', trigger: 'blur' }
  ],
  problemType: [
    { max: 100, message: '问题类型最多100个字符', trigger: 'blur' }
  ],
  source: [
    { max: 500, message: '来源说明最多500个字符', trigger: 'blur' }
  ]
}

/**
 * 监听 visible 变化
 */
watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val) {
    // 对话框打开时，初始化表单数据
    nextTick(() => {
      if (props.isEdit) {
        // 编辑模式：填充数据
        Object.assign(form, {
          bookId: props.formData.bookId,
          bookName: props.formData.bookName,
          author: props.formData.author,
          isbn: props.formData.isbn,
          publisher: props.formData.publisher,
          publishYear: props.formData.publishYear,
          problemType: props.formData.problemType,
          source: props.formData.source
        })
      } else {
        // 新增模式：重置表单
        resetForm()
      }
    })
  }
})

/**
 * 监听对话框显示状态变化
 */
watch(dialogVisible, (val) => {
  emit('update:visible', val)
})

/**
 * 重置表单
 */
const resetForm = () => {
  Object.assign(form, {
    bookId: null,
    bookName: '',
    author: '',
    isbn: '',
    publisher: '',
    publishYear: '',
    problemType: '',
    source: ''
  })
  formRef.value?.clearValidate()
}

/**
 * 关闭对话框
 */
const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  // 表单验证
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitLoading.value = true
  try {
    let response
    if (props.isEdit) {
      // 编辑问题书目
      const updateData = {
        bookId: form.bookId,
        bookName: form.bookName,
        author: form.author,
        isbn: form.isbn,
        publisher: form.publisher,
        publishYear: form.publishYear,
        problemType: form.problemType,
        source: form.source
      }
      response = await updateProblemBook(updateData)
    } else {
      // 新增问题书目
      const createData = {
        bookName: form.bookName,
        author: form.author,
        isbn: form.isbn,
        publisher: form.publisher,
        publishYear: form.publishYear,
        problemType: form.problemType,
        source: form.source
      }
      response = await createProblemBook(createData)
    }

    if (response.code === 200) {
      ElMessage.success(props.isEdit ? '修改成功' : '新增成功')
      emit('success')
      handleClose()
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
/* 样式可根据需要调整 */
</style>
