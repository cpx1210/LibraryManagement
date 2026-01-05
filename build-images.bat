@echo off
chcp 65001 >nul
echo ====================================
echo 图书馆管理系统 - 离线镜像打包脚本
echo ====================================
echo.

:: 设置镜像版本（可根据需要修改）
set VERSION=v1.0
set BACKEND_IMAGE=library-backend:%VERSION%
set FRONTEND_IMAGE=library-frontend:%VERSION%

echo [步骤 1/6] 检查Docker环境...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Docker，请先安装Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)
echo [✓] Docker环境正常
echo.

echo [步骤 2/6] 构建后端镜像（预计耗时: 10-15分钟）...
echo 镜像名称: %BACKEND_IMAGE%
docker build -t %BACKEND_IMAGE% ./library-management-backend
if %errorlevel% neq 0 (
    echo [错误] 后端镜像构建失败
    pause
    exit /b 1
)
echo [✓] 后端镜像构建成功
echo.

echo [步骤 3/6] 构建前端镜像（预计耗时: 5-10分钟）...
echo 镜像名称: %FRONTEND_IMAGE%
docker build -t %FRONTEND_IMAGE% ./library-management-frontend
if %errorlevel% neq 0 (
    echo [错误] 前端镜像构建失败
    pause
    exit /b 1
)
echo [✓] 前端镜像构建成功
echo.

echo [步骤 4/6] 导出后端镜像为tar文件...
docker save -o library-backend-%VERSION%.tar %BACKEND_IMAGE%
if %errorlevel% neq 0 (
    echo [错误] 后端镜像导出失败
    pause
    exit /b 1
)
echo [✓] 后端镜像已导出: library-backend-%VERSION%.tar
echo.

echo [步骤 5/6] 导出前端镜像为tar文件...
docker save -o library-frontend-%VERSION%.tar %FRONTEND_IMAGE%
if %errorlevel% neq 0 (
    echo [错误] 前端镜像导出失败
    pause
    exit /b 1
)
echo [✓] 前端镜像已导出: library-frontend-%VERSION%.tar
echo.

echo [步骤 6/6] 验证导出的文件...
echo.
echo 导出的镜像文件：
dir /b library-backend-%VERSION%.tar 2>nul
dir /b library-frontend-%VERSION%.tar 2>nul
echo.

echo 文件大小信息：
for %%F in (library-backend-%VERSION%.tar library-frontend-%VERSION%.tar) do (
    if exist "%%F" (
        for %%A in ("%%F") do (
            set size=%%~zA
            set /a sizeMB=!size! / 1048576
            echo   %%F - 约 !sizeMB! MB
        )
    )
)
echo.

echo ====================================
echo ✓ 所有镜像打包完成！
echo ====================================
echo.
echo 接下来的操作：
echo 1. 将以下文件上传到服务器:
echo    - library-backend-%VERSION%.tar
echo    - library-frontend-%VERSION%.tar
echo    - docker-compose-offline.yml
echo    - deploy-server.sh
echo.
echo 2. 在服务器上执行:
echo    chmod +x deploy-server.sh
echo    ./deploy-server.sh
echo.
pause





