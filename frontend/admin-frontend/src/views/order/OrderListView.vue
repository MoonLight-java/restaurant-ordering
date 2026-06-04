<template>
  <div class="page">
    <div class="page-card">
      <div class="page-header">
        <div class="page-title">
          <el-icon :size="18"><Tickets /></el-icon>
          <span>订单管理</span>
        </div>
        <div class="header-right">
          <el-input v-model="orderNo" placeholder="搜索订单号..." clearable class="search-input" @change="loadData">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="statusFilter" placeholder="全部状态" clearable @change="loadData">
            <el-option :value="0" label="待支付" />
            <el-option :value="1" label="已确认" />
            <el-option :value="2" label="制作中" />
            <el-option :value="3" label="已完成" />
            <el-option :value="4" label="已取消" />
          </el-select>
        </div>
      </div>

      <el-table :data="orders" stripe class="data-table">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column label="实付金额" width="110">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.payAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="status-badge" :class="'status-' + row.status">
              <span class="status-dot"></span>
              {{ statusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="160" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button size="small" round @click="goDetail(row.id)">详情</el-button>
            <el-button v-if="row.status === 1" size="small" type="success" round @click="changeStatus(row, 2)">制作</el-button>
            <el-button v-if="row.status === 2" size="small" type="primary" round @click="changeStatus(row, 3)">出餐</el-button>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrders, changeOrderStatus } from '@/api/order'

const router = useRouter()
const orders = ref<any[]>([])
const orderNo = ref('')
const statusFilter = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(loadData)

async function loadData() {
  const params: any = { page: page.value, size: size.value }
  if (orderNo.value) params.orderNo = orderNo.value
  if (statusFilter.value !== null && statusFilter.value !== undefined) params.status = statusFilter.value
  const res = await getOrders(params)
  if (res.data) {
    orders.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

function statusText(status: number): string {
  return ['待支付', '已确认', '制作中', '已完成', '已取消'][status] || '未知'
}

function goDetail(id: string) { router.push(`/orders/${id}`) }

async function changeStatus(row: any, newStatus: number) {
  await changeOrderStatus(row.id, { orderId: row.id, newStatus })
  ElMessage.success('状态更新成功')
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
  flex-wrap: wrap; gap: 12px;
}
.page-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: var(--text-primary); }
.header-right { display: flex; gap: 12px; align-items: center; }
.search-input { width: 220px; }
.search-input :deep(.el-input__wrapper) { border-radius: 24px; }

.data-table { border-radius: var(--radius); overflow: hidden; }
.data-table :deep(.el-table__row:hover) { background: rgba(var(--primary-rgb), 0.03); }

.price-text { font-weight: 600; color: #e74c3c; }

.status-badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 500;
}
.status-dot { width: 6px; height: 6px; border-radius: 50%; }
.status-0 { background: #fef9e7; color: #b7950b; } .status-0 .status-dot { background: #f1c40f; }
.status-1 { background: #eaf7ee; color: #1e8449; } .status-1 .status-dot { background: #27ae60; }
.status-2 { background: #edf2fd; color: #2471a3; } .status-2 .status-dot { background: #3498db; }
.status-3 { background: #f5f3ef; color: #616a6b; } .status-3 .status-dot { background: #95a5a6; }
.status-4 { background: #fdedec; color: #b03a2e; } .status-4 .status-dot { background: #e74c3c; }

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
