#!/bin/bash

# AI决策日志颜色演示启动脚本

echo "🏮 启动象棋AI决策日志颜色演示..."
echo "="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到Java环境，请先安装Java 11或更高版本"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误：未找到Maven环境，请先安装Maven"
    exit 1
fi

# 进入项目目录
cd "$(dirname "$0")"

echo "📦 编译项目..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ 项目编译失败"
    exit 1
fi

echo "🚀 启动AI决策日志颜色演示..."
echo "="
echo "📝 功能说明："
echo "   🔴 红方AI决策 - 红色字体"
echo "   ⚫ 黑方AI决策 - 黑色字体" 
echo "   🟢 一般信息 - 绿色字体"
echo "   🤖 智能颜色检测 - 自动识别并应用对应颜色"
echo "="

# 启动演示程序
mvn exec:java -Dexec.mainClass="com.example.chinesechess.demo.AILogColorDemo" -Dexec.cleanupDaemonThreads=false -q

echo "👋 演示程序已结束"
