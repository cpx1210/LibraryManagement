<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
      <div class="logo">
        <span v-if="!isCollapse">图书馆管理</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="layout-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="dictionary">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>词库管理</span>
          </template>
          <el-menu-item index="/sensitive-words">
            <el-icon><Warning /></el-icon>
            <template #title>敏感词库</template>
          </el-menu-item>
          <el-menu-item index="/problem-books">
            <el-icon><DocumentDelete /></el-icon>
            <template #title>问题书目库</template>
          </el-menu-item>
          <el-menu-item index="/publisher-whitelist">
            <el-icon><DocumentChecked /></el-icon>
            <template #title>出版社白名单</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="detection">
          <template #title>
            <el-icon><Search /></el-icon>
            <span>书单检测</span>
          </template>
          <el-menu-item index="/detection/check">
            <el-icon><Upload /></el-icon>
            <template #title>检测书单</template>
          </el-menu-item>
          <el-menu-item index="/detection/history">
            <el-icon><Clock /></el-icon>
            <template #title>检测历史</template>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="collection">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>馆藏图书</span>
          </template>
          <el-menu-item index="/collection-books">
            <el-icon><Notebook /></el-icon>
            <template #title>馆藏图书</template>
          </el-menu-item>
          <el-menu-item index="/collection-books/problem">
            <el-icon><DocumentDelete /></el-icon>
            <template #title>馆藏问题图书</template>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/logs">
          <el-icon><Document /></el-icon>
          <template #title>操作日志</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" style="background-color: #409eff;">
                {{ userStore.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.realName || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <el-tag :type="userStore.role === 'admin' ? 'danger' : 'primary'" size="small">
                    {{ userStore.role === 'admin' ? '管理员' : '普通用户' }}
                  </el-tag>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  Setting,
  User,
  Collection,
  Warning,
  DocumentDelete,
  DocumentChecked,
  Search,
  Upload,
  Clock,
  Reading,
  Document,
  Fold,
  Expand,
  ArrowDown,
  SwitchButton,
  Notebook
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前路由
const currentRoute = computed(() => route)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

/**
 * 切换侧边栏折叠状态
 */
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

/**
 * 处理下拉菜单命令
 */
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      // 执行登出
      await userStore.logout()
    } catch (error) {
      // 用户取消了操作
      if (error !== 'cancel') {
        console.error('登出失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* 侧边栏 */
.layout-aside {
  background: #001529;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
}

.layout-menu {
  border: none;
  background: #001529;
  --el-menu-text-color: #ffffff;
  --el-menu-hover-text-color: #ffffff;
  --el-menu-bg-color: #001529;
  --el-menu-hover-bg-color: #1890ff;
  --el-menu-active-color: #ffffff;
}

.layout-menu:not(.el-menu--collapse) {
  width: 200px;
}

/* 顶部导航栏 */
.layout-header {
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s;
}

.collapse-icon:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 用户下拉菜单 */
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

/* 主内容区 */
.layout-main {
  background: #f0f2f5;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .username {
    display: none;
  }

  .layout-header {
    padding: 0 16px;
  }
}
</style>
