# VesselEMS 代码质量分析报告与重构方案

> 创建时间：2026-06-11  
> 目的：优化项目代码构建，遵循低耦合高内聚原则，模块关系与存放位置清晰，解决多轮迭代残留问题

---

## 一、当前项目结构总览

### 后端 (Spring Boot 4.0.3, Java 21, Maven)
```
backend/
├── pom.xml
├── Dockerfile
├── mvnw / mvnw.cmd
├── docs/                        ← 空目录，无内容
└── src/main/
    ├── java/vesselems/
    │   ├── VesselEMSApplication.java
    │   ├── common/
    │   │   └── ApiResponse.java
    │   ├── config/
    │   │   └── SecurityConfig.java
    │   ├── controller/
    │   │   ├── AuthController.java       (180行，过重)
    │   │   ├── ConfigController.java
    │   │   ├── DashboardController.java
    │   │   ├── DatasourceController.java
    │   │   ├── DepartmentController.java
    │   │   ├── DialogController.java
    │   │   ├── LogController.java
    │   │   ├── MenuController.java
    │   │   ├── ModelController.java
    │   │   ├── PermissionController.java
    │   │   ├── RoleController.java
    │   │   └── UserController.java
    │   ├── dto/
    │   │   ├── CreateUserDto.java
    │   │   ├── LoginDto.java
    │   │   ├── RegisterDto.java
    │   │   ├── UserResponseDto.java
    │   │   └── UserUpdateDto.java
    │   ├── exception/
    │   │   └── GlobalExceptionHandler.java
    │   ├── model/
    │   │   ├── Config.java
    │   │   ├── Datasource.java
    │   │   ├── Department.java
    │   │   ├── Dialog.java
    │   │   ├── Log.java
    │   │   ├── Menu.java                 (168行，大量样板getter/setter)
    │   │   ├── MenuPermission.java       (关联表)
    │   │   ├── Model.java
    │   │   ├── Permission.java
    │   │   ├── PermissionRole.java       (关联表)
    │   │   ├── Role.java
    │   │   ├── RoleMenu.java             (关联表)
    │   │   ├── Session.java
    │   │   ├── User.java
    │   │   └── UserRole.java             (关联表)
    │   ├── repository/
    │   │   └── (15个JPA Repository)
    │   ├── security/
    │   │   ├── JwtFilter.java
    │   │   ├── JwtService.java
    │   │   └── LocalUserDetailsService.java
    │   └── service/
    │       ├── AuthService.java
    │       ├── ConfigService.java
    │       ├── DepartmentService.java
    │       ├── DSManager.java
    │       ├── LLMService.java
    │       ├── LogService.java
    │       ├── MenuPermissionService.java
    │       ├── MenuService.java
    │       ├── NL2SQLService.java
    │       ├── PermissionRoleService.java
    │       ├── PermissionService.java
    │       ├── RoleMenuService.java
    │       ├── RoleService.java
    │       ├── SchemaService.java
    │       ├── UserRoleService.java
    │       └── UserService.java
    └── resources/
        └── application.properties       (未分环境)
```

### 前端 (Vue 3 + Vite + Element Plus)
```
frontend/
├── Dockerfile
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── App.vue
    ├── main.js
    ├── api/
    │   └── request.js                  (Axios封装，与router有循环依赖风险)
    ├── components/                      ← 空目录，无共享组件
    ├── router/
    │   ├── index.js                    (路由定义 + 导航守卫)
    │   └── dynamicRoutes.js            (动态菜单路由注册)
    ├── stores/
    │   ├── permissions.js              (权限store)
    │   └── user.js                     (用户store)
    └── views/
        ├── Login.vue
        ├── Register.vue
        ├── 403.vue
        ├── 404.vue
        ├── workspace.vue               (布局组件)
        └── workspace/                  ← 13个页面平铺，无子目录分组
            ├── ConfigManage.vue
            ├── Dashboard.vue
            ├── DataSource.vue
            ├── DeptManage.vue
            ├── Dialog.vue
            ├── DocumentManage.vue
            ├── MenuManage.vue
            ├── Model.vue
            ├── NLQuery.vue
            ├── PermissionManage.vue
            ├── RoleManage.vue
            ├── UserManage.vue
            └── UserProfile.vue
```

---

## 二、多轮迭代残留问题总结

### 🔴 严重问题

#### 1. AuthController 过重（180行，违反单一职责原则）

**问题描述：**
- `AuthController` 直接注入了 10 个依赖（AuthService、JwtService、4个Repository、5个Service）
- `/api/auth/permissions` 接口内含 60+ 行权限计算逻辑：
  - 判断 super_admin
  - 遍历 UserRole → Role → RoleMenu 收集 menuIds
  - 遍历 UserRole → Role → PermissionRole 收集 permissionIds
  - 通过 MenuPermission 关联表做 menu-permission 映射过滤
