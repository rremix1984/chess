#!/bin/bash

echo "🧪 五子棋AI增强测试"
echo "======================="
echo ""

# 设置类路径
CLASSPATH="game-launcher/target/game-launcher-1.0-SNAPSHOT.jar:gomoku/target/classes:game-common/target/classes"

echo "🎯 启动五子棋游戏..."
echo "新的AI增强功能："
echo "  ✅ 搜索深度大幅提升（简单4层→大师12层）"
echo "  ✅ 强化的评估函数（支持跳跃连接和复杂棋形）"
echo "  ✅ 威胁检测系统（自动识别活四、冲四、双三等）"
echo "  ✅ GomokuZero MCTS模拟次数4倍提升"
echo "  ✅ 双重威胁机会检测"
echo ""

echo "💡 建议测试方法："
echo "  1. 选择'高级AI'或'GomokuZero'类型"
echo "  2. 设置难度为'困难'或更高"
echo "  3. 观察AI的思考过程（在聊天框显示）"
echo "  4. 感受更强的对弈挑战！"
echo ""

# 启动游戏
java -cp "$CLASSPATH" com.example.gomoku.GomokuFrame
