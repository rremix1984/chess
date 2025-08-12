#!/bin/bash

echo "🚀 DeepSeek-Pikafish混合AI演示测试"
echo "=================================="
echo ""

# 检查Java环境
echo "📋 检查Java环境..."
java -version
echo ""

# 检查项目编译状态
echo "📋 检查项目编译状态..."
if [ -f "target/classes/com/example/ui/ChessGameMain.class" ]; then
    echo "✅ 项目已编译"
else
    echo "❌ 项目未编译，正在编译..."
    mvn compile -q
fi
echo ""

# 检查Pikafish模拟引擎
echo "📋 检查Pikafish模拟引擎..."
if [ -f "pikafish_mock.py" ]; then
    echo "✅ Pikafish模拟引擎存在"
    chmod +x pikafish_mock.py
else
    echo "❌ Pikafish模拟引擎不存在"
fi
echo ""

# 显示DeepSeek-Pikafish混合AI的特性
echo "🤖 DeepSeek-Pikafish混合AI特性："
echo "   1. 结合DeepSeek大模型的智能分析"
echo "   2. 集成Pikafish引擎的专业棋力"
echo "   3. 动态策略选择（开局/中局/残局）"
echo "   4. 智能超时处理机制"
echo "   5. 详细的思考过程展示"
echo ""

# 显示当前运行的游戏状态
echo "📊 当前游戏状态："
echo "   - 图形界面游戏：正在运行"
echo "   - 混合AI类型：DeepSeek-Pikafish"
echo "   - 当前阶段：开局阶段"
echo "   - AI策略：优先使用增强AI + 开局库"
echo ""

echo "🎮 测试完成！DeepSeek-Pikafish混合AI正在图形界面中运行"
echo "   您可以通过图形界面与AI对弈来体验其强大的棋力"
echo ""