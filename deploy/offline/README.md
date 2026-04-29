# 离线部署说明

本目录用于把已经在本地打好的镜像和部署文件拷贝到纯内网服务器。

## 目录内容

- `docker-compose.offline.yml`：离线部署用 compose 文件
- `.env.example`：环境变量模板
- `sql/`：MySQL 初始化脚本
- `images/`：前后端镜像离线包（由本地导出）

## 默认访问路径

- 公开提交页：`http://服务器IP/`
- 管理后台登录页：`http://服务器IP/login`
- 后端健康检查：`http://服务器IP:8080/api/actuator/health`
- API 文档：`http://服务器IP:8080/api/doc.html`

## 默认账号

- 用户名：`admin`
- 密码：`admin123`

部署后请立即修改默认密码。
