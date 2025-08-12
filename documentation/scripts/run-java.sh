#!/bin/bash

# 中国象棋终端版 - Java直接启动脚本
# 作者：AI助手
# 版本：1.0

echo "🎯 中国象棋终端版 - Java直接启动"
echo "================================"

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到Java环境，请先安装Java 8或更高版本"
    exit 1
fi

echo "✅ Java环境检查通过"

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误：未找到Maven环境，请先安装Maven"
    exit 1
fi

echo "✅ Maven环境检查通过"

# 编译项目
echo "🔨 编译项目..."
if ! mvn compile -q; then
    echo "❌ 编译失败，请检查代码"
    exit 1
fi

echo "✅ 编译成功"

# 设置classpath
CLASSPATH="target/classes"
for jar in target/dependency/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# 确保依赖已下载并复制到target/dependency目录
if [ ! -d "target/dependency" ] || [ -z "$(ls -A "target/dependency" 2>/dev/null)" ]; then
    echo "📦 准备依赖..."
    mvn dependency:copy-dependencies -q
    if [ $? -ne 0 ]; then
        echo "❌ 依赖下载失败，请检查网络连接"
        exit 1
    fi
    echo "✅ 依赖下载完成"
fi

# 运行游戏
echo "🚀 启动游戏..."
java -cp "$CLASSPATH" com.example.terminal.TerminalChessGame

echo "👋 游戏结束，感谢游戏！"