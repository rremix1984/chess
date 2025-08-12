@echo off
chcp 65001 >nul

echo 🏮 启动终端象棋游戏...

REM 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未找到Java环境，请先安装Java
    pause
    exit /b 1
)

REM 检查Maven环境
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未找到Maven环境，请先安装Maven
    pause
    exit /b 1
)

REM 编译项目
echo 🔨 编译项目...
mvn compile -q

if %errorlevel% neq 0 (
    echo ❌ 编译失败，请检查代码
    pause
    exit /b 1
)

echo ✅ 编译成功

REM 运行终端版本
echo 🚀 启动游戏...
REM 使用 -q 参数减少Maven输出，并确保交互式运行
mvn exec:java -Dexec.mainClass="com.example.terminal.TerminalChessGame" -Dexec.args="" -q

echo 👋 游戏结束，感谢游戏！
pause