#!/bin/bash

echo "=== 纯 JavaFX 游戏启动器 ==="
echo "基于 Java 11 + JavaFX 17"
echo "避免 Swing+JavaFX 混合导致的 macOS 崩溃"
echo "JDK版本: $(java -version 2>&1 | head -1)"
echo ""

cd /Users/wangxiaozhe/workspace/chinese-chess-game

# 设置优化的环境变量 (macOS 优化)
export JAVA_OPTS="-Djava.awt.headless=false -Dapple.awt.application.name=GameCenter"
export JAVAFX_OPTS="-Dprism.verbose=false -Djavafx.animation.fullspeed=false"

# 获取 JavaFX 模块路径
JAVAFX_PATH=$(find /usr/local/Cellar/openjdk@11 -name "javafx*.jar" | tr '\n' ':' | sed 's/:$//')
if [ -z "$JAVAFX_PATH" ]; then
    # 备用路径检查
    JAVAFX_PATH="/usr/local/lib/javafx/lib"
fi

# 检查是否需要编译
if [ ! -f "JavaFXGameLauncher.class" ] || [ "JavaFXGameLauncher.java" -nt "JavaFXGameLauncher.class" ]; then
    echo "正在编译 JavaFX 启动器..."
    if [ -n "$JAVAFX_PATH" ]; then
        javac --module-path "$JAVAFX_PATH" --add-modules javafx.controls,javafx.fxml JavaFXGameLauncher.java
    else
        echo "警告: 未找到 JavaFX 路径，尝试使用系统默认..."
        javac JavaFXGameLauncher.java
    fi
    
    if [ $? -ne 0 ]; then
        echo "编译失败! 请检查 JavaFX 是否正确安装。"
        exit 1
    fi
fi

echo "启动纯 JavaFX 游戏中心..."
echo "这个启动器使用独立进程启动游戏，避免了 macOS 上的窗口管理冲突。"
echo ""

# 启动 JavaFX 应用
if [ -n "$JAVAFX_PATH" ]; then
    java --module-path "$JAVAFX_PATH" --add-modules javafx.controls,javafx.fxml JavaFXGameLauncher
else
    java JavaFXGameLauncher
fi

echo ""
echo "游戏中心已关闭"