- Controller 层不应承担如此复杂的业务逻辑

**当前代码片段 (AuthController.java):**
```java
// 注入10个依赖
private final AuthService authService;
private final JwtService jwtService;
private final UserRepository userRepository;        // ← Repository直接注入Controller
private final UserRoleRepository userRoleRepository;  // ← 跨层依赖
private final RoleRepository roleRepository;          // ← 跨层依赖
private final RoleMenuService roleMenuService;
private final PermissionRoleService permissionRoleService;
private final PermissionService permissionService;
private final MenuPermissionRepository menuPermissionRepository; // ← 跨层依赖
private final AuthenticationManager authenticationManager;
private final MenuService menuService;
```

#### 2. Controller 层直接注入 Repository（跨层耦合）

**问题描述：**
- `AuthController` 直接依赖 `UserRepository`、`RoleRepository`、`UserRoleRepository`、`MenuPermissionRepository`
- 违反经典三层/四层架构：Controller → Service → Repository
- UserController 中也有类似问题

#### 3. 后端包结构完全扁平化，无功能域划分

**问题描述：**
- model/ 下有 15 个实体平铺，controller/ 下 12 个控制器平铺，service/ 下 18 个服务平铺
- 权限系统（User/Role/Menu/Permission）与业务模块（Datasource/Model/Dialog/Log）混杂
- 可读性和可维护性随着功能增长会急剧下降

#### 4. 无任何单元测试

**问题描述：**
- pom.xml 已引入 `spring-boot-starter-*-test` 测试依赖
- 但 `src/test` 目录完全为空，无任何测试类

---

### 🟡 中等问题

#### 5. Menu.java 大量样板 getter/setter（168行）

**问题描述：**
- 11 个字段的 getter/setter 占用约 150 行
- `@Transient children` 字段声明放在文件末尾，可读性差
- 可考虑 Lombok，或至少将字段声明集中管理

#### 6. MenuService.updateMenu 逐一判空赋值（14行重复模式）

```java
public Menu updateMenu(Long id, Menu updated) {
    Menu menu = getMenuById(id);
    if (updated.getMenuName() != null) menu.setMenuName(updated.getMenuName());
    if (updated.getMenuPath() != null) menu.setMenuPath(updated.getMenuPath());
    if (updated.getMenuComponent() != null) menu.setMenuComponent(updated.getMenuComponent());
    if (updated.getMenuIcon() != null) menu.setMenuIcon(updated.getMenuIcon());
    if (updated.getMenuType() != null) menu.setMenuType(updated.getMenuType());
    if (updated.getParentId() != null) menu.setParentId(updated.getParentId());
    if (updated.getVisible() != null) menu.setVisible(updated.getVisible());
    if (updated.getSortOrder() != null) menu.setSortOrder(updated.getSortOrder());
    if (updated.getStatus() != null) menu.setStatus(updated.getStatus());
    return menuRepository.save(menu);
}
```

#### 7. 关联表 Service 碎片化

**问题描述：**
- `RoleMenuService`、`PermissionRoleService`、`MenuPermissionService`、`UserRoleService` 各仅做简单 CRUD
- 每个关联表单独一个 Service + Repository 过于碎片化
- 这些关联表通常作为父实体的子集合操作，独立成 Service 增加耦合面

#### 8. pom.xml 空标签噪音

```xml
<description/>
<url/>
<license/>
<developer/>
<scm>
    <connection/>
    <developerConnection/>
    <tag/>
    <url/>
</scm>
```

#### 9. application.properties 问题

- `logging.level.org.hibernate.SQL=DEBUG` + `BasicBinder=TRACE` 不应在所有环境开启
- `spring.sql.init.mode=always` 每次启动都执行初始化脚本
- 未按 dev/prod 做环境拆分

#### 10. 前端 API 调用散落在各处

**问题描述：**
- 所有 HTTP 调用直接用 `request.get/post` 散落在 Vue 组件和 Pinia Store 中
- 缺少 `api/auth.js`、`api/menu.js`、`api/user.js` 等 API 抽象层
- API 路径硬编码在各处，修改路径需全局搜索

#### 11. request.js 存在循环依赖风险

**依赖链：**
```
request.js → import router/index.js
router/index.js → import stores/permissions.js
stores/permissions.js → import api/request.js
```
形成循环：request.js → router → permissions → request.js

#### 12. views/workspace/ 下 13 个页面平铺

**问题描述：**
- 所有功能页面平铺在 `views/workspace/` 下
- 无按功能域分子目录（如 `system/` 系统管理、`datasource/` 数据源、`llm/` 智能问答等）

#### 13. stores/user.js 中 loginUser 职责过重

