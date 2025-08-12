#!/bin/bash

echo "🔍 AI思考过程测试脚本"
echo "========================"

# 检查Ollama服务
echo "1. 检查Ollama服务状态..."
if ! command -v ollama &> /dev/null; then
    echo "❌ Ollama未安装，请先安装Ollama"
    exit 1
fi

# 检查Ollama是否运行
if ! curl -s http://localhost:11434/api/tags > /dev/null; then
    echo "❌ Ollama服务未运行，正在启动..."
    ollama serve &
    sleep 3
fi

echo "✅ Ollama服务正常"

# 检查可用模型
echo "2. 检查可用模型..."
ollama list

# 测试模型连接
echo "3. 测试模型连接..."
echo "测试deepseek-r1:7b模型..."
curl -s -X POST http://localhost:11434/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-r1:7b",
    "prompt": "简单回答：你好",
    "stream": false
  }' | grep -o '"response":"[^"]*"' | head -1

echo ""
# 获取脚本所在目录和项目根目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"

echo "4. 编译项目并下载依赖..."
cd "$PROJECT_ROOT" && mvn compile

if [ $? -eq 0 ]; then
    echo "✅ 编译成功"
else
    echo "❌ 编译失败"
    exit 1
fi

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

echo ""
echo "5. 启动游戏..."
echo "📋 使用说明："
echo "   1. 游戏启动后，在界面上选择 'AI类型' 为 '大模型AI'"
echo "   2. 选择你的颜色（红方或黑方）"
echo "   3. 点击 '启用AI对弈' 按钮"
echo "   4. 开始下棋，观察AI的详细思考过程"
echo ""
echo "🎯 预期看到的AI输出格式："
echo "   🤖 红方AI思考中..."
echo "      🧠 分析棋局... 🎯 推理中... ✅"
echo "   🧠 AI详细思考过程："
echo "   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "   🎯 【局面分析】：..."
echo "   ⚠️  【威胁评估】：..."
echo "   🤔 【候选走法】：..."
echo "   💡 【最终决策】：..."
echo "   🎮 【走法】：..."
echo "   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "⚠️  注意事项："
echo "   - 如果没有看到思考过程，请确保选择了'大模型AI'"
echo "   - 如果AI响应很慢，这是正常的，大模型需要时间思考"
echo "   - 如果出现错误，请检查Ollama服务和模型是否正常"
echo ""

# 启动游戏（确保包含所有依赖）
mvn exec:java -Dexec.mainClass="com.example.App" -Dexec.classpathScope=runtime