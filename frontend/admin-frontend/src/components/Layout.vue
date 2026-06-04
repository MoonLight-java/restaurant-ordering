<template>
  <el-container class="layout-container">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '64px' : '240px'" class="sidebar">
      <div class="logo-area">
        <div class="logo-icon">
          <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="12" fill="url(#logoGrad)"/>
            <path d="M12 18c0-1.5 1-3 2.5-3.5l5-1.5c1-.3 2-.3 3 0l5 1.5c1.5.5 2.5 2 2.5 3.5v8c0 2-1.5 3.5-3.5 3.5h-11C13.5 29.5 12 28 12 26v-8z" fill="white" opacity="0.95"/>
            <circle cx="20" cy="21" r="6" fill="url(#logoGrad)"/>
            <circle cx="20" cy="21" r="2" fill="white" opacity="0.8"/>
            <defs>
              <linearGradient id="logoGrad" x1="0" y1="0" x2="40" y2="40">
                <stop stop-color="#E67E22"/><stop offset="1" stop-color="#e74c3c"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <transition name="fade">
          <span v-show="!isCollapsed" class="logo-text">美味餐厅</span>
        </transition>
      </div>

      <div class="menu-wrapper">
        <div
          v-for="item in menuItems"
          :key="item.path"
          class="menu-item"
          :class="{ active: isActive(item.path) }"
          @click="navigate(item.path)"
        >
          <span class="menu-icon">
            <el-icon :size="20"><component :is="item.icon" /></el-icon>
          </span>
          <transition name="fade">
            <span v-show="!isCollapsed" class="menu-label">{{ item.label }}</span>
          </transition>
          <span v-if="isActive(item.path) && !isCollapsed" class="active-dot"></span>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="collapse-btn" @click="isCollapsed = !isCollapsed">
          <el-icon :size="18">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
      </div>
    </el-aside>

    <!-- Main Area -->
    <el-container class="main-container">
      <!-- Header -->
      <el-header class="top-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="header-time">{{ timeGreeting }}</div>
          <div class="user-menu" @click="toggleUserMenu">
            <div class="user-avatar">
              <span>{{ userInitial }}</span>
            </div>
            <span class="user-name">{{ userStore.user?.nickname || '管理员' }}</span>
            <el-icon :size="14" class="arrow-icon"><ArrowDown /></el-icon>
          </div>
          <div v-if="showUserMenu" class="user-dropdown" @click.stop>
            <div class="dropdown-item" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </div>
          </div>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="main-content">
        <router-view v-slot="{ Component, route }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)
const showUserMenu = ref(false)

const menuItems = [
  { path: '/dashboard', label: '仪表盘', icon: 'DataAnalysis' },
  { path: '/categories', label: '分类管理', icon: 'Menu' },
  { path: '/dishes', label: '菜品管理', icon: 'Food' },
  { path: '/orders', label: '订单管理', icon: 'Tickets' },
  { path: '/users', label: '用户管理', icon: 'User' },
]

function isActive(path: string) {
  if (path === '/dashboard') return route.path === '/dashboard'
  return route.path.startsWith(path)
}

function navigate(path: string) {
  router.push(path)
  showUserMenu.value = false
}

const currentTitle = computed(() => {
  const item = menuItems.find(m => route.path.startsWith(m.path) && m.path !== '/dashboard')
  return item?.label || ''
})

const userInitial = computed(() => {
  const name = userStore.user?.nickname || '管'
  return name.charAt(0)
})

const timeGreeting = ref('')

function updateGreeting() {
  const hour = new Date().getHours()
  if (hour < 6) timeGreeting.value = '夜深了 🌙'
  else if (hour < 12) timeGreeting.value = '早上好 ☀️'
  else if (hour < 14) timeGreeting.value = '中午好 🌤️'
  else if (hour < 18) timeGreeting.value = '下午好 🌈'
  else timeGreeting.value = '晚上好 🌆'
}

let greetTimer: number

onMounted(() => {
  updateGreeting()
  greetTimer = window.setInterval(updateGreeting, 60000)
  document.addEventListener('click', () => { showUserMenu.value = false })
})

