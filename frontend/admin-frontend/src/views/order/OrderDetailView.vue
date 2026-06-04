<template>
  <div class="page">
    <el-button round @click="goBack" class="back-btn">
      <el-icon><ArrowLeft /></el-icon>返回列表
    </el-button>

    <div class="page-card" v-if="order">
      <div class="page-header">
        <div class="page-title">
          <span class="order-no">{{ order.orderNo }}</span>
          <span class="status-badge" :class="'status-' + order.status">
            <span class="status-dot"></span>{{ statusText }}
          </span>
        </div>
        <div class="header-actions">
          <el-button v-if="order.status === 1" type="success" round @click="changeStatus(2)">开始制作</el-button>
          <el-button v-if="order.status === 2" type="primary" round @click="changeStatus(3)">完成出餐</el-button>
          <el-button v-if="order.status < 3 && order.status !== 4" type="danger" round plain @click="cancelDialogVisible = true">取消订单</el-button>
        </div>
      </div>

      <div class="info-section">
        <h4>基本信息</h4>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ order.userId }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="总金额"><span class="price-text">¥{{ order.totalAmount }}</span></el-descriptions-item>
          <el-descriptions-item label="实付金额"><span class="price-text">¥{{ order.payAmount }}</span></el-descriptions-item>
          <el-descriptions-item label="备注">{{ order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="info-section" v-if="address">
        <h4>地址信息</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="联系人">{{ address.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ address.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ address.province }}{{ address.city }}{{ address.district }} {{ address.detailAddress }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="info-section">
        <h4>菜品明细</h4>
        <el-table :data="items" stripe class="data-table">
          <el-table-column label="图片" width="80">
            <template #default="{ row }">
              <div class="thumb-wrapper">
                <el-image :src="row.dishImage || '/placeholder.png'" class="item-thumb" fit="cover" />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="dishName" label="菜品名称" />
          <el-table-column label="规格" min-width="140">
            <template #default="{ row }">
              <span v-if="row.specJson" class="spec-text">{{ formatSpec(row.specJson) }}</span>
              <span v-else class="no-spec">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column label="单价" width="100">
            <template #default="{ row }">¥{{ row.unitPrice }}</template>
          </el-table-column>
          <el-table-column label="小计" width="100">
            <template #default="{ row }"><span class="price-text">¥{{ row.subTotal }}</span></template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="cancelDialogVisible" title="取消订单" width="420px" class="styled-dialog">
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因">
          <el-input v-model="cancelForm.reason" type="textarea" :rows="3" placeholder="请输入取消原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false" round>返回</el-button>
        <el-button type="danger" @click="handleCancel" round>确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, changeOrderStatus } from '@/api/order'

const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id as string)
const order = ref<any>(null)
const items = ref<any[]>([])
const address = ref<any>(null)
const cancelDialogVisible = ref(false)
const cancelForm = ref({ reason: '' })

const statusText = computed(() => {
  if (!order.value) return ''
  return ['待支付', '已确认', '制作中', '已完成', '已取消'][order.value.status]
})

onMounted(async () => {
  const res = await getOrderDetail(id.value)
  if (res.data) {
    order.value = res.data.order || res.data
    items.value = res.data.items || []
    if (order.value && order.value.addressJson) {
      try { address.value = JSON.parse(order.value.addressJson) } catch { address.value = null }
    }
  }
})

function goBack() { router.back() }

async function changeStatus(newStatus: number) {
  await changeOrderStatus(id.value, { orderId: id.value, newStatus })
  ElMessage.success('状态更新成功')
  const res = await getOrderDetail(id.value)
  if (res.data) order.value = res.data.order || res.data
}

async function handleCancel() {
  await changeOrderStatus(id.value, { orderId: id.value, newStatus: 4, cancelReason: cancelForm.value.reason })
  ElMessage.success('订单已取消')
  cancelDialogVisible.value = false
  const res = await getOrderDetail(id.value)
  if (res.data) order.value = res.data.order || res.data
}

function formatSpec(specJson: string): string {
  try {
    const specs = JSON.parse(specJson)
    if (Array.isArray(specs)) {
      return specs.map((s: any) => `${s.groupName || ''}:${s.itemName || ''}`).join(', ')
    }
    return specJson
  } catch { return specJson }
}
</script>

<style scoped>
.back-btn { margin-bottom: 16px; }

.page-card {
  background: var(--bg-card); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card); padding: 24px;
}
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px; flex-wrap: wrap; gap: 12px;
}
.page-title { display: flex; align-items: center; gap: 12px; }
.order-no { font-size: 18px; font-weight: 700; color: var(--text-primary); }
.header-actions { display: flex; gap: 8px; }

.status-badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 500;
}
.status-dot { width: 6px; height: 6px; border-radius: 50%; }
.status-0 { background: #fef9e7; color: #b7950b; } .status-0 .status-dot { background: #f1c40f; }
.status-1 { background: #eaf7ee; color: #1e8449; } .status-1 .status-dot { background: #27ae60; }
.status-2 { background: #edf2fd; color: #2471a3; } .status-2 .status-dot { background: #3498db; }
.status-3 { background: #f5f3ef; color: #616a6b; } .status-3 .status-dot { background: #95a5a6; }
.status-4 { background: #fdedec; color: #b03a2e; } .status-4 .status-dot { background: #e74c3c; }

.info-section { margin-bottom: 24px; }
.info-section h4 { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 12px; }

.price-text { font-weight: 600; color: #e74c3c; }

.data-table { border-radius: var(--radius); overflow: hidden; }
.data-table :deep(.el-table__row:hover) { background: rgba(var(--primary-rgb), 0.03); }
.thumb-wrapper { width: 50px; height: 50px; border-radius: 8px; overflow: hidden; }
.item-thumb { width: 50px; height: 50px; display: block; }
.spec-text { font-size: 12px; color: var(--text-secondary); }
.no-spec { color: var(--text-muted); }
</style>
