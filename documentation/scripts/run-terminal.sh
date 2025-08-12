#!/bin/bash

# 终端象棋游戏启动脚本

echo "🏮 启动终端象棋游戏..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 未找到Java环境，请先安装Java"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 未找到Maven环境，请先安装Maven"
    exit 1
fi

# 获取脚本所在目录和项目根目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"

# 编译项目
echo "🔨 编译项目..."
cd "$PROJECT_ROOT" && mvn compile -q

if [ $? -ne 0 ]; then
    echo "❌ 编译失败，请检查代码"
    exit 1
fi

echo "✅ 编译成功"

# 确保依赖已下载并复制到target/dependency目录
if [ ! -d "$PROJECT_ROOT/target/dependency" ] || [ -z "$(ls -A "$PROJECT_ROOT/target/dependency" 2>/dev/null)" ]; then
    echo "📚 下载项目依赖..."
    cd "$PROJECT_ROOT" && mvn dependency:copy-dependencies -q
    if [ $? -ne 0 ]; then
        echo "❌ 依赖下载失败，请检查网络连接"
        exit 1
    fi
    echo "✅ 依赖下载完成"
fi

# 运行终端版本
echo "🚀 启动游戏..."
# 使用 -q 参数减少Maven输出，并确保交互式运行
cd "$PROJECT_ROOT" && mvn exec:java -Dexec.mainClass="com.example.terminal.TerminalChessGame" -Dexec.args="" -Dexec.classpathScope=runtime -q

echo "👋 游戏结束，感谢游戏！"