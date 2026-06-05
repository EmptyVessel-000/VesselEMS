import { createRouter, createWebHistory } from 'vue-router'
import request from '../api/request.js'

const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')
const Main = () => import('../views/Main.vue')
const Dashboard = () => import('../views/Dashboard.vue')

const UserManage = () => import('../views/system/UserManage.vue')
const RoleManage = () => import('../views/system/RoleManage.vue')
const MenuManage = () => import('../views/system/MenuManage.vue')
const DictManage = () => import('../views/system/DictManage.vue')
const PermissionManage = () => import('../views/system/PermissionManage.vue')
const DeptManage = () => import('../views/system/DeptManage.vue')
const SystemConfig = () => import('../views/system/SystemConfig.vue')

const DocumentManage = () => import('../views/rag/DocumentManage.vue')

const UserProfile = () => import('../views/user/UserProfile.vue')

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login, meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: Register, meta: { title: '注册' } },
  {
    path: '/main', name: 'Main', component: Main, redirect: '/main/dashboard',
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: Dashboard, meta: { title: '仪表盘' } },
      { path: 'system/users', name: 'UserManage', component: UserManage, meta: { title: '用户管理' } },
      { path: 'system/roles', name: 'RoleManage', component: RoleManage, meta: { title: '角色管理' } },
      { path: 'system/menus', name: 'MenuManage', component: MenuManage, meta: { title: '菜单管理' } },
      { path: 'system/dict', name: 'DictManage', component: DictManage, meta: { title: '字典管理' } },
      { path: 'system/permission', name: 'PermissionManage', component: PermissionManage, meta: { title: '权限管理' } },
      { path: 'system/dept', name: 'DeptManage', component: DeptManage, meta: { title: '部门管理' } },
      { path: 'system/config', name: 'SystemConfig', component: SystemConfig, meta: { title: '系统配置' } },
      { path: 'rag/documents', name: 'DocumentManage', component: DocumentManage, meta: { title: '文档管理' } },
      { path: 'user/profile', name: 'UserProfile', component: UserProfile, meta: { title: '用户个人中心' } }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

// 认证守卫
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