#!/bin/bash

# VesselEMS 生产环境部署脚本（Arch Linux）
# 使用说明：bash deploy.sh

set -e  # 任何错误都会停止执行

echo "======================================"
echo "VesselEMS 部署脚本"
echo "======================================"
echo ""

# 1. 检查必要工具
echo "📋 检查必要工具..."

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装"
    echo "安装方式：sudo pacman -S docker"
    exit 1
fi

# 检查 docker-compose
if ! command -v docker-compose &> /dev/null; then
    echo "⚠️  docker-compose 未找到"
    echo "📥 正在安装 docker-compose..."
    sudo pacman -S --noconfirm docker-compose
fi

echo "✅ Docker 版本：$(docker --version)"
echo "✅ Docker Compose 版本：$(docker-compose --version)"
echo ""

# 2. 启动 Docker 服务
echo "🐳 启动 Docker 服务..."
sudo systemctl start docker
sudo systemctl enable docker
echo "✅ Docker 服务已启动"
echo ""

# 3. 检查 PostgreSQL
echo "🗄️  检查 PostgreSQL..."
if ! sudo systemctl is-active --quiet postgresql; then
    echo "⚠️  PostgreSQL 未运行"
    echo "📥 启动 PostgreSQL..."
    sudo systemctl start postgresql
    sudo systemctl enable postgresql
fi

# 验证数据库连接
echo "🔐 验证数据库..."
if ! sudo -u postgres psql -c "SELECT 1" > /dev/null 2>&1; then
    echo "❌ PostgreSQL 连接失败"
    exit 1
fi

# 检查数据库和用户
if ! sudo -u postgres psql -lqt | cut -d \| -f 1 | grep -qw vessel_ems; then
    echo "📝 创建数据库 vessel_ems..."
    sudo -u postgres psql << EOF
CREATE DATABASE vessel_ems;
CREATE USER vessel_user WITH PASSWORD 'Vessel127';
ALTER ROLE vessel_user SET client_encoding TO 'utf8';
ALTER ROLE vessel_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE vessel_user SET default_transaction_deferrable TO on;
ALTER ROLE vessel_user SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE vessel_ems TO vessel_user;
EOF
    echo "✅ 数据库已创建"
else
    echo "✅ 数据库已存在"
fi
echo ""

# 4. 配置 Docker 网络（可选但推荐）
echo "🌐 配置 Docker 网络..."
if ! docker network ls | grep -q vesselems-network; then
    docker network create vesselems-network
    echo "✅ Docker 网络已创建"
else
    echo "✅ Docker 网络已存在"
fi
echo ""

# 5. 构建镜像
echo "🔨 构建 Docker 镜像..."
echo "   构建后端镜像..."
docker-compose build backend
echo "   构建前端镜像..."
docker-compose build frontend
echo "✅ 镜像构建完成"
echo ""

# 6. 启动容器
echo "🚀 启动容器..."
docker-compose up -d
echo "✅ 容器已启动"
echo ""

# 7. 等待服务就绪
echo "⏳ 等待服务就绪（最多 30 秒）..."
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if curl -s http://localhost:8080/api/auth/permissions > /dev/null 2>&1; then
        echo "✅ 后端服务已就绪"
        break
    fi
    echo "   等待后端服务... ($((attempt + 1))/$max_attempts)"
    sleep 1
    ((attempt++))
done

if [ $attempt -eq $max_attempts ]; then
    echo "⚠️  后端服务可能未就绪，请检查日志"
fi
echo ""

# 8. 验证部署
echo "✔️  验证部署..."
echo ""
echo "📊 容器状态："
docker-compose ps
echo ""

# 测试后端 API
echo "🔌 测试后端 API："
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/auth/permissions 2>/dev/null || echo "000")
if [ "$response" = "401" ] || [ "$response" = "403" ]; then
    echo "   ✅ 后端 API 响应正常（HTTP $response）"
elif [ "$response" = "200" ]; then
    echo "   ✅ 后端 API 无需认证（HTTP $response）"
else
    echo "   ⚠️  后端 API 无响应（HTTP $response）"
fi

echo ""
echo "🎉 部署完成！"
echo ""
echo "📍 访问地址："
echo "   前端：http://localhost:80"
echo "   后端：http://localhost:8080"
echo ""
echo "📝 查看日志："
echo "   docker-compose logs -f"
echo ""
echo "⛔ 停止服务："
echo "   docker-compose down"
echo ""