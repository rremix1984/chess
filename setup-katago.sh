#!/bin/bash

KATAGO_DIR="$HOME/.katago"
MODEL_DIR="$KATAGO_DIR/models"
CONFIG_DIR="$KATAGO_DIR/configs"

echo "设置 KataGo 配置..."

# 创建目录
mkdir -p "$MODEL_DIR" "$CONFIG_DIR"

# 复制模型文件
if [ ! -f "$MODEL_DIR/model.bin.gz" ]; then
    echo "复制模型文件..."
    cp /opt/homebrew/Cellar/katago/1.16.3/share/katago/g170-b40c256x2-s5095420928-d1229425124.bin.gz "$MODEL_DIR/model.bin.gz"
fi

# 复制配置文件
if [ ! -f "$CONFIG_DIR/gtp_example.cfg" ]; then
    echo "复制配置文件..."
    cp /opt/homebrew/Cellar/katago/1.16.3/share/katago/configs/gtp_example.cfg "$CONFIG_DIR/"
fi

# 验证文件
echo "验证 KataGo 设置..."
echo "模型文件: $(ls -la "$MODEL_DIR/model.bin.gz" 2>/dev/null || echo '不存在')"
echo "配置文件: $(ls -la "$CONFIG_DIR/gtp_example.cfg" 2>/dev/null || echo '不存在')"

echo "KataGo 设置完成！"
