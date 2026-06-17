import { reactive } from 'vue'
import { fetchPermissions } from '../api/auth.js'

export const SUPER_ADMIN_ROLE = 'super_admin'
export const BUILT_IN_ROLES = [SUPER_ADMIN_ROLE]

export const permissionStore = reactive({
  menus: [],
  permissions: [],
  menuTree: [],
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

/**
 * 从 menuTree 中查找 workspace 子树下第一个可见且用户有权限的页面路径
 * 用于登录后跳转、面包屑"首页"链接、403/404"返回首页"按钮
 */
export function getDashboardPath() {
  const wsNode = permissionStore.menuTree.find(n => n.menuComponent === 'workspace')
  if (!wsNode?.children) return '/workspace'

  function findFirst(nodes) {
    for (const n of nodes) {
      if (n.menuType === 1 && n.menuPath && n.visible === 1 && hasMenu(n.id)) {
        return `/workspace/${n.menuPath}`
      }
      if (n.children) {
        const f = findFirst(n.children)
        if (f) return f
      }
    }
    return null
  }
  return findFirst(wsNode.children) || '/workspace/profile'
}

export async function loadPermissions() {
  try {
    const d = await fetchPermissions()
    if (d) {
      permissionStore.menus = d.menus || []
      permissionStore.permissions = d.permissions || []
      permissionStore.menuTree = d.menuTree || []
    }
  } catch {
    permissionStore.menus = []
    permissionStore.permissions = []
  }
  permissionStore.loaded = true
}

export function resetPermissions() {
  permissionStore.menus = []
  permissionStore.permissions = []
  permissionStore.menuTree = []
  permissionStore.loaded = false
}
