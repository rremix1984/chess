#!/bin/bash

echo "=== 多游戏平台启动器 (修复版) ==="
echo "基于 Java 11 + JavaFX 17"
echo "使用优化的 JVM 参数避免 macOS 崩溃"
echo ""

cd /Users/wangxiaozhe/workspace/chinese-chess-game

# 检查项目是否构建
if [ ! -f "game-launcher/target/game-launcher-1.0-SNAPSHOT.jar" ]; then
    echo "正在构建项目..."
    mvn clean install -q
fi

echo "Java 版本: $(java -version 2>&1 | head -1)"
echo ""
echo "启动游戏中心（使用优化的 JVM 参数）..."

# 使用优化的 JVM 参数来避免 macOS 相关问题
java \
    -Djava.awt.headless=false \
    -Dapple.awt.application.name="多游戏平台" \
    -Dapple.laf.useScreenMenuBar=true \
    -Xdock:name="多游戏平台" \
    -XX:+UseG1GC \
    -Xms256m \
    -Xmx1024m \
    -Dprism.verbose=false \
    -Djavafx.animation.fullspeed=false \
    -jar game-launcher/target/game-launcher-1.0-SNAPSHOT.jar

echo ""
echo "游戏中心已退出"
