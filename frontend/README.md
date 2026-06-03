# Worklist 前端项目

这是 Worklist 任务管理系统的前端项目，使用 Vue 3 + Vite 构建。

## 项目结构

```
frontend/
├── src/
│   ├── main.js           # 应用入口
│   └── App.vue           # 根组件
├── public/               # 静态资源（HTML、CSS、JS、图片等）
├── index.html            # HTML 入口文件
├── package.json          # 项目依赖配置
├── vite.config.js        # Vite 配置文件
└── README.md             # 项目说明
```

## 技术栈

- **Vue 3** - 前端框架
- **Vite** - 构建工具
- **Element Plus** - UI 组件库
- **ECharts** - 数据可视化
- **Axios** - HTTP 客户端

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

开发服务器将在 `http://localhost:5173` 启动，并自动打开浏览器。

### 生产构建

```bash
npm run build
```

构建后的文件将输出到 `dist` 目录。

### 预览构建结果

```bash
npm run preview
```

## 后端 API 配置

前端通过代理访问后端 API，代理配置在 `vite.config.js` 中：

- 后端 API 地址：`http://localhost:8080`
- 前端代理路径：`/api`

所有以 `/api` 开头的请求都会被代理到后端。

## 功能模块

- **首页** - 项目介绍和快速导航
- **登录/注册** - 用户认证
- **任务管理** - 创建、编辑、分配任务
- **统计分析** - 任务统计和数据分析
- **角色管理** - 不同角色的权限控制

## 开发指南

### 添加新页面

1. 在 `src/pages` 目录下创建新的 `.vue` 文件
2. 在路由配置中添加路由规则
3. 在导航菜单中添加链接

### 调用后端 API

使用 Axios 调用后端 API：

```javascript
import axios from 'axios'

axios.get('/api/users').then(response => {
  console.log(response.data)
})
```

## 注意事项

- 确保后端服务运行在 `http://localhost:8080`
- 开发时使用 `npm run dev` 启动开发服务器
- 生产部署前运行 `npm run build` 进行构建