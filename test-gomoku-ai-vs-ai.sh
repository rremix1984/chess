#!/bin/bash

echo "🎮 测试五子棋AI对AI模式"
echo "========================================"

# 设置Java路径
JAVA_CMD="java"

# 设置类路径
CLASSPATH=""
for jar in $(find . -name "*.jar" 2>/dev/null); do
    if [[ -n "$CLASSPATH" ]]; then
        CLASSPATH="$CLASSPATH:$jar"
    else
        CLASSPATH="$jar"
    fi
done

# 添加编译后的类文件目录
for target_dir in $(find . -name "target" -type d 2>/dev/null); do
    classes_dir="$target_dir/classes"
    if [[ -d "$classes_dir" ]]; then
        if [[ -n "$CLASSPATH" ]]; then
            CLASSPATH="$CLASSPATH:$classes_dir"
        else
            CLASSPATH="$classes_dir"
        fi
    fi
done

echo "📂 类路径设置: $CLASSPATH"
echo ""

echo "🚀 启动五子棋游戏（AI对AI模式测试）..."
echo "💡 启动后，请按以下步骤测试："
echo "   1. 选择 'AI对AI' 模式"
echo "   2. 点击 '启动游戏'"
echo "   3. 观察AI是否开始自动下棋"
echo ""

# 启动五子棋游戏
$JAVA_CMD -cp "$CLASSPATH" com.example.gomoku.GomokuFrame
