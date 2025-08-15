#!/bin/bash

# 中国象棋网络服务器启动脚本
echo "🚀 启动中国象棋网络服务器..."

# 进入项目目录
cd /Users/rremixwang/workspace/chinese/chinese-chess

# 编译项目
echo "📦 编译项目..."
mvn compile -q

# 启动服务器
echo "🌟 启动服务器 (端口: 8080)..."
mvn exec:java -Dexec.mainClass="com.example.chinesechess.network.ChessGameServer" -Dexec.args="8080" -q

echo "🛑 服务器已关闭"
