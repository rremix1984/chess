#!/bin/bash

echo "=== 多游戏平台启动器 (纯 JavaFX) ==="
echo "基于 Java 11 + JavaFX 17"
echo "避免 Swing+JavaFX 混合导致的崩溃"
echo ""

cd /Users/wangxiaozhe/workspace/chinese-chess-game

# 检查 Java 版本
echo "检查 Java 环境:"
java -version

echo ""
echo "编译并启动纯 JavaFX 游戏启动器..."

# 编译 JavaFX 启动器
javac -cp "game-launcher/target/game-launcher-1.0-SNAPSHOT.jar" JavaFXGameLauncher.java

if [ $? -eq 0 ]; then
    echo "✅ 编译成功，启动游戏中心..."
    echo ""
    
    # 启动纯 JavaFX 启动器
    java -cp "game-launcher/target/game-launcher-1.0-SNAPSHOT.jar:." JavaFXGameLauncher
else
    echo "❌ 编译失败"
    exit 1
fi

echo ""
echo "游戏中心已关闭"
