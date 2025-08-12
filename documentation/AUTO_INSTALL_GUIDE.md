# Python-Chinese-Chess 自动安装功能说明

## 概述

为了解决在不同电脑上需要手动安装 `python-chinese-chess` 库的问题，我们在启动脚本中集成了自动检测和安装功能。现在启动脚本会自动检查库是否已安装，如果未安装则自动进行安装。

## 修改的文件

### 1. 主要启动脚本

- **`start_chess.sh`** - 项目根目录的主启动脚本
- **`docs/scripts/start_ui.sh`** - GUI界面启动脚本
- **`docs/scripts/start_ui_optimized.sh`** - 优化版GUI启动脚本

### 2. 测试脚本

- **`test_auto_install.sh`** - 自动安装功能测试脚本

## 自动安装逻辑

每个启动脚本都包含以下检测和安装逻辑：

```bash
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
```

## 工作流程

1. **检测库状态**: 尝试导入 `cchess` 模块
2. **检查源码目录**: 确认 `python-chinese-chess` 目录存在
3. **自动安装**: 如果库未安装且源码目录存在，则执行 `pip3 install .`
4. **状态反馈**: 显示安装结果和状态信息
5. **继续启动**: 无论安装是否成功，游戏都会继续启动

## 使用方法

### 在新电脑上首次使用

1. 克隆或复制整个项目到新电脑
2. 确保包含 `python-chinese-chess` 目录
3. 运行任意启动脚本：
   ```bash
   # 方式1：使用主启动脚本
   ./start_chess.sh
   
   # 方式2：使用GUI启动脚本
   ./docs/scripts/start_ui.sh
   
   # 方式3：使用优化版GUI启动脚本
   ./docs/scripts/start_ui_optimized.sh
   ```
4. 脚本会自动检测并安装 `python-chinese-chess` 库
5. 游戏正常启动

### 测试自动安装功能

运行测试脚本验证功能：
```bash
./test_auto_install.sh
```

## 前置条件

- **Python 3**: 系统需要安装 Python 3
- **pip3**: 需要 pip3 包管理器
- **源码目录**: 项目中需要包含 `python-chinese-chess` 目录
- **网络连接**: 首次安装时可能需要下载依赖

## 错误处理

### 常见问题及解决方案

1. **Python3 未安装**
   ```
   ❌ 错误：未找到Python3环境
   ```
   解决：安装 Python 3.7 或更高版本

2. **pip3 未安装**
   ```
   ❌ 错误：未找到pip3
   ```
   解决：安装 pip3 或使用 `python3 -m pip`

3. **源码目录缺失**
   ```
   ⚠️  警告：python-chinese-chess目录不存在，跳过安装
   ```
   解决：确保项目包含完整的 `python-chinese-chess` 目录

4. **权限问题**
   ```
   ❌ python-chinese-chess库安装失败，但游戏仍可运行
   ```
   解决：使用 `sudo` 或配置用户级安装

## 优势

1. **自动化**: 无需手动执行安装命令
2. **跨平台**: 在不同电脑上都能自动工作
3. **容错性**: 安装失败不会阻止游戏启动
4. **智能检测**: 避免重复安装已存在的库
5. **用户友好**: 提供清晰的状态反馈

## 技术细节

### 检测机制

使用 `python3 -c "import cchess"` 来检测库是否已安装：
- 成功导入：库已安装
- 导入失败：库未安装，需要安装

### 安装方式

使用 `pip3 install . -q` 进行本地安装：
- `.` 表示当前目录（python-chinese-chess）
- `-q` 静默安装，减少输出

### 路径处理

自动检测项目根目录：
```bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"
```

## 维护说明

如果需要在其他启动脚本中添加相同功能，请复制以下代码块：

```bash
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
```

确保在项目编译之前添加此代码块。

## 版本历史

- **v1.0** (2025-01-11): 初始版本，支持自动检测和安装功能
- 集成到主要启动脚本中
- 添加完整的测试和文档

---

现在您可以在任何电脑上直接运行启动脚本，无需担心 `python-chinese-chess` 库的安装问题！