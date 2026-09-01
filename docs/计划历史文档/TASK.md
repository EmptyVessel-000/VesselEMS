# VesselEMS 页面资源管理系统 — 任务书

> 版本：1.0 | 日期：2026-06-11

---

## 一、项目目标

将系统从"前端硬编码所有页面路由和菜单"重构为"数据库驱动 + 前端无状态渲染"的数据驱动架构。

**入口**：用户在 `MenuManage.vue` 中新增一条菜单记录 → 侧边栏自动出现对应菜单项 → 点击后路由生效并加载对应 Vue 组件 → 整个链路零代码改动。

---

## 二、核心设计

### 2.1 数据层的唯一规则

```
menu_type = 1（页面）  →  menu_component 非空  →  动态路由层据此注册路由
menu_type = 2（分组）  →  menu_component 为空  →  不注册路由
```

**渲染行为不由数据层决定。** 同一条记录在不同父页面下可以有不同的渲染方式：

| 父页面 | menu_type=1 的渲染方式 | menu_type=2 的渲染方式 |
|--------|----------------------|----------------------|
| workspace.vue（侧边栏） | `el-menu-item` 可点击项 | `el-sub-menu` 折叠容器 |
| 未来的主页 | 功能卡片 | 分类标题行 |
| 未来的产品页 | 标签页 | 分区折叠面板 |

**每个父页面自主决定：** 从 menuTree 中按 `parent_id` 筛选自己的子节点后，自行选择如何将 `menu_type` 映射到具体的 UI 组件。menu 表只负责注册资源及其层级关系，不预设渲染行为。

### 2.2 最终字段集

```sql
CREATE TABLE menu (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id       BIGINT       DEFAULT NULL,
  menu_name       VARCHAR(100) NOT NULL,
  menu_path       VARCHAR(256) DEFAULT NULL,    -- 路由路径。挂载在父路由下写相对路径（如 'dashboard'），顶级独立页面写绝对路径（如 '/index'），分组为 NULL
  menu_component  VARCHAR(256) DEFAULT NULL,    -- 相对 views 路径，如 'workspace/Dashboard'（分组为 NULL）
  menu_icon       VARCHAR(128) DEFAULT NULL,
  menu_type       INT          DEFAULT 1,       -- 1=页面, 2=分组
  visible         INT          DEFAULT 1,       -- 0=隐藏, 1=显示
  sort_order      INT          DEFAULT 0,
  status          INT          DEFAULT 1,
  create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  modify_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**删除的旧字段**：`is_frame`、`permission`。

### 2.3 设计决策汇总

| 决策 | 结论 | 理由 |
|------|------|------|
| `menu_type` | 保留，1=页面/2=分组 | 显式语义，避免用 component 是否为空隐式推断 |
| `visible` | 保留 | 软开关：临时隐藏菜单不删除数据 |
| `is_frame` | 删除 | 无 iframe 外嵌场景 |
| `permission`（字段） | 删除 | 权限由 role_menu / permission_role 关联表管理 |
| `menu_path` 策略 | 父路由下相对路径，顶级绝对路径 | 挂载在父路由下如 `users`，独立顶级页面如 `/index`。分组写 null |
| 权限过滤 | 后端返回完整树 + 前端 `hasMenu(id)` 检查 | 避免分组节点因权限被过滤导致树结构断裂 |

---

## 三、权限机制

### 3.1 数据流

```
POST /api/auth/login
  → 获得 token

GET /api/auth/permissions
  返回：
  {
    menuTree:    [完整嵌套树, 不过滤]   ← 包含所有节点（无论用户有无权限）
    menus:       [2, 21, 22, ...]       ← 用户有权限的菜单ID
    permissions: ["user:list", ...]     ← 用户有权限的权限码
  }

前端消费：
  ├── registerMenuRoutes(router, menuTree)     → 注册所有路由（基于完整树）
  ├── router.beforeEach 检查 meta.requiredMenuId → 路由守卫拦截无权限访问
  └── workspace.vue hasMenu(id) 筛选渲染         → 侧边栏只显示有权限的项
