#!/bin/bash

echo "🎮 测试游戏界面改进效果"
echo "===================================="

echo "📍 当前目录: $(pwd)"
echo "🔍 查找1.webp文件: $(find . -name "1.webp" 2>/dev/null | head -1)"

echo ""
echo "🎯 五子棋改进："
echo "  • 棋子样式：参考国际象棋和围棋的立体效果"
echo "  • 阴影效果：多层环境阴影，创造柔和的立体感"
echo "  • 渐变设计：黑子使用深灰色渐变，白子使用亮色渐变"
echo "  • 光照效果：添加表面高光和反射光效果"

echo ""
echo "🎯 大富翁改进："
echo "  • 背景图片：使用1.webp作为棋盘中央背景"
echo "  • 智能搜索：自动搜索多个可能的图片路径"
echo "  • 缩放适应：保持图片原始纵横比"
echo "  • 文字覆盖：在图片上添加半透明的标题背景"

echo ""
echo "🚀 启动五子棋（立体棋子效果）:"
echo "cd /Users/wangxiaozhe/workspace/chinese-chess-game && java -cp \"gomoku/target/classes:gomoku/target/dependency/*\" com.example.gomoku.ui.GomokuBoardPanel"

echo ""
echo "🚀 启动大富翁（背景图片效果）:"
echo "cd /Users/wangxiaozhe/workspace/chinese-chess-game && java -cp \"monopoly/target/classes:monopoly/target/dependency/*\" com.example.monopoly.MonopolyFrame"

echo ""
echo "✅ 改进完成！"
echo "===================================="
