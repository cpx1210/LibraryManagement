@echo off
chcp 65001 >nul
REM =====================================================
REM Library Problem Books Management System
REM MySQL Database Initialization Script (Windows)
REM =====================================================
REM This script will:
REM 1. Create the database
REM 2. Create all tables
REM 3. Insert initial Chinese data
REM =====================================================

setlocal enabledelayedexpansion

echo =====================================================
echo Library Management System - MySQL Database Setup
echo =====================================================
echo.

REM =====================================================
REM Configuration - Please modify these settings
REM =====================================================
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASSWORD=583201844Aa*

REM Get current script directory
set SCRIPT_DIR=%~dp0

echo [Configuration]
echo MySQL Host: %MYSQL_HOST%
echo MySQL Port: %MYSQL_PORT%
echo MySQL User: %MYSQL_USER%
echo Script Directory: %SCRIPT_DIR%
echo.

REM =====================================================
REM Check if MySQL client is available
REM =====================================================
echo [Step 1/4] Checking MySQL client...
where mysql >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] MySQL client not found in PATH
    echo Please install MySQL client or add it to system PATH
    echo Common MySQL bin path: C:\Program Files\MySQL\MySQL Server 8.0\bin
    pause
    exit /b 1
)
echo [OK] MySQL client found
echo.

REM =====================================================
REM Prompt for password if not set
REM =====================================================
if "%MYSQL_PASSWORD%"=="" (
    echo [Notice] MySQL password not set in script
    set /p MYSQL_PASSWORD="Please enter MySQL root password (press Enter if no password): "
)

REM =====================================================
REM Test MySQL connection
REM =====================================================
echo [Step 2/4] Testing MySQL connection...
if "%MYSQL_PASSWORD%"=="" (
    mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -e "SELECT 1;" >nul 2>&1
) else (
    mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% -e "SELECT 1;" >nul 2>&1
)

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Cannot connect to MySQL server
    echo Please check:
    echo   1. MySQL service is running
    echo   2. Host and port are correct
    echo   3. Username and password are correct
    pause
    exit /b 1
)
echo [OK] MySQL connection successful
echo.

REM =====================================================
REM Execute schema script
REM =====================================================
echo [Step 3/4] Creating database and tables...
echo Executing: mysql_schema.sql

if "%MYSQL_PASSWORD%"=="" (
    mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% --default-character-set=utf8mb4 < "%SCRIPT_DIR%mysql_schema.sql"
) else (
    mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% --default-character-set=utf8mb4 < "%SCRIPT_DIR%mysql_schema.sql"
)

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to create database schema
    echo Please check the error messages above
    pause
    exit /b 1
)
echo [OK] Database and tables created successfully
echo.

REM =====================================================
REM Execute data script
REM =====================================================
echo [Step 4/4] Inserting initial data...
echo Executing: mysql_data_cn.sql

if "%MYSQL_PASSWORD%"=="" (
    mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% --default-character-set=utf8mb4 < "%SCRIPT_DIR%mysql_data_cn.sql"
) else (
    mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% --default-character-set=utf8mb4 < "%SCRIPT_DIR%mysql_data_cn.sql"
)

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to insert initial data
    echo Please check the error messages above
    pause
    exit /b 1
)
echo [OK] Initial data inserted successfully
echo.

REM =====================================================
REM Verify installation
REM =====================================================
echo [Verification] Checking created tables...
if "%MYSQL_PASSWORD%"=="" (
    mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -e "USE library_management; SHOW TABLES;"
) else (
    mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% -e "USE library_management; SHOW TABLES;"
)
echo.

echo =====================================================
echo Database Setup Complete!
echo =====================================================
echo.
echo Database: library_management
echo Tables: 9 tables created
echo Initial Data: Admin user and test data inserted
echo.
echo Default Login Credentials:
echo   Username: admin
echo   Password: admin123
echo.
echo [IMPORTANT] Please change the default password after first login!
echo.
echo Next Steps:
echo 1. Update application.yml with MySQL connection settings
echo 2. Add MySQL driver dependency to pom.xml
echo 3. Start the Spring Boot application
echo.
echo =====================================================

pause

