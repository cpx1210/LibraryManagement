@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo   图书馆管理系统 Docker 部署脚本
echo ==========================================
echo.

REM 检查Docker是否安装
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未检测到 Docker，请先安装 Docker Desktop
    pause
    exit /b 1
)

REM 检查Docker Compose是否安装
docker compose version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未检测到 Docker Compose
    pause
    exit /b 1
)

echo ✅ Docker 环境检查通过
echo.

REM 检查.env文件
if not exist .env (
    echo ⚠️  未找到 .env 文件
    if exist env.template (
        echo 📋 从模板创建 .env 文件...
        copy env.template .env >nul
        echo ⚠️  请编辑 .env 文件，修改数据库密码和JWT密钥后重新运行此脚本
        pause
        exit /b 1
    ) else (
        echo ❌ 错误: 未找到 env.template 文件
        pause
        exit /b 1
    )
)

echo ✅ 环境变量文件检查通过
echo.

REM 询问部署模式
echo 请选择部署模式:
echo 1) 开发环境 (标准配置)
echo 2) 生产环境 (优化配置)
set /p mode="请输入选项 (1 或 2): "

if "%mode%"=="2" (
    set COMPOSE_FILE=-f docker-compose.yml -f docker-compose.prod.yml
    echo 📦 使用生产环境配置
) else (
    set COMPOSE_FILE=-f docker-compose.yml
    echo 📦 使用开发环境配置
)

echo.
echo 开始构建和启动服务...
echo.

REM 构建镜像
echo 🔨 构建 Docker 镜像...
docker compose %COMPOSE_FILE% build
if errorlevel 1 (
    echo ❌ 构建失败
    pause
    exit /b 1
)

REM 启动服务
echo.
echo 🚀 启动服务...
docker compose %COMPOSE_FILE% up -d
if errorlevel 1 (
    echo ❌ 启动失败
    pause
    exit /b 1
)

REM 等待服务启动
echo.
echo ⏳ 等待服务启动...
timeout /t 10 /nobreak >nul

REM 检查服务状态
echo.
echo 📊 服务状态:
docker compose %COMPOSE_FILE% ps

echo.
echo ==========================================
echo ✅ 部署完成！
echo ==========================================
echo.
echo 📝 访问地址:
echo    - 前端: http://你的服务器IP
echo    - 后端API: http://你的服务器IP:8080/api
echo    - API文档: http://你的服务器IP:8080/api/doc.html
echo.
echo 📋 常用命令:
echo    - 查看日志: docker compose %COMPOSE_FILE% logs -f
echo    - 停止服务: docker compose %COMPOSE_FILE% down
echo    - 重启服务: docker compose %COMPOSE_FILE% restart
echo.
echo 📖 更多信息请查看 DEPLOY.md
echo.

pause

