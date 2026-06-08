# VesselEMS 后端项目

VesselEMS 企业级管理系统后端，基于 Spring Boot 4.0.3 + Java 21。

作者：EmptyVessel

## 技术栈

- **Spring Boot 4.0.3** - 应用框架
- **Spring Security** - 安全认证（JWT 无状态）
- **Spring Data JPA** - 数据访问
- **PostgreSQL** - 数据库
- **jjwt 0.12.5** - JWT 令牌

## 快速开始

```bash
# 构建
.\mvnw.cmd clean package

# 运行（开发环境）
.\mvnw.cmd spring-boot:run
```

服务将在 `http://localhost:8080` 启动。

## 项目结构

```
src/main/java/vesselems/
├── config/       # 配置类
├── controller/   # 控制层
├── service/      # 业务逻辑
├── repository/   # 数据访问
├── model/        # 数据模型
├── dto/          # 数据传输对象
├── security/     # JWT 安全
├── common/       # 通用工具
└── exception/    # 异常处理
```

## 作者

EmptyVessel