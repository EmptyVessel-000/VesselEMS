import { createRouter, createWebHistory } from 'vue-router'

const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')
const Main = () => import('../views/Main.vue')
const Dashboard = () => import('../views/Dashboard.vue')

const UserManage = () => import('../views/system/UserManage.vue')
const RoleManage = () => import('../views/system/RoleManage.vue')
const MenuManage = () => import('../views/system/MenuManage.vue')

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

// TODO: 接入后端后取消注释，启用认证守卫
// router.beforeEach(async (to, from, next) => { ... })

export default router