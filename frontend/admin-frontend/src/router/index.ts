import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      component: () => import('@/components/Layout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '仪表盘', icon: 'DataAnalysis' }
        },
        {
          path: 'categories',
          name: 'Categories',
          component: () => import('@/views/category/CategoryManageView.vue'),
          meta: { title: '分类管理', icon: 'Menu' }
        },
        {
          path: 'dishes',
          name: 'Dishes',
          component: () => import('@/views/dish/DishManageView.vue'),
          meta: { title: '菜品管理', icon: 'Food' }
        },
        {
          path: 'dishes/create',
          name: 'DishCreate',
          component: () => import('@/views/dish/DishEditView.vue'),
          meta: { title: '添加菜品', icon: 'Plus' }
        },
        {
          path: 'dishes/:id/edit',
          name: 'DishEdit',
          component: () => import('@/views/dish/DishEditView.vue'),
          meta: { title: '编辑菜品', icon: 'Edit' }
        },
        {
          path: 'orders',
          name: 'Orders',
          component: () => import('@/views/order/OrderListView.vue'),
          meta: { title: '订单管理', icon: 'Tickets' }
        },
        {
          path: 'orders/:id',
          name: 'OrderDetail',
          component: () => import('@/views/order/OrderDetailView.vue'),
          meta: { title: '订单详情', icon: 'Document' }
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('@/views/user/UserManageView.vue'),
          meta: { title: '用户管理', icon: 'User' }
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
