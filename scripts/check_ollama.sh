#!/bin/bash

echo "🔍 检查ollama安装和模型状态..."
echo "================================"

# 检查ollama是否安装
if command -v ollama &> /dev/null; then
    echo "✅ ollama已安装"
else
    echo "❌ ollama未安装，请先安装ollama"
    echo "   安装方法: curl -fsSL https://ollama.ai/install.sh | sh"
    exit 1
fi

# 检查ollama服务状态
echo ""
echo "🔧 检查ollama服务状态..."
if ollama list &> /dev/null; then
    echo "✅ ollama服务正在运行"
    echo ""
    echo "📋 可用模型列表:"
    ollama list
else
    echo "❌ ollama服务未运行"
    echo "   请先启动ollama服务: ollama serve"
    echo "   或者在后台运行: nohup ollama serve &"
    echo ""
    echo "💡 如果没有模型，可以下载推荐模型:"
    echo "   ollama pull deepseek-r1:7b"
    echo "   ollama pull qwen2.5:7b"
fi

echo ""
echo "🎮 现在可以启动游戏，AI模型选择框将自动列出可用模型！"