```javascript
export async function loginUser(email, password) {
  const res = await request.post('/api/auth/login', { email, password })  // 发请求
  if (res.token) setToken(res.token)       // 写localStorage
  if (res.user) userStore.user = res.user  // 写store
  else userStore.user = res
  userStore.isAuthenticated = true         // 改状态
  return res
}
```

---

### 🟢 轻微问题

#### 14. components/ 目录为空

- `frontend/src/components/` 完全空置
- 部分可复用 UI 片段（如表格分页、搜索栏、树形选择器）散落在各个 View 中重复编写

#### 15. backend/docs/ 空目录

- `backend/docs/` 目录存在但无任何内容

#### 16. deploy/ 目录未经测试

- deploy.sh、docker-compose.yml、nginx.conf 等部署配置可能已过期或含硬编码

---

## 三、重构优化方案

### 总体原则
- **低耦合高内聚**：按功能域分包，同一域的文件聚合在一起
- **分层清晰**：Controller → Service → Repository 不跨层
- **不改变功能**：只移动代码、提取方法、调整结构
- **渐进式**：优先处理严重问题，逐步推进

---

### 第一阶段：后端结构优化

#### Step 1：后端按功能域分包

```
vesselems/
├── VesselEMSApplication.java
├── common/
│   └── ApiResponse.java
├── config/
│   └── SecurityConfig.java
├── exception/
│   └── GlobalExceptionHandler.java
├── system/                           ← 系统管理域（用户角色权限菜单部门）
│   ├── model/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Menu.java
│   │   ├── Permission.java
│   │   ├── Department.java
│   │   ├── UserRole.java
│   │   ├── RoleMenu.java
│   │   ├── PermissionRole.java
│   │   └── MenuPermission.java
│   ├── repository/
│   │   └── (对应Repository)
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── RoleService.java
│   │   ├── MenuService.java
│   │   ├── PermissionService.java
│   │   └── DepartmentService.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── RoleController.java
│   │   ├── MenuController.java
│   │   ├── PermissionController.java
│   │   └── DepartmentController.java
│   └── dto/
│       ├── LoginDto.java
│       ├── RegisterDto.java
│       ├── CreateUserDto.java
│       ├── UserResponseDto.java
│       └── UserUpdateDto.java
├── business/                         ← 业务域（数据源/模型/对话/日志/配置）
│   ├── model/
│   │   ├── Datasource.java
│   │   ├── Model.java
│   │   ├── Dialog.java
│   │   ├── Session.java
│   │   ├── Log.java
│   │   └── Config.java
│   ├── repository/
│   │   └── (对应Repository)
│   ├── service/
│   │   ├── DSManager.java
│   │   ├── LLMService.java
│   │   ├── NL2SQLService.java
│   │   ├── SchemaService.java
│   │   ├── LogService.java
│   │   └── ConfigService.java
│   └── controller/
│       ├── DatasourceController.java
│       ├── DialogController.java
│       ├── ModelController.java
│       ├── DashboardController.java
│       ├── LogController.java
│       └── ConfigController.java
└── security/                         ← 基础设施域（JWT/认证）
    ├── JwtFilter.java
    ├── JwtService.java
    └── LocalUserDetailsService.java
```

#### Step 2：AuthController 瘦身 — 提取 PermissionService

将 `/api/auth/permissions` 中的权限计算逻辑（60+行）提取到 `PermissionService.calculateUserPermissions(Long userId)` 方法。

**修改后的 AuthController 依赖（10个→3个）：**
```java
public AuthController(
    AuthService authService,
    JwtService jwtService,
    AuthenticationManager authenticationManager
) { ... }
```

**新增 PermissionService 方法：**
```java
public UserPermissions calculateUserPermissions(Long userId) {
    // 1. 判断 super_admin
    // 2. 收集 menuIds (UserRole → Role → RoleMenu)
    // 3. 收集 permissionIds (UserRole → Role → PermissionRole)
    // 4. 通过 MenuPermission 过滤
    // 5. 返回 Map {menus, permissions, menuTree}
}
```

#### Step 3：Controller 移除所有 Repository 直接依赖

- AuthController 移除 UserRepository、RoleRepository、UserRoleRepository、MenuPermissionRepository
- 全部通过 Service 层访问

#### Step 4：清理 pom.xml 空标签

删除所有无意义的空 XML 标签。

#### Step 5：application.properties 拆分环境

```
application.properties          ← 公共配置
application-dev.properties      ← 开发环境（含 DEBUG SQL）
application-prod.properties     ← 生产环境（关闭 DEBUG）
```

---

### 第二阶段：前端结构优化

#### Step 6：建立 API 模块层

新建：
```
src/api/
├── request.js          ← 现有，保持不变
├── auth.js             ← 登录/注册/获取当前用户/获取权限
├── menu.js             ← 菜单CRUD
├── user.js             ← 用户CRUD
├── role.js             ← 角色CRUD
├── permission.js       ← 权限CRUD
└── ...
```

