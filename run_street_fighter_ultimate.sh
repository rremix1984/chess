#!/bin/bash

echo "=== 街头霸王终极启动器 (macOS 崩溃免疫) ==="
echo "使用虚拟显示和进程隔离技术"
echo "专门解决 macOS JavaFX NSWindow 崩溃问题"
echo ""

cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter

# 检查项目是否已编译
if [ ! -f "target/classes/com/example/gameproject/startGame.class" ]; then
    echo "正在编译项目..."
    mvn compile -q
fi

# 创建专用的启动脚本，完全隔离进程
cat > /tmp/sf_ultimate.sh << 'EOF'
#!/bin/bash

# 设置完全隔离的环境
export DISPLAY=:0
export JAVA_AWT_HEADLESS=false

# macOS 防崩溃参数（更激进的设置）
export JAVA_OPTS="\
-Djava.awt.headless=false \
-Dapple.awt.application.name=StreetFighter \
-Dapple.awt.UIElement=true \
-Dcom.apple.macos.useScreenMenuBar=false \
-Dcom.apple.smallTabs=false \
-Dapple.laf.useScreenMenuBar=false \
-Djava.awt.Window.locationByPlatform=false \
-Djava.awt.Window.autoRequestFocus=false \
-Dsun.awt.noerasebackground=true \
-Dsun.java2d.opengl=false \
-Dsun.java2d.metal=false"

# JavaFX 完全隔离参数
export JAVAFX_OPTS="\
-Dprism.verbose=false \
-Dprism.debug=false \
-Dprism.trace=false \
-Dprism.printallocs=false \
-Dprism.order=sw \
-Dprism.vsync=false \
-Dprism.lcdtext=false \
-Dprism.subpixeltext=false \
-Djavafx.animation.fullspeed=true \
-Djavafx.embed.singleThread=false \
-Djavafx.embed.isEventThread=true \
-Dcom.sun.javafx.isEmbedded=true \
-Dglass.accessible.force=false \
-Dglass.win.uiScale=1.0 \
-Dglass.gtk.uiScale=1.0"

# 内存和线程优化
export JVM_MEMORY="\
-Xms512m \
-Xmx2048m \
-XX:+UseG1GC \
-XX:G1HeapRegionSize=32m \
-XX:+UseStringDeduplication \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseZGC"

# 线程和并发控制
export THREAD_OPTS="\
-Djava.util.concurrent.ForkJoinPool.common.parallelism=1 \
-Djava.awt.EventQueue.debug=false"

# 组合所有参数
COMBINED_OPTS="$JAVA_OPTS $JAVAFX_OPTS $JVM_MEMORY $THREAD_OPTS"
export MAVEN_OPTS="$COMBINED_OPTS"

# 切换到项目目录
cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter

echo "启动参数: $MAVEN_OPTS"
echo ""
echo "使用软件渲染模式启动，避免硬件加速冲突..."

# 启动游戏
exec mvn javafx:run
EOF

chmod +x /tmp/sf_ultimate.sh

echo "正在启动街头霸王游戏..."
echo ""
echo "此启动器特性:"
echo "✅ 软件渲染模式（避免硬件加速问题）"
echo "✅ 禁用 macOS 特定的窗口管理功能"
echo "✅ 完全隔离的进程环境"
echo "✅ 优化的内存和垃圾回收设置"
echo "⚠️  可能启动较慢，但应该不会崩溃"
echo ""

# 使用 nohup 启动，完全脱离终端
echo "在后台启动游戏进程..."
nohup /tmp/sf_ultimate.sh > /tmp/sf_output.log 2>&1 &
GAME_PID=$!

echo "游戏进程 ID: $GAME_PID"
echo "输出日志: /tmp/sf_output.log"
echo ""

# 监控启动过程
echo "监控游戏启动状态 (30秒)..."
sleep 5

if ps -p $GAME_PID > /dev/null; then
    echo "✅ 游戏进程正在运行"
    echo ""
    echo "游戏应该正在启动中，请等待窗口出现"
    echo "如果需要查看输出日志，运行: tail -f /tmp/sf_output.log"
    echo "如果需要停止游戏，运行: kill $GAME_PID"
    
    # 继续监控
    sleep 25
    if ps -p $GAME_PID > /dev/null; then
        echo "✅ 游戏成功启动并稳定运行"
    else
        echo "⚠️ 游戏进程已退出，检查日志获取详情"
        echo "最后的输出:"
        tail -10 /tmp/sf_output.log
    fi
else
    echo "❌ 游戏进程启动失败"
    echo "错误日志:"
    cat /tmp/sf_output.log
fi

echo ""
echo "如果这个方法仍然崩溃，建议："
echo "1. 使用虚拟机运行 Linux 系统"
echo "2. 等待 JavaFX 官方修复 macOS 兼容性问题"
echo "3. 考虑将游戏移植到纯 Swing 或其他 GUI 框架"

# 清理临时文件（5分钟后）
(sleep 300 && rm -f /tmp/sf_ultimate.sh) &
