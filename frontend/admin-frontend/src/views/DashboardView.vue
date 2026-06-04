<template>
  <div class="dashboard">
    <!-- Welcome Row -->
    <div class="welcome-row">
      <div>
        <h2 class="welcome-title">{{ timeGreeting }}，{{ userStore.user?.nickname || '管理员' }}</h2>
        <p class="welcome-sub">今天是 {{ today }}，以下是今日经营概览</p>
      </div>
    </div>

    <!-- Stat Cards -->
    <div class="stat-grid">
      <div class="stat-card" v-for="card in statCards" :key="card.label">
        <div class="stat-card-inner">
          <div class="stat-icon" :style="{ background: card.gradient }">
            <el-icon :size="22"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value">
              <span v-if="card.prefix" class="stat-prefix">{{ card.prefix }}</span>
              <span class="stat-number">{{ card.value }}</span>
            </div>
            <div class="stat-sub">{{ card.sub }}</div>
          </div>
        </div>
        <div class="stat-card-bg" :style="{ background: card.gradient }"></div>
      </div>
    </div>

    <!-- Bottom Grid -->
    <div class="bottom-grid">
      <!-- Recent Orders -->
      <div class="panel">
        <div class="panel-header">
          <h3>最近订单</h3>
          <el-button text type="primary" @click="goOrders">查看全部</el-button>
        </div>
        <el-table :data="recentOrders" style="width: 100%" :show-header="true" stripe>
          <el-table-column prop="orderNo" label="订单号" width="160" />
          <el-table-column label="金额" width="80">
            <template #default="{ row }">¥{{ row.payAmount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <span class="status-dot" :class="statusClass(row.status)"></span>
              {{ statusText(row.status) }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="140" />
        </el-table>
      </div>

      <!-- Quick Actions -->
      <div class="panel">
        <div class="panel-header">
          <h3>快捷操作</h3>
        </div>
        <div class="quick-actions">
          <div class="action-card" v-for="action in quickActions" :key="action.label" @click="action.click">
            <div class="action-icon" :style="{ background: action.color }">
              <el-icon :size="20"><component :is="action.icon" /></el-icon>
            </div>
            <div class="action-info">
              <div class="action-label">{{ action.label }}</div>
              <div class="action-desc">{{ action.desc }}</div>
            </div>
            <el-icon class="action-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getDashboardStats, getOrders } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()

const recentOrders = ref<any[]>([])

const stats = reactive({
  todayOrderCount: 0,
  todayRevenue: '0.00',
  pendingOrders: 0,
  todayNewUsers: 0
})

const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

const timeGreeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const statCards = computed(() => [
  {
    label: '今日订单', value: stats.todayOrderCount, sub: '较昨日 +12%',
    icon: 'Tickets', gradient: 'linear-gradient(135deg, #667eea, #764ba2)',
  },
  {
    label: '今日营收', value: stats.todayRevenue, prefix: '¥', sub: '实收金额',
    icon: 'Money', gradient: 'linear-gradient(135deg, #27ae60, #2ecc71)',
  },
  {
    label: '待处理', value: stats.pendingOrders, sub: '需要及时处理',
    icon: 'Clock', gradient: 'linear-gradient(135deg, #f39c12, #e67e22)',
  },
  {
    label: '新增用户', value: stats.todayNewUsers, sub: '今日注册用户',
    icon: 'User', gradient: 'linear-gradient(135deg, #e74c3c, #c0392b)',
  },
])

const quickActions = [
  { label: '添加菜品', desc: '新增菜单菜品', icon: 'Plus', color: 'linear-gradient(135deg, #E67E22, #F0A04B)', click: () => router.push('/dishes/create') },
  { label: '订单管理', desc: '查看和处理订单', icon: 'Tickets', color: 'linear-gradient(135deg, #3498db, #5dade2)', click: () => router.push('/orders') },
  { label: '分类管理', desc: '管理菜品分类', icon: 'Menu', color: 'linear-gradient(135deg, #27ae60, #58d68d)', click: () => router.push('/categories') },
]

function statusText(status: number): string {
  return ['待支付', '已确认', '制作中', '已完成', '已取消'][status] || '未知'
}

function statusClass(status: number): string {
  return ['s-warning', 's-success', 's-info', 's-primary', 's-danger'][status] || ''
}

function goOrders() { router.push('/orders') }

onMounted(async () => {
  try {
    const [statsRes, ordersRes] = await Promise.all([
      getDashboardStats(),
      getOrders({ page: 1, size: 5 })
    ])
    if (statsRes.data) Object.assign(stats, statsRes.data)
    if (ordersRes.data?.records) recentOrders.value = ordersRes.data.records
  } catch { /* ignore */ }
})
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 24px; }

.welcome-title { font-size: 22px; font-weight: 700; color: var(--text-primary); }
.welcome-sub { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }

/* ===== Stat Cards ===== */
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.stat-card {
  background: var(--bg-card); border-radius: var(--radius-lg);
  padding: 24px; position: relative; overflow: hidden;
  box-shadow: var(--shadow-card); cursor: pointer;
  transition: all var(--transition);
}
.stat-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-hover); }
.stat-card-bg {
  position: absolute; top: -30px; right: -30px;
  width: 120px; height: 120px; border-radius: 50%; opacity: 0.06;
  transition: all var(--transition);
}
.stat-card:hover .stat-card-bg { transform: scale(1.3); opacity: 0.10; }
.stat-card-inner { display: flex; align-items: flex-start; gap: 16px; position: relative; z-index: 1; }
.stat-icon {
  width: 48px; height: 48px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.stat-label { font-size: 13px; color: var(--text-secondary); margin-bottom: 4px; }
.stat-value { display: flex; align-items: baseline; gap: 2px; }
.stat-prefix { font-size: 16px; font-weight: 600; color: var(--text-primary); }
.stat-number { font-size: 28px; font-weight: 800; color: var(--text-primary); line-height: 1.2; }
.stat-sub { font-size: 11px; color: var(--text-muted); margin-top: 4px; }

/* ===== Bottom Grid ===== */
.bottom-grid { display: grid; grid-template-columns: 1.4fr 1fr; gap: 20px; }
.panel {
  background: var(--bg-card); border-radius: var(--radius-lg);
  padding: 20px 24px; box-shadow: var(--shadow-card);
}
.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px;
}
.panel-header h3 { font-size: 16px; font-weight: 700; color: var(--text-primary); }

.status-dot {
  display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 4px;
}
.s-warning { background: #f39c12; }
.s-success { background: #27ae60; }
.s-info { background: #3498db; }
.s-primary { background: #E67E22; }
.s-danger { background: #e74c3c; }

/* Quick Actions */
.quick-actions { display: flex; flex-direction: column; gap: 10px; }
.action-card {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 16px; border-radius: var(--radius);
  border: 1px solid var(--border-color); cursor: pointer;
  transition: all var(--transition-fast);
}
.action-card:hover {
  border-color: var(--primary-light); background: rgba(var(--primary-rgb), 0.03);
  transform: translateX(3px);
}
.action-icon {
  width: 42px; height: 42px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.action-label { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.action-desc { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
.action-arrow { color: var(--text-muted); margin-left: auto; transition: transform var(--transition-fast); }
.action-card:hover .action-arrow { transform: translateX(3px); color: var(--primary); }
</style>