onUnmounted(() => {
  clearInterval(greetTimer)
})

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function handleLogout() {
  userStore.logout()
}
</script>

<style scoped>
.layout-container { height: 100vh; overflow: hidden; }

/* ===== Sidebar ===== */
.sidebar {
  background: linear-gradient(180deg, var(--sidebar-bg-start) 0%, var(--sidebar-bg-end) 100%);
  display: flex; flex-direction: column;
  transition: width var(--transition);
  overflow: hidden; position: relative; z-index: 10;
  box-shadow: 2px 0 24px rgba(0,0,0,0.15);
}

.logo-area {
  height: 68px; display: flex; align-items: center; padding: 0 16px; gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.08); flex-shrink: 0;
}
.logo-icon svg { display: block; width: 36px; height: 36px; flex-shrink: 0; }
.logo-text {
  font-size: 18px; font-weight: 700;
  background: linear-gradient(135deg, #F0A04B, #e74c3c);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  white-space: nowrap;
}

.menu-wrapper { flex: 1; padding: 12px 8px; overflow-y: auto; display: flex; flex-direction: column; gap: 2px; }

.menu-item {
  display: flex; align-items: center; padding: 12px 16px; border-radius: 10px;
  cursor: pointer; transition: all var(--transition-fast);
  color: rgba(255,255,255,0.60); position: relative; gap: 12px;
  white-space: nowrap; user-select: none;
}
.menu-item:hover { color: rgba(255,255,255,0.90); background: rgba(255,255,255,0.06); }
.menu-item.active {
  color: #fff; background: rgba(var(--primary-rgb), 0.20);
  font-weight: 600;
}
.menu-icon { display: flex; align-items: center; justify-content: center; width: 24px; flex-shrink: 0; }
.menu-label { font-size: 14px; }
.active-dot {
  position: absolute; right: 12px; width: 6px; height: 6px; border-radius: 50%;
  background: var(--primary); box-shadow: 0 0 8px var(--primary);
}

.sidebar-footer {
  padding: 12px; border-top: 1px solid rgba(255,255,255,0.08); flex-shrink: 0;
}
.collapse-btn {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border-radius: 8px;
  color: rgba(255,255,255,0.45); cursor: pointer;
  transition: all var(--transition-fast);
}
.collapse-btn:hover { color: #fff; background: rgba(255,255,255,0.08); }

/* ===== Main Area ===== */
.main-container { flex-direction: column; overflow: hidden; }

.top-header {
  height: var(--header-height); background: rgba(255,255,255,0.85);
  backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; flex-shrink: 0; z-index: 5;
}

.header-left :deep(.el-breadcrumb__item) { font-size: 13px; }
.header-left :deep(.el-breadcrumb__inner) { color: var(--text-secondary); font-weight: 400; }
.header-left :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-primary); font-weight: 600;
}

.header-right { display: flex; align-items: center; gap: 20px; position: relative; }
.header-time { font-size: 13px; color: var(--text-secondary); }

.user-menu {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 6px 12px 6px 6px; border-radius: 24px;
  transition: all var(--transition-fast);
  background: var(--bg-page);
}
.user-menu:hover { background: #e8e6e1; }
.user-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), #e74c3c);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-weight: 700; font-size: 14px;
}
.user-name { font-size: 13px; color: var(--text-primary); font-weight: 500; }
.arrow-icon { color: var(--text-muted); transition: transform var(--transition-fast); }

.user-dropdown {
  position: absolute; top: calc(100% + 8px); right: 0; width: 160px;
  background: #fff; border-radius: var(--radius);
  box-shadow: var(--shadow-hover); border: 1px solid var(--border-color);
  padding: 6px; z-index: 100; animation: dropdownIn 0.15s ease;
}
@keyframes dropdownIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}
.dropdown-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 12px;
  border-radius: var(--radius-sm); cursor: pointer; font-size: 13px;
  color: #e74c3c; transition: background var(--transition-fast);
}
.dropdown-item:hover { background: #fef2f2; }

.main-content {
  background: var(--bg-page); padding: 24px; overflow-y: auto; flex: 1;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
