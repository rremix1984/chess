# Pikafish 引擎安装指南

## 概述

本项目使用系统安装的 Pikafish 引擎来提供强大的中国象棋分析能力。请按照以下步骤安装 Pikafish。

## 安装方法

### macOS

推荐使用 Homebrew 安装：

```bash
# 使用 Homebrew 安装
brew install pikafish

# 验证安装
pikafish --version
```

或者手动编译安装：

```bash
# 克隆源代码
git clone https://github.com/official-pikafish/Pikafish.git
cd Pikafish

# 编译
make -j profile-build ARCH=apple-silicon

# 安装到系统路径
sudo cp pikafish /usr/local/bin/

# 创建配置目录
mkdir -p ~/.pikafish

# 下载神经网络文件
curl -L -o ~/.pikafish/pikafish.nnue \
  "https://github.com/official-pikafish/Networks/releases/download/master-net/pikafish.nnue"
```

### Linux

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install pikafish

# 或者从源码编译
git clone https://github.com/official-pikafish/Pikafish.git
cd Pikafish
make -j profile-build ARCH=x86_64-avx2
sudo cp pikafish /usr/local/bin/

# 创建配置目录并下载神经网络文件
mkdir -p ~/.pikafish
curl -L -o ~/.pikafish/pikafish.nnue \
  "https://github.com/official-pikafish/Networks/releases/download/master-net/pikafish.nnue"
```

### Windows

```cmd
# 使用 winget 安装
winget install pikafish

# 或者下载预编译版本
# 从 https://github.com/official-pikafish/Pikafish/releases 下载
# 解压并将 pikafish.exe 添加到 PATH

# 创建配置目录
mkdir %USERPROFILE%\.pikafish

# 下载神经网络文件到该目录
```

## 神经网络文件

Pikafish 需要神经网络文件（NNUE）才能发挥最佳性能：

```bash
# 检查文件是否存在
ls -la ~/.pikafish/pikafish.nnue

# 如果不存在，手动下载
curl -L -o ~/.pikafish/pikafish.nnue \
  "https://github.com/official-pikafish/Networks/releases/download/master-net/pikafish.nnue"
```

文件大小约为 30-50MB，下载成功后应该看到正确的文件大小。

## 验证安装

```bash
# 检查 Pikafish 是否在 PATH 中
which pikafish

# 测试引擎
echo "uci" | pikafish

# 应该看到类似输出：
# id name Pikafish YYYY-MM-DD
# id author the Pikafish developers
# ...
# uciok
```

## 配置文件

项目会自动使用以下配置：

```properties
# 系统安装的 Pikafish
pikafish.engine.path=pikafish

# 神经网络文件路径
pikafish.engine.neural.network.path=${user.home}/.pikafish/pikafish.nnue

# 引擎参数
pikafish.engine.threads=2
pikafish.engine.hash.size=64
```

## 故障排除

### 1. 引擎未找到

```bash
# 错误：command not found: pikafish
# 解决：确保 pikafish 在 PATH 中

# 检查安装位置
which pikafish
# 或
whereis pikafish

# 如果不在 PATH 中，添加到 shell 配置文件
echo 'export PATH="/usr/local/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### 2. 神经网络文件缺失

```bash
# 错误：NNUE file not found
# 解决：下载神经网络文件

mkdir -p ~/.pikafish
curl -L -o ~/.pikafish/pikafish.nnue \
  "https://github.com/official-pikafish/Networks/releases/download/master-net/pikafish.nnue"

# 验证文件大小（应该是 30-50MB）
ls -lh ~/.pikafish/pikafish.nnue
```

### 3. 权限问题

```bash
# 确保引擎文件有执行权限
chmod +x /usr/local/bin/pikafish
```

## 备用方案

如果 Pikafish 不可用，项目会自动降级使用内置的增强 AI：

- ✅ **增强 AI**: 基于开局库和高级算法的智能 AI
- ✅ **完整游戏功能**: 不影响游戏的正常进行
- ⏳ **Pikafish 引擎**: 需要正确安装后才能使用

## 性能建议

- **线程数**: 建议设置为 CPU 核心数的一半
- **内存**: 哈希表大小建议 64-256MB
- **神经网络**: 使用最新的官方 NNUE 文件

## 相关链接

- [Pikafish 官方仓库](https://github.com/official-pikafish/Pikafish)
- [神经网络文件下载](https://github.com/official-pikafish/Networks/releases)
- [编译说明](https://github.com/official-pikafish/Pikafish#compiling-pikafish-yourself)

## 联系支持

如果在安装过程中遇到问题，请检查：

1. 操作系统兼容性
2. 网络连接（下载神经网络文件）
3. 系统权限
4. PATH 环境变量

安装完成后，重新启动游戏即可使用 Pikafish 引擎！
