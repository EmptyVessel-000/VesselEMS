# VesselEMS 前端项目

VesselEMS 企业级管理系统前端，基于 **Vue 3 + Vite + Element Plus**，采用数据库驱动的动态路由架构。

作者：EmptyVessel

---

## 目录

- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [页面总览](#页面总览)
- [路由系统](#路由系统)
- [权限控制](#权限控制)
- [状态管理](#状态管理)
- [API 通信](#api-通信)
- [UI 设计规范](#ui-设计规范)
- [构建与部署](#构建与部署)

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.3.4 | 前端框架 |
| Vite | 4.4.9 | 构建工具 |
| Element Plus | 2.14.1 | UI 组件库 |
| Vue Router | 4.6.4 | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.5.0 | HTTP 客户端 |
| @element-plus/icons-vue | 2.3.2 | 图标库 |

---

## 快速开始

### 前置要求

- Node.js 18+
- npm 9+

### 安装与运行

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 生产构建
npm run build

# 预览构建结果
npm run preview
```

开发服务器将在 `http://localhost:5173` 启动。

### 后端 API 配置

前端通过 Vite 代理访问后端 API，配置在 `vite.config.js` 中：

```js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

所有以 `/api` 开头的请求都会被代理到后端 `http://localhost:8080`。

---

## 项目结构

```
src/
├── main.js                          # 应用入口（注册路由、状态管理）
├── App.vue                          # 根组件
├── api/                             # API 请求层
│   ├── request.js                   # Axios 实例（拦截器、Token 管理）
│   └── auth.js                      # 认证相关 API
├── router/                          # 路由配置
│   ├── index.js                     # 静态路由 + 路由守卫
│   └── dynamicRoutes.js             # 动态路由注册（从菜单树生成）
├── stores/                          # Pinia 状态管理
│   ├── user.js                      # 用户状态（Token、登录信息）
│   └── permissions.js               # 权限状态（菜单树、权限码）
└── views/                           # 页面组件
    ├── Login.vue                    # 登录页
    ├── Register.vue                 # 注册页
    ├── Index.vue                    # 介绍页
    ├── 403.vue                      # 无权限提示页
    ├── 404.vue                      # 页面不存在提示页
    ├── workspace.vue                # 工作台主布局（侧边栏 + 内容区）
    └── workspace/                   # 工作台子页面
        ├── Dashboard.vue            # 仪表盘
        ├── UserManage.vue           # 用户管理
        ├── RoleManage.vue           # 角色管理
        ├── MenuManage.vue           # 菜单管理
        ├── PermissionManage.vue     # 权限管理
        ├── DeptManage.vue           # 部门管理
        ├── ConfigManage.vue         # 系统配置
        ├── DataSource.vue           # 数据源管理
        ├── Model.vue                # 模型配置
        ├── NLQuery.vue              # 自然语言查询
        ├── Dialog.vue               # 查询历史
        ├── RAGQuery.vue             # RAG 问答
        ├── RAGKnowledge.vue         # RAG 知识库
        ├── UserProfile.vue          # 用户个人中心
        └── Dialog.vue               # 对话历史
```

---

## 页面总览

### 认证页面

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录页 | `/login` | 用户登录，渐变品牌区域 + 装饰性元素 |
| 注册页 | `/register` | 用户注册，与登录页风格统一 |
| 介绍页 | `/index` | 项目介绍/首页 |

### 工作台主布局

`workspace.vue` 是系统的核心布局组件，包含：
- **侧边栏**：从数据库菜单树动态渲染，支持展开/折叠（200px / 56px）
- **顶部栏**：面包屑导航 + 用户信息下拉菜单
- **内容区**：路由视图容器

### 管理页面

所有管理页面采用统一布局：

```
┌─────────────────────────────────┐
│  page-container                 │
│  ┌───────────────────────────┐  │
│  │  search-section           │  │  ← 搜索/筛选区域
│  │  [搜索框] [新增按钮]      │  │
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │  table-section            │  │  ← 表格/数据展示区域
│  │  ┌─────┬─────┬─────┐     │  │
│  │  │  ID │ 名称 │ 操作 │     │  │
│  │  ├─────┼─────┼─────┤     │  │
│  │  │ ... │ ... │ ... │     │  │
│  │  └─────┴─────┴─────┘     │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

| 页面 | 路由 | 功能 |
|------|------|------|
| 仪表盘 | `/workspace/dashboard` | 欢迎卡片 + 4 个统计卡片 + 快捷操作 + 最近活动 |
| 用户管理 | `/workspace/users` | 用户 CRUD、角色分配、Excel 导入 |
| 角色管理 | `/workspace/roles` | 角色 CRUD、菜单分配、权限分配（按菜单分组） |
| 菜单管理 | `/workspace/menus` | 菜单 CRUD、树形表格、上移/下移排序 |
| 权限管理 | `/workspace/permissions` | 权限 CRUD |
| 部门管理 | `/workspace/depts` | 部门 CRUD、树形结构 |
| 系统配置 | `/workspace/config` | 配置 CRUD |
| 数据源管理 | `/workspace/datasources` | 数据源 CRUD、连接测试、Schema 查看 |
| 模型配置 | `/workspace/models` | 模型 CRUD |
| 自然语言查询 | `/workspace/nlquery` | NL2SQL 交互式查询 |
| 查询历史 | `/workspace/dialogs` | 对话历史列表与删除 |
| RAG 问答 | `/workspace/ragquery` | RAG 知识库问答 |
| RAG 知识库 | `/workspace/ragknowledge` | 知识库与文档管理 |
| 用户个人中心 | `/workspace/profile` | 个人信息编辑、密码修改 |

---

## 路由系统

### 架构

采用 **静态路由 + 动态路由** 双轨注册机制：

```
main.js 启动时：
  ├── 注册静态路由（login, register, index, 403, 404, workspace）
  └── 调用 bootstrap() 检查登录状态
       └── 如果已登录 → 调用 loginUser() 加载权限 → 注册动态路由

路由守卫 beforeEach：
  ├── 检查登录状态（Token 是否存在/有效）
  ├── 加载权限（如果未加载）
  ├── 注册动态路由（如果未注册）
  └── 路由权限检查（requiredMenuId）
```

### 静态路由

在 `router/index.js` 中定义，始终可用：

| 路由 | 组件 | 说明 |
|------|------|------|
| `/login` | Login.vue | 登录页（无需认证） |
| `/register` | Register.vue | 注册页（无需认证） |
| `/index` | Index.vue | 介绍页（无需认证） |
| `/403` | 403.vue | 无权限提示 |
| `/404` | 404.vue | 页面不存在提示 |
| `/workspace` | workspace.vue | 工作台父路由（需认证） |

### 动态路由

在 `dynamicRoutes.js` 中从数据库菜单树生成：

```js
export function registerMenuRoutes(router, menuTree) {
  // 从 menuTree 中找到 workspace 节点
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

**关键设计**：
- 只遍历 workspace 子树，不干扰其他顶级路由
- 使用 `router.addRoute('workspace', ...)` 注入子路由
- `routesRegistered` 标志位防止重复注册
- 动态导入（`() => import(...)`）实现代码分割

### 路由守卫

```js
router.beforeEach(async (to, from, next) => {
  if (to.matched.some(r => r.meta.requiresAuth)) {
    // 检查登录状态
    if (!userStore.token) return next('/login')
    if (userStore.isTokenExpired()) {
      userStore.clearAuth()
      clearDynamicRoutes(router)
      return next('/login')
    }
    // 加载权限
    await permissionStore.loadPermissions()
    // 注册动态路由
    if (!routesRegistered) {
      registerMenuRoutes(router, permissionStore.menuTree)
      routesRegistered = true
    }
    // 路由权限检查
    if (to.meta.requiredMenuId && !hasMenu(to.meta.requiredMenuId)) {
      return next('/403')
    }
  }
  next()
})
```

---

## 权限控制

### 数据流

```
登录成功 → GET /api/auth/permissions
  → 返回：
    {
      menuTree:    [完整嵌套树],     ← 所有菜单节点
      menus:       [2, 21, 22, ...], ← 用户有权限的菜单 ID
      permissions: ["user:create", ...] ← 用户有权限的权限码
    }

前端消费：
  ├── registerMenuRoutes(router, menuTree)  → 注册所有路由
  ├── router.beforeEach 检查 requiredMenuId → 路由守卫拦截
  └── workspace.vue hasMenu(id) 筛选渲染    → 侧边栏只显示有权限的项
```

### 权限函数

```js
// stores/permissions.js
export function hasMenu(menuId) {
  if (permissionStore.menus.includes(-1)) return true  // super_admin
  return permissionStore.menus.includes(menuId)
}

export function hasPermission(code) {
  if (permissionStore.permissions.includes('*')) return true  // super_admin
  return permissionStore.permissions.includes(code)
}
```

### 超级管理员短路

- 后端返回 `menus: [-1]`、`permissions: ['*']`
- `hasMenu()` 和 `hasPermission()` 自动放行
- 侧边栏显示所有菜单项

### 侧边栏渲染逻辑

```
workspace.vue 侧边栏：
  1. 从 menuTree 反查 menu_component === 'workspace' → 得到 workspaceId
  2. 筛选 parent_id === workspaceId 的子节点
  3. 递归渲染：
     menu_type=1 → el-menu-item（可点击）
     menu_type=2 → el-sub-menu（折叠容器）
  4. 过滤条件：visible === 1 && hasMenu(node.id)
  5. 分组权限继承：分组下至少有一个可见子菜单才显示
```

---

## 状态管理

### Pinia Store

#### user.js — 用户状态

| 状态 | 类型 | 说明 |
|------|------|------|
| `token` | String | JWT 令牌 |
| `userInfo` | Object | 用户信息（id, username, roles） |

| 方法 | 说明 |
|------|------|
| `login(credentials)` | 登录并保存 Token |
| `register(data)` | 注册 |
| `logout()` | 登出（清除 Token + 动态路由） |
| `fetchUserInfo()` | 获取用户信息 |
| `clearAuth()` | 清除认证状态 |
| `isTokenExpired()` | 检查 Token 是否过期 |

#### permissions.js — 权限状态

| 状态 | 类型 | 说明 |
|------|------|------|
| `menuTree` | Array | 完整菜单树 |
| `menus` | Array | 用户有权限的菜单 ID 列表 |
| `permissions` | Array | 用户有权限的权限码列表 |

| 方法 | 说明 |
|------|------|
| `loadPermissions()` | 从后端加载权限数据 |
| `clearPermissions()` | 清除权限数据 |

---

## API 通信

### Axios 实例配置

`api/request.js` 中创建 Axios 实例，配置：

```js
const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器：自动添加 Token
request.interceptors.request.use(config => {
  const token = useUserStore().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      // Token 过期 → 清除状态 → 跳转登录
      useUserStore().clearAuth()
      clearDynamicRoutes(router)
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

### API 响应格式

后端统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

前端通过响应拦截器自动解包，组件中直接使用 `data`。

---

## UI 设计规范

### 色彩系统

| 用途 | 色值 | 说明 |
|------|------|------|
| 主色 | `#2563eb` | 品牌色，按钮/链接/强调 |
| 主色悬停 | `#1d4ed8` | 按钮悬停状态 |
| 主色浅色 | `#eff6ff` | 浅色背景/激活状态 |
| 背景色 | `#f5f5f4` | 页面主背景（暖灰） |
| 卡片背景 | `#ffffff` | 卡片/容器背景 |
| 文字主色 | `#1c1917` | 主要文字 |
| 文字次色 | `#78716c` | 次要文字 |
| 边框色 | `#e7e5e4` | 分割线/边框 |

### 圆角规范

| 层级 | 值 | 应用 |
|------|-----|------|
| 小 | 6px | 输入框、小按钮 |
| 中 | 8px | 卡片、弹窗 |
| 大 | 12px | 大容器、品牌区域 |

### 布局规范

- **侧边栏**：展开 200px，折叠 56px
- **页面容器**：`page-container` 统一内边距 20px
- **搜索区域**：`search-section` 白色卡片背景，底部 16px 间距
- **表格区域**：`table-section` 白色卡片背景
- **卡片阴影**：`box-shadow: 0 1px 3px rgba(0,0,0,0.06)`

### 组件风格

- **按钮**：圆角 6px，主色填充
- **表格**：无边框风格，悬停高亮
- **弹窗**：圆角 8px，标题加粗
- **输入框**：圆角 6px，聚焦时主色边框
- **标签**：圆角 4px，浅色背景

---

## 构建与部署

### 开发环境

```bash
npm run dev
```

### 生产构建

```bash
npm run build
```

构建后的文件输出到 `dist` 目录。

### Docker 部署

```dockerfile
# frontend/Dockerfile
FROM nginx:alpine
COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
```

### Nginx 配置

```nginx
# deploy/nginx.conf
server {
    listen 80;
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }
    location /api/ {
        proxy_pass http://backend:8080;
    }
}
```

---

## 开发规范

### 组件命名

- 页面组件：`PascalCase.vue`（如 `UserManage.vue`）
- 路由 path：kebab-case（如 `/user-manage`）

### 代码风格

- 使用 Composition API（`<script setup>`）
- 组件内按以下顺序组织：
  1. `import` 语句
  2. `defineProps` / `defineEmits`
  3. 响应式状态（`ref` / `reactive`）
  4. 计算属性（`computed`）
  5. 方法（`function`）
  6. 生命周期钩子
  7. `<template>` 模板

### 状态管理

- 全局状态使用 Pinia Store
- 组件内状态使用 `ref` / `reactive`
- 避免在组件间直接传递过多 props

---

## 作者

EmptyVessel