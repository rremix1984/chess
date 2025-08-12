#!/bin/bash

echo "=== 街头霸王增强版启动器 ==="
echo "基于 Java 11 + JavaFX 17"
echo "全新的街头霸王游戏体验"
echo ""

cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighterNew

# 检查项目是否已编译
if [ ! -f "target/classes/com/example/gameproject/startGame.class" ]; then
    echo "正在编译项目..."
    mvn compile -q
fi

# macOS 特定的 JVM 参数
export JAVA_OPTS="\
-Djava.awt.headless=false \
-Dapple.awt.application.name=StreetFighterNew \
-Dcom.apple.macos.useScreenMenuBar=true \
-Djava.awt.Window.locationByPlatform=true"

# JavaFX 优化参数
export JAVAFX_OPTS="\
-Dprism.verbose=false \
-Djavafx.embed.singleThread=true \
-Dcom.sun.javafx.isEmbedded=false"

# 合并所有 JVM 参数
COMBINED_OPTS="$JAVA_OPTS $JAVAFX_OPTS"
export MAVEN_OPTS="$COMBINED_OPTS"

echo "正在启动街头霸王增强版..."
echo "特性:"
echo "✅ 多人游戏支持"
echo "✅ 高级动画和特效"  
echo "✅ 可自定义 UI 缩放"
echo "✅ 音效和音乐控制"
echo "✅ 设置持久化存储"
echo ""

# 启动游戏
mvn javafx:run

echo ""
echo "街头霸王增强版已退出"
