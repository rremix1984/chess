#!/bin/bash

echo "开始修复com.example.core引用..."

# 修复chinese-chess模块中的com.example.core引用
find chinese-chess/src -name "*.java" -type f -exec sed -i '' 's/com\.example\.core\./com.example.chinesechess.core./g' {} \;

# 修复international-chess模块中的com.example.core引用
find international-chess/src -name "*.java" -type f -exec sed -i '' 's/com\.example\.core\./com.example.internationalchess.core./g' {} \;

# 修复gomoku模块中的com.example.core引用
find gomoku/src -name "*.java" -type f -exec sed -i '' 's/com\.example\.core\./com.example.gomoku.core./g' {} \;

# 修复go-game模块中的com.example.core引用
find go-game/src -name "*.java" -type f -exec sed -i '' 's/com\.example\.core\./com.example.gogame.core./g' {} \;

# 修复flight-chess模块中的com.example.core引用
find flight-chess/src -name "*.java" -type f -exec sed -i '' 's/com\.example\.core\./com.example.flightchess.core./g' {} \;

# 修复game-launcher模块中的com.example.core引用
find game-launcher/src -name "*.java" -type f -exec sed -i '' 's/com\.example\.core\./com.example.gamelauncher.core./g' {} \;

# 修复game-common模块中的com.example.core引用
find game-common/src -name "*.java" -type f -exec sed -i '' 's/com\.example\.core\./com.example.gamecommon.core./g' {} \;

echo "com.example.core引用修复完成！"