#!/bin/bash

# 中国象棋游戏启动脚本
# Chinese Chess Game Startup Script

echo "🐎 中国象棋游戏启动中..."
echo "================================"

# 获取脚本所在目录和项目根目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR"

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java环境，请先安装Java 11或更高版本"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven环境，请先安装Maven"
    exit 1
fi

# 检查Ollama服务（用于大模型AI）
echo "🔍 检查Ollama服务状态..."
if pgrep -f "ollama" > /dev/null; then
    echo "✅ Ollama服务正在运行"
    
    # 检查可用模型
    echo "📋 可用的AI模型:"
    ollama list 2>/dev/null | grep -v "NAME" | awk '{print "   - " $1}' || echo "   暂无可用模型"
else
    echo "⚠️  警告: Ollama服务未运行，大模型AI功能将不可用"
    echo "   如需使用大模型AI，请先运行: ollama serve"
fi

echo ""
echo "🎮 游戏功能说明:"
echo "   - 传统AI: 基于规则的象棋AI"
echo "   - 大模型AI: 基于大语言模型的智能AI"
echo "   - 支持人机对战和AI观战模式"
echo ""

# 检查并安装python-chinese-chess库
echo "🐍 检查Python中国象棋库..."
if ! python3 -c "import cchess" 2>/dev/null; then
    echo "📦 python-chinese-chess库未安装，正在安装..."
    if [ -d "$PROJECT_ROOT/python-chinese-chess" ]; then
        cd "$PROJECT_ROOT/python-chinese-chess" && pip3 install . -q
        if [ $? -eq 0 ]; then
            echo "✅ python-chinese-chess库安装成功"
        else
            echo "❌ python-chinese-chess库安装失败，但游戏仍可运行"
        fi
    else
        echo "⚠️  警告：python-chinese-chess目录不存在，跳过安装"
    fi
else
    echo "✅ python-chinese-chess库已安装"
fi
echo ""

# 编译项目
echo "🔨 编译项目..."
# 在项目根目录执行Maven命令
cd "$PROJECT_ROOT" && mvn compile -q

if [ $? -ne 0 ]; then
    echo "❌ 编译失败，请检查代码"
    exit 1
fi

echo "✅ 编译成功"

# 确保依赖已下载并复制到target/dependency目录
if [ ! -d "$PROJECT_ROOT/target/dependency" ] || [ -z "$(ls -A "$PROJECT_ROOT/target/dependency" 2>/dev/null)" ]; then
    echo "📚 下载项目依赖..."
    cd "$PROJECT_ROOT" && mvn dependency:copy-dependencies -q
    if [ $? -ne 0 ]; then
        echo "❌ 依赖下载失败，请检查网络连接"
        exit 1
    fi
    echo "✅ 依赖下载完成"
fi
echo ""

# 启动游戏
echo "🚀 启动游戏..."
echo "================================"
echo "💡 提示: AI决策日志将显示在此控制台中"
echo "   关闭此窗口将结束游戏"
echo ""

# 运行游戏（确保包含所有依赖）
echo "🎮 正在启动游戏选择界面..."
cd "$PROJECT_ROOT" && mvn exec:java -Dexec.mainClass="com.example.App" -Dexec.classpathScope=runtime -q

echo ""
echo "👋 游戏已结束，感谢使用！"