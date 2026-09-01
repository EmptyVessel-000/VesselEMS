# 前端系统问题清单

> 保存时间：2026-06-16
> 优先级：P0(严重) > P1(中等) > P2(轻微)

---

## P0 — 必须修复

### 1. 刷新页面 404（路由系统缺陷）✅ 已修复

**状态**：✅ 已修复
**修复内容**：方案二 — 路由注册从 `beforeEach` 移到 `App.vue` 的 `onMounted` 中
**涉及文件**：
- `router/dynamicRoutes.js` — 重写为 `initDynamicRoutes` + `clearDynamicRoutes`
- `stores/permissions.js` — 新增 `routesReady` 状态
- `App.vue` — 新增启动逻辑，加载权限后注册路由
- `router/index.js` — 移除 `registerMenuRoutes`，改为等待 `routesReady`
- `stores/user.js` — 退出时调用 `clearDynamicRoutes(router)`
- `views/workspace.vue` — 移除重复的权限加载

---

### 2. 退出登录后动态路由未清除 ✅ 已修复

**状态**：✅ 已修复
**修复内容**：`clearDynamicRoutes(router)` 遍历并移除所有 `menu_` 开头的动态子路由

**文件**：`router/index.js`、`router/dynamicRoutes.js`、`App.vue`

**问题**：动态路由在 `beforeEach` 中注册，但路由匹配是同步的。刷新页面时，动态路由还没注册完，Vue Router 已经匹配完了，导致走到 `/:pathMatch(.*)*` → 404。

**修复方案**：方案二 — 将路由注册从 `beforeEach` 移到 `App.vue` 的 `onMounted` 中，在路由守卫执行前完成注册。

**涉及文件**：
- `router/index.js` — 移除 `registerMenuRoutes` 调用，改为等待 `routesReady`
- `router/dynamicRoutes.js` — 重写，支持清除旧路由 + 重新注册
- `App.vue` — 新增 `onMounted` 启动逻辑
- `stores/permissions.js` — 新增 `routesReady` 状态
- `stores/user.js` — 退出时清除动态路由
- `views/workspace.vue` — 移除重复的权限加载

---

### 2. 退出登录后动态路由未清除

**文件**：`router/dynamicRoutes.js`、`stores/user.js`

**问题**：`logout()` 调用了 `resetRoutesRegistered()` 只是把 `routesRegistered` 标志重置为 `false`，但 `router.addRoute()` 添加的路由仍然存在于路由表中。下次登录时，`registerMenuRoutes()` 因为 `routesRegistered` 已重置会再次执行，但旧路由还在，导致路由重复注册。

**修复方案**：在 `dynamicRoutes.js` 中增加 `clearDynamicRoutes(router)` 函数，遍历并移除 workspace 的所有动态子路由。

---

## P1 — 建议修复

### 3. API 响应解包不一致

**文件**：`api/request.js`、所有 `views/workspace/*.vue`

**问题**：后端接口统一返回 `ApiResponse<T>` 格式 `{ code, message, data }`，但前端有的页面直接使用响应数据（自动解包了），有的页面需要手动解包。`request.js` 的拦截器可能已经处理了解包，但部分页面行为不一致。

**需要检查**：
- `request.js` 的响应拦截器是否统一解包了 `.data`
- 所有 `request.get/post` 调用是否一致地使用了解包后的数据

---

### 4. 错误处理缺失

**文件**：多个 `views/workspace/*.vue`

**问题**：大量 `catch {}` 是空的，后端返回的错误信息被静默吞掉。用户操作失败时没有任何提示。

**涉及页面**：UserManage、RoleManage、ConfigManage、DeptManage、MenuManage、PermissionManage、DataSource、Model

**修复方案**：给所有空的 `catch {}` 加上 `ElMessage.error(e?.message || '操作失败')`。

---

### 5. Token 过期无统一处理

**文件**：`api/request.js`

**问题**：如果 token 过期，后端返回 401，但前端没有统一的响应拦截器处理。每个页面各自处理，有的跳转登录，有的直接报错。

**修复方案**：在 `request.js` 的响应拦截器中，如果状态码是 401，自动清除 token 并跳转到 `/login`。

---

### 6. 导入用户使用原生 fetch

**文件**：`views/workspace/UserManage.vue`

**问题**：`handleImport()` 方法中使用了 `fetch()` 而不是统一的 `request` 对象，导致请求头、错误处理等不一致。

**修复方案**：改用 `request.post()` 并配置 `headers: { 'Content-Type': 'multipart/form-data' }`。

---

## P2 — 可优化

### 7. 硬编码菜单 ID

**文件**：`stores/permissions.js`

**问题**：`hasMenu()` 中硬编码了 `menuId === 6 || menuId === 61` 来放行用户中心菜单。如果数据库中的菜单 ID 变化，这里会失效。

**修复方案**：改为通过菜单名称或路径判断，或者从 `menuTree` 中动态查找。

---

### 8. 菜单树构建逻辑重复

**文件**：`views/workspace.vue`、`router/dynamicRoutes.js`

**问题**：`workspace.vue` 的 `visibleMenus` 和 `dynamicRoutes.js` 的 `traverse` 都遍历了菜单树，过滤逻辑类似但代码分散在两处。

**修复方案**：将菜单树过滤逻辑提取到 `permissions.js` 中，统一导出。

---

### 9. 批量删除后分页不重置

**文件**：所有有批量删除的页面

**问题**：批量删除后，如果当前页的数据被删完，分页组件仍然显示当前页，但数据为空。应该自动回退到上一页。

**修复方案**：在删除操作后，检查 `pagedData` 是否为空，如果是则 `currentPage--`。

---

### 10. 用户管理编辑字段缺失（已修复）

**文件**：`views/workspace/UserManage.vue`、`dto/UserResponseDto.java`、`service/UserService.java`

**状态**：✅ 已修复

**问题**：`UserResponseDto` 和 `buildUserResponse()` 缺少 `realName`、`gender`、`departmentId`、`remark` 字段，导致编辑用户时这些字段显示为空。

---

### 11. 角色管理分配权限唯一约束冲突（已修复）

**文件**：`service/RoleService.java`

**状态**：✅ 已修复

**问题**：`assignMenus()` 和 `assignPermissions()` 中循环 `deleteById` 后没有 `flush()`，紧接着 `save` 时数据库中的旧数据还没删完，触发唯一约束冲突。

---

## 修复顺序

```
第一轮（P0）：
  1 → 路由系统重构（方案二）
  2 → 退出登录清除动态路由

第二轮（P1）：
  3 → API 响应解包检查
  4 → 错误处理补充
  5 → Token 过期统一处理
  6 → 导入用户改用 request

第三轮（P2）：
  7 → 硬编码菜单 ID
  8 → 菜单树逻辑提取
  9 → 分页重置