@echo off
chcp 65001 >nul
echo ========================================
echo 图书馆问题图书管理系统 - 启动应用
echo ========================================
echo.

echo [提示] 首次运行请先执行：mvnw clean install （无需安装 Maven）
echo.
echo 正在启动应用...
echo 启动地址：http://localhost:8080/api
echo API 文档：http://localhost:8080/api/doc.html
echo.
echo 按 Ctrl+C 可停止应用
echo ========================================
echo.

call .\mvnw spring-boot:run

