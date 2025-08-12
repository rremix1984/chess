#!/bin/bash

echo "=== 街头霸王安全启动器 (macOS 优化) ==="
echo "基于 Java 11 + JavaFX 17"
echo "包含 macOS 窗口管理崩溃保护"
echo ""

cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter

# 检查项目是否已编译
if [ ! -f "target/classes/com/example/gameproject/startGame.class" ]; then
    echo "正在编译项目..."
    mvn compile -q
fi

# macOS 特定的 JVM 参数，用于减少窗口管理冲突
export JAVA_OPTS="\
-Djava.awt.headless=false \
-Dapple.awt.application.name=StreetFighter \
-Dapple.awt.UIElement=false \
-Dcom.apple.mrj.application.apple.menu.about.name=StreetFighter \
-Dcom.apple.macos.useScreenMenuBar=true \
-Dcom.apple.macos.use-file-dialog-packages=true \
-Dcom.apple.smallTabs=true \
-Dapple.laf.useScreenMenuBar=true \
-Djava.awt.Window.locationByPlatform=true"

# JavaFX 特定优化参数
export JAVAFX_OPTS="\
-Dprism.verbose=false \
-Dprism.debug=false \
-Dprism.trace=false \
-Dprism.printallocs=false \
-Djavafx.animation.fullspeed=false \
-Djavafx.embed.singleThread=true \
-Djavafx.embed.isEventThread=false \
-Dcom.sun.javafx.isEmbedded=false \
-Dglass.win.minHiDPI=1.0 \
-Dglass.win.maxHiDPI=2.0"

# 内存和垃圾回收优化
export JVM_MEMORY="\
-Xms256m \
-Xmx1024m \
-XX:+UseG1GC \
-XX:G1HeapRegionSize=16m \
-XX:+UseStringDeduplication"

# 合并所有 JVM 参数
COMBINED_OPTS="$JAVA_OPTS $JAVAFX_OPTS $JVM_MEMORY"

echo "正在启动街头霸王游戏..."
echo "使用 macOS 优化参数，减少窗口管理冲突"
echo ""
echo "重要提示:"
echo "1. 如果游戏崩溃，这是 macOS + JavaFX 的已知问题"
echo "2. 可以尝试使用纯 JavaFX 启动器: ./run_javafx_launcher.sh"
echo "3. 游戏界面出现后，避免快速调整窗口大小"
echo "4. 如需退出，请正常关闭游戏窗口，不要强制终止"
echo ""

# 使用 timeout 命令监控启动过程，如果30秒内没有响应则终止
echo "启动中... (30秒超时保护)"

# 创建临时启动脚本以便更好地控制进程
cat > /tmp/sf_start.sh << EOF
#!/bin/bash
export MAVEN_OPTS="$COMBINED_OPTS"
cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter
exec mvn javafx:run
EOF

chmod +x /tmp/sf_start.sh

# 启动游戏，带有超时和错误处理
if timeout 30s /tmp/sf_start.sh; then
    echo ""
    echo "街头霸王游戏已正常退出"
else
    exit_code=$?
    echo ""
    if [ $exit_code -eq 124 ]; then
        echo "⚠️ 游戏启动超时 (30秒内无响应)"
        echo "这可能是 macOS 窗口管理问题导致的"
    else
        echo "❌ 游戏启动失败 (退出代码: $exit_code)"
        echo "建议使用纯 JavaFX 启动器避免此问题"
    fi
    
    echo ""
    echo "替代启动方式:"
    echo "1. 纯 JavaFX 启动器: ./run_javafx_launcher.sh"
    echo "2. 直接使用 Java 命令启动"
    echo "3. 检查是否有其他 Java 进程占用资源"
fi

# 清理临时文件
rm -f /tmp/sf_start.sh

echo ""
echo "启动器已退出"