```

### 3.2 为何返回完整树而非后端过滤？

若后端过滤 tree：用户无权限 → 分组节点被移除 → 子节点失去 parent_id → 树结构断裂 → 侧边栏渲染异常。

若后端过滤 tree：分组下的路由未注册 → 用户直接输入 URL → 返回 404 而非 403 → 安全漏洞。

### 3.3 前端权限函数

```js
// 已在 /src/stores/permissions.js 中存在，无需重写
export function hasMenu(menuId) {
  if (permissionStore.menus.includes(-1)) return true  // super_admin
  return permissionStore.menus.includes(menuId)
}
export function hasPermission(code) {
  if (permissionStore.permissions.includes('*')) return true
  return permissionStore.permissions.includes(code)
}
```

### 3.4 super_admin 短路

`AuthController.permissions()` 第 132-137 行已实现：若为 super_admin → 返回 `menus: [-1]`、`permissions: ['*']`，跳过 6 次 JOIN 查询。此处仅需补上 `menuTree`。

---

## 四、动态路由机制

### 4.1 核心模块（新建文件）

`/frontend/src/router/dynamicRoutes.js`：

```js
export function registerMenuRoutes(router, menuTree) {
  // 从 menuTree 中找到 workspace 节点，只遍历它的子节点
  const workspaceNode = menuTree.find(n => n.menu_component === 'workspace')
  if (!workspaceNode?.children) return

  function traverse(nodes) {
    nodes.forEach(node => {
      // menu_type=1 且 component 非空 → 注册路由
      if (node.menu_type === 1 && node.menu_component) {
        router.addRoute('workspace', {
          path: node.menu_path,
          name: `menu_${node.id}`,
          component: () => import(`../views/${node.menu_component}.vue`),
          meta: { requiredMenuId: node.id, title: node.menu_name }
        })
      }
      if (node.children?.length) traverse(node.children)
    })
  }
  traverse(workspaceNode.children)
}
```

**为什么只遍历 workspace 子树而非整棵 menuTree？** menuTree 包含所有顶级节点（如介绍页 `id=100`，`parent_id=null`），介绍页由 `router/index.js` 静态路由 `/index` 注册，不挂在 workspace 父路由下。`registerMenuRoutes` 只负责向 `'workspace'` 注入子路由。

`menu_component` 存相对于 `frontend/src/views` 的路径（如 `workspace/UserManage`），Vite 构建时自动解析。

### 4.2 注册时机与路由守卫

在 `router/index.js` 的 `beforeEach` 中，`loadPermissions()` 完成后、`next()` 之前一次性注册：

```js
let routesRegistered = false

router.beforeEach(async (to, from, next) => {
  if (to.matched.some(r => r.meta.requiresAuth)) {
    // ... 登录检查、checkAuth() ...
    await loadPermissions()
    if (!routesRegistered) {
      registerMenuRoutes(router, permissionStore.menuTree)
      routesRegistered = true
    }
    // 路由权限守卫
    if (to.meta.requiredMenuId && !hasMenu(to.meta.requiredMenuId)) {
      next('/403')
      return
    }
  }
  next()
})
```

关键：注册在 `next()` 之前完成，确保目标路由已存在；`routesRegistered` 标志位防止重复注册。

### 4.3 workspace.js 清空

所有子路由由 `registerMenuRoutes` 动态注入，静态路由表改为 `export default []`。

---

## 五、侧边栏渲染

### 5.1 workspace.vue 逻辑

```
1. 从 menuTree 反查 menu_component === 'workspace' 的记录 → 得到 workspaceId
2. 从 menuTree 筛选 parent_id === workspaceId 的子节点
3. 递归渲染：
   menu_type=1 → el-menu-item（可点击，index 由父路由前缀 + menu_path 拼接）
   menu_type=2 → el-sub-menu（折叠容器，递归子节点）
4. 渲染前过滤：visible === 1 && hasMenu(node.id)
5. 分组权限继承：分组下至少有一个 visible 子菜单才显示
```

### 5.2 关键约束

- **不硬编码任何菜单 ID 或路径**
- 通过反查 `component` 值定位 workspace 自身，而非写死 `id=1`
- 其他父页面（未来可能的主页、产品页）同理筛选自己 `parent_id` 的子节点，互不干扰

---

## 六、MenuManage.vue 完整 CRUD

### 6.1 功能清单

| 功能 | 交互 |
|------|------|
| 搜索 | 顶部搜索栏，按名称模糊搜索 |
| 新增 | 点击「新增」→ 弹窗表单：parentId 下拉树选择、menuName、menuPath、menuComponent、menuIcon、menuType（页面/分组）、visible、sortOrder |
| 编辑 | 行内编辑按钮 → 弹窗回填数据 |
| 删除 | 行内删除按钮 → 二次确认弹窗 |
| 上移/下移 | 行内按钮 → `POST /api/menus/{id}/move?direction=up\|down` |
| 表格 | `el-table` 树形展示（`row-key="id"`, `tree-props`） |

### 6.2 后端接口

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/menus` | GET | 列表（支持 parentId / enabled 参数） |
| `/api/menus/{id}` | GET | 单条 |
| `/api/menus` | POST | 新增 |
| `/api/menus/{id}` | PUT | 修改 |
| `/api/menus/{id}` | DELETE | 删除 |
| `/api/menus/tree` | GET | **新增** — 完整嵌套树 |
| `/api/menus/{id}/move?direction=up\|down` | POST | **新增** — 交换排序 |

---

## 七、待实施文件清单

按先后端再前端的顺序执行。

### 后端 (4 files)

