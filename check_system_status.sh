#!/bin/bash

echo "=============================================="
echo "  多游戏平台系统状态检查"
echo "  Java 11 + JavaFX 17 升级后状态"
echo "=============================================="
echo ""

# 检查 Java 版本
echo "🔍 Java 环境检查:"
echo "JAVA_HOME: $JAVA_HOME"
java -version
echo ""

# 检查 JavaFX 可用性
echo "🔍 JavaFX 检查:"
if command -v javac >/dev/null 2>&1; then
    echo "尝试编译简单的 JavaFX 测试..."
    cat > JavaFXTest.java << 'EOF'
import javafx.application.Application;
import javafx.stage.Stage;
public class JavaFXTest extends Application {
    public void start(Stage stage) { 
        System.out.println("JavaFX 可用!");
        System.exit(0);
    }
    public static void main(String[] args) { launch(args); }
}
EOF
    if javac JavaFXTest.java 2>/dev/null; then
        echo "✅ JavaFX 编译成功"
        rm -f JavaFXTest.java JavaFXTest.class
    else
        echo "❌ JavaFX 编译失败"
    fi
else
    echo "❌ javac 命令不可用"
fi
echo ""

# 检查项目结构和编译状态
echo "🔍 项目结构检查:"
projects=("ChineseChess" "Go" "FlightChess" "TankBattle" "StreetFighter" "game-launcher")

for project in "${projects[@]}"; do
    if [ -d "$project" ]; then
        echo "✅ $project 目录存在"
        if [ -f "$project/pom.xml" ]; then
            echo "  📄 pom.xml 存在"
            # 检查 Java 版本设置
            java_version=$(grep -o "<maven.compiler.target>[^<]*" "$project/pom.xml" | cut -d'>' -f2)
            if [ "$java_version" = "11" ]; then
                echo "  ✅ 目标 Java 版本: $java_version"
            else
                echo "  ⚠️ 目标 Java 版本: $java_version (应该是 11)"
            fi
            
            # 检查编译状态
            if [ -d "$project/target/classes" ]; then
                echo "  ✅ 已编译 (target/classes 存在)"
            else
                echo "  ❓ 未编译或编译失败"
            fi
        else
            echo "  ❓ 不是 Maven 项目"
        fi
    else
        echo "❌ $project 目录不存在"
    fi
    echo ""
done

# 检查启动脚本
echo "🔍 启动脚本检查:"
scripts=("run_street_fighter.sh" "run_javafx_launcher.sh" "game_menu.sh")
for script in "${scripts[@]}"; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            echo "✅ $script (可执行)"
        else
            echo "⚠️ $script (存在但不可执行)"
        fi
    else
        echo "❌ $script 不存在"
    fi
done
echo ""

# 检查 JavaFX 游戏启动器
echo "🔍 JavaFX 启动器检查:"
if [ -f "JavaFXGameLauncher.java" ]; then
    echo "✅ JavaFXGameLauncher.java 存在"
    if [ -f "JavaFXGameLauncher.class" ]; then
        echo "✅ 已编译 (JavaFXGameLauncher.class 存在)"
    else
        echo "❓ 未编译"
    fi
else
    echo "❌ JavaFXGameLauncher.java 不存在"
fi
echo ""

# 街头霸王特别检查
echo "🔍 街头霸王特别检查:"
if [ -d "StreetFighter" ]; then
    cd StreetFighter
    if [ -f "src/main/java/com/example/gameproject/startGame.java" ]; then
        echo "✅ startGame.java 存在"
    else
        echo "❌ startGame.java 不存在"
    fi
    
    if [ -f "target/classes/com/example/gameproject/startGame.class" ]; then
        echo "✅ startGame.class 已编译"
    else
        echo "❓ startGame.class 未编译"
    fi
    
    # 检查 pom.xml 中的 mainClass 设置
    if grep -q "com.example.gameproject.startGame" pom.xml; then
        echo "✅ pom.xml mainClass 设置正确"
    else
        echo "⚠️ pom.xml mainClass 可能需要检查"
    fi
    cd ..
else
    echo "❌ StreetFighter 目录不存在"
fi
echo ""

# macOS 特别提示
echo "🍎 macOS 兼容性提示:"
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "✅ 检测到 macOS 系统"
    echo "⚠️ 已知问题: Swing+JavaFX 混合可能导致 NSTrackingRectTag 崩溃"
    echo "✅ 解决方案: 使用纯 JavaFX 启动器 (JavaFXGameLauncher)"
    echo "✅ 街头霸王使用独立进程启动，避免崩溃"
else
    echo "ℹ️ 非 macOS 系统，无需特别处理"
fi
echo ""

echo "=============================================="
echo "系统检查完成!"
echo ""
echo "推荐使用方式:"
echo "1. 运行纯 JavaFX 启动器: ./run_javafx_launcher.sh"
echo "2. 直接运行街头霸王: ./run_street_fighter.sh" 
echo "3. 使用终端菜单: ./game_menu.sh"
echo "=============================================="
