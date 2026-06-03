# Worklist 项目 AI 指导（instructions.md）

description: "Worklist 项目 AI 行为守则，供 Copilot / Agent 自动加载"

## 一、核心原则

1. **极简优先**：不引入不必要依赖，能手写的尽量手写，避免过度框架。
2. **环境隔离**：开发/生产配置完全分离，使用 `application-{profile}.properties` + Docker env 环境变量。
3. **容器化交付**：目标是“任何装了 Docker 的环境都能跑起来”。
4. **半自动部署**：代码提交在 GitHub，部署通过脚本或 CI 实现 `git pull`、构建、重启。

---

## 二、标准栈（当前沿用）

- 后端：Spring Boot + Maven + REST API
- 前端：Vue 3 选项式 API（当前静态引入 `vue.js`）
- 数据库：PostgreSQL（Spring Data JPA）
- 容器化：Docker 多阶段构建（构建镜像 + 运行镜像）
- 配置：Spring Profile（`dev` / `prod`）
- 版本控制：Git（建议 main 分支策略，保护分支由团队配置）

> 只遵循项目需要的标准，不刻意引入额外规范。

---

## 三、当前实现（与代码一致）

- 前端：左右栏布局、任务状态滑块、弹窗表单、剩余时间计算 + 超期标红、页面切换用 `v-show`。
- 后端：`TeskController` / `TeskRepository` / `Tesk` 实体（id、name、description、startTime、endTime、status）。
- 部署：脚本 `worklist-deploy.sh` -> `git pull` / `docker build` / `docker stop/rm` / `docker run`，容器名 worklist，端口 8080:8080，重启策略 `unless-stopped`，生产 `SPRING_PROFILES_ACTIVE=prod`。

---

## 四、禁止事项

- 禁止硬编码 IP、端口、密码。
- 禁止在生产配置中写入真实密码（使用环境变量或 CI/CD Secret）。
- 禁止在生产机器上手动修改代码，变更必须走 Git（`git pull` / CI 流程）。
- 禁止在 Windows 环境直接运行生产配置（本地开发与生产隔离）。

---

## 五、AI 交互守则

- 每次交互时，优先遵循此 `instructions.md` 中的规范。
- 如果做出建议，必须考虑 `dev` / `prod` 配置分离与容器化部署机制。
- 对于代码审查和改进场景，优先保证“可读性、错误少、依赖少、实现可运行”。
- 对于性能/安全性建议，建议使用现有项目约定（Spring Boot + JPA + PostgreSQL + Docker）并写出迁移路径。

---

## 六、总结

本文件是 Worklist 项目长期运营规则，供所有自动化 Agent 在每次运行时参考。遵守项目原则、当前栈、禁止清单，并在建议中尽量提供可落地方案。

## 七、相关参考文档

- 业务目标、差异与迭代计划参见：`docs/roadmap.md`
- 设计与可行性分析可写入：`docs/feasibility-study.md` 或 `docs/project-overview.md`