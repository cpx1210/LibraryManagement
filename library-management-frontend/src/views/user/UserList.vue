<template>
  <div class="user-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="queryForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input
            v-model="queryForm.realName"
            placeholder="请输入真实姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="queryForm.role"
            placeholder="请选择角色"
            clearable
            style="width: 150px"
          >
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-input
            v-model="queryForm.department"
            placeholder="请输入部门"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.isActive"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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

    <!-- 操作按钮 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd" :icon="Plus">
          新增用户
        </el-button>
      </div>

      <!-- 用户表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="employeeId" label="工号" width="120" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'admin'" type="danger">管理员</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="150" />
        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isActive === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="lastLoginTime" label="最后登录" width="180" />
        <el-table-column label="操作" width="280" fixed="right" align="center">
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
              type="warning"
              size="small"
              link
              @click="handleResetPassword(row)"
              :icon="Key"
            >
              重置密码
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
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户表单对话框 -->
    <UserForm
      v-model:visible="dialogVisible"
      :form-data="currentUser"
      :is-edit="isEdit"
      @success="handleSuccess"
    />

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="400px"
    >
      <el-form :model="resetPasswordForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="currentUser.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="currentUser.realName" disabled />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPassword" :loading="resetPasswordLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Key } from '@element-plus/icons-vue'
import { getUserList, deleteUser, resetPassword } from '@/api/user'
import UserForm from './UserForm.vue'

// 查询表单
const queryForm = reactive({
  page: 1,
  size: 10,
  username: '',
  realName: '',
  role: '',
  department: '',
  isActive: null
})

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentUser = ref({})

// 重置密码对话框
const resetPasswordDialogVisible = ref(false)
const resetPasswordForm = reactive({
  userId: null,
  newPassword: ''
})
const resetPasswordLoading = ref(false)

/**
 * 加载用户列表
 */
const loadUserList = async () => {
  loading.value = true
  try {
    const response = await getUserList(queryForm)
    if (response.code === 200) {
      tableData.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 查询按钮点击
 */
const handleQuery = () => {
  queryForm.page = 1
  loadUserList()
}

/**
 * 重置按钮点击
 */
const handleReset = () => {
  queryForm.page = 1
  queryForm.size = 10
  queryForm.username = ''
  queryForm.realName = ''
  queryForm.role = ''
  queryForm.department = ''
  queryForm.isActive = null
  loadUserList()
}

/**
 * 新增用户
 */
const handleAdd = () => {
  isEdit.value = false
  currentUser.value = {}
  dialogVisible.value = true
}

/**
 * 编辑用户
 */
const handleEdit = (row) => {
  isEdit.value = true
  currentUser.value = { ...row }
  dialogVisible.value = true
}

/**
 * 删除用户
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.realName}"(${row.username})吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await deleteUser(row.userId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadUserList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
    }
  }
}

/**
 * 重置密码
 */
const handleResetPassword = (row) => {
  currentUser.value = { ...row }
  resetPasswordForm.userId = row.userId
  resetPasswordForm.newPassword = ''
  resetPasswordDialogVisible.value = true
}

/**
 * 确认重置密码
 */
const confirmResetPassword = async () => {
  if (!resetPasswordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }

  resetPasswordLoading.value = true
  try {
    const response = await resetPassword(resetPasswordForm)
    if (response.code === 200) {
      ElMessage.success('密码重置成功')
      resetPasswordDialogVisible.value = false
      resetPasswordForm.newPassword = ''
    }
  } catch (error) {
    console.error('重置密码失败:', error)
  } finally {
    resetPasswordLoading.value = false
  }
}

/**
 * 表单操作成功回调
 */
const handleSuccess = () => {
  dialogVisible.value = false
  loadUserList()
}

/**
 * 每页数量变化
 */
const handleSizeChange = () => {
  loadUserList()
}

/**
 * 当前页变化
 */
const handleCurrentChange = () => {
  loadUserList()
}

// 页面加载时获取数据
onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-list-container {
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
</style>
