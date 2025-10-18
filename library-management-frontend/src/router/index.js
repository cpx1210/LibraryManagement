import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由配置
 * 定义应用的所有路由规则
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      requiresAuth: false,  // 不需要登录
      title: '登录'
    }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Home.vue'),
    redirect: '/dashboard',
    meta: {
      requiresAuth: true   // 需要登录才能访问
    },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          requiresAuth: true,
          title: '首页'
        }
      },
      {
        path: '/users',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: {
          requiresAuth: true,
          title: '用户管理'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',  // 404 页面
    name: 'NotFound',
    redirect: '/login'
  }
]

/**
 * 创建路由实例
 * - createWebHistory: 使用 HTML5 History 模式（URL 无 # 号）
 * - routes: 路由配置数组
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

/**
 * 全局前置守卫
 * 在每次路由跳转前执行，用于权限验证
 */
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '图书馆管理系统'

  // 从 localStorage 获取 token
  const token = localStorage.getItem('token')

  // 判断目标路由是否需要登录
  const requiresAuth = to.meta.requiresAuth !== false  // 默认需要登录

  if (requiresAuth) {
    // 需要登录的页面
    if (token) {
      // 已登录，允许访问
      next()
    } else {
      // 未登录，跳转到登录页
      console.log('未登录，跳转到登录页')
      next({
        path: '/login',
        query: { redirect: to.fullPath }  // 保存目标路由，登录后跳转
      })
    }
  } else {
    // 不需要登录的页面（如登录页）
    if (to.path === '/login' && token) {
      // 已登录用户访问登录页，重定向到首页
      console.log('已登录，跳转到首页')
      next('/')
    } else {
      // 允许访问
      next()
    }
  }
})

export default router
