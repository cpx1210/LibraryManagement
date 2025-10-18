<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <!-- 用户名 -->
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="form.username"
          placeholder="请输入用户名"
          :disabled="isEdit"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 密码（仅新增时显示） -->
      <el-form-item v-if="!isEdit" label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          show-password
          maxlength="50"
        />
      </el-form-item>

      <!-- 真实姓名 -->
      <el-form-item label="真实姓名" prop="realName">
        <el-input
          v-model="form.realName"
          placeholder="请输入真实姓名"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 工号 -->
      <el-form-item label="工号" prop="employeeId">
        <el-input
          v-model="form.employeeId"
          placeholder="请输入工号"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 角色 -->
      <el-form-item label="角色" prop="role">
        <el-select
          v-model="form.role"
          placeholder="请选择角色"
          style="width: 100%"
        >
          <el-option label="管理员" value="admin" />
          <el-option label="普通用户" value="user" />
        </el-select>
      </el-form-item>

      <!-- 部门 -->
      <el-form-item label="部门" prop="department">
        <el-input
          v-model="form.department"
          placeholder="请输入部门"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <!-- 状态（仅编辑时显示） -->
      <el-form-item v-if="isEdit" label="状态" prop="isActive">
        <el-radio-group v-model="form.isActive">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
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
import { createUser, updateUser } from '@/api/user'

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
  userId: null,
  username: '',
  password: '',
  realName: '',
  employeeId: '',
  role: 'user',
  department: '',
  isActive: 1
})

// 提交加载状态
const submitLoading = ref(false)

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度在6-50个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 50, message: '真实姓名不能超过50个字符', trigger: 'blur' }
  ],
  employeeId: [
    { max: 50, message: '工号不能超过50个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  department: [
    { max: 100, message: '部门不能超过100个字符', trigger: 'blur' }
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
          userId: props.formData.userId,
          username: props.formData.username,
          realName: props.formData.realName,
          employeeId: props.formData.employeeId || '',
          role: props.formData.role,
          department: props.formData.department || '',
          isActive: props.formData.isActive
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
    userId: null,
    username: '',
    password: '',
    realName: '',
    employeeId: '',
    role: 'user',
    department: '',
    isActive: 1
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
      // 编辑用户
      const updateData = {
        userId: form.userId,
        realName: form.realName,
        employeeId: form.employeeId,
        role: form.role,
        department: form.department,
        isActive: form.isActive
      }
      response = await updateUser(updateData)
    } else {
      // 新增用户
      const createData = {
        username: form.username,
        password: form.password,
        realName: form.realName,
        employeeId: form.employeeId,
        role: form.role,
        department: form.department
      }
      response = await createUser(createData)
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
