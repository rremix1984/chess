#!/bin/bash

# DeepSeek+Pikafish AI 国际象棋演示脚本
# 展示如何使用DeepSeek大模型与Pikafish引擎的集成

echo "🎯 DeepSeek+Pikafish AI 国际象棋演示"
echo "=================================================="
echo ""
echo "🤖 AI组合说明："
echo "   • DeepSeek大模型: 提供战略分析和自然语言解释"
echo "   • Pikafish引擎: 提供精确的局面评估和最佳走法计算"
echo "   • 智能集成: 结合两者优势，提供高质量的AI对弈体验"
echo ""
echo "🎮 使用步骤："
echo "   1. 启动国际象棋界面"
echo "   2. 在AI类型下拉菜单中选择 'DeepSeek+Pikafish AI'"
echo "   3. 点击'启用AI对弈'按钮"
echo "   4. 开始下棋，观察AI的智能决策"
echo ""
echo "🔍 观察要点："
echo "   • AI决策日志: 查看详细的思考过程"
echo "   • 响应时间: 通常5-15秒"
echo "   • 走法质量: 结合战略思考和精确计算"
echo "   • 解释能力: 提供自然语言的决策解释"
echo ""

# 检查依赖
echo "📋 检查系统依赖..."
dependencies_ok=true

if ! command -v java &> /dev/null; then
    echo "❌ Java未安装"
    dependencies_ok=false
else
    echo "✅ Java: $(java -version 2>&1 | head -n 1)"
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven未安装"
    dependencies_ok=false
else
    echo "✅ Maven: $(mvn -version | head -n 1)"
fi

if ! command -v ollama &> /dev/null; then
    echo "❌ Ollama未安装"
    dependencies_ok=false
else
    echo "✅ Ollama已安装"
    if ollama list | grep -q "deepseek"; then
        echo "✅ DeepSeek模型: $(ollama list | grep deepseek | head -n 1)"
    else
        echo "❌ DeepSeek模型未安装，请运行: ollama pull deepseek-r1:7b"
        dependencies_ok=false
    fi
fi

if [ ! -f "./pikafish_mock.py" ]; then
    echo "❌ Pikafish模拟器未找到"
    dependencies_ok=false
else
    echo "✅ Pikafish模拟器已就绪"
fi

if [ "$dependencies_ok" = false ]; then
    echo ""
    echo "❌ 依赖检查失败，请先安装缺失的组件"
    exit 1
fi

echo ""
echo "🚀 启动DeepSeek+Pikafish AI国际象棋..."
echo "=================================================="
echo ""
echo "💡 提示："
echo "   • 界面启动后，请在AI选择菜单中选择 'DeepSeek+Pikafish AI'"
echo "   • 启用AI对弈后，AI将作为黑方与您对弈"
echo "   • 观察控制台输出，可以看到AI的详细思考过程"
echo "   • 首次AI决策可能需要较长时间（模型加载）"
echo ""

# 设置环境变量
export PIKAFISH_PATH="./pikafish_mock.py"

# 启动国际象棋GUI
echo "🎮 正在启动国际象棋界面..."
mvn exec:java -Dexec.mainClass="com.example.App" -Dexec.args="international" -Dpikafish.path="./pikafish_mock.py" -q