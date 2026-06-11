import { createRouter, createWebHistory } from 'vue-router'
import { checkAuth, userStore } from '../stores/user.js'
import { hasMenu, loadPermissions, permissionStore, getDashboardPath } from '../stores/permissions.js'
import { registerMenuRoutes } from './dynamicRoutes.js'

const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')
const Workspace = () => import('../views/workspace.vue')
const Forbidden = () => import('../views/403.vue')
const NotFound = () => import('../views/404.vue')

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login, meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: Register, meta: { title: '注册' } },
  { path: '/403', name: 'Forbidden', component: Forbidden, meta: { title: '无权限' } },
  { path: '/404', name: 'NotFound', component: NotFound, meta: { title: '页面不存在' } },
  {
    path: '/workspace', name: 'workspace', component: Workspace,
    meta: { requiresAuth: true },
    children: []
  },
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(async (to, from, next) => {
  if (to.matched.some(r => r.meta.requiresAuth)) {
    const token = localStorage.getItem('token')
    if (!token) { next('/login'); return }

    if (!userStore.isAuthenticated) {
      try {
        await checkAuth()
      } catch {
        localStorage.removeItem('token')
        next('/login')
        return
      }
    }

    // 加载权限并注册动态路由
    if (!permissionStore.loaded) {
      await loadPermissions()
    }
    registerMenuRoutes(router, permissionStore.menuTree)

    // /workspace 自动跳转到第一个可用子页面
    if (to.path === '/workspace') {
      next(getDashboardPath())
      return
    }

    // 路由权限守卫
    if (to.meta.requiredMenuId) {
      if (!hasMenu(to.meta.requiredMenuId)) {
        next('/403')
        return
      }
    }

    next()
  } else {
    next()
  }
})

export default router