import { createRouter, createWebHistory } from 'vue-router'
import { checkAuth, userStore } from '../stores/user.js'
import { hasMenu, loadPermissions, permissionStore, getDashboardPath } from '../stores/permissions.js'

const Index = () => import('../views/Index.vue')
const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')
const Workspace = () => import('../views/workspace.vue')
const Forbidden = () => import('../views/403.vue')
const NotFound = () => import('../views/404.vue')
const UserProfile = () => import('../views/workspace/UserProfile.vue')

const routes = [
  { path: '/', name: 'Index', component: Index, meta: { title: 'VesselEMS' } },
  { path: '/login', name: 'Login', component: Login, meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: Register, meta: { title: '注册' } },
  { path: '/403', name: 'Forbidden', component: Forbidden, meta: { title: '无权限' } },
  { path: '/404', name: 'NotFound', component: NotFound, meta: { title: '页面不存在' } },
  {
    path: '/workspace', name: 'workspace', component: Workspace,
    meta: { requiresAuth: true },
    children: [
      // 用户个人中心 — 所有用户均可访问，不校验菜单权限
      { path: 'profile', name: 'UserProfile', component: UserProfile, meta: { title: '用户个人中心' } }
    ]
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

    // 动态路由已在 main.js 的 bootstrap() 中注册完成
    // 这里只做权限校验

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