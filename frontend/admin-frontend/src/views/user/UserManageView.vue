<template>
  <div class="page">
    <div class="page-card">
      <div class="page-header">
        <div class="page-title">
          <el-icon :size="18"><User /></el-icon>
          <span>用户管理</span>
        </div>
        <el-input v-model="keyword" placeholder="搜索用户..." clearable class="search-input" @change="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <el-table :data="users" stripe class="data-table">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column label="用户" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-avatar-sm">
                <span>{{ row.nickname?.charAt(0) || '用' }}</span>
              </div>
              <span class="user-nickname">{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <span class="role-tag" :class="'role-' + row.role">{{ roleText(row.role) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status === 1 ? 'enabled' : 'disabled'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="170" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              size="small" type="danger" round plain
              @click="toggleStatus(row, 0)"
            >禁用</el-button>
            <el-button
              v-else
              size="small" type="success" round
              @click="toggleStatus(row, 1)"
            >启用</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUsers, updateUserStatus } from '@/api/user'

const users = ref<any[]>([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(loadData)

async function loadData() {
  const res = await getUsers({ keyword: keyword.value, page: page.value, size: size.value })
  if (res.data) {
    users.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

function roleText(role: number): string {
  return ['普通用户', '管理员', '超级管理员'][role] || '未知'
}

async function toggleStatus(row: any, newStatus: number) {
  await updateUserStatus(row.id, newStatus)
  ElMessage.success('操作成功')
  loadData()
}
</script>

<style scoped>
.page-card {
  background: var(--bg-card); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card); padding: 24px;
}
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
}
.page-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: var(--text-primary); }
.search-input { width: 240px; }
.search-input :deep(.el-input__wrapper) { border-radius: 24px; }

.data-table { border-radius: var(--radius); overflow: hidden; }
.data-table :deep(.el-table__row:hover) { background: rgba(var(--primary-rgb), 0.03); }

.user-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar-sm {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-light), #e74c3c);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 13px; font-weight: 600;
}
.user-nickname { font-weight: 500; }

.role-tag {
  display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 12px;
}
.role-0 { background: #f5f3ef; color: var(--text-secondary); }
.role-1 { background: #fef3e2; color: #E67E22; }
.role-2 { background: #fdedec; color: #e74c3c; }

.status-badge {
  display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 12px;
}
.enabled { background: #eaf7ee; color: #27ae60; }
.disabled { background: #f5f3ef; color: var(--text-muted); }

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
