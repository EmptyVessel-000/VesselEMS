# VesselEMS Arch Linux 混合部署指南

## 部署架构
- **前端**: Docker 容器 (Nginx)
- **后端**: Docker 容器 (Spring Boot)  
- **数据库**: 主机 PostgreSQL (默认本地地址)

---

## 前置要求

### 1. Arch Linux 服务器安装 Docker
```bash
# 安装 Docker 和 Docker Compose
sudo pacman -S docker docker-compose

# 启动 Docker 服务
sudo systemctl start docker
sudo systemctl enable docker

# 添加当前用户到 docker 组（可选，避免每次都用 sudo）
sudo usermod -aG docker $USER
```

### 2. 验证 PostgreSQL 在主机运行
```bash
# 检查 PostgreSQL 是否运行
sudo systemctl status postgresql

# 如果未运行，启动 PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# 验证数据库连接（默认用户：postgres）
psql -U postgres -d template1
```

### 3. 创建必需的数据库和用户
```bash
# 以 postgres 用户登录
sudo -u postgres psql

# 在 PostgreSQL 提示符中执行以下命令：
CREATE DATABASE vessel_ems;
CREATE USER vessel_user WITH PASSWORD 'Vessel127';
ALTER ROLE vessel_user SET client_encoding TO 'utf8';
ALTER ROLE vessel_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE vessel_user SET default_transaction_deferrable TO on;
ALTER ROLE vessel_user SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE vessel_ems TO vessel_user;
\q
```

---

## 部署步骤

### 1. 克隆项目到服务器
```bash
cd /home/user  # 或其他合适的目录
git clone https://github.com/EmptyVessel-000/VesselEMS.git
cd VesselEMS
```

### 2. 配置 PostgreSQL 远程访问（Docker 容器连接）
编辑 `/etc/postgresql/postgresql.conf`（Arch Linux 通常在 `/var/lib/postgres/data/`）：
```bash
# 找到 listen_addresses 行，改为：
listen_addresses = 'localhost'  # Docker 容器通过 host.docker.internal 访问

# 或者允许 Docker 网桥访问（不推荐）
listen_addresses = '*'
```

编辑 `/var/lib/postgres/data/pg_hba.conf`，确保包含：
```
# Docker 容器本地访问
host    all    all    127.0.0.1/32    md5
host    all    all    ::1/128         md5
```

重启 PostgreSQL：
```bash
sudo systemctl restart postgresql
```

### 3. 构建并启动 Docker 容器
```bash
cd deploy

# 构建镜像
docker-compose build

# 启动容器
docker-compose up -d

# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend
```

### 4. 验证部署

**检查容器运行状态：**
```bash
docker ps
```

**测试后端 API：**
```bash
curl http://localhost:8080/api/auth/permissions
# 如果看到 401 错误或 JSON 响应，说明后端正常运行
```

**访问前端：**
```
http://your-server-ip:80
或
http://localhost:80
```

**检查后端数据库连接：**
```bash
# 查看后端容器日志
docker-compose logs backend

# 寻找 "Connected to PostgreSQL" 或类似消息
```

---

## 故障排查

### 问题 1：后端无法连接数据库
**症状：** 后端容器日志中看到 `Connection refused`

**解决方案：**
```bash
# 检查主机 PostgreSQL 是否运行
sudo systemctl status postgresql

# 检查 PostgreSQL 监听地址
sudo -u postgres psql -c "SHOW listen_addresses;"

# 从 Docker 容器内部测试连接（替换为容器 ID）
docker exec vesselems-backend bash -c "nc -zv host.docker.internal 5432"
```

### 问题 2：前端无法访问后端 API
**症状：** 前端网页加载但无法调用 API

**解决方案：**
```bash
# 检查 nginx 配置是否正确
docker exec vesselems-frontend nginx -t

# 查看 nginx 日志
docker exec vesselems-frontend tail -f /var/log/nginx/error.log

# 确保 backend 容器可访问
docker exec vesselems-frontend curl http://backend:8080/api/auth/permissions
```

### 问题 3：容器启动失败
**症状：** `docker-compose up` 返回错误

**解决方案：**
```bash
# 查看详细错误日志
docker-compose logs

# 清理并重新构建
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

---

## 停止和清理

```bash
# 停止容器但保留数据
docker-compose stop

# 启动已停止的容器
docker-compose start

# 完全移除容器但保留镜像
docker-compose down

# 移除所有数据（谨慎！）
docker-compose down -v
```

---

## 日常管理

**查看所有容器日志：**
```bash
docker-compose logs -f
```

**重启特定容器：**
```bash
docker-compose restart backend
docker-compose restart frontend
```

**更新代码并重新部署：**
```bash
git pull origin main
docker-compose up -d --build
```

---

## 关键配置说明

**docker-compose.yml 中的重要变更：**
- ✅ 删除了 `db` 服务（PostgreSQL 在主机运行）
- ✅ 数据库连接改为 `jdbc:postgresql://host.docker.internal:5432/vessel_ems`
- ✅ 添加了 Docker 自定义网络 `vesselems-network`
- ✅ 前端 nginx 配置通过 volumes 挂载

**frontend/Dockerfile 中的重要变更：**
- ✅ 添加了 nginx.conf 的 COPY 命令（用于 API 代理配置）

---

## 端口配置

| 服务 | 容器端口 | 主机端口 | 访问地址 |
|------|---------|---------|---------|
| 前端 (Nginx) | 80 | 80 | http://localhost:80 |
| 后端 (Spring) | 8080 | 8080 | http://localhost:8080 |
| PostgreSQL | 5432 | 5432 | localhost:5432 |

---

## 安全建议

1. **生产环境修改数据库密码**
   - 当前密码 `Vessel127` 需要更改
   - 在 docker-compose.yml 中修改环境变量
   - 在 PostgreSQL 中更新用户密码

2. **配置 HTTPS/SSL**
   - 编辑 nginx.conf 添加 SSL 配置
   - 获取 Let's Encrypt 证书

3. **设置防火墙规则**
   ```bash
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   sudo ufw allow 8080/tcp
   ```

---

**部署完成！** 🎉