#!/bin/bash

# 多游戏平台启动脚本
# 作者: AI助手
# 版本: 1.0

echo "🎮 欢迎使用多游戏平台！"
echo "========================================"

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误：未找到Maven，请先安装Maven"
    exit 1
fi

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到Java，请先安装Java 11或更高版本"
    exit 1
fi

# 获取当前目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "📁 当前目录: $SCRIPT_DIR"

# 显示菜单
echo ""
echo "请选择要启动的游戏："
echo "1. 游戏启动器 (推荐 - 包含所有游戏)"
echo "2. 中国象棋"
echo "3. 飞行棋"
echo "4. 五子棋"
echo "5. 围棋"
echo "6. 国际象棋"
echo "7. 坦克大战"
echo "8. 编译所有游戏"
echo "9. 退出"
echo ""

read -p "请输入选择 (1-9): " choice

case $choice in
    1)
        echo "🚀 启动游戏启动器..."
        cd game-launcher
        mvn exec:java -Dexec.mainClass="com.example.launcher.GameLauncher" -q
        ;;
    2)
        echo "🚀 启动中国象棋..."
        cd chinese-chess
        mvn exec:java -Dexec.mainClass="com.example.chinesechess.ChineseChessMain" -q
        ;;
    3)
        echo "🚀 启动飞行棋..."
        cd flight-chess
        mvn exec:java -Dexec.mainClass="com.example.flightchess.FlightChessMain" -q
        ;;
    4)
        echo "🚀 启动五子棋..."
        cd gomoku
        mvn exec:java -Dexec.mainClass="com.example.gomoku.GomokuMain" -q
        ;;
    5)
        echo "🚀 启动围棋..."
        cd go-game
        mvn exec:java -Dexec.mainClass="com.example.go.GoMain" -q
        ;;
    6)
        echo "🚀 启动国际象棋..."
        cd international-chess
        mvn exec:java -Dexec.mainClass="com.example.internationalchess.InternationalChessMain" -q
        ;;
    7)
        echo "🚀 启动坦克大战..."
        cd tank-battle-game
        mvn exec:java -Dexec.mainClass="com.tankbattle.TankBattleGame" -q
        ;;
    8)
        echo "🔧 编译所有游戏..."
        mvn clean compile -Dmaven.test.skip=true
        echo "✅ 编译完成！"
        ;;
    9)
        echo "👋 再见！"
        exit 0
        ;;
    *)
        echo "❌ 无效选择，请输入1-9之间的数字"
        ;;
esac
