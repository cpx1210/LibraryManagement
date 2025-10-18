@echo off
chcp 65001 >nul
echo ========================================
echo 创建 PostgreSQL 数据库
echo ========================================
echo.

echo 正在创建数据库 library_management...
echo.

psql -U postgres -c "CREATE DATABASE library_management WITH ENCODING='UTF8' LC_COLLATE='Chinese (Simplified)_China.936' LC_CTYPE='Chinese (Simplified)_China.936';"

if errorlevel 1 (
    echo.
    echo ❌ 数据库创建失败！
    echo 可能数据库已存在，或者 PostgreSQL 未正确安装。
    pause
    exit /b 1
)

echo ✅ 数据库创建成功！
echo.

echo 正在安装扩展...
psql -U postgres -d library_management -c "CREATE EXTENSION IF NOT EXISTS pg_trgm;"
psql -U postgres -d library_management -c "CREATE EXTENSION IF NOT EXISTS btree_gin;"

echo.
echo ========================================
echo ✅ 数据库初始化完成！
echo ========================================
echo.
echo 下一步：运行 init-database-postgresql.bat 创建表结构
echo ========================================
pause

