@echo off
chcp 65001 >nul
echo ========================================
echo Library Management System
echo MySQL Data Initialization
echo ========================================
echo.

echo Please enter MySQL root password:
set /p MYSQL_PASSWORD=

echo.
echo Initializing MySQL database with sample data...
echo.

REM Use --force to continue even if some data already exists
mysql -u root -p%MYSQL_PASSWORD% --force < src\main\resources\db\mysql_data_cn.sql

if errorlevel 1 (
    echo.
    echo [ERROR] Data initialization failed!
    echo Please check:
    echo 1. MySQL service is running
    echo 2. Database library_management exists
    echo 3. MySQL root password is correct
    echo 4. MySQL CLI is in PATH
    pause
    exit /b 1
)

echo.
echo ========================================
echo [SUCCESS] Data initialization completed!
echo ========================================
echo.
echo Initialized data:
echo - 2 user accounts (admin, testuser)
echo - 5 sensitive word categories
echo - 4 sample sensitive words
echo - 10 publisher whitelist entries
echo.
echo Default accounts:
echo - Admin: admin / admin123
echo - Test User: testuser / user123
echo.
echo Next step: Run run.bat to start the application
echo ========================================
pause

