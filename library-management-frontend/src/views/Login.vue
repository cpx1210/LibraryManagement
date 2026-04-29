<template>
  <div class="login-container">
    <!-- 登录卡片 -->
    <el-card class="login-card" shadow="always">
      <!-- 标题 -->
      <div class="login-header">
        <h1>图书馆管理系统</h1>
        <p>Library Management System</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <!-- 用户名输入框 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 用户 store
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref(null)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 加载状态
const loading = ref(false)

/**
 * 处理登录
 */
const handleLogin = async () => {
  // 表单验证
  if (!loginFormRef.value) return

  try {
    // 验证表单
    await loginFormRef.value.validate()

    // 显示加载状态
    loading.value = true

    // 调用登录接口（通过 Pinia store）
    await userStore.login(loginForm)

    // 登录成功会自动跳转到首页（在 store 中处理）
  } catch (error) {
    // 表单验证失败或登录失败
    if (error.errors) {
      // 表单验证失败
      ElMessage.warning('请检查输入的信息')
    } else {
      // 登录失败（API 错误在 store 和 request.js 中已处理）
      console.error('登录失败:', error)
    }
  } finally {
    // 隐藏加载状态
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: white;
  padding: 20px;
}

/* 登录卡片 */
.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
}

/* 登录头部 */
.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: #909399;
}

/* 登录表单 */
.login-form {
  padding: 0 20px;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  margin-top: 10px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    max-width: 100%;
  }

  .login-header h1 {
    font-size: 24px;
  }
}
</style>
