<template>
  <router-view v-slot="{ Component, route }">
    <transition name="page-fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
</script>

<style>
:root {
  --primary: #E67E22;
  --primary-light: #F0A04B;
  --primary-dark: #D35400;
  --primary-rgb: 230, 126, 34;
  --sidebar-bg-start: #1a1a2e;
  --sidebar-bg-end: #16213e;
  --sidebar-width: 240px;
  --sidebar-collapsed: 64px;
  --header-height: 60px;
  --bg-page: #f5f3ef;
  --bg-card: #ffffff;
  --text-primary: #1e1e2d;
  --text-secondary: #7f8c8d;
  --text-muted: #b0b8c1;
  --border-color: #eef0f2;
  --shadow-sm: 0 1px 4px rgba(0,0,0,0.04);
  --shadow-card: 0 2px 12px rgba(0,0,0,0.06);
  --shadow-hover: 0 8px 28px rgba(0,0,0,0.10);
  --shadow-lg: 0 16px 48px rgba(0,0,0,0.12);
  --radius-sm: 8px;
  --radius: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
  --transition: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-fast: 0.15s cubic-bezier(0.4, 0, 0.2, 1);

  --el-color-primary: var(--primary);
  --el-color-primary-light-3: var(--primary-light);
  --el-color-primary-dark-2: var(--primary-dark);
  --el-border-radius-base: 8px;
  --el-border-radius-round: 20px;
}

* { margin: 0; padding: 0; box-sizing: border-box; }

html, body {
  font-family: 'Inter', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-page);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #d0d5dd; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #b0b8c1; }

.page-fade-enter-active, .page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.page-fade-enter-from { opacity: 0; transform: translateY(6px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-6px); }
</style>