各模块示例 (`api/auth.js`)：
```javascript
import request from './request.js'

export function login(email, password) {
  return request.post('/api/auth/login', { email, password })
}

export function getCurrentUser() {
  return request.get('/api/auth/me')
}

export function loadPermissions() {
  return request.get('/api/auth/permissions')
}
```

#### Step 7：解除 request.js 循环依赖

将 `request.js` 中的 `import router from '../router/index.js'` 改为运行时获取：
```javascript
// 响应拦截器 - 401 处理
response.interceptors.response.use(
  (response) => { ... },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      // 动态获取 router，避免循环依赖
      // 通过 window.location 跳转，不依赖 router
      if (window.location.pathname !== '/login') {
        ElMessage.error('登录已过期，请重新登录')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)
```

#### Step 8：views/workspace/ 按功能域分子目录

```
src/views/workspace/
├── system/                    ← 系统管理
│   ├── UserManage.vue
│   ├── RoleManage.vue
│   ├── MenuManage.vue
│   ├── PermissionManage.vue
│   ├── DeptManage.vue
│   └── ConfigManage.vue
├── data/                      ← 数据管理
│   ├── DataSource.vue
│   ├── Model.vue
│   └── DocumentManage.vue
├── query/                     ← 智能查询
│   ├── NLQuery.vue
│   └── Dialog.vue
├── Dashboard.vue
└── UserProfile.vue
```

同时更新 `menu_component` 字段值和 `dynamicRoutes.js` 中的 glob 路径。

#### Step 9：重构 stores/user.js

```javascript
// api/auth.js
export async function loginUser(email, password) {
  const res = await request.post('/api/auth/login', { email, password })
  return res  // 返回原始响应，由调用方处理
}

// stores/user.js
export async function doLogin(email, password) {
  const res = await loginUser(email, password)  // 调用 API 层
  if (res.token) setToken(res.token)
  userStore.user = res.user || res
  userStore.isAuthenticated = true
  return res
}
```

---

### 第三阶段：代码质量微调

#### Step 10：Menu.java 字段声明整理

将 `@Transient children` 字段从文件末尾移至字段声明区，与其他字段并列。

#### Step 11：updateMenu 简化

使用 Spring BeanUtils.copyProperties 忽略 null 值：
```java
public Menu updateMenu(Long id, Menu updated) {
    Menu menu = getMenuById(id);
    BeanUtils.copyProperties(updated, menu, 
        "id", "createTime", "modifyTime", "children");
    return menuRepository.save(menu);
}
```

#### Step 12：清理 backend/docs/ 空目录

删除 `backend/docs/` 空目录。

---

## 四、实施优先级评估

| 优先级 | 步骤 | 风险 | 收益 |
|--------|------|------|------|
| P0 | Step 2: AuthController 瘦身 | 低 | 极高 |
| P0 | Step 3: Controller 移除 Repository | 低 | 高 |
| P0 | Step 6: 前端 API 模块化 | 低 | 高 |
| P0 | Step 7: 解除循环依赖 | 低 | 高 |
| P1 | Step 1+5: 后端分包 | 中（import全改） | 高 |
| P1 | Step 8: 前端组件分包 | 中（路径全改） | 中 |
| P1 | Step 4: pom.xml 清理 | 无 | 低 |
| P2 | Step 10-11: 代码微调 | 无 | 低 |
| P3 | Step 12: 清理空目录 | 无 | 低 |

---

## 五、暂不引入的优化

以下优化方案经评估暂不建议实施：

1. **引入 Lombok** — 当前依赖量较小，引入 Lombok 增加 IDE 配置负担，收益不大
2. **引入 MapStruct** — 仅 updateMenu 一处有 DTO 转换需求，不值得引入重型依赖
3. **关联表 Service 合并** — 当前各 Service 虽小但职责清晰，合并反而不利于后续扩展
4. **deploy/ 目录更新** — 部署配置需结合实际部署环境验证，不在本次纯代码重构范围

---

## 六、变更影响范围总览

| 影响范围 | 变更类型 | 文件数 |
|----------|----------|--------|
| 后端 import 路径 | 更新（包移动后） | ~40个 |
| 后端 Controller | 重构（减依赖/提取逻辑） | 1个 (AuthController) |
| 后端 Service | 新增方法 | 1个 (PermissionService) |
| 后端 pom.xml | 清理空标签 | 1个 |
| 后端 application.properties | 拆分文件 | 3个 |
| 前端 api/ | 新建模块 | ~7个 |
| 前端 stores/ | 重构 | 2个 |
| 前端 views/ | 移动+路径更新 | ~13个 |
| 前端 router/ | 更新路径 | 1个 (dynamicRoutes) |
| 数据库 menu 表 | 更新 menu_component 字段 | N条记录 |