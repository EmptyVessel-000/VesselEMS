// Vite import.meta.glob — 构建时自动扫描 views/ 下所有 .vue 文件
const modules = import.meta.glob('../views/**/*.vue')

/**
 * 注册动态路由：从菜单树中遍历所有页面节点，注册为 workspace 的子路由
 * 每次调用会先清除旧路由再重新注册，保证幂等性
 */
export function initDynamicRoutes(router, menuTree) {
  // 先清除所有已注册的动态路由
  clearDynamicRoutes(router)

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

/**
 * 清除 workspace 的所有动态子路由
 * 通过遍历路由表找到 name 以 'menu_' 开头的路由并移除
 */
export function clearDynamicRoutes(router) {
  const workspaceRoute = router.getRoutes().find(r => r.name === 'workspace')
  if (!workspaceRoute) return

  // 收集所有需要移除的子路由 name
  const toRemove = workspaceRoute.children
    .filter(child => typeof child.name === 'string' && child.name.startsWith('menu_'))
    .map(child => child.name)

  // 逐个移除
  toRemove.forEach(name => {
    if (router.hasRoute(name)) {
      router.removeRoute(name)
    }
  })
}