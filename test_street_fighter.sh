#!/bin/bash

# Test Street Fighter JavaFX application
echo "Testing Street Fighter with JavaFX..."

cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter

# Method 1: Try with Maven JavaFX plugin (non-blocking)
echo "Trying Maven JavaFX plugin..."
timeout 10s mvn javafx:run 2>&1 | head -n 20

echo -e "\n=== Test completed ==="
