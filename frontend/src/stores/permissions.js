import { reactive } from 'vue'
import request from '../api/request.js'

export const SUPER_ADMIN_ROLE = 'super_admin'
export const NEW_USER_ROLE = 'new_user'
export const BUILT_IN_ROLES = [SUPER_ADMIN_ROLE, NEW_USER_ROLE]

export const permissionStore = reactive({
  menus: [],
  permissions: [],
  loaded: false
})

export function hasMenu(menuId) {
  if (permissionStore.menus.includes(-1)) return true
  return permissionStore.menus.includes(menuId)
}

export function hasPermission(code) {
  if (permissionStore.permissions.includes('*')) return true
  return permissionStore.permissions.includes(code)
}

export async function loadPermissions() {
  try {
    const d = await request.get('/api/auth/permissions')
    if (d) {
      permissionStore.menus = d.menus || []
      permissionStore.permissions = d.permissions || []
    }
  } catch {
    permissionStore.menus = []
    permissionStore.permissions = []
  }
  permissionStore.loaded = true
}