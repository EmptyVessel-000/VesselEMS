import { reactive } from 'vue'
import { login as authLogin, getCurrentUser } from '../api/auth.js'
import { resetPermissions, loadPermissions, permissionStore } from './permissions.js'
import { clearDynamicRoutes, initDynamicRoutes } from '../router/dynamicRoutes.js'
import router from '../router/index.js'

export const userStore = reactive({
  user: null,
  isAuthenticated: false
})

export function getToken() {
  return localStorage.getItem('token')
}

export function setToken(token) {
  localStorage.setItem('token', token)
}

export async function loginUser(email, password) {
  const res = await authLogin(email, password)
  if (res.token) setToken(res.token)
  if (res.user) userStore.user = res.user
  else userStore.user = res
  userStore.isAuthenticated = true

  // 登录成功后加载权限并注册动态路由
  // 确保跳转到 /workspace 时路由已就绪
  await loadPermissions()
  initDynamicRoutes(router, permissionStore.menuTree)

  return res
}

export async function checkAuth() {
  const res = await getCurrentUser()
  userStore.user = res
  userStore.isAuthenticated = true
  return res
}

export function logout() {
  localStorage.removeItem('token')
  userStore.user = null
  userStore.isAuthenticated = false
  clearDynamicRoutes(router)
  resetPermissions()
}