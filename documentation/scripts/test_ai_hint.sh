#!/bin/bash

echo "🤖 AI助手功能演示脚本"
echo "================================"
echo ""
echo "本脚本演示新增的AI助手功能："
echo ""
echo "1. 在游戏中，当您不知道如何下棋时"
echo "2. 可以输入 'hint' 命令"
echo "3. AI会分析当前局面并给出建议"
echo ""
echo "AI助手功能特点："
echo "✅ 智能分析当前局面"
echo "✅ 提供最佳走法建议"
echo "✅ 显示标准描述和盲棋术语"
echo "✅ 分析走法原因和战术意图"
echo "✅ 可视化显示建议走法"
echo "✅ 评估棋子价值和威胁"
echo ""
echo "使用方法："
echo "1. 启动游戏：java -cp target/classes com.example.terminal.TerminalChessGame"
echo "2. 选择快速开始（输入0）"
echo "3. 在'请输入走法'提示时，输入 'hint'"
echo "4. AI会给出详细的走法建议"
echo ""
echo "示例输出："
echo "🤖 AI助手正在分析当前局面..."
echo "💡 AI建议走法："
echo "   📝 标准描述：马二进三"
echo "   🎯 盲棋术语：第1手 马二进三"
echo "   ⏱️  分析时间：15ms"
echo "🧠 走法分析："
echo "   🎯 位置走法：改善棋子位置"
echo "   🎪 战术意图：向前推进，增加攻击性"
echo "🎯 走法可视化："
echo "   起始位置 ◎    目标位置 ★"
echo "   （显示棋盘，标记起始和目标位置）"
echo ""
echo "现在启动游戏进行测试..."
echo ""

# 获取脚本所在目录和项目根目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"

# 确保依赖已下载并复制到target/dependency目录
if [ ! -d "$PROJECT_ROOT/target/dependency" ] || [ -z "$(ls -A "$PROJECT_ROOT/target/dependency" 2>/dev/null)" ]; then
    echo "📚 下载项目依赖..."
    cd "$PROJECT_ROOT" && mvn dependency:copy-dependencies
    if [ $? -ne 0 ]; then
        echo "❌ 依赖下载失败，请检查网络连接"
        exit 1
    fi
    echo "✅ 依赖下载完成"
fi

# 启动游戏（包含依赖库）
cd "$PROJECT_ROOT" && java -cp "$PROJECT_ROOT/target/classes:$PROJECT_ROOT/target/dependency/*" com.example.terminal.TerminalChessGame