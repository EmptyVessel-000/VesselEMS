# VesselEMS 前端项目

VesselEMS 企业级管理系统前端，使用 Vue 3 + Vite 构建。

作者：EmptyVessel

## 技术栈

- **Vue 3** - 前端框架
- **Vite** - 构建工具
- **Element Plus** - UI 组件库
- **Axios** - HTTP 客户端
- **Pinia** - 状态管理
- **Vue Router** - 路由管理

## 快速开始

```bash
npm install
npm run dev
```

开发服务器将在 `http://localhost:5173` 启动。

### 生产构建

```bash
npm run build
```

构建后的文件将输出到 `dist` 目录。

## 后端 API 配置

前端通过 Vite 代理访问后端 API，配置在 `vite.config.js` 中：

- 后端 API 地址：`http://localhost:8080`
- 前端代理路径：`/api`

所有以 `/api` 开头的请求都会被代理到后端。