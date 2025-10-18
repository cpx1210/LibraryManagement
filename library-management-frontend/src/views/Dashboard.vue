<template>
  <div class="dashboard-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统首页</h2>
    </div>

    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎回来，{{ userStore.realName }}</span>
        </div>
      </template>

      <div class="user-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">
            {{ userStore.username }}
          </el-descriptions-item>
          <el-descriptions-item label="真实姓名">
            {{ userStore.realName }}
          </el-descriptions-item>
          <el-descriptions-item label="用户角色">
            <el-tag :type="userStore.isAdmin ? 'danger' : 'primary'">
              {{ getRoleText(userStore.role) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="部门">
            {{ userStore.userInfo.department || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="账号状态" :span="2">
            <el-tag :type="userStore.userInfo.isActive ? 'success' : 'danger'">
              {{ userStore.userInfo.isActive ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 快速操作 -->
    <el-card class="quick-actions-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>快速操作</span>
        </div>
      </template>

      <el-space wrap :size="15">
        <el-button type="primary" icon="User" @click="goToUsers">
          用户管理
        </el-button>
        <el-button type="success" icon="Reading">
          图书管理
        </el-button>
        <el-button type="warning" icon="DocumentCopy">
          书单检测
        </el-button>
        <el-button type="info" icon="DataAnalysis">
          数据统计
        </el-button>
      </el-space>

      <el-alert
        title="提示"
        type="info"
        description="完整的功能模块正在开发中，敬请期待..."
        :closable="false"
        style="margin-top: 20px;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

/**
 * 获取角色文本
 */
const getRoleText = (role) => {
  const roleMap = {
    'admin': '管理员',
    'user': '普通用户'
  }
  return roleMap[role] || role
}

/**
 * 跳转到用户管理
 */
const goToUsers = () => {
  router.push('/users')
}
</script>

<style scoped>
.dashboard-container {
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

.card-header {
  font-size: 16px;
  font-weight: 500;
}

.user-info {
  margin: 20px 0;
}
</style>
