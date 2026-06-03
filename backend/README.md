# Worklist - 任务管理系统

基于 Spring Boot + Vue.js 的任务管理系统，支持多角色权限分离和完整的任务生命周期管理。

## 功能特性

- **多角色权限** - Guest（甲方）、Member（执行方）、Manager（协调方）
- **任务管理** - 创建、分配、执行、验收、打回
- **统计分析** - 任务统计、绩效分析、七日趋势
- **安全认证** - Spring Security 权限控制

## 技术栈

- **后端** - Spring Boot 3.x、Spring Security、MySQL 8.0
- **前端** - Vue.js 3、Element Plus、ECharts 5

## 快速开始

### 环境要求
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 安装运行

```bash
# 克隆项目
git clone https://github.com/EmptyVessel-000/Worklist.git
cd Worklist

# 配置数据库
# 修改 application.properties 中的数据库连接信息

# 构建运行
mvn clean package
java -jar target/worklist-1.0.0.jar

# 访问系统
# http://localhost:8080
```

## 项目结构

```
src/main/
├── java/emptyvessel/worklist/
│   ├── config/       # 配置类
│   ├── controller/   # 控制层
│   ├── service/      # 业务逻辑
│   ├── repository/   # 数据访问
│   ├── model/        # 数据模型
│   └── dto/          # 数据传输对象
└── resources/
    └── static/       # 前端页面
```

## 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 作者

EmptyVessel