<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑敏感词' : '新增敏感词'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <!-- 敏感词内容 -->
      <el-form-item label="敏感词内容" prop="keyword">
        <el-input
          v-model="form.keyword"
          placeholder="请输入敏感词内容"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <!-- 敏感词类别 -->
      <el-form-item label="敏感词类别" prop="categoryId">
        <el-select
            v-model="form.categoryId"
            placeholder="请选择敏感词类别"
            style="width: 100%"
            filterable
        >
          <el-option
              v-for="category in categoryList"
              :key="category.categoryId"
              :label="category.categoryName"
              :value="category.categoryId"
          />
        </el-select>
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
import { ref, reactive, watch, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createSensitiveWord, updateSensitiveWord, getSensitiveWordCategories } from '@/api/sensitiveWord'

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
// 敏感词类别列表
const categoryList = ref([])
// 表单数据
const form = reactive({
  wordId: null,
  keyword: '',
  categoryId: null
})

// 提交加载状态
const submitLoading = ref(false)

// 表单验证规则
const rules = {
  keyword: [
    { required: true, message:
          '请输入敏感词内容', trigger: 'blur' },
    { min: 1, max: 100, message:
          '敏感词内容长度在1-100个字符', trigger:
          'blur' }
  ],
  categoryId: [
    { required: true, message:
          '请选择敏感词类别', trigger: 'change' }
  ]
}
/**
 * 加载分类列表
 */
const loadCategories = async () => {
  try {
    const response = await getSensitiveWordCategories()
    if (response.code === 200) {
      console.log('✅ 分类列表:', categoryList.value)
      categoryList.value = response.data
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('加载分类列表失败')
  }
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
          wordId: props.formData.wordId,
          keyword: props.formData.keyword,
          categoryId: props.formData.categoryId
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
    wordId: null,
    keyword: '',
    categoryId: null
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
      // 编辑敏感词
      const updateData = {
        wordId: form.wordId,
        keyword: form.keyword,
        categoryId: form.categoryId
      }
      response = await updateSensitiveWord(updateData)
    } else {
      // 新增敏感词
      console.log('📝 当前表单数据:', form)
      console.log('📝 form.categoryId 类型:', typeof form.categoryId)
      const createData = {
        keyword: form.keyword,
        categoryId: form.categoryId
      }
      console.log('📤 准备提交的数据:', createData)
      response = await createSensitiveWord(createData)
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
// 组件挂载时加载分类列表
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
/* 样式可根据需要调整 */
</style>
