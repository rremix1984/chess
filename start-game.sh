#!/bin/bash

# 多游戏平台启动脚本 (Unix/Linux/macOS版本)
# 作者: 游戏开发团队
# 版本: 1.0

echo "==================================="
echo "     多游戏平台启动器"
echo "==================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装Java 11或更高版本"
    echo "请从 https://adoptium.net/ 下载并安装Java"
    exit 1
fi

# 检查Maven环境
#if ! command -v mvn &> /dev/null; then
#    echo "错误: 未找到Maven环境，请先安装Maven"
#    echo "请从 https://maven.apache.org/download.cgi 下载并安装Maven"
#    exit 1
#fi

#echo "正在编译和打包项目..."
#mvn clean package -DskipTests -q
#if [ $? -ne 0 ]; then
#    echo "编译失败，请检查代码"
#    exit 1
#fi

echo "编译完成，启动游戏选择界面..."

# 设置KataGo
./setup-katago.sh

# 设置Pikafish
./setup-pikafish.sh

# 设置Stockfish
./setup-stockfish.sh

# 启动游戏启动器（使用打包后的JAR文件）
java -cp game-launcher/target/game-launcher-1.0-SNAPSHOT.jar com.example.launcher.GameLauncher

if [ $? -ne 0 ]; then
    echo "启动失败，请检查错误信息"
    exit 1
fi

echo "游戏已退出"
