#!/bin/bash

# 图书馆管理系统 - 服务器部署脚本（离线版本）
# 适用于Ubuntu 22.04，已有MySQL和Docker环境

set -e  # 遇到错误立即退出

echo "===================================="
echo "图书馆管理系统 - 服务器部署脚本"
echo "===================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 版本配置
VERSION="v1.0"
BACKEND_TAR="library-backend-${VERSION}.tar"
FRONTEND_TAR="library-frontend-${VERSION}.tar"

# 配置参数（请根据实际情况修改）
MYSQL_HOST="localhost"
MYSQL_PORT="3306"
MYSQL_DATABASE="library_management"
MYSQL_USER="root"
MYSQL_PASSWORD="PS*RA3sE:6M27F)C*j<2"

echo -e "${YELLOW}[步骤 1/8] 检查系统环境...${NC}"
# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}[错误] 请使用root权限运行此脚本${NC}"
    echo "请执行: sudo bash $0"
    exit 1
fi

# 检查操作系统
if [ ! -f /etc/os-release ]; then
    echo -e "${RED}[错误] 无法检测操作系统版本${NC}"
    exit 1
fi

source /etc/os-release
echo -e "${GREEN}[✓] 操作系统: $PRETTY_NAME${NC}"

# 检查Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}[错误] 未检测到Docker，请先安装Docker${NC}"
    exit 1
fi
echo -e "${GREEN}[✓] Docker版本: $(docker --version)${NC}"

# 检查Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}[错误] 未检测到Docker Compose，请先安装${NC}"
    exit 1
fi
echo -e "${GREEN}[✓] Docker Compose版本: $(docker-compose --version)${NC}"

# 检查MySQL
if ! command -v mysql &> /dev/null; then
    echo -e "${YELLOW}[警告] 未检测到MySQL客户端，跳过MySQL连接测试${NC}"
else
    echo -e "${GREEN}[✓] MySQL客户端已安装${NC}"
fi
echo ""

echo -e "${YELLOW}[步骤 2/8] 检查镜像文件...${NC}"
if [ ! -f "$BACKEND_TAR" ]; then
    echo -e "${RED}[错误] 找不到后端镜像文件: $BACKEND_TAR${NC}"
    echo "请确保已上传所有镜像文件到当前目录"
    exit 1
fi

if [ ! -f "$FRONTEND_TAR" ]; then
    echo -e "${RED}[错误] 找不到前端镜像文件: $FRONTEND_TAR${NC}"
    echo "请确保已上传所有镜像文件到当前目录"
    exit 1
fi

if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}[错误] 找不到docker-compose.yml文件${NC}"
    exit 1
fi

echo -e "${GREEN}[✓] 后端镜像: $BACKEND_TAR ($(du -h $BACKEND_TAR | cut -f1))${NC}"
echo -e "${GREEN}[✓] 前端镜像: $FRONTEND_TAR ($(du -h $FRONTEND_TAR | cut -f1))${NC}"
echo -e "${GREEN}[✓] Docker Compose配置文件存在${NC}"
echo ""

echo -e "${YELLOW}[步骤 3/8] 导入后端镜像...${NC}"
docker load -i "$BACKEND_TAR"
echo -e "${GREEN}[✓] 后端镜像导入成功${NC}"
echo ""

echo -e "${YELLOW}[步骤 4/8] 导入前端镜像...${NC}"
docker load -i "$FRONTEND_TAR"
echo -e "${GREEN}[✓] 前端镜像导入成功${NC}"
echo ""

echo -e "${YELLOW}[步骤 5/8] 验证镜像...${NC}"
docker images | grep library
echo ""

echo -e "${YELLOW}[步骤 6/8] 创建必要的目录...${NC}"
mkdir -p logs/backend logs/nginx uploads
chmod -R 755 logs uploads
echo -e "${GREEN}[✓] 目录创建完成${NC}"
echo ""

echo -e "${YELLOW}[步骤 7/8] 配置环境变量...${NC}"
cat > .env << EOF
# MySQL数据库配置
MYSQL_USER=${MYSQL_USER}
MYSQL_PASSWORD=${MYSQL_PASSWORD}

# JWT密钥（生产环境请修改为随机字符串）
JWT_SECRET=

# JVM参数
JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
EOF
echo -e "${GREEN}[✓] 环境变量配置完成${NC}"
echo ""

echo -e "${YELLOW}[步骤 8/8] 启动服务...${NC}"

# 停止旧容器（如果存在）
if [ "$(docker ps -a -q -f name=library-)" ]; then
    echo "停止并删除旧容器..."
    docker-compose -f docker-compose.yml down
fi

# 启动新容器
echo "启动容器..."
docker-compose -f docker-compose.yml up -d

# 等待服务启动
echo ""
echo "等待服务启动（30秒）..."
sleep 30

# 检查服务状态
echo ""
echo -e "${YELLOW}检查服务状态...${NC}"
docker-compose -f docker-compose.yml ps

echo ""
echo "===================================="
echo -e "${GREEN}✓ 部署完成！${NC}"
echo "===================================="
echo ""
echo "服务访问地址："
echo "  - 前端: http://$(hostname -I | awk '{print $1}')"
echo "  - 后端API: http://$(hostname -I | awk '{print $1}'):8080/api"
echo ""
echo "常用命令："
echo "  - 查看日志: docker-compose -f docker-compose.yml logs -f"
echo "  - 停止服务: docker-compose -f docker-compose.yml down"
echo "  - 重启服务: docker-compose -f docker-compose.yml restart"
echo "  - 查看状态: docker-compose -f docker-compose.yml ps"
echo ""
echo "注意事项："
echo "  1. 请确保MySQL数据库已创建并初始化"
echo "  2. 请确保防火墙已开放80和8080端口"
echo "  3. 首次登录账号密码请查看数据库初始化脚本"
echo ""

