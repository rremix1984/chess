#!/bin/bash

clear
echo "=============================================="
echo "     🎮 多游戏平台菜单 - macOS 优化版"
echo "     基于 Java 11 + JavaFX 17"
echo "=============================================="
echo ""
echo "系统信息: $(java -version 2>&1 | head -1)"
echo "⚠️  已针对 macOS JavaFX 窗口管理崩溃问题进行优化"
echo ""
echo "🎯 街头霸王启动选项 (推荐使用选项 A):"
echo "  A. 👊 街头霸王 (纯JavaFX启动器-最安全)"
echo "  B. 👊 街头霸王 (安全模式-带崩溃保护)"
echo "  C. 👊 街头霸王 (直接启动-跳过Maven)"
echo "  D. 👊 街头霸王 (标准模式-可能崩溃)"
echo ""
echo "🎮 其他游戏:"
echo "  1. 🏮 中国象棋"
echo "  2. ⚫⚪ 围棋"
echo "  3. ✈️ 飞行棋"
echo "  4. 🚗 坦克大战"
echo ""
echo "🔧 系统工具:"
echo "  5. 🎮 纯JavaFX游戏中心"
echo "  6. 🔍 系统状态检查"
echo "  0. 🚪 退出"
echo ""
echo -n "请输入选项: "

read choice

case $choice in
    A|a)
        echo "🎮 启动街头霸王 (纯JavaFX启动器模式)"
        echo "这是最安全的启动方式，使用独立进程避免 macOS 崩溃"
        cd /Users/wangxiaozhe/workspace/chinese-chess-game
        ./run_javafx_launcher.sh
        ;;
    B|b)
        echo "🛡️ 启动街头霸王 (安全模式)"
        echo "包含 macOS 窗口管理崩溃保护和超时检测"
        cd /Users/wangxiaozhe/workspace/chinese-chess-game
        ./run_street_fighter_safe.sh
        ;;
    C|c)
        echo "⚡ 启动街头霸王 (直接模式)"
        echo "跳过 Maven，直接使用 Java 命令启动"
        cd /Users/wangxiaozhe/workspace/chinese-chess-game
        ./run_street_fighter_direct.sh
        ;;
    D|d)
        echo "📦 启动街头霸王 (标准模式)"
        echo "使用 Maven javafx:run 启动 (可能在 macOS 上崩溃)"
        cd /Users/wangxiaozhe/workspace/chinese-chess-game
        ./run_street_fighter.sh
        ;;
    1)
        echo "启动中国象棋游戏..."
        cd /Users/wangxiaozhe/workspace/chinese-chess-game
        java -cp "game-launcher/target/game-launcher-1.0-SNAPSHOT.jar" com.example.chinesechess.ChineseChessMain
        ;;
    2)
        echo "启动围棋游戏..."
        cd /Users/wangxiaozhe/workspace/chinese-chess-game
        java -cp "game-launcher/target/game-launcher-1.0-SNAPSHOT.jar" com.example.go.GoFrame
        ;;
    3)
        echo "启动飞行棋游戏..."
        cd /Users/wangxiaozhe/workspace/chinese-chess-game
        java -cp "game-launcher/target/game-launcher-1.0-SNAPSHOT.jar" com.example.flightchess.FlightChessFrame
        ;;
    4)
        echo "启动坦克大战游戏..."
        cd /Users/wangxiaozhe/workspace/chinese-chess-game
        java -cp "game-launcher/target/game-launcher-1.0-SNAPSHOT.jar" com.tankbattle.TankBattleGame
        ;;
    5)
        echo "🎪 启动纯 JavaFX 游戏中心"
        echo "图形化界面，支持多个游戏，避免 Swing+JavaFX 混合问题"
        cd /Users/wangxiaozhe/workspace/chinese-chess-game
        ./run_javafx_launcher.sh
        ;;
    6)
        echo "🔍 系统状态检查"
        echo "检查 Java 环境、项目编译状态和兼容性"
        cd /Users/wangxiaozhe/workspace/chinese-chess-game
        ./check_system_status.sh
        ;;
    0)
        echo "👋 再见！感谢使用多游戏平台"
        exit 0
        ;;
    *)
        echo "❌ 无效选项，请重新选择"
        sleep 2
        exec $0
        ;;
esac

echo ""
echo "游戏已退出，按任意键返回菜单..."
read -n 1
exec $0
