#!/bin/bash

echo "开始修复import语句..."

# 修复chinese-chess模块的import
echo "修复chinese-chess模块的import..."
find chinese-chess/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.core\./import com.example.chinesechess.core./g' {} \;
find chinese-chess/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ai\./import com.example.chinesechess.ai./g' {} \;
find chinese-chess/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ui\./import com.example.chinesechess.ui./g' {} \;

# 修复international-chess模块的import
echo "修复international-chess模块的import..."
find international-chess/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.core\./import com.example.internationalchess.core./g' {} \;
find international-chess/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ai\./import com.example.internationalchess.ai./g' {} \;
find international-chess/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ui\./import com.example.internationalchess.ui./g' {} \;

# 修复gomoku模块的import
echo "修复gomoku模块的import..."
find gomoku/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.core\./import com.example.gomoku.core./g' {} \;
find gomoku/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ai\./import com.example.gomoku.ai./g' {} \;
find gomoku/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ui\./import com.example.gomoku.ui./g' {} \;

# 修复go-game模块的import
echo "修复go-game模块的import..."
find go-game/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.core\./import com.example.gogame.core./g' {} \;
find go-game/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ai\./import com.example.gogame.ai./g' {} \;
find go-game/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ui\./import com.example.gogame.ui./g' {} \;

# 修复flight-chess模块的import
echo "修复flight-chess模块的import..."
find flight-chess/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.core\./import com.example.flightchess.core./g' {} \;
find flight-chess/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ai\./import com.example.flightchess.ai./g' {} \;
find flight-chess/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ui\./import com.example.flightchess.ui./g' {} \;

# 修复game-launcher模块的import
echo "修复game-launcher模块的import..."
find game-launcher/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.core\./import com.example.gamelauncher.core./g' {} \;
find game-launcher/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ai\./import com.example.gamelauncher.ai./g' {} \;
find game-launcher/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ui\./import com.example.gamelauncher.ui./g' {} \;

# 修复game-common模块的import
echo "修复game-common模块的import..."
find game-common/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.core\./import com.example.gamecommon.core./g' {} \;
find game-common/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ai\./import com.example.gamecommon.ai./g' {} \;
find game-common/src -name "*.java" -type f -exec sed -i '' 's/import com\.example\.ui\./import com.example.gamecommon.ui./g' {} \;

echo "import语句修复完成！"