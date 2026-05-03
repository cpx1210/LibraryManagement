# Docker 部署步骤

## 1. 安装 Docker

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin git mysql-client
sudo systemctl enable --now docker
docker --version
docker compose version
```

## 2. 拉取代码

```bash
cd /opt
sudo git clone https://github.com/cpx1210/LibraryManagement.git
sudo chown -R $USER:$USER /opt/LibraryManagement
cd /opt/LibraryManagement
```

如需部署修复分支：

```bash
git fetch origin
git checkout codex/fix-booklist-upload-detection-flow
```

## 3. 准备 MySQL

如果服务器已有 MySQL，直接执行：

```bash
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS library_management
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'library_user'@'%' IDENTIFIED BY '修改成强密码';
GRANT ALL PRIVILEGES ON library_management.* TO 'library_user'@'%';
FLUSH PRIVILEGES;
EXIT;
```

允许 Docker 容器访问宿主机 MySQL：

```bash
sudo grep -R "bind-address" /etc/mysql/ /etc/my.cnf* 2>/dev/null
```

Ubuntu 常见路径：

```bash
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
```

把：

```ini
bind-address = 127.0.0.1
```

改成：

```ini
bind-address = 0.0.0.0
```

重启 MySQL：

```bash
sudo systemctl restart mysql
```

不要对公网开放 MySQL：

```bash
sudo ufw deny 3306/tcp
```

导入表结构和初始化数据：

```bash
mysql -u root -p --default-character-set=utf8mb4 < deploy/offline/sql/mysql_schema.sql
mysql -u root -p --default-character-set=utf8mb4 library_management < deploy/offline/sql/mysql_data_cn.sql
```

## 4. 创建 `.env`

```bash
cp env.template .env
nano .env
```

填写：

```env
MYSQL_USER=library_user
MYSQL_PASSWORD=修改成上一步设置的密码
JWT_SECRET=修改成至少32位随机字符串
JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
```

生成 JWT 密钥：

```bash
openssl rand -base64 48
```

## 5. 构建镜像

```bash
docker build -t library-backend:v1.0 ./library-management-backend
docker build -t library-frontend:v1.0 ./library-management-frontend
```

## 6. 启动服务

```bash
mkdir -p logs/backend logs/nginx uploads
docker compose up -d
docker compose ps
```

## 7. 检查日志

```bash
docker logs -f library-backend
```

另开一个终端：

```bash
docker logs -f library-frontend
```

## 8. 验证访问

```bash
curl http://127.0.0.1:8080/api/actuator/health
curl -I http://127.0.0.1:3000/
```

浏览器访问：

```text
http://服务器IP:3000/
```

后台登录：

```text
http://服务器IP:3000/login
```

默认账号：

```text
admin / admin123
```

首次登录后立即修改管理员密码。

## 9. 开放防火墙

如果使用 Ubuntu UFW：

```bash
sudo ufw allow 3000/tcp
sudo ufw allow 8080/tcp
sudo ufw reload
```

如果只允许前端访问，开放 `3000` 即可。

## 10. 常用命令

停止：

```bash
docker compose down
```

重启：

```bash
docker compose restart
```

更新代码后重新部署：

```bash
git pull
docker build -t library-backend:v1.0 ./library-management-backend
docker build -t library-frontend:v1.0 ./library-management-frontend
docker compose up -d
```

查看后端日志文件：

```bash
tail -f logs/backend/application.log
```

查看上传文件：

```bash
ls -lah uploads
```

## 11. 生产端口改为 80

编辑 `docker-compose.yml`：

```yaml
frontend:
  ports:
    - "80:80"
```

重启：

```bash
docker compose up -d
```
