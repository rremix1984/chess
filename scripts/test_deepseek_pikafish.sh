#!/bin/bash

# DeepSeek-Pikafish AI 集成测试脚本
# 用于测试 DeepSeekPikafishAI 是否正确集成到游戏中

echo "🚀 开始测试 DeepSeek-Pikafish AI 集成..."
echo "================================================"

# 检查Java环境
echo "📋 检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到Java环境，请先安装Java"
    exit 1
fi

java_version=$(java -version 2>&1 | head -n 1)
echo "✅ Java环境: $java_version"

# 检查项目结构
echo ""
echo "📋 检查项目结构..."
if [ ! -f "pom.xml" ]; then
    echo "❌ 错误：未找到pom.xml文件"
    exit 1
fi
echo "✅ 找到Maven项目配置文件"

if [ ! -d "src/main/java" ]; then
    echo "❌ 错误：未找到源代码目录"
    exit 1
fi
echo "✅ 找到源代码目录"

# 检查关键文件
echo ""
echo "📋 检查关键文件..."
files=(
    "src/main/java/com/example/ai/DeepSeekPikafishAI.java"
    "src/main/java/com/example/terminal/TerminalChessGame.java"
    "src/main/java/com/example/ai/ChessAI.java"
    "src/main/java/com/example/ai/Move.java"
    "src/main/java/com/example/core/Position.java"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ 缺失: $file"
        exit 1
    fi
done

# 编译项目
echo ""
echo "🔨 编译项目..."
if mvn clean compile -q; then
    echo "✅ 项目编译成功"
else
    echo "❌ 项目编译失败"
    exit 1
fi

# 检查DeepSeekPikafishAI类是否正确编译
echo ""
echo "📋 检查DeepSeekPikafishAI类..."
if [ -f "target/classes/com/example/ai/DeepSeekPikafishAI.class" ]; then
    echo "✅ DeepSeekPikafishAI类编译成功"
else
    echo "❌ DeepSeekPikafishAI类编译失败"
    exit 1
fi

# 检查TerminalChessGame是否包含DeepSeekPikafishAI集成
echo ""
echo "📋 检查游戏集成..."
if grep -q "DeepSeekPikafishAI" src/main/java/com/example/terminal/TerminalChessGame.java; then
    echo "✅ TerminalChessGame已集成DeepSeekPikafishAI"
else
    echo "❌ TerminalChessGame未集成DeepSeekPikafishAI"
    exit 1
fi

# 检查AI选项菜单
if grep -q "DeepSeek-Pikafish AI" src/main/java/com/example/terminal/TerminalChessGame.java; then
    echo "✅ AI选择菜单包含DeepSeek-Pikafish选项"
else
    echo "❌ AI选择菜单缺少DeepSeek-Pikafish选项"
    exit 1
fi

# 检查资源清理
if grep -q "cleanupAI" src/main/java/com/example/terminal/TerminalChessGame.java; then
    echo "✅ 包含资源清理逻辑"
else
    echo "❌ 缺少资源清理逻辑"
    exit 1
fi

# 创建测试运行脚本
echo ""
echo "📝 创建测试运行脚本..."
cat > run_deepseek_test.sh << 'EOF'
#!/bin/bash
echo "🎮 启动DeepSeek-Pikafish AI测试..."
echo "请在游戏中选择选项5 (DeepSeek-Pikafish AI) 进行测试"
echo "================================================"
java -cp target/classes com.example.terminal.TerminalChessGame
EOF

chmod +x run_deepseek_test.sh
echo "✅ 测试运行脚本已创建: run_deepseek_test.sh"

echo ""
echo "🎉 DeepSeek-Pikafish AI 集成测试完成！"
echo "================================================"
echo "✅ 所有检查项目都通过"
echo ""
echo "🚀 使用方法："
echo "1. 运行: ./run_deepseek_test.sh"
echo "2. 在游戏中选择选项 5 (DeepSeek-Pikafish AI)"
echo "3. 选择 DeepSeek-R1 模型"
echo "4. 开始游戏测试"
echo ""
echo "📝 注意事项："
echo "- 确保网络连接正常（用于访问DeepSeek API）"
echo "- 确保已配置DeepSeek API密钥"
echo "- 首次使用时可能需要下载Pikafish引擎"
echo ""
echo "🔧 如果遇到问题，请检查："
echo "- API密钥配置"
echo "- 网络连接"
echo "- Pikafish引擎状态"