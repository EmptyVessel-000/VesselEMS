import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../stores/user.js'
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

const StudentManage = () => import('../views/student/StudentManage.vue')

const UserProfile = () => import('../views/user/UserProfile.vue')

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册' }
  },
  {
    path: '/main',
    name: 'Main',
    component: Main,
    redirect: '/main/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '仪表盘' }
      },
      // 系统管理
      {
        path: 'system/users',
        name: 'UserManage',
        component: UserManage,
        meta: { title: '用户管理' }
      },
      {
        path: 'system/roles',
        name: 'RoleManage',
        component: RoleManage,
        meta: { title: '角色管理' }
      },
      {
        path: 'system/menus',
        name: 'MenuManage',
        component: MenuManage,
        meta: { title: '菜单管理' }
      },
      {
        path: 'system/dict',
        name: 'DictManage',
        component: DictManage,
        meta: { title: '字典管理' }
      },
      {
        path: 'system/permission',
        name: 'PermissionManage',
        component: PermissionManage,
        meta: { title: '权限管理' }
      },
      {
        path: 'system/dept',
        name: 'DeptManage',
        component: DeptManage,
        meta: { title: '部门管理' }
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: SystemConfig,
        meta: { title: '系统配置' }
      },
      // RAG管理
      {
        path: 'rag/documents',
        name: 'DocumentManage',
        component: DocumentManage,
        meta: { title: '文档管理' }
      },
      // 学生中心
      {
        path: 'student/manage',
        name: 'StudentManage',
        component: StudentManage,
        meta: { title: '学生管理' }
      },
      // 用户中心
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: UserProfile,
        meta: { title: '用户个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 演示模式：跳过认证守卫
// TODO: 接入后端后恢复下方认证逻辑
// router.beforeEach(async (to, from, next) => { ... })

export default router