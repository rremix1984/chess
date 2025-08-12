#!/bin/bash

echo "启动坦克大战游戏测试..."
echo "新的背景特性包括："
echo "- 300颗动态闪烁的星星"
echo "- 15朵流动的云朵"
echo "- 400个彩色粒子"
echo "- 12个脉冲渐变区域"
echo "- 25个旋转几何图形"
echo "- 动态颜色爆发效果"
echo "- 随机闪电效果"
echo "- 流动的彩虹条纹"
echo "- 多层动态渐变背景"
echo ""
echo "按下 Ctrl+C 可以停止游戏"
echo ""

# 启动游戏
java -cp 'bin:lib/json-20230227.jar:lib/httpclient-4.5.14.jar:lib/httpcore-4.4.16.jar:lib/commons-logging-1.2.jar:lib/commons-codec-1.15.jar' com.tankbattle.TankBattleGame
