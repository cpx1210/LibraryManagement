@echo off
chcp 65001 >nul
echo ========================================
echo 图书馆管理系统 - 数据库初始化
echo ========================================
echo.
echo 本脚本将执行以下操作：
echo 1. 创建数据库 library_management
echo 2. 安装必要的 PostgreSQL 扩展
echo 3. 创建所有数据表（schema.sql）
echo 4. 初始化基础数据（data.sql）
echo.
echo ⚠️  警告：如果数据库已存在，将会删除并重建！
echo.
set /p confirm="确认继续？(Y/N): "
if /i not "%confirm%"=="Y" (
    echo 操作已取消。
    pause
    exit /b 0
)

echo.
echo ========================================
echo 步骤 1/4: 删除已存在的数据库
echo ========================================
psql -U postgres -c "DROP DATABASE IF EXISTS library_management;" 2>nul
echo ✅ 旧数据库已清理

echo.
echo ========================================
echo 步骤 2/4: 创建新数据库
echo ========================================
psql -U postgres -c "CREATE DATABASE library_management WITH ENCODING='UTF8';"

if errorlevel 1 (
    echo.
    echo ❌ 数据库创建失败！
    echo 请检查：
    echo   1. PostgreSQL 是否正确安装
    echo   2. PostgreSQL 服务是否启动
    echo   3. 当前用户是否有创建数据库权限
    echo.
    pause
    exit /b 1
)

echo ✅ 数据库创建成功

echo.
echo ========================================
echo 步骤 3/4: 执行建表脚本（schema.sql）
echo ========================================
psql -U postgres -d library_management -f schema.sql

if errorlevel 1 (
    echo.
    echo ❌ 建表脚本执行失败！
    echo 请检查 schema.sql 文件是否存在且格式正确。
    echo.
    pause
    exit /b 1
)

echo ✅ 数据表创建成功（共9张表）

echo.
echo ========================================
echo 步骤 4/4: 执行初始化数据脚本（data.sql）
echo ========================================
psql -U postgres -d library_management -f data.sql

if errorlevel 1 (
    echo.
    echo ❌ 数据初始化失败！
    echo 请检查 data.sql 文件是否存在且格式正确。
    echo.
    pause
    exit /b 1
)

echo ✅ 初始化数据导入成功

echo.
echo ========================================
echo 🎉 数据库初始化完成！
echo ========================================
echo.
echo 📊 数据库信息：
echo   - 数据库名: library_management
echo   - 数据表数: 9 张
echo   - 字符集: UTF-8
echo.
echo 🔐 默认账号：
echo   - 管理员: admin / admin123
echo   - 测试用户: testuser / user123
echo.
echo ⚠️  重要提示：
echo   1. 生产环境请务必修改默认密码
echo   2. 请在 application-dev.yml 中配置数据库连接
echo   3. 数据库连接地址: jdbc:postgresql://localhost:5432/library_management
echo.
echo ========================================
pause

