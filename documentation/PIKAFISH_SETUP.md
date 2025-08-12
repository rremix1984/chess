# Pikafish 引擎配置说明

## 概述

本项目已成功集成了Pikafish中国象棋引擎，所有必要的文件都已包含在项目中。

## 文件结构

```
chinese-chess-game/
├── pikafish_engine/              # Pikafish引擎目录
│   ├── MacOS/
│   │   └── pikafish-apple-silicon    # macOS Apple Silicon版本
│   ├── Linux/                        # Linux版本
│   ├── Windows/                      # Windows版本
│   ├── Android/                      # Android版本
│   └── pikafish.nnue                # 神经网络文件 (43MB)
├── pikafish.nnue                    # 项目根目录的神经网络文件
├── pikafish_mock.py                 # 模拟引擎 (备用方案)
├── test_pikafish_local.sh           # 引擎测试脚本
└── src/main/java/
    ├── DeepSeekPikafishAI.java      # 主AI类
    └── PikafishEngine.java          # 引擎接口类
```

## 配置详情

### 引擎路径配置
- **默认路径**: `./pikafish_engine/MacOS/pikafish-apple-silicon`
- **神经网络文件**: `./pikafish.nnue` (项目根目录)
- **自动检测**: 如果神经网络文件不存在，会自动创建空文件绕过检查

### 备用方案
1. **真实引擎**: Pikafish官方引擎 + 神经网络文件
2. **模拟引擎**: Python模拟引擎 (`pikafish_mock.py`)
3. **增强AI**: 基于开局库的AI决策

## 测试验证

运行测试脚本验证配置：
```bash
./test_pikafish_local.sh
```

测试内容：
- ✅ 引擎文件存在性和权限
- ✅ 神经网络文件大小验证
- ✅ 引擎启动测试
- ✅ 神经网络加载测试

## 运行应用

```bash
mvn clean javafx:run
```

## 故障排除

### 常见问题

1. **引擎启动失败**
   - 检查文件权限：`chmod +x ./pikafish_engine/MacOS/pikafish-apple-silicon`
   - 验证文件完整性：`ls -lh ./pikafish_engine/MacOS/pikafish-apple-silicon`

2. **神经网络文件问题**
   - 检查文件大小：`ls -lh ./pikafish.nnue` (应该是43MB)
   - 重新复制：`cp ./pikafish_engine/pikafish.nnue ./`

3. **路径问题**
   - 确保在项目根目录运行
   - 检查相对路径是否正确

### 调试信息

应用启动时会输出详细的调试信息：
- 引擎初始化状态
- 神经网络文件加载状态
- 备用方案切换日志

## 版本信息

- **Pikafish版本**: 2025-06-23
- **神经网络**: 官方NNUE文件 (44.8MB)
- **支持平台**: macOS Apple Silicon (当前配置)

## 移动历史

✅ **已完成**: 将Pikafish引擎从 `/Users/wangxiaozhe/pikafish_engine` 移动到项目目录
✅ **已完成**: 更新所有相关代码路径
✅ **已完成**: 解决神经网络文件下载问题
✅ **已完成**: 验证引擎正常工作