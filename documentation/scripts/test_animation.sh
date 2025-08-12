#!/bin/bash

echo "🎬 象棋吃子动画效果测试"
echo "================================"
echo ""
echo "🎯 测试说明："
echo "1. 游戏将自动启动"
echo "2. 选择快速开始（输入0）"
echo "3. 进行几步走法，尝试吃子"
echo "4. 观察动画效果"
echo ""
echo "💡 建议的测试走法："
echo "   - 兵五进一（红方兵前进）"
echo "   - 等AI走棋"
echo "   - 继续移动棋子到可以吃子的位置"
echo ""
echo "🌟 动画特点："
echo "   💥 被吃棋子和周围棋子会跳跃"
echo "   🌊 包含3帧动画：↑跳跃 → ↓落下 → ～摇摆"
echo "   ✨ 动画结束后恢复正常棋盘"
echo ""
echo "按Enter键开始测试..."
read

# 获取脚本所在目录和项目根目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"

echo "🚀 启动游戏..."
cd "$PROJECT_ROOT"

# 确保依赖已下载并复制到target/dependency目录
if [ ! -d "$PROJECT_ROOT/target/dependency" ] || [ -z "$(ls -A "$PROJECT_ROOT/target/dependency" 2>/dev/null)" ]; then
    echo "📚 下载项目依赖..."
    mvn dependency:copy-dependencies
    if [ $? -ne 0 ]; then
        echo "❌ 依赖下载失败，请检查网络连接"
        exit 1
    fi
    echo "✅ 依赖下载完成"
fi

# 使用Maven执行（确保包含所有依赖）
mvn exec:java -Dexec.mainClass="com.example.terminal.TerminalChessGame" -Dexec.classpathScope=runtime