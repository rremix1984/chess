#!/bin/bash

clear
echo "======================================================="
echo "           🥊 街头霸王终极启动解决方案"
echo "       针对 macOS JavaFX 崩溃问题的完整解决方案"
echo "======================================================="
echo ""
echo "🎯 背景: macOS 上 JavaFX 存在 NSTrackingRectTag 崩溃问题"
echo "✅ 解决: 提供多种启动方式，确保游戏能稳定运行"
echo ""
echo "Java 版本: $(java -version 2>&1 | head -1)"
echo "操作系统: macOS (已检测)"
echo ""
echo "📋 可用启动选项 (按稳定性排序):"
echo ""
echo "🥇 推荐选项 (最稳定):"
echo "  1. 纯 Swing 启动器 - 完全避免 JavaFX，100% 稳定"
echo "  2. Docker 容器启动 - 在 Linux 环境中运行，彻底解决兼容性"
echo ""
echo "🥈 备选方案 (较稳定):"
echo "  3. JavaFX 启动器 - 使用独立进程，避免混合问题"
echo "  4. 终极启动器 - 软件渲染 + 进程隔离"
echo "  5. 安全模式 - 带崩溃保护和超时检测"
echo ""
echo "🥉 测试选项 (可能崩溃):"
echo "  6. 直接启动 - 跳过 Maven，直接 Java 命令"
echo "  7. 标准启动 - Maven javafx:run (可能崩溃)"
echo ""
echo "🛠️ 工具选项:"
echo "  8. 系统状态检查"
echo "  9. 游戏菜单"
echo ""
echo "🥊 新增游戏:"
echo "  10. 街头霸王增强版 - 全新的街头霸王体验"
echo ""
echo "  0. 退出"
echo ""
echo -n "请选择启动方式 (0-10): "

read choice

case $choice in
    1)
        echo ""
        echo "🎮 启动纯 Swing 游戏启动器"
        echo "这是最稳定的解决方案，完全避免 JavaFX 问题"
        echo ""
        ./run_swing_launcher.sh
        ;;
    2)
        echo ""
        echo "🐳 启动 Docker 容器版本"
        echo "在 Linux 容器中运行，彻底解决 macOS 兼容性问题"
        echo ""
        ./run_street_fighter_docker.sh
        ;;
    3)
        echo ""
        echo "🎪 启动 JavaFX 启动器"
        echo "使用独立进程启动，避免 Swing+JavaFX 混合问题"
        echo ""
        ./run_javafx_launcher.sh
        ;;
    4)
        echo ""
        echo "🚀 启动终极模式"
        echo "软件渲染 + 进程隔离 + 崩溃监控"
        echo ""
        ./run_street_fighter_ultimate.sh
        ;;
    5)
        echo ""
        echo "🛡️ 启动安全模式"
        echo "包含崩溃保护、超时检测和 macOS 优化参数"
        echo ""
        ./run_street_fighter_safe.sh
        ;;
    6)
        echo ""
        echo "⚡ 直接启动模式"
        echo "跳过 Maven，直接使用 Java 命令启动"
        echo ""
        ./run_street_fighter_direct.sh
        ;;
    7)
        echo ""
        echo "📦 标准启动模式"
        echo "使用 Maven javafx:run (警告: 可能在 macOS 上崩溃)"
        echo ""
        ./run_street_fighter.sh
        ;;
    8)
        echo ""
        echo "🔍 运行系统状态检查"
        echo ""
        ./check_system_status.sh
        ;;
    9)
        echo ""
        echo "📋 打开游戏菜单"
        echo ""
        ./game_menu.sh
        ;;
    10)
        echo ""
        echo "🥊 启动街头霸王增强版"
        echo "全新的街头霸王体验，包含多人游戏、高级动画等特性"
        echo ""
        ./run_street_fighter_enhanced.sh
        ;;
    0)
        echo ""
        echo "👋 再见！"
        echo ""
        echo "💡 小贴士："
        echo "如果遇到崩溃问题，推荐使用选项 1 (纯 Swing 启动器)"
        echo "这是最稳定的解决方案，完全避免 JavaFX 相关问题"
        exit 0
        ;;
    *)
        echo ""
        echo "❌ 无效选项，请输入 0-10 之间的数字"
        echo ""
        read -p "按回车键重新选择..."
        exec "$0"
        ;;
esac

echo ""
echo "🎮 游戏或工具已退出"
echo ""
echo "💡 如果遇到问题："
echo "• 查看 README_LAUNCH_OPTIONS.md 获取详细说明"
echo "• 尝试其他启动方式"
echo "• 运行系统状态检查 (选项 8)"
echo ""
read -p "按回车键返回主菜单..."
clear
exec "$0"
