@echo off
chcp 65001 >nul
echo ========================================
echo 图书馆问题图书管理系统
echo 数据填充脚本
echo ========================================
echo.

echo 正在向数据库填充初始数据...
echo.

psql -U postgres -d library_management -f src\main\resources\db\data.sql

if errorlevel 1 (
    echo.
    echo ❌ 数据填充失败！
    echo 请检查：
    echo 1. PostgreSQL 服务是否已启动
    echo 2. 数据库 library_management 是否存在
    echo 3. 用户名密码是否正确（默认：postgres/postgres）
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ 数据填充完成！
echo ========================================
echo.
echo 填充的数据：
echo - 2 个用户账号（admin、testuser）
echo - 5 个敏感词分类
echo - 4 个示例敏感词
echo - 10 个出版社白名单
echo.
echo 默认账号：
echo - 管理员：admin / admin123
echo - 测试用户：testuser / user123
echo.
echo 下一步：运行 run.bat 启动应用
echo ========================================
pause

