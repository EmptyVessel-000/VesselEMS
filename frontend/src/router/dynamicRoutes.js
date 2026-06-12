// Vite import.meta.glob — 构建时自动扫描 views/ 下所有 .vue 文件
const modules = import.meta.glob('../views/**/*.vue')

let routesRegistered = false

export function registerMenuRoutes(router, menuTree) {
  if (routesRegistered) return
  routesRegistered = true

  // 从 menuTree 中反查 workspace 节点，只遍历它的子节点
  const workspaceNode = menuTree.find(n => n.menuComponent === 'workspace')
  if (!workspaceNode?.children) return

  function traverse(nodes) {
    nodes.forEach(node => {
      // menuType=1 且 component 非空 → 注册路由
      if (node.menuType === 1 && node.menuComponent) {
        const key = `../views/${node.menuComponent}.vue`
        if (modules[key]) {
          router.addRoute('workspace', {
            path: node.menuPath,
            name: `menu_${node.id}`,
            component: modules[key],
            meta: { requiredMenuId: node.id, title: node.menuName }
          })
        }
      }
      if (node.children?.length) traverse(node.children)
    })
  }

  traverse(workspaceNode.children)
}

export function resetRoutesRegistered() {
  routesRegistered = false
}
