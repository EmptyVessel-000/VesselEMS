import request from './request.js'

export function login(email, password) {
  return request.post('/api/auth/login', { email, password })
}

export function getCurrentUser() {
  return request.get('/api/auth/me')
}

export function fetchPermissions() {
  return request.get('/api/auth/permissions')
}