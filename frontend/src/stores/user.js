import { reactive } from 'vue'
import { login as authLogin, getCurrentUser } from '../api/auth.js'
import { resetPermissions } from './permissions.js'
import { resetRoutesRegistered } from '../router/dynamicRoutes.js'

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
  resetPermissions()
  resetRoutesRegistered()
}
