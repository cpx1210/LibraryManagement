import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由配置
 * 定义应用的所有路由规则
 */
const routes = [
  {
    path: '/',
    name: 'PublicUpload',
    component: () => import('@/views/SimpleUploadHome.vue'),
    meta: {
      requiresAuth: false,
      title: '书单提交'
    }
  },
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
    path: '/app',
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
      },
      {
        path: '/sensitive-words',
        name: 'SensitiveWordList',
        component: () => import('@/views/sensitiveword/SensitiveWordList.vue'),
        meta: {
          requiresAuth: true,
          title: '敏感词管理'
        }
      },
      {
        path: '/problem-books',
        name: 'ProblemBookList',
        component: () => import('@/views/problembook/ProblemBookList.vue'),
        meta: {
          requiresAuth: true,
          title: '问题书目管理'
        }
      },
      {
        path: '/publisher-whitelist',
        name: 'PublisherWhitelistList',
        component: () => import('@/views/publisher/PublisherWhitelistList.vue'),
        meta: {
          requiresAuth: true,
          title: '出版社白名单管理'
        }
      },
      // 馆藏图书模块
      {
        path: '/collection-books',
        name: 'CollectionBookList',
        component: () => import('@/views/collectionbook/CollectionBookList.vue'),
        meta: {
          requiresAuth: true,
          title: '馆藏图书'
        }
      },
      {
        path: '/collection-books/problem',
        name: 'CollectionProblemBookList',
        component: () => import('@/views/collectionbook/CollectionProblemBookList.vue'),
        meta: {
          requiresAuth: true,
          title: '馆藏问题图书'
        }
      },
      // 书单检测模块
      {
        path: '/detection/check',
        name: 'BooklistCheck',
        component: () => import('@/views/detection/BooklistCheck.vue'),
        meta: {
          requiresAuth: true,
          title: '书单检测'
        }
      },
      {
        path: '/detection/history',
        name: 'CheckHistory',
        component: () => import('@/views/detection/CheckHistory.vue'),
        meta: {
          requiresAuth: true,
          title: '检测历史记录'
        }
      },
      {
        path: '/detection/result/:taskId',
        name: 'CheckResult',
        component: () => import('@/views/detection/CheckResult.vue'),
        meta: {
          requiresAuth: true,
          title: '检测结果详情'
        }
      },
      // 日志管理模块
      {
        path: '/logs',
        name: 'OperationLogList',
        component: () => import('@/views/log/OperationLogList.vue'),
        meta: {
          requiresAuth: true,
          title: '操作日志'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',  // 404 页面
    name: 'NotFound',
    redirect: '/'
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
      console.log('已登录，跳转到后台首页')
      next('/dashboard')
    } else {
      // 允许访问
      next()
    }
  }
})

export default router
