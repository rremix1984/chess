#!/bin/bash

echo "=== 街头霸王 Docker 启动器 ==="
echo "在 Linux 容器中运行，完全避免 macOS JavaFX 问题"
echo ""

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装"
    echo ""
    echo "请先安装 Docker Desktop for Mac:"
    echo "1. 访问 https://www.docker.com/products/docker-desktop"
    echo "2. 下载并安装 Docker Desktop"
    echo "3. 启动 Docker Desktop"
    echo "4. 重新运行此脚本"
    exit 1
fi

# 检查 Docker 是否运行
if ! docker info &> /dev/null; then
    echo "❌ Docker 未运行"
    echo ""
    echo "请先启动 Docker Desktop，然后重新运行此脚本"
    exit 1
fi

echo "✅ Docker 环境检查通过"
echo ""

# 构建 Docker 镜像
echo "正在构建 Street Fighter Docker 镜像..."
docker build -t streetfighter-macos-fix .

if [ $? -ne 0 ]; then
    echo "❌ Docker 镜像构建失败"
    exit 1
fi

echo "✅ Docker 镜像构建成功"
echo ""

echo "启动 Street Fighter 容器..."
echo ""
echo "🎮 游戏将在 Linux 容器中运行"
echo "📺 可以通过 VNC 查看游戏界面:"
echo "   - VNC 地址: localhost:5900"
echo "   - 推荐 VNC 客户端: RealVNC Viewer 或 Screen Sharing (macOS 自带)"
echo ""
echo "启动容器中..."

# 运行容器
docker run -it --rm \
  -p 5900:5900 \
  --name streetfighter \
  streetfighter-macos-fix

echo ""
echo "游戏容器已退出"
