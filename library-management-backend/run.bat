@echo off
chcp 65001 >nul
echo ========================================
echo 图书馆问题图书管理系统 - 启动应用
echo ========================================
echo.

echo 正在检查本地环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo 未检测到本地 Java（JDK）。请先安装 JDK 21+ 并配置 PATH。
    echo 下载地址： https://adoptium.net/
    pause
    exit /b 1
)

where mvn >nul 2>&1
if errorlevel 1 (
    echo.
    echo 未检测到本地 Maven（mvn）。请先安装 Maven 并配置 PATH。
    echo 下载地址： https://maven.apache.org/
    pause
    exit /b 1
)

set "MVN_CMD=mvn"

echo [提示] 首次运行请先执行：mvn clean install （需已安装 Maven）
echo.
echo 正在启动应用...
echo 启动地址：http://localhost:8080/api
echo API 文档：http://localhost:8080/api/doc.html
echo.
echo 按 Ctrl+C 可停止应用
echo ========================================
echo.

call %MVN_CMD% spring-boot:run

