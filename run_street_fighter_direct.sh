#!/bin/bash

echo "=== 街头霸王直接启动器 (跳过 Maven) ==="
echo "使用原生 Java 命令启动，避免 Maven 相关问题"
echo ""

cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter

# 检查项目是否已编译
if [ ! -f "target/classes/com/example/gameproject/startGame.class" ]; then
    echo "正在编译项目..."
    mvn compile -q
fi

# 获取项目依赖 classpath
echo "正在获取项目依赖..."
CLASSPATH=$(mvn dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt -q && cat /tmp/cp.txt && rm /tmp/cp.txt)

if [ -z "$CLASSPATH" ]; then
    echo "❌ 无法获取项目依赖，尝试使用 Maven 生成依赖"
    mvn dependency:copy-dependencies -DoutputDirectory=target/lib -q
    CLASSPATH="target/lib/*"
fi

# 添加编译后的类路径
FULL_CLASSPATH="target/classes:$CLASSPATH"

# macOS 和 JavaFX 优化参数
JAVA_ARGS="\
-Djava.awt.headless=false \
-Dapple.awt.application.name=StreetFighter \
-Djavafx.embed.singleThread=true \
-Dprism.verbose=false \
-Dcom.apple.macos.useScreenMenuBar=true \
-Xms256m \
-Xmx1024m \
--add-modules javafx.controls,javafx.fxml,javafx.media \
--add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
--add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
--add-exports javafx.base/com.sun.javafx.binding=ALL-UNNAMED \
--add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
--add-exports javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED"

echo "启动命令: java $JAVA_ARGS -cp \"$FULL_CLASSPATH\" com.example.gameproject.startGame"
echo ""
echo "正在启动街头霸王..."
echo "注意: 如果出现模块相关错误，这是正常的，游戏应该仍能运行"
echo ""

# 直接使用 java 命令启动
java $JAVA_ARGS -cp "$FULL_CLASSPATH" com.example.gameproject.startGame

exit_code=$?
echo ""
if [ $exit_code -eq 0 ]; then
    echo "✅ 游戏正常退出"
else
    echo "❌ 游戏退出，代码: $exit_code"
    if [ $exit_code -eq 134 ]; then
        echo "这是 macOS JavaFX 窗口管理崩溃 (SIGABRT)"
        echo "建议使用独立进程启动方式"
    fi
fi
