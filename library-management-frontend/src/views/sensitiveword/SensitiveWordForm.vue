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

      <!-- 敏感词类别（检测类型） -->
      <el-form-item label="敏感词类别" prop="detectionType">
        <el-select
          v-model="form.detectionType"
          placeholder="请选择敏感词类别"
          style="width: 100%"
        >
          <el-option label="关键词（全局检测）" value="关键词" />
          <el-option label="作者（仅检测著者字段）" value="作者" />
          <el-option label="书名（仅检测题名字段）" value="书名" />
        </el-select>
        <div class="form-tip">
          <el-text type="info" size="small">
            关键词：在书名、作者、内容简介等所有字段中检测；
            作者：仅在著者字段中检测；
            书名：仅在题名字段中检测
          </el-text>
        </div>
      </el-form-item>

      <!-- 风险等级 -->
      <el-form-item label="风险等级" prop="riskLevel">
        <el-select
          v-model="form.riskLevel"
          placeholder="请选择风险等级"
          style="width: 100%"
        >
          <el-option label="低风险" :value="1">
            <span style="display: flex; align-items: center;">
              <el-tag type="success" effect="light" size="small" style="margin-right: 8px;">低风险</el-tag>
              <span style="color: #606266; font-size: 12px;">对内容影响较小</span>
            </span>
          </el-option>
          <el-option label="中风险" :value="2">
            <span style="display: flex; align-items: center;">
              <el-tag type="warning" effect="light" size="small" style="margin-right: 8px;">中风险</el-tag>
              <span style="color: #606266; font-size: 12px;">需要注意审核</span>
            </span>
          </el-option>
          <el-option label="高风险" :value="3">
            <span style="display: flex; align-items: center;">
              <el-tag type="danger" effect="light" size="small" style="margin-right: 8px;">高风险</el-tag>
              <span style="color: #606266; font-size: 12px;">严重问题，需立即处理</span>
            </span>
          </el-option>
        </el-select>
        <div class="form-tip">
          <el-text type="info" size="small">
            命中敏感词时，会根据风险等级在检测结果中显示不同的警告级别
          </el-text>
        </div>
      </el-form-item>

      <!-- 警报信息 -->
      <el-form-item label="警报信息" prop="alertMessage">
        <el-input
          v-model="form.alertMessage"
          type="textarea"
          placeholder="请输入警报信息（可选），如：疑似敏感政治内容"
          maxlength="200"
          show-word-limit
          :rows="3"
        />
        <div class="form-tip">
          <el-text type="info" size="small">
            命中敏感词时显示的提示信息，用于说明问题原因
          </el-text>
        </div>
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
import { createSensitiveWord, updateSensitiveWord } from '@/api/sensitiveWord'

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
  wordId: null,
  keyword: '',
  detectionType: '关键词',
  riskLevel: 2, // 默认中风险
  alertMessage: ''
})

// 提交加载状态
const submitLoading = ref(false)

// 表单验证规则
const rules = {
  keyword: [
    { required: true, message: '请输入敏感词内容', trigger: 'blur' },
    { min: 1, max: 100, message: '敏感词内容长度在1-100个字符', trigger: 'blur' }
  ],
  detectionType: [
    { required: true, message: '请选择敏感词类别', trigger: 'change' }
  ],
  riskLevel: [
    { required: true, message: '请选择风险等级', trigger: 'change' }
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
          wordId: props.formData.wordId,
          keyword: props.formData.keyword,
          detectionType: props.formData.detectionType || '关键词',
          riskLevel: props.formData.riskLevel || 2,
          alertMessage: props.formData.alertMessage || ''
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
    detectionType: '关键词',
    riskLevel: 2,
    alertMessage: ''
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
        detectionType: form.detectionType,
        riskLevel: form.riskLevel,
        alertMessage: form.alertMessage
      }
      response = await updateSensitiveWord(updateData)
    } else {
      // 新增敏感词
      const createData = {
        keyword: form.keyword,
        detectionType: form.detectionType,
        riskLevel: form.riskLevel,
        alertMessage: form.alertMessage
      }
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
</script>

<style scoped>
.form-tip {
  margin-top: 4px;
  line-height: 1.4;
}
</style>
