#!/bin/bash

echo "🧪 测试本地Pikafish引擎配置"
echo "================================"

# 检查引擎文件
ENGINE_PATH="./pikafish_engine/MacOS/pikafish-apple-silicon"
NNUE_PATH="./pikafish.nnue"

echo "📁 检查文件存在性..."
if [ -f "$ENGINE_PATH" ]; then
    echo "✅ Pikafish引擎文件存在: $ENGINE_PATH"
    echo "   文件大小: $(ls -lh "$ENGINE_PATH" | awk '{print $5}')"
    echo "   权限: $(ls -l "$ENGINE_PATH" | awk '{print $1}')"
else
    echo "❌ Pikafish引擎文件不存在: $ENGINE_PATH"
    exit 1
fi

if [ -f "$NNUE_PATH" ]; then
    echo "✅ 神经网络文件存在: $NNUE_PATH"
    echo "   文件大小: $(ls -lh "$NNUE_PATH" | awk '{print $5}')"
else
    echo "❌ 神经网络文件不存在: $NNUE_PATH"
    exit 1
fi

echo ""
echo "🔧 测试引擎启动..."
echo "quit" | "$ENGINE_PATH" > /tmp/pikafish_test.log 2>&1 &
ENGINE_PID=$!
sleep 2
kill $ENGINE_PID 2>/dev/null

if [ -s /tmp/pikafish_test.log ]; then
    echo "✅ 引擎可以正常启动"
    echo "引擎信息:"
    grep "id name\|id author" /tmp/pikafish_test.log | head -2
else
    echo "❌ 引擎启动失败"
    echo "错误信息:"
    cat /tmp/pikafish_test.log
    exit 1
fi

echo ""
echo "🎯 测试神经网络文件加载..."
echo -e "uci\nsetoption name EvalFile value $(pwd)/pikafish.nnue\nisready\nquit" | "$ENGINE_PATH" > /tmp/pikafish_nnue_test.log 2>&1 &
NNUE_PID=$!
sleep 3
kill $NNUE_PID 2>/dev/null

if grep -q "readyok" /tmp/pikafish_nnue_test.log; then
    echo "✅ 神经网络文件加载成功"
else
    echo "❌ 神经网络文件加载失败"
    echo "详细信息:"
    cat /tmp/pikafish_nnue_test.log
    exit 1
fi

echo ""
echo "🎉 所有测试通过！"
echo "✅ Pikafish引擎已成功移动到项目目录"
echo "✅ 神经网络文件配置正确"
echo "✅ 引擎可以正常启动和加载神经网络"

# 清理临时文件
rm -f /tmp/pikafish_test.log /tmp/pikafish_nnue_test.log

echo ""
echo "📝 配置信息:"
echo "   引擎路径: $ENGINE_PATH"
echo "   神经网络文件: $NNUE_PATH"
echo "   项目目录: $(pwd)"