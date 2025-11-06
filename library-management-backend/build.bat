@echo off
chcp 65001 >nul
echo ========================================
echo 图书馆问题图书管理系统 - 构建项目
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

echo 正在清理旧的构建文件...
call %MVN_CMD% clean

echo.
echo 正在编译并安装依赖...
echo （首次运行会下载依赖包，可能需要几分钟）
call %MVN_CMD% install -DskipTests

if errorlevel 1 (
    echo.
    echo ❌ 构建失败！请检查错误信息。
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ 构建成功！
echo ========================================
echo.
echo 下一步：运行 run.bat 启动应用
echo ========================================
pause

