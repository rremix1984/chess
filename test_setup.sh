#!/bin/bash

echo "=== 街头霸王游戏环境测试 ==="
echo ""

echo "1. Java 版本检查:"
java -version

echo ""
echo "2. Maven 版本检查:"
mvn -version | head -1

echo ""
echo "3. 检查主类文件是否存在:"
MAIN_CLASS_FILE="/Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter/target/classes/com/example/gameproject/startGame.class"
if [ -f "$MAIN_CLASS_FILE" ]; then
    echo "✅ 主类文件存在: $MAIN_CLASS_FILE"
else
    echo "❌ 主类文件不存在，正在编译..."
    cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter
    mvn compile -q
    if [ -f "$MAIN_CLASS_FILE" ]; then
        echo "✅ 编译成功，主类文件已创建"
    else
        echo "❌ 编译失败"
        exit 1
    fi
fi

echo ""
echo "4. 检查 JavaFX 依赖:"
cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter
mvn dependency:tree | grep javafx-controls | head -1

echo ""
echo "5. 检查 pom.xml 配置:"
grep -A 1 "mainClass" pom.xml

echo ""
echo "=== 测试完成 ==="
echo "如果以上所有检查都显示 ✅，那么街头霸王游戏应该可以正常启动。"
echo ""
echo "启动方法:"
echo "1. 使用脚本: ./run_street_fighter.sh"
echo "2. 使用 Maven: cd StreetFighter && mvn javafx:run"
