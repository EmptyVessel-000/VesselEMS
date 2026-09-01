# VesselEMS 后端项目

VesselEMS 企业级管理系统后端，基于 **Spring Boot 4.0.3 + Java 21**，提供 RESTful API 服务。

作者：EmptyVessel

---

## 目录

- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [API 接口总览](#api-接口总览)
- [数据模型](#数据模型)
- [安全认证](#安全认证)
- [权限控制](#权限控制)
- [操作日志](#操作日志)
- [RAG 知识库](#rag-知识库)
- [NL2SQL 自然语言查询](#nl2sql-自然语言查询)
- [数据库配置](#数据库配置)
- [部署](#部署)

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.0.3 | 应用框架 |
| Java | 21 | 运行环境 |
| Spring Security | 6.x | 安全认证与授权 |
| Spring Data JPA | 3.x | 数据访问层 |
| PostgreSQL | 16+ | 主数据库 |
| MySQL | 8.x | 兼容数据库（可选） |
| jjwt | 0.12.5 | JWT 令牌生成与验证 |
| AspectJ | 1.9.22.1 | AOP 切面编程 |
| Apache POI | 5.2.5 | Excel/CSV 文件导入 |
| Apache Tika | 3.1.0 | 文档解析（PDF/Word/Markdown） |
| pgvector | 0.1.6 | PostgreSQL 向量扩展（RAG 语义搜索） |

---

## 快速开始

### 前置要求

- JDK 21+
- PostgreSQL 16+（推荐）或 MySQL 8+
- Maven 3.9+（或使用项目自带的 `mvnw.cmd`）

### 配置数据库

创建数据库和用户：

```sql
CREATE DATABASE vessel_ems;
CREATE USER vessel_user WITH PASSWORD 'Vessel127';
GRANT ALL PRIVILEGES ON DATABASE vessel_ems TO vessel_user;
```

### 配置 application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vessel_ems
    username: vessel_user
    password: Vessel127
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

### 构建与运行

```bash
# 构建
.\mvnw.cmd clean package -DskipTests

# 运行（开发模式）
.\mvnw.cmd spring-boot:run

# 运行（生产模式）
java -jar target/vesselems-0.0.1-SNAPSHOT.jar
```

服务将在 `http://localhost:8080` 启动。

---

## 项目结构

```
src/main/java/vesselems/
├── VesselEMSApplication.java          # 应用入口
├── config/                            # 配置类
│   ├── SecurityConfig.java            # Spring Security 配置（URL 权限规则）
│   └── AsyncConfig.java               # 异步任务配置
├── controller/                        # 控制层（REST API）
│   ├── AuthController.java            # 认证（登录/注册/权限查询）
│   ├── UserController.java            # 用户管理
│   ├── RoleController.java            # 角色管理
│   ├── MenuController.java            # 菜单管理
│   ├── PermissionController.java      # 权限管理
│   ├── DepartmentController.java      # 部门管理
│   ├── ConfigController.java          # 系统配置
│   ├── DatasourceController.java      # 数据源管理
│   ├── ModelController.java           # 模型配置
│   ├── DashboardController.java       # 仪表盘统计
│   ├── DialogController.java          # NL2SQL 对话历史
│   ├── LibraryController.java         # RAG 知识库管理
│   ├── DocumentController.java        # RAG 文档管理
│   ├── RAGController.java             # RAG 问答接口
│   ├── LogController.java             # 操作日志查询
│   └── LogController.java             # 操作日志查询
├── service/                           # 业务逻辑层
│   ├── AuthService.java               # 认证业务
│   ├── UserService.java               # 用户业务
│   ├── RoleService.java               # 角色业务
│   ├── MenuService.java               # 菜单业务（含树形结构）
│   ├── PermissionService.java         # 权限业务
│   ├── DepartmentService.java         # 部门业务
│   ├── ConfigService.java             # 配置业务
│   ├── DatasourceService.java         # 数据源业务
│   ├── ModelService.java              # 模型业务
│   ├── DialogService.java             # 对话业务
│   ├── NL2SQLService.java             # NL2SQL 核心引擎
│   ├── LibraryService.java            # 知识库业务
│   ├── DocumentService.java           # 文档业务
│   ├── DocumentChunkingService.java   # 文档分块
│   ├── DocumentProcessService.java    # 文档处理流程
│   ├── EmbeddingService.java          # 向量嵌入
│   ├── VectorStoreService.java        # 向量存储与检索
│   ├── RAGService.java                # RAG 问答引擎
│   ├── LLMService.java                # LLM 接口调用
│   ├── SchemaService.java             # 数据库 schema 获取
│   ├── DSManager.java                 # 数据源连接管理
│   ├── LogService.java                # 操作日志业务
│   ├── MenuPermissionService.java     # 菜单-权限关联
│   ├── PermissionRoleService.java     # 权限-角色关联
│   ├── RoleMenuService.java           # 角色-菜单关联
│   └── UserRoleService.java           # 用户-角色关联
├── repository/                        # 数据访问层（JPA Repository）
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── MenuRepository.java
│   ├── PermissionRepository.java
│   ├── DepartmentRepository.java
│   ├── ConfigRepository.java
│   ├── DatasourceRepository.java
│   ├── ModelRepository.java
│   ├── DialogRepository.java
│   ├── LibraryRepository.java
│   ├── DocumentRepository.java
│   ├── AnnotationRepository.java
│   ├── LogRepository.java
│   ├── UserRoleRepository.java
│   ├── RoleMenuRepository.java
│   ├── PermissionRoleRepository.java
│   ├── MenuPermissionRepository.java
│   └── SessionRepository.java
├── model/                             # 数据模型（JPA Entity）
│   ├── User.java                      # 用户
│   ├── Role.java                      # 角色
│   ├── Menu.java                      # 菜单
│   ├── Permission.java                # 权限
│   ├── Department.java                # 部门
│   ├── Config.java                    # 系统配置
│   ├── Datasource.java                # 数据源
│   ├── Model.java                     # 模型
│   ├── Dialog.java                    # 对话记录
│   ├── Library.java                   # 知识库
│   ├── Document.java                  # 文档
│   ├── Annotation.java                # 标注
│   ├── Log.java                       # 操作日志
│   ├── UserRole.java                  # 用户-角色关联
│   ├── RoleMenu.java                  # 角色-菜单关联
│   ├── PermissionRole.java            # 权限-角色关联
│   ├── MenuPermission.java            # 菜单-权限关联
│   └── Session.java                   # 会话
├── dto/                               # 数据传输对象
│   ├── LoginDto.java                  # 登录请求
│   ├── RegisterDto.java               # 注册请求
│   ├── CreateUserDto.java             # 创建用户请求
│   ├── UserUpdateDto.java             # 更新用户请求
│   └── UserResponseDto.java           # 用户响应
├── security/                          # 安全模块
│   ├── JwtService.java                # JWT 令牌服务
│   ├── JwtFilter.java                 # JWT 认证过滤器
│   └── LocalUserDetailsService.java   # 用户详情服务
├── common/                            # 通用工具
│   └── ApiResponse.java               # 统一 API 响应封装
├── exception/                         # 异常处理
│   └── GlobalExceptionHandler.java    # 全局异常处理器
└── annotation/                        # 自定义注解
    └── OperateLog.java                # 操作日志注解
```

---

## API 接口总览

### 认证模块

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/auth/login` | POST | 用户登录 | 公开 |
| `/api/auth/register` | POST | 用户注册 | 公开 |
| `/api/auth/permissions` | GET | 获取当前用户权限（菜单树+权限码） | 登录 |

### 用户管理

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/users` | GET | 用户列表 | 登录 |
| `/api/users/members` | GET | 成员列表 | 登录 |
| `/api/users/{id}` | GET | 用户详情 | 登录 |
| `/api/users` | POST | 新增用户 | `user:create` |
| `/api/users/{id}` | DELETE | 删除用户 | `user:delete` |
| `/api/users/{id}/info` | PUT | 修改用户信息 | `user:edit` |
| `/api/users/{id}/password` | PATCH | 修改密码 | `user:edit` |
| `/api/users/import` | POST | Excel 导入用户 | `user:create` |

### 角色管理

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/roles` | GET | 角色列表 | 登录 |
| `/api/roles/{id}` | GET | 角色详情 | 登录 |
| `/api/roles` | POST | 新增角色 | `role:create` |
| `/api/roles/{id}` | PUT | 修改角色 | `role:edit` |
| `/api/roles/{id}` | DELETE | 删除角色 | `role:delete` |
| `/api/roles/{id}/menus` | POST | 分配菜单 | `role:assign` |
| `/api/roles/{id}/menus` | GET | 获取角色菜单 | 登录 |
| `/api/roles/{id}/permissions` | POST | 分配权限 | `role:assign` |
| `/api/roles/{id}/permissions` | GET | 获取角色权限 | 登录 |
| `/api/roles/{id}/permissions/tree` | GET | 权限树（按菜单分组） | 登录 |

### 菜单管理

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/menus` | GET | 菜单列表 | 登录 |
| `/api/menus/{id}` | GET | 菜单详情 | 登录 |
| `/api/menus/tree` | GET | 完整嵌套树 | 登录 |
| `/api/menus` | POST | 新增菜单 | `menu:create` |
| `/api/menus/{id}` | PUT | 修改菜单 | `menu:edit` |
| `/api/menus/{id}` | DELETE | 删除菜单 | `menu:delete` |
| `/api/menus/{id}/move` | POST | 移动排序 | `menu:move` |

### 权限管理

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/permissions` | GET | 权限列表 | 登录 |
| `/api/permissions/{id}` | GET | 权限详情 | 登录 |
| `/api/permissions` | POST | 新增权限 | `perm:create` |
| `/api/permissions/{id}` | PUT | 修改权限 | `perm:edit` |
| `/api/permissions/{id}` | DELETE | 删除权限 | `perm:delete` |

### 部门管理

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/departments` | GET | 部门列表 | 登录 |
| `/api/departments/{id}` | GET | 部门详情 | 登录 |
| `/api/departments` | POST | 新增部门 | `dept:create` |
| `/api/departments/{id}` | PUT | 修改部门 | `dept:edit` |
| `/api/departments/{id}` | DELETE | 删除部门 | `dept:delete` |

### 系统配置

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/configs` | GET | 配置列表 | 登录 |
| `/api/configs/{id}` | GET | 配置详情 | 登录 |
| `/api/configs` | POST | 新增配置 | `config:create` |
| `/api/configs/{id}` | PUT | 修改配置 | `config:edit` |
| `/api/configs/{id}` | DELETE | 删除配置 | `config:delete` |

### 数据源管理

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/ds` | GET | 数据源列表 | 登录 |
| `/api/ds/{id}` | GET | 数据源详情 | 登录 |
| `/api/ds` | POST | 新增数据源 | `ds:create` |
| `/api/ds/{id}` | PUT | 修改数据源 | `ds:edit` |
| `/api/ds/{id}` | DELETE | 删除数据源 | `ds:delete` |
| `/api/ds/{id}/test` | POST | 测试连接 | 登录 |
| `/api/ds/{id}/schema` | GET | 获取数据库 schema | 登录 |

### 模型配置

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/model` | GET | 模型列表 | 登录 |
| `/api/model/{id}` | GET | 模型详情 | 登录 |
| `/api/model` | POST | 新增模型 | `model:create` |
| `/api/model/{id}` | PUT | 修改模型 | `model:edit` |
| `/api/model/{id}` | DELETE | 删除模型 | `model:delete` |

### 仪表盘

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/dashboard/stats` | GET | 统计数据 | 登录 |
| `/api/dashboard/recent-activities` | GET | 最近活动 | 登录 |

### 操作日志

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/logs` | GET | 日志列表（分页） | 登录 |

### RAG 知识库

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/libraries` | GET | 知识库列表 | 登录 |
| `/api/libraries` | POST | 创建知识库 | 登录 |
| `/api/libraries/{id}` | PUT | 修改知识库 | 登录 |
| `/api/libraries/{id}` | DELETE | 删除知识库 | 登录 |
| `/api/documents` | GET | 文档列表 | 登录 |
| `/api/documents` | POST | 上传文档 | 登录 |
| `/api/documents/{id}` | DELETE | 删除文档 | 登录 |
| `/api/rag/query` | POST | RAG 问答 | 登录 |

### NL2SQL

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/dialogs` | GET | 对话历史列表 | 登录 |
| `/api/dialogs` | POST | 创建对话 | 登录 |
| `/api/dialogs/{id}` | DELETE | 删除对话 | 登录 |
| `/api/nl2sql/query` | POST | 自然语言查询 | 登录 |

---

## 数据模型

### 核心实体关系

```
User ──< UserRole >── Role ──< RoleMenu >── Menu
                          │
                          └──< PermissionRole >── Permission ──< MenuPermission >── Menu
```

### 菜单表 (menu)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| parent_id | BIGINT | 父菜单 ID（支持多级嵌套） |
| menu_name | VARCHAR(100) | 菜单名称 |
| menu_path | VARCHAR(256) | 路由路径（相对/绝对） |
| menu_component | VARCHAR(256) | Vue 组件路径 |
| menu_icon | VARCHAR(128) | 图标名称 |
| menu_type | INT | 1=页面, 2=分组 |
| visible | INT | 0=隐藏, 1=显示 |
| sort_order | INT | 排序序号 |
| status | INT | 状态（0=禁用, 1=启用） |

### 权限表 (permission)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| permission_code | VARCHAR(100) | 权限标识（如 `user:create`） |
| description | VARCHAR(255) | 权限描述 |

### 完整权限清单（33 个）

| 模块 | 权限码 | 说明 |
|------|--------|------|
| 用户管理 | `user:create` | 新增用户 |
| 用户管理 | `user:edit` | 修改用户 |
| 用户管理 | `user:delete` | 删除用户 |
| 角色管理 | `role:create` | 新增角色 |
| 角色管理 | `role:edit` | 修改角色 |
| 角色管理 | `role:delete` | 删除角色 |
| 角色管理 | `role:assign` | 分配菜单/权限 |
| 系统配置 | `config:create` | 新增配置 |
| 系统配置 | `config:edit` | 修改配置 |
| 系统配置 | `config:delete` | 删除配置 |
| 菜单管理 | `menu:create` | 新增菜单 |
| 菜单管理 | `menu:edit` | 修改菜单 |
| 菜单管理 | `menu:delete` | 删除菜单 |
| 菜单管理 | `menu:move` | 移动排序 |
| 权限管理 | `perm:create` | 新增权限 |
| 权限管理 | `perm:edit` | 修改权限 |
| 权限管理 | `perm:delete` | 删除权限 |
| 部门管理 | `dept:create` | 新增部门 |
| 部门管理 | `dept:edit` | 修改部门 |
| 部门管理 | `dept:delete` | 删除部门 |
| 数据源管理 | `ds:create` | 新增数据源 |
| 数据源管理 | `ds:edit` | 修改数据源 |
| 数据源管理 | `ds:delete` | 删除数据源 |
| 模型管理 | `model:create` | 新增模型 |
| 模型管理 | `model:edit` | 修改模型 |
| 模型管理 | `model:delete` | 删除模型 |
| 对话管理 | `dialog:delete` | 删除对话 |
| RAG 知识库 | `rag:lib:create` | 创建知识库 |
| RAG 知识库 | `rag:lib:edit` | 修改知识库 |
| RAG 知识库 | `rag:lib:delete` | 删除知识库 |
| RAG 文档 | `rag:doc:upload` | 上传文档 |
| RAG 文档 | `rag:doc:delete` | 删除文档 |
| NL2SQL | `nl2sql:export` | 导出查询结果 |

---

## 安全认证

### JWT 认证流程

```
客户端请求 → JwtFilter.doFilterInternal()
  ├── 公开路径（/api/auth/login, /api/auth/register）→ 直接放行
  ├── 无 Token 或 Token 无效 → 返回 401
  └── Token 有效 → 解析 userId → 加载权限 → 设置 SecurityContext
```

### JwtFilter 权限加载逻辑

```
1. 查询用户的角色列表
2. 遍历角色：
   ├── 添加 ROLE_xxx 角色标识
   └── 如果是 super_admin → 标记为超级管理员
3. 权限加载：
   ├── 超级管理员 → 加载全部 33 个权限码（permissionRepository.findAll()）
   └── 普通用户 → 根据角色关联的权限 ID 加载对应权限码
4. 设置 UsernamePasswordAuthenticationToken → SecurityContextHolder
```

### URL 模式匹配权限控制

权限规则在 `SecurityConfig.java` 中通过 URL 模式统一配置，Controller 上无需 `@PreAuthorize` 注解：

```java
// 示例：用户管理权限规则
.requestMatchers(HttpMethod.POST, "/api/users/**").hasAuthority("user:create")
.requestMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority("user:edit")
.requestMatchers(HttpMethod.PATCH, "/api/users/**").hasAuthority("user:edit")
.requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("user:delete")
```

**规则**：
- **GET 请求**（读操作）→ 登录即可访问
- **POST/PUT/DELETE**（写操作）→ 需要对应权限码
- **超级管理员** → JwtFilter 自动注入全部权限码，所有检查通过

---

## 操作日志

### 注解方式

使用 `@OperateLog` 注解自动记录操作日志：

```java
@PostMapping
@OperateLog(module = "用户管理", operation = "新增用户")
public ApiResponse<User> create(@Valid @RequestBody CreateUserDto request) {
    return ApiResponse.success(userService.createUser(request));
}
```

### AOP 切面

`LogAspect` 环绕通知自动捕获：
- 请求方法、URL、IP
- 操作结果（成功/失败）
- 执行耗时
- 异常信息

### 日志表结构

```sql
CREATE TABLE log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(100),
    module          VARCHAR(100),     -- 操作模块
    operation       VARCHAR(200),     -- 操作描述
    request_method  VARCHAR(10),      -- HTTP 方法
    request_url     VARCHAR(500),     -- 请求 URL
    ip              VARCHAR(50),      -- 客户端 IP
    status          INT DEFAULT 1,    -- 1=成功, 0=失败
    duration        BIGINT,           -- 执行耗时(ms)
    error_msg       TEXT,             -- 错误信息
    create_time     TIMESTAMP
);
```

---

## RAG 知识库

### 架构

```
文档上传 → DocumentProcessService
  ├── Apache Tika 解析（PDF/Word/Markdown/TXT → 纯文本）
  └── DocumentChunkingService 分块
       └── EmbeddingService 生成向量
            └── VectorStoreService 存储到 pgvector

用户提问 → RAGService
  ├── EmbeddingService 生成问题向量
  ├── VectorStoreService 相似度检索（pgvector 余弦距离）
  └── LLMService 调用大模型生成回答
```

### 技术组件

- **文档解析**：Apache Tika 3.1.0（支持 PDF、DOCX、Markdown、TXT）
- **向量存储**：PostgreSQL pgvector 扩展
- **向量检索**：余弦相似度（COSINE distance）
- **LLM 接口**：通过 `LLMService` 调用外部大模型 API

---

## NL2SQL 自然语言查询

### 流程

```
用户输入自然语言 → NL2SQLService
  ├── SchemaService 获取数据库表结构
  ├── LLMService 生成 SQL
  ├── 执行 SQL 查询
  └── 返回结果集

对话历史 → DialogService 持久化
```

### 数据源连接管理

`DSManager` 管理多个数据源的 JDBC 连接池，支持：
- 连接池缓存与自动失效
- 连接测试
- Schema 获取

---

## 数据库配置

### 主数据库（PostgreSQL）

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vessel_ems
    username: vessel_user
    password: Vessel127
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
```

### pgvector 扩展

RAG 功能需要启用 pgvector 扩展：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### 兼容 MySQL

项目也支持 MySQL，切换配置即可：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vessel_ems?useUnicode=true&characterEncoding=utf-8
    username: root
    password: your_password
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
```

---

## 部署

### Docker 部署

项目提供完整的 Docker 部署方案，详见 `deploy/` 目录：

```bash
cd deploy
docker-compose build
docker-compose up -d
```

### 部署架构

```
┌─────────────┐     ┌─────────────┐     ┌──────────────┐
│  前端容器    │────▶│  后端容器    │────▶│  主机 PostgreSQL│
│  (Nginx)    │     │  (Spring)   │     │  (host.docker)│
└─────────────┘     └─────────────┘     └──────────────┘
```

### 端口映射

| 服务 | 容器端口 | 主机端口 |
|------|---------|---------|
| 前端 (Nginx) | 80 | 80 |
| 后端 (Spring) | 8080 | 8080 |
| PostgreSQL | 5432 | 5432 |

---

## 开发规范

### 代码分层

```
Controller → Service → Repository
                ↓
            Model (Entity)
```

- **Controller**：接收请求、参数校验、调用 Service、返回响应
- **Service**：业务逻辑、事务管理
- **Repository**：数据访问（JPA Repository）
- **Model**：JPA Entity，映射数据库表

### API 响应格式

统一使用 `ApiResponse` 封装：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 异常处理

`GlobalExceptionHandler` 统一处理：
- `AccessDeniedException` → 403 无权限
- `IllegalArgumentException` → 400 参数错误
- `Exception` → 500 服务器错误

---

## 作者

EmptyVessel