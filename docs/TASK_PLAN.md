# VesselEMS 系统优化任务计划

> 创建时间：2026-06-10

---

## 任务概览

本次共涵盖 6 项需求，按依赖关系分 6 步顺序实施。

---

## 0. 路由名修正（前置修复）

**问题**：`router/index.js` 中 workspace 父路由 `name: 'Main'`，应改为 `name: 'workspace'`，以便 `router.addRoute('workspace', ...)` 注入子路由。

**改动**：
- `frontend/src/router/index.js` — `name: 'Main'` → `name: 'workspace'`

---

## 1. Super_admin 角色判断优化

**需求**：super_admin 不查验角色关联菜单、权限，自动拥有所有菜单和权限。

**方案**：在 `AuthController.permissions()` 方法开头增加短路判断，若为 super_admin 直接返回哨兵值（`menus: [-1]`, `permissions: ['*']`, `menuTree: allMenus`），跳过 6 次不必要的 JOIN 查询。

**改动**：
- `backend/src/main/java/vesselems/controller/AuthController.java` — 约 5 行

---

## 2. 菜单管理增强 + 动态路由 + 侧边栏动态渲染

**需求**：
- 菜单管理增加新增/编辑/删除/排序（上移/下移）操作
- 侧边栏从硬编码改为数据库驱动
- 路由从静态改为动态注入（菜单表 `menu_path` 配置实时生效）

**方案**：
- 后端新增 `MenuService.moveMenu(id, direction)` 交换排序值
- 后端新增 `GET /api/menus/tree` 返回有序菜单树
- 前端新建 `router/dynamicRoutes.js`，维护 `menu_component` 到 Vue 组件的映射表
- `workspace.vue` 侧边栏从 `permissionStore.menuTree` 动态渲染
- `workspace.js` 移除静态路由

**改动**：

| 文件 | 说明 |
|------|------|
| `backend/.../service/MenuService.java` | 新增 `moveMenu()` 方法 |
| `backend/.../controller/MenuController.java` | 新增 `POST /{id}/move` + `GET /tree` |
| `frontend/src/views/workspace/MenuManage.vue` | 重写为完整 CRUD + 排序按钮 |
| `frontend/src/stores/permissions.js` | 新增 `menuTree` 状态 |
| `frontend/src/router/dynamicRoutes.js` | **新建** — 组件映射表 + `registerMenuRoutes()` |
| `frontend/src/router/index.js` | `beforeEach` 中调用 `registerMenuRoutes()` |
| `frontend/src/views/workspace.vue` | 侧边栏改为从 `menuTree` 渲染 |
| `frontend/src/router/workspace.js` | 移除静态路由 |

---

## 3. 数据源管理重构

**需求**：
- 删除 `url` 字段
- 改为分字段填写：名称、数据库类型（下拉选择）、主机名/IP、端口、数据库名、用户名、密码
- 同步更新表结构、前端页面、Controller

**方案**：
- `datasource` 表新增 `db_type`, `host`, `port`, `database_name`，删除 `url`, `username`, `password` 保留
- `DSManager` 内部根据 `db_type` 拼接 JDBC URL
- 前端表单改为分字段输入 + 数据库类型下拉（MySQL/PostgreSQL/Oracle/SQL Server/MariaDB）

**数据库类型映射**：

| 选项 | 驱动类 | 默认端口 |
|------|--------|----------|
| MySQL | `com.mysql.cj.jdbc.Driver` | 3306 |
| PostgreSQL | `org.postgresql.Driver` | 5432 |
| Oracle | `oracle.jdbc.OracleDriver` | 1521 |
| SQL Server | `com.microsoft.sqlserver.jdbc.SQLServerDriver` | 1433 |
| MariaDB | `org.mariadb.jdbc.Driver` | 3306 |

**改动**：

| 文件 | 说明 |
|------|------|
| `backend/.../model/Datasource.java` | 字段重构 |
| `backend/.../controller/DatasourceController.java` | 适配新字段 |
| `backend/.../service/DSManager.java` | 内部拼接 JDBC URL |
| `frontend/src/views/workspace/DataSource.vue` | 表单改为分字段 + 类型下拉 |

---

## 4. 自然语言查询 UI 改进

**需求**：
- 新对话界面显示"请开始您的任务"标题
- 对话记录持久化当前数据源和模型（session 级别）
- 增大顶部数据源和模型选择框
- 增加 LLM 智能总结导出按钮（导出为 Markdown）

**方案**：
- 新建 `session` 表（`session_id`, `datasource_id`, `model_id`, `create_time`）
- 创建新会话时自动在 session 表插入记录
- 点击历史会话时恢复数据源和模型选择框
- 导出按钮：收集全会话轮次 → 拼装 prompt → 调 LLM 生成结构化 Markdown 报告 → 前端下载

**改动**：

| 文件 | 说明 |
|------|------|
| `backend/.../model/Session.java` | **新建** 实体 |
| `backend/.../repository/SessionRepository.java` | **新建** |
| `backend/.../service/NL2SQLService.java` | 适配 session 表，新增 `summary()` 方法 |
| `backend/.../controller/DialogController.java` | 新增 `POST /summary` 端点 |
| `frontend/src/views/workspace/NLQuery.vue` | 空对话提示、选择框尺寸、选择框持久化、导出按钮 |

---

## 5. Excel/CSV 批量导入用户

**需求**：通过上传 Excel 或 CSV 文件批量创建用户。

**方案**：
- CSV 格式：`用户名,密码,邮箱,真实姓名,手机号`
- 后端解析文件，逐行校验 + BCrypt 加密密码 + 插入数据库
- 前端上传弹窗 + 下载模板按钮

**改动**：

| 文件 | 说明 |
|------|------|
| `backend/pom.xml` | 新增 Apache POI 依赖 |
| `backend/.../controller/UserController.java` | 新增 `POST /import` 端点 |
| `backend/.../service/UserService.java` | 新增 `importUsers()` 方法 |
| `frontend/src/views/workspace/UserManage.vue` | 新增导入按钮和弹窗 |

---

## 总计换算

| 类型 | 预估量 |
|------|--------|
| 后端新增/修改文件 | 12 |
| 前端新增/修改文件 | 9 |
| Maven 新依赖 | 1 |
| 数据库新表 | 1 (session) |
| 数据库字段变更 | 1 (datasource) |
| 总代码行数 | ~440 行 |