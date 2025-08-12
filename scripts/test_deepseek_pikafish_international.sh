#!/bin/bash

# DeepSeek+Pikafish AI 国际象棋集成测试脚本
# 测试DeepSeek大模型与Pikafish引擎在国际象棋中的集成

echo "🚀 开始测试 DeepSeek+Pikafish AI 国际象棋集成..."
echo "=================================================="

# 检查必要的依赖
echo "📋 检查依赖..."

# 检查Java
if ! command -v java &> /dev/null; then
    echo "❌ Java未安装"
    exit 1
fi
echo "✅ Java已安装: $(java -version 2>&1 | head -n 1)"

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven未安装"
    exit 1
fi
echo "✅ Maven已安装: $(mvn -version | head -n 1)"

# 检查Ollama
if ! command -v ollama &> /dev/null; then
    echo "❌ Ollama未安装"
    exit 1
fi
echo "✅ Ollama已安装"

# 检查DeepSeek模型
echo "🔍 检查DeepSeek模型..."
if ollama list | grep -q "deepseek"; then
    echo "✅ DeepSeek模型已安装"
    ollama list | grep deepseek
else
    echo "❌ DeepSeek模型未安装，请先运行: ollama pull deepseek-r1:7b"
    exit 1
fi

# 检查Pikafish模拟器
if [ ! -f "./pikafish_mock.py" ]; then
    echo "❌ Pikafish模拟器未找到"
    exit 1
fi
echo "✅ Pikafish模拟器已就绪"

# 编译项目
echo "🔨 编译项目..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ 项目编译失败"
    exit 1
fi
echo "✅ 项目编译成功"

# 测试DeepSeek+Pikafish AI集成
echo "🎯 测试DeepSeek+Pikafish AI集成..."
echo "=================================================="

# 启动国际象棋GUI模式（带AI）
echo "🎮 启动国际象棋GUI模式..."
echo "   - 将自动启用AI对弈"
echo "   - DeepSeek模型: deepseek-r1:7b"
echo "   - Pikafish引擎: 已集成"
echo "   - 请在GUI中选择'DeepSeek+Pikafish AI'进行测试"
echo ""
echo "💡 测试步骤："
echo "   1. 在AI选择下拉菜单中选择 'DeepSeek+Pikafish AI'"
echo "   2. 点击'启用AI对弈'按钮"
echo "   3. 开始下棋，观察AI的决策过程"
echo "   4. 查看AI决策日志，确认DeepSeek和Pikafish的协作"
echo ""
echo "🔍 预期结果："
echo "   - AI能够正常响应用户走法"
echo "   - 决策日志显示DeepSeek分析和Pikafish评估"
echo "   - 走法符合国际象棋规则"
echo "   - 响应时间合理（通常5-15秒）"
echo ""

# 设置Pikafish路径并启动
export PIKAFISH_PATH="./pikafish_mock.py"
mvn exec:java -Dexec.mainClass="com.example.App" -Dexec.args="international" -Dpikafish.path="./pikafish_mock.py"