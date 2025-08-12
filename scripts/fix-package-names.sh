#!/bin/bash

# 修复中国象棋模块的包名
echo "修复中国象棋模块的包名..."
find chinese-chess/src/main/java/com/example/chinesechess -name "*.java" -exec sed -i '' 's/^package com\.example\.core;/package com.example.chinesechess.core;/g' {} \;
find chinese-chess/src/main/java/com/example/chinesechess -name "*.java" -exec sed -i '' 's/^package com\.example\.ai;/package com.example.chinesechess.ai;/g' {} \;
find chinese-chess/src/main/java/com/example/chinesechess -name "*.java" -exec sed -i '' 's/^package com\.example\.ui;/package com.example.chinesechess.ui;/g' {} \;
find chinese-chess/src/main/java/com/example/chinesechess -name "*.java" -exec sed -i '' 's/^package com\.example\.terminal;/package com.example.chinesechess.terminal;/g' {} \;

# 修复国际象棋模块的包名
echo "修复国际象棋模块的包名..."
find international-chess/src/main/java/com/example/internationalchess -name "*.java" -exec sed -i '' 's/^package com\.example\.ui;/package com.example.internationalchess.ui;/g' {} \;
find international-chess/src/main/java/com/example/internationalchess -name "*.java" -exec sed -i '' 's/^package com\.example\.ai;/package com.example.internationalchess.ai;/g' {} \;
find international-chess/src/main/java/com/example/internationalchess -name "*.java" -exec sed -i '' 's/^package com\.example\.core;/package com.example.internationalchess.core;/g' {} \;

# 修复五子棋模块的包名
echo "修复五子棋模块的包名..."
find gomoku/src/main/java/com/example/gomoku -name "*.java" -exec sed -i '' 's/^package com\.example\.ui;/package com.example.gomoku.ui;/g' {} \;
find gomoku/src/main/java/com/example/gomoku -name "*.java" -exec sed -i '' 's/^package com\.example\.core;/package com.example.gomoku.core;/g' {} \;

# 修复游戏启动器模块的包名
echo "修复游戏启动器模块的包名..."
find game-launcher/src/main/java/com/example/launcher -name "*.java" -exec sed -i '' 's/^package com\.example\.ui;/package com.example.launcher.ui;/g' {} \;

# 修复飞行棋模块的包名
echo "修复飞行棋模块的包名..."
find flight-chess/src/main/java/com/example/flightchess -name "*.java" -exec sed -i '' 's/^package com\.example\.flight;/package com.example.flightchess;/g' {} \;

# 修复game-common模块的包名
echo "修复game-common模块的包名..."
find game-common/src/main/java/com/example/common -name "*.java" -exec sed -i '' 's/^package com\.example\.sound;/package com.example.common.sound;/g' {} \;

echo "包名修复完成！"