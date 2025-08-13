#!/bin/bash

echo "设置 Pikafish 引擎..."

# 检查 Pikafish 是否安装
if ! command -v pikafish &> /dev/null; then
    echo "❌ Pikafish 引擎未找到"
    echo "请从 https://github.com/official-pikafish/Pikafish 安装 Pikafish"
    exit 1
fi

# 检查神经网络文件
NNUE_PATH="/Users/rremixwang/workspace/chinese/chinese-chess/pikafish.nnue"

if [ ! -f "$NNUE_PATH" ]; then
    echo "❌ Pikafish 神经网络文件不存在: $NNUE_PATH"
    echo "请确保 pikafish.nnue 文件在正确位置"
    exit 1
fi

# 验证设置
echo "✅ Pikafish 引擎: $(which pikafish)"
echo "✅ 神经网络文件: $NNUE_PATH ($(du -h "$NNUE_PATH" | cut -f1))"

# 测试 Pikafish
echo "🧪 测试 Pikafish 引擎..."
echo -e "uci\nquit" | pikafish | head -5

echo "Pikafish 设置完成！"
