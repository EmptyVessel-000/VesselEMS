import { reactive } from 'vue'
import request from '../api/request.js'

export const userStore = reactive({
  user: null,
  isAuthenticated: false
})

export async function loginUser(email, password) {
  const res = await request.post('/api/auth/login', { email, password })
  userStore.user = res
  userStore.isAuthenticated = true
}

export async function checkAuth() {
  const res = await request.get('/api/auth/me')
  userStore.user = res
  userStore.isAuthenticated = true
}

export function logout() {
  userStore.user = null
  userStore.isAuthenticated = false
}
