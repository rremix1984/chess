#!/bin/bash

# 中国象棋GUI优化启动脚本
# 功能：启动象棋游戏的图形界面（优化版）
# 版本：2.1 - 解决界面布局和Java性能问题

echo "🏮 启动中国象棋GUI界面（优化版）..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到Java环境，请先安装Java 8或更高版本"
    exit 1
fi

# 显示Java版本
echo "☕ Java版本信息："
java -version

# 获取脚本所在目录和项目根目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"

# 检查并安装python-chinese-chess库
echo "🐍 检查Python中国象棋库..."
if ! python3 -c "import cchess" 2>/dev/null; then
    echo "📦 python-chinese-chess库未安装，正在安装..."
    if [ -d "$PROJECT_ROOT/python-chinese-chess" ]; then
        cd "$PROJECT_ROOT/python-chinese-chess" && pip3 install . -q
        if [ $? -eq 0 ]; then
            echo "✅ python-chinese-chess库安装成功"
        else
            echo "❌ python-chinese-chess库安装失败，但游戏仍可运行"
        fi
    else
        echo "⚠️  警告：python-chinese-chess目录不存在，跳过安装"
    fi
else
    echo "✅ python-chinese-chess库已安装"
fi

# 检查项目是否已编译
if [ ! -d "$PROJECT_ROOT/target/classes" ]; then
    echo "📦 项目未编译，正在编译..."
    if command -v mvn &> /dev/null; then
        # 切换到项目根目录执行Maven命令
        cd "$PROJECT_ROOT" && mvn compile
        if [ $? -ne 0 ]; then
            echo "❌ 编译失败，请检查项目配置"
            exit 1
        fi
    else
        echo "❌ 错误：未找到Maven，请先安装Maven或手动编译项目"
        exit 1
    fi
fi

# 确保依赖已下载并复制到target/dependency目录
if [ ! -d "$PROJECT_ROOT/target/dependency" ] || [ -z "$(ls -A "$PROJECT_ROOT/target/dependency" 2>/dev/null)" ]; then
    echo "📚 下载项目依赖..."
    cd "$PROJECT_ROOT" && mvn dependency:copy-dependencies
    if [ $? -ne 0 ]; then
        echo "❌ 依赖下载失败，请检查网络连接"
        exit 1
    fi
    echo "✅ 依赖下载完成"
fi

# 确保后续命令在项目根目录执行
cd "$PROJECT_ROOT"

echo "🚀 启动GUI界面（优化版）..."
echo "✨ 界面优化："
echo "   • 窗口尺寸：1300x900 (优化棋盘显示)"
echo "   • 控制面板高度：100px (减少遮挡)"
echo "   • 状态栏高度：35px (紧凑设计)"
echo "   • Java内存优化：增加代码缓存和堆内存"
echo ""

# Java优化参数
JAVA_OPTS="-Xmx1024m -Xms512m -XX:ReservedCodeCacheSize=256m -XX:InitialCodeCacheSize=64m"

# 启动GUI应用（使用优化的Java参数）
java $JAVA_OPTS -cp "$PROJECT_ROOT/target/classes:$PROJECT_ROOT/target/dependency/*" com.example.ui.ChessGameMain

echo "👋 游戏已退出"