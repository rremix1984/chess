#!/bin/bash

# 坦克大战游戏Maven编译脚本

echo "正在使用Maven编译坦克大战游戏..."

# 检查是否安装了Maven
if ! command -v mvn &> /dev/null; then
    echo "错误：Maven未安装。请先安装Maven。"
    echo "在macOS上，可以使用：brew install maven"
    exit 1
fi

# 清理并编译
mvn clean compile

if [ $? -eq 0 ]; then
    echo "编译成功！"
    echo ""
    echo "运行游戏："
    echo "mvn exec:java"
    echo ""
    echo "或者打包后运行："
    echo "mvn package"
    echo "java -jar target/tank-battle-game-1.0-SNAPSHOT.jar"
else
    echo "编译失败！"
    exit 1
fi
