#!/bin/bash

echo "=== 街头霸王游戏启动器 ==="
echo "基于 Java 11 + JavaFX 17"
echo "JDK版本: $(java -version 2>&1 | head -1)"
echo ""

cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter

# 检查项目是否已编译
if [ ! -f "target/classes/com/example/gameproject/startGame.class" ]; then
    echo "正在编译项目..."
    mvn compile -q
fi

# 启动街头霸王
echo "正在启动街头霸王游戏..."
echo "提示: macOS 上可能会出现窗口管理相关的警告，这是已知问题。"
echo "游戏界面会正常显示，警告信息可以忽略。"
echo "如需退出游戏，请关闭游戏窗口或按 Ctrl+C"
echo ""

# 使用优化的环境变量启动
export JAVAFX_OPTS="-Dprism.verbose=false -Djavafx.animation.fullspeed=false"
export JAVA_OPTS="-Djava.awt.headless=false -Dapple.awt.application.name=StreetFighter"

mvn javafx:run

echo "街头霸王游戏已退出"
