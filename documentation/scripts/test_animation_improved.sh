#!/bin/bash

# 测试改进后的吃子动画效果
# 作者：AI助手
# 日期：2025-08-07

echo "🎬 测试改进后的象棋吃子动画效果"
echo "=================================="
echo ""
echo "🎯 动画改进内容："
echo "  ✅ 增加播放时间：每帧800ms"
echo "  ✅ 添加用户交互：按回车继续"
echo "  ✅ 改进视觉效果：使用emoji符号"
echo "  ✅ 详细信息显示：影响位置和棋子"
echo "  ✅ 分帧标题：清晰的动画说明"
echo ""
echo "🎮 测试步骤："
echo "  1. 启动游戏"
echo "  2. 选择快速开始（输入0）"
echo "  3. 进行走法，寻找吃子机会"
echo "  4. 观察动画效果"
echo ""
echo "💡 吃子动画特点："
echo "  🔺 第1帧：棋子向上跳跃（红色三角）"
echo "  🔻 第2帧：棋子向下震动（蓝色三角）"
echo "  💫 第3帧：棋子左右摇摆（星星效果）"
echo ""
echo "🎯 影响范围：被吃棋子位置 + 周围8个方向"
echo ""

read -p "按回车键启动游戏测试..."

echo ""
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

# 启动游戏（包含依赖库）
java -cp "$PROJECT_ROOT/target/classes:$PROJECT_ROOT/target/dependency/*" com.example.terminal.TerminalChessGame