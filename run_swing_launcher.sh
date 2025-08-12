#!/bin/bash

echo "=== 纯 Swing 游戏启动器 ==="
echo "完全避免 JavaFX，解决 macOS 崩溃问题"
echo "使用 Java Swing GUI，100% macOS 兼容"
echo ""

cd /Users/wangxiaozhe/workspace/chinese-chess-game

# 检查 Java 版本
echo "检查 Java 环境..."
java_version=$(java -version 2>&1 | head -1)
echo "Java 版本: $java_version"

if [[ "$java_version" == *"11"* ]]; then
    echo "✅ Java 11 检测成功"
else
    echo "⚠️ 建议使用 Java 11，当前版本可能有兼容性问题"
fi
echo ""

# 编译 Swing 启动器
if [ ! -f "SwingGameLauncher.class" ] || [ "SwingGameLauncher.java" -nt "SwingGameLauncher.class" ]; then
    echo "编译纯 Swing 启动器..."
    javac SwingGameLauncher.java
    
    if [ $? -eq 0 ]; then
        echo "✅ 编译成功"
    else
        echo "❌ 编译失败"
        exit 1
    fi
else
    echo "✅ 启动器已编译"
fi

echo ""
echo "启动纯 Swing 游戏中心..."
echo ""
echo "特点:"
echo "✅ 纯 Swing GUI - 避免所有 JavaFX 问题"
echo "✅ macOS 原生集成 - 使用系统菜单栏"
echo "✅ 独立进程启动 - 每个游戏运行在独立进程中"
echo "✅ 完全稳定 - 不会出现 NSTrackingRectTag 崩溃"
echo ""

# 设置 macOS 特定的 JVM 参数
MACOS_OPTS=""
if [[ "$OSTYPE" == "darwin"* ]]; then
    MACOS_OPTS="-Dapple.awt.application.name=GameCenter -Dcom.apple.macos.useScreenMenuBar=true -Dapple.laf.useScreenMenuBar=true"
fi

# 启动 Swing 启动器
java $MACOS_OPTS SwingGameLauncher

echo ""
echo "Swing 游戏中心已关闭"
