import { reactive } from 'vue'
import request from '../api/request.js'

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
  const res = await request.post('/api/auth/login', { email, password })
  // 后端返回 { token, user } 格式
  if (res.token) {
    setToken(res.token)
  }
  if (res.user) {
    userStore.user = res.user
  } else {
    userStore.user = res
  }
  userStore.isAuthenticated = true
  return res
}

export async function checkAuth() {
  const res = await request.get('/api/auth/me')
  userStore.user = res
  userStore.isAuthenticated = true
  return res
}

export function logout() {
  localStorage.removeItem('token')
  userStore.user = null
  userStore.isAuthenticated = false
}