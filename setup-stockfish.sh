#!/bin/bash

echo "设置 Stockfish 引擎..."

# 检查 Stockfish 是否安装
if ! command -v stockfish &> /dev/null; then
    echo "❌ Stockfish 引擎未找到"
    echo "正在安装 Stockfish..."
    if command -v brew &> /dev/null; then
        brew install stockfish
    else
        echo "请安装 Homebrew 或手动安装 Stockfish"
        exit 1
    fi
fi

# 验证 Stockfish 安装
echo "✅ Stockfish 引擎: $(which stockfish)"

# 获取 Stockfish 版本信息
STOCKFISH_VERSION=$(stockfish --help 2>&1 | head -1)
echo "✅ 版本信息: $STOCKFISH_VERSION"

# 创建国际象棋目录
NNUE_DIR="/Users/rremixwang/workspace/chinese/international-chess"
mkdir -p "$NNUE_DIR"

# 使用 Stockfish 内置神经网络文件
echo "ℹ️  使用 Stockfish 内置神经网络文件 (nn-1c0000000000.nnue)"

# 测试 Stockfish UCI 协议
echo "🧪 测试 Stockfish 引擎..."
echo -e "uci\nquit" | stockfish | head -5

# 测试引擎基本功能
echo "🎯 测试引擎分析..."
echo -e "uci\nposition startpos\ngo movetime 500\nquit" | stockfish | grep -E "(bestmove|info string)" | head -3

echo "Stockfish 设置完成！"
