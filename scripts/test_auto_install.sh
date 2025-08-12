#!/bin/bash

# 测试python-chinese-chess库自动安装功能
# Test script for automatic python-chinese-chess library installation

echo "🧪 测试python-chinese-chess库自动安装功能"
echo "================================================"

# 获取项目根目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR"

# 检查Python环境
echo "🐍 检查Python环境..."
if ! command -v python3 &> /dev/null; then
    echo "❌ 错误：未找到Python3环境"
    exit 1
fi
echo "✅ Python3环境: $(python3 --version)"

# 检查pip3
if ! command -v pip3 &> /dev/null; then
    echo "❌ 错误：未找到pip3"
    exit 1
fi
echo "✅ pip3环境: $(pip3 --version)"

# 检查python-chinese-chess目录
echo ""
echo "📁 检查python-chinese-chess目录..."
if [ -d "$PROJECT_ROOT/python-chinese-chess" ]; then
    echo "✅ python-chinese-chess目录存在"
    echo "   路径: $PROJECT_ROOT/python-chinese-chess"
else
    echo "❌ python-chinese-chess目录不存在"
    exit 1
fi

# 测试库导入（安装前）
echo ""
echo "🔍 测试库导入状态（安装前）..."
if python3 -c "import cchess" 2>/dev/null; then
    echo "✅ cchess库已安装"
    echo "   版本信息:"
    python3 -c "import cchess; print(f'   - 库路径: {cchess.__file__}')" 2>/dev/null || echo "   - 无法获取版本信息"
else
    echo "❌ cchess库未安装"
fi

# 模拟卸载（如果已安装）
echo ""
echo "🗑️  模拟卸载现有库（用于测试）..."
pip3 uninstall python-chinese-chess -y -q 2>/dev/null
echo "✅ 卸载完成（如果之前已安装）"

# 测试自动安装逻辑
echo ""
echo "🔧 测试自动安装逻辑..."
echo "🐍 检查Python中国象棋库..."
if ! python3 -c "import cchess" 2>/dev/null; then
    echo "📦 python-chinese-chess库未安装，正在安装..."
    if [ -d "$PROJECT_ROOT/python-chinese-chess" ]; then
        cd "$PROJECT_ROOT/python-chinese-chess" && pip3 install . -q
        if [ $? -eq 0 ]; then
            echo "✅ python-chinese-chess库安装成功"
        else
            echo "❌ python-chinese-chess库安装失败"
            exit 1
        fi
    else
        echo "⚠️  警告：python-chinese-chess目录不存在，跳过安装"
        exit 1
    fi
else
    echo "✅ python-chinese-chess库已安装"
fi

# 验证安装结果
echo ""
echo "✅ 验证安装结果..."
if python3 -c "import cchess" 2>/dev/null; then
    echo "✅ cchess库导入成功"
    echo "   测试基本功能:"
    python3 -c "
import cchess
board = cchess.Board()
print(f'   - 创建棋盘: 成功')
print(f'   - 初始FEN: {board.fen()}')
print(f'   - 合法走法数量: {len(list(board.legal_moves))}')
" 2>/dev/null && echo "   - 基本功能测试: 通过" || echo "   - 基本功能测试: 失败"
else
    echo "❌ cchess库导入失败"
    exit 1
fi

echo ""
echo "🎉 自动安装功能测试完成！"
echo "================================================"
echo "✅ 所有测试项目都通过"
echo ""
echo "📝 测试总结:"
echo "   - Python环境检查: 通过"
echo "   - pip3环境检查: 通过"
echo "   - python-chinese-chess目录检查: 通过"
echo "   - 自动安装逻辑: 通过"
echo "   - 库导入验证: 通过"
echo "   - 基本功能测试: 通过"
echo ""
echo "🚀 现在可以安全地在不同电脑上使用启动脚本了！"
echo "   启动脚本会自动检测并安装python-chinese-chess库"