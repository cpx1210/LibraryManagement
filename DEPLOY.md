# 🚀 Docker 部署指南

## 📋 部署前准备

### 1. 服务器要求

- **操作系统**: Linux (推荐 Ubuntu 20.04+ 或 CentOS 7+)
- **Docker**: 20.10+
- **Docker Compose**: 1.29+
- **内存**: 至少 4GB（推荐 8GB+）
- **磁盘**: 至少 20GB 可用空间

### 2. 需要开放的端口

根据你的部署方式，需要向服务器管理员申请开放以下端口：

#### 方案一：标准部署（推荐）

| 端口 | 服务 | 说明 | 是否必须开放 |
|------|------|------|------------|
| **80** | 前端（HTTP） | 用户访问入口 | ✅ **必须** |
| **443** | 前端（HTTPS） | 如果使用SSL证书 | ⚠️ 推荐 |
| **8080** | 后端API | 后端服务端口 | ✅ **必须** |
| **3306** | MySQL | 数据库端口 | ❌ 不需要（容器内通信） |

**注意**：
- MySQL的3306端口**不需要对外暴露**，只在Docker网络内部通信
- 如果使用Nginx反向代理，8080端口也可以不对外暴露

#### 方案二：使用Nginx反向代理（更安全）

| 端口 | 服务 | 说明 | 是否必须开放 |
|------|------|------|------------|
| **80** | Nginx | HTTP访问 | ✅ **必须** |
| **443** | Nginx | HTTPS访问 | ⚠️ 推荐 |
| **8080** | 后端API | 后端服务端口 | ❌ 不需要（Nginx代理） |
| **3306** | MySQL | 数据库端口 | ❌ 不需要（容器内通信） |

### 3. 环境变量配置

创建 `.env` 文件（用于配置敏感信息）：

```bash
# MySQL配置
MYSQL_ROOT_PASSWORD=你的强密码
MYSQL_DATABASE=library_management
MYSQL_USER=library_user
MYSQL_PASSWORD=你的数据库用户密码

# JWT密钥（生产环境请务必修改）
JWT_SECRET=你的JWT密钥（至少32位随机字符串）

# JVM参数（可选，根据服务器配置调整）
JAVA_OPTS=-Xms1024m -Xmx2048m -XX:+UseG1GC
```

## 🔧 部署步骤

### 步骤1: 安装Docker和Docker Compose

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-plugin

# CentOS/RHEL
sudo yum install -y docker docker-compose-plugin

# 启动Docker服务
sudo systemctl start docker
sudo systemctl enable docker

# 验证安装
docker --version
docker compose version
```

### 步骤2: 上传项目文件

将整个项目目录上传到服务器，或使用Git克隆：

```bash
git clone <你的仓库地址>
cd LibraryManagement
```

### 步骤3: 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑环境变量（使用vim或其他编辑器）
vim .env
```

**重要**：请修改以下配置：
- `MYSQL_ROOT_PASSWORD`: 设置强密码
- `JWT_SECRET`: 生成随机密钥（可以使用 `openssl rand -base64 32`）

### 步骤4: 配置前端API地址

如果前端需要直接访问后端（不使用Nginx代理），需要修改前端配置：

编辑 `library-management-frontend/src/utils/request.js`，将API地址改为服务器IP或域名。

### 步骤5: 构建和启动服务

```bash
# 构建所有镜像
docker compose build

# 启动所有服务（后台运行）
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 步骤6: 验证部署

1. **检查服务状态**：
```bash
docker compose ps
```

所有服务应该显示为 `Up` 状态。

2. **检查后端健康状态**：
```bash
curl http://localhost:8080/api/actuator/health
```

3. **访问前端**：
在浏览器中访问 `http://你的服务器IP` 或 `http://你的域名`

4. **访问API文档**：
`http://你的服务器IP:8080/api/doc.html`

## 📝 常用管理命令

### 查看日志

```bash
# 查看所有服务日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
```

### 停止服务

```bash
# 停止所有服务
docker compose down

# 停止并删除数据卷（⚠️ 会删除数据库数据）
docker compose down -v
```

### 重启服务

```bash
# 重启所有服务
docker compose restart

# 重启特定服务
docker compose restart backend
```

### 更新服务

```bash
# 重新构建并启动
docker compose up -d --build

# 只更新特定服务
docker compose up -d --build backend
```

### 备份数据库

```bash
# 备份数据库
docker compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} library_management > backup_$(date +%Y%m%d_%H%M%S).sql

# 恢复数据库
docker compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} library_management < backup.sql
```

## 🔒 安全建议

### 1. 修改默认密码

- ✅ 修改MySQL root密码
- ✅ 修改JWT密钥
- ✅ 修改默认管理员账号密码

### 2. 使用HTTPS

生产环境强烈建议使用HTTPS：

1. 申请SSL证书（Let's Encrypt免费证书）
2. 配置Nginx支持HTTPS
3. 修改前端配置使用HTTPS

### 3. 防火墙配置

```bash
# Ubuntu/Debian (ufw)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --reload
```

### 4. 定期备份

设置定时任务自动备份数据库：

```bash
# 编辑crontab
crontab -e

# 添加每天凌晨2点备份
0 2 * * * cd /path/to/LibraryManagement && docker compose exec -T mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} library_management > /backup/library_$(date +\%Y\%m\%d).sql
```

## 🐛 常见问题

### Q1: 容器启动失败

**检查方法**：
```bash
# 查看容器日志
docker compose logs [服务名]

# 检查容器状态
docker compose ps
```

**常见原因**：
- 端口被占用
- 环境变量配置错误
- 数据库连接失败

### Q2: 前端无法访问后端API

**解决方案**：
1. 检查后端服务是否正常运行
2. 检查防火墙是否开放8080端口
3. 检查前端nginx配置中的proxy_pass地址

### Q3: 数据库连接失败

**检查方法**：
```bash
# 进入MySQL容器
docker compose exec mysql bash

# 测试连接
mysql -u root -p
```

**常见原因**：
- MySQL容器未完全启动（等待健康检查通过）
- 数据库密码错误
- 网络配置问题

### Q4: 内存不足

**解决方案**：
1. 调整JVM参数（减少内存使用）
2. 增加服务器内存
3. 优化Docker资源限制

## 📞 技术支持

如遇到问题，请提供以下信息：
- Docker版本：`docker --version`
- 服务状态：`docker compose ps`
- 相关日志：`docker compose logs [服务名]`

---

**祝部署顺利！** 🎉

