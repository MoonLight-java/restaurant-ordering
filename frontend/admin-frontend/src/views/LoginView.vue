<template>
  <div class="login-page">
    <!-- Left: Brand Panel -->
    <div class="brand-panel">
      <div class="brand-overlay"></div>
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
        <div class="shape shape-5"></div>
      </div>
      <div class="brand-content">
        <div class="brand-logo">
          <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="loginLogoGrad" x1="0" y1="0" x2="80" y2="80">
                <stop stop-color="#E67E22"/>
                <stop offset="1" stop-color="#e74c3c"/>
              </linearGradient>
            </defs>
            <rect width="80" height="80" rx="24" fill="url(#loginLogoGrad)"/>
            <path d="M24 36c0-3 2-6 5-7l10-3c2-.6 4-.6 6 0l10 3c3 1 5 4 5 7v16c0 4-3 7-7 7H31c-4 0-7-3-7-7V36z" fill="white" opacity="0.9"/>
            <circle cx="40" cy="42" r="12" fill="url(#loginLogoGrad)"/>
            <circle cx="40" cy="42" r="4" fill="white" opacity="0.9"/>
          </svg>
        </div>
        <h1 class="brand-title">美味餐厅</h1>
        <p class="brand-subtitle">智能化饭店点餐管理系统</p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-dot"></span>订单管理智能化
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>菜品分类灵活配置
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>数据统计实时掌握
          </div>
        </div>
      </div>
    </div>

    <!-- Right: Login Form -->
    <div class="login-panel">
      <div class="login-card">
        <div class="card-header">
          <h2>管理员登录</h2>
          <p>欢迎回来，请登录您的账户</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <div class="input-group">
            <label class="input-label">用户名</label>
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="input-group">
            <label class="input-label">密码</label>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              class="custom-input"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </div>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()

const form = reactive({
  username: 'admin',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await adminLogin(form.username, form.password)
    userStore.setLogin({ ...res.data, token: res.data.token })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh; display: flex; overflow: hidden;
}

/* ===== Brand Panel ===== */
.brand-panel {
  flex: 1; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative; display: flex; align-items: center; justify-content: center;
  overflow: hidden;
}
.brand-overlay {
  position: absolute; inset: 0;
  background: radial-gradient(circle at 20% 50%, rgba(230,126,34,0.15) 0%, transparent 60%),
              radial-gradient(circle at 80% 20%, rgba(231,76,60,0.10) 0%, transparent 50%);
}

.floating-shapes .shape {
  position: absolute; border-radius: 50%;
  background: rgba(255,255,255,0.03);
  animation: floatShape 8s ease-in-out infinite;
}
.shape-1 { width: 200px; height: 200px; top: 10%; left: 15%; animation-delay: 0s; }
.shape-2 { width: 140px; height: 140px; top: 60%; left: 50%; animation-delay: 1.5s; }
.shape-3 { width: 100px; height: 100px; top: 20%; right: 20%; animation-delay: 3s; }
.shape-4 { width: 160px; height: 160px; bottom: 15%; right: 30%; animation-delay: 4.5s; }
.shape-5 { width: 80px; height: 80px; bottom: 30%; left: 35%; animation-delay: 6s; }
@keyframes floatShape {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-20px) scale(1.05); }
}

.brand-content {
  position: relative; z-index: 2; text-align: center; padding: 40px;
}
.brand-logo { margin-bottom: 24px; display: flex; justify-content: center; }
.brand-logo svg { display: block; }
.brand-title {
  font-size: 36px; font-weight: 800; color: #fff; margin-bottom: 8px;
  letter-spacing: 2px;
}
.brand-subtitle { font-size: 15px; color: rgba(255,255,255,0.55); margin-bottom: 40px; }
.brand-features { text-align: left; display: inline-block; }
.feature-item {
  color: rgba(255,255,255,0.65); font-size: 14px;
  margin-bottom: 12px; display: flex; align-items: center; gap: 10px;
}
.feature-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: var(--primary); box-shadow: 0 0 8px var(--primary);
}

/* ===== Login Panel ===== */
.login-panel {
  width: 480px; display: flex; align-items: center; justify-content: center;
  background: var(--bg-page); padding: 40px;
}
.login-card {
  width: 100%; max-width: 380px;
}
.card-header { text-align: center; margin-bottom: 36px; }
.card-header h2 {
  font-size: 26px; font-weight: 700; color: var(--text-primary); margin-bottom: 6px;
}
.card-header p { font-size: 14px; color: var(--text-secondary); }

.input-group { margin-bottom: 20px; }
.input-label {
  display: block; font-size: 13px; font-weight: 600;
  color: var(--text-primary); margin-bottom: 6px;
}
.custom-input :deep(.el-input__wrapper) {
  border-radius: 10px; box-shadow: 0 0 0 1px var(--border-color);
  transition: all var(--transition-fast); padding: 2px 12px;
}
.custom-input :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px var(--primary-light); }
.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(var(--primary-rgb), 0.25);
}
.custom-input :deep(.el-input__prefix) { color: var(--text-muted); }

.login-btn {
  width: 100%; height: 46px; border-radius: 10px;
  font-size: 16px; font-weight: 600; letter-spacing: 4px; margin-top: 8px;
  background: linear-gradient(135deg, var(--primary), #e74c3c);
  border: none; transition: all var(--transition);
}
.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(var(--primary-rgb), 0.4);
}
.login-btn:active { transform: translateY(0); }
</style>
