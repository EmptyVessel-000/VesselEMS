import { createRouter, createWebHistory } from 'vue-router'
import request from '../api/request.js'
import workspaceRoutes from './workspace.js'

const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')
const Workspace = () => import('../views/workspace.vue')

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login, meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: Register, meta: { title: '注册' } },
  {
    path: '/workspace', name: 'Main', component: Workspace, redirect: '/workspace/dashboard',
    meta: { requiresAuth: true },
    children: workspaceRoutes
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(async (to, from, next) => {
  if (to.matched.some(r => r.meta.requiresAuth)) {
    const token = localStorage.getItem('token')
    if (!token) { next('/login'); return }
    try {
      await request.get('/api/auth/me')
      next()
    } catch {
      localStorage.removeItem('token')
      next('/login')
    }
  } else {
    next()
  }
})

export default router