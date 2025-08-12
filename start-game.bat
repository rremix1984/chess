@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 多游戏平台启动脚本 (Windows版本)
REM 作者: 游戏开发团队
REM 版本: 1.0

echo ===================================
echo     多游戏平台启动器
echo ===================================

REM 检查Java环境
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Java环境，请先安装Java 11或更高版本
    echo 请从 https://adoptium.net/ 下载并安装Java
    pause
    exit /b 1
)

REM 检查Maven环境
mvn -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Maven环境，请先安装Maven
    echo 请从 https://maven.apache.org/download.cgi 下载并安装Maven
    pause
    exit /b 1
)

echo 正在编译和打包项目...
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo 编译失败，请检查代码
    pause
    exit /b 1
)

echo 编译完成，启动游戏选择界面...

REM 启动游戏启动器（使用打包后的JAR文件）
java -cp game-launcher/target/game-launcher-1.0-SNAPSHOT.jar com.example.launcher.GameLauncher

if errorlevel 1 (
    echo 启动失败，请检查错误信息
    pause
)

echo 游戏已退出
pause