| # | 文件 | 改动 |
|---|------|------|
| 1 | `backend/.../model/Menu.java` | 删除 `isFrame`/`permission` 字段；新增 `@Transient List<Menu> children` |
| 2 | `backend/.../service/MenuService.java` | 新增 `getTree()` 返回嵌套树；新增 `moveMenu(id, direction)` 交换排序；`updateMenu()` 去掉对 `isFrame`/`permission` 的赋值 |
| 3 | `backend/.../controller/MenuController.java` | 新增 `GET /api/menus/tree`；新增 `POST /api/menus/{id}/move` |
| 4 | `backend/.../controller/AuthController.java` | `permissions()` 返回值增加 `menuTree`（调用 `menuService.getTree()`） |

### 前端 (8 files)

| # | 文件 | 改动 |
|---|------|------|
| 6 | `frontend/src/stores/permissions.js` | `permissionStore` 新增 `menuTree: []`；`loadPermissions()` 加载 `d.menuTree` |
| 7 | `frontend/src/router/dynamicRoutes.js` | **新建文件** — `registerMenuRoutes()` |
| 8 | `frontend/src/router/index.js` | `beforeEach` 中在 `checkAuth()` 后调用 `registerMenuRoutes()` + 权限守卫 |
| 9 | `frontend/src/router/workspace.js` | 清空为 `export default []` |
| 10 | `frontend/src/views/workspace.vue` | 侧边栏删除硬编码菜单项，改为从 `menuTree` 递归渲染 |
| 11 | `frontend/src/views/workspace/MenuManage.vue` | 完整重写：搜索/新增/编辑/删除/上移/下移/树形表格 |
| 12 | `frontend/src/views/403.vue` | **新建** — 无权限提示页面 |
| 13 | `frontend/src/views/404.vue` | **新建** — 页面不存在提示页 |

---

## 八、初始 SQL

```sql
DELETE FROM menu;

INSERT INTO menu (id, parent_id, menu_name, menu_path, menu_component, menu_icon, menu_type, visible, sort_order, status) VALUES
-- 独立顶级页面（由 index 路由静态注册）
(100, null, '介绍页',       '/index',                      'index',                    'Ship',          1, 1, 0, 1),
-- workspace 自身（由 router/index.js 静态注册为父路由 /workspace）
(1,   null, 'workspace',    null,                          'workspace',                null,            2, 1, 1, 1),
-- 仪表盘
(2,   1,    '仪表盘',       'dashboard',                   'workspace/Dashboard',      'DataBoard',     1, 1, 0, 1),
-- 系统管理（分组）
(3,   1,    '系统管理',      null,                          null,                       'Setting',       2, 1, 1, 1),
(31,  3,    '用户管理',     'users',                       'workspace/UserManage',     null,            1, 1, 1, 1),
(32,  3,    '角色管理',     'roles',                       'workspace/RoleManage',     null,            1, 1, 2, 1),
(33,  3,    '菜单管理',     'menus',                       'workspace/MenuManage',     null,            1, 1, 3, 1),
(34,  3,    '权限管理',     'permissions',                 'workspace/PermissionManage', null,          1, 1, 4, 1),
(35,  3,    '部门管理',     'depts',                       'workspace/DeptManage',     null,            1, 1, 5, 1),
(36,  3,    '系统配置',     'config',                      'workspace/ConfigManage',   null,            1, 1, 6, 1),
-- RAG管理（分组）
(4,   1,    'RAG管理',       null,                          null,                       'Cpu',           2, 1, 5, 1),
(41,  4,    '文档管理',     'documents',                   'workspace/DocumentManage', null,            1, 1, 1, 1),
-- NL2SQL分析（分组）
(5,   1,    'NL2SQL分析',    null,                          null,                       'ChatDotRound',  2, 1, 8, 1),
(51,  5,    '数据源管理',   'datasources',                 'workspace/DataSource',     null,            1, 1, 1, 1),
(52,  5,    '自然语言查询', 'nlquery',                     'workspace/NLQuery',        null,            1, 1, 2, 1),
(53,  5,    '查询历史',     'dialogs',                     'workspace/Dialog',         null,            1, 1, 3, 1),
(54,  5,    '模型配置',     'models',                      'workspace/Model',          null,            1, 1, 4, 1),
-- 用户中心（分组）
(6,   1,    '用户中心',      null,                          null,                       'User',          2, 1, 99, 1),
(61,  6,    '用户个人中心', 'profile',                     'workspace/UserProfile',    null,            1, 1, 1, 1);
```

**关键约定**：
- `id=1` workspace 自身 `menu_type=2`、`menu_path=null`，由 `router/index.js` 静态注册为父路由 `/workspace`
- 分组（menu_type=2）不注册动态路由
- 侧边栏的展开/折叠行为由 workspace.vue 自行决定，非数据层预设
- `menu_path` 为相对路径，运行时由 workspace.vue 拼接为 `'/workspace/' + menu_path` 作为导航目标
