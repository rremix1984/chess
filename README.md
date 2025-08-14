# 中国象棋游戏平台

## 概述

这是一个功能丰富的多游戏平台，支持中国象棋、国际象棋、围棋、五子棋和飞行棋等多种棋类游戏。

## 快速开始

### 1. 编译和运行

```bash
# 编译项目
mvn clean compile

# 运行游戏
./start-game.sh  # Linux/macOS
start-game.bat   # Windows
```

### 2. 系统要求

- Java 8 或更高版本
- Maven 3.6+

## AI 引擎设置

### Pikafish（中国象棋引擎）

本项目支持使用 Pikafish 引擎提供强大的中国象棋 AI。Pikafish 需要单独安装：

```bash
# macOS
brew install pikafish

# Linux
sudo apt-get install pikafish

# 下载神经网络文件
mkdir -p ~/.pikafish
curl -L -o ~/.pikafish/pikafish.nnue \
  "https://github.com/official-pikafish/Networks/releases/download/master-net/pikafish.nnue"
```

详细安装说明请参考 [PIKAFISH_INSTALL.md](PIKAFISH_INSTALL.md)

### 备用 AI

如果 Pikafish 不可用，系统会自动使用内置的增强 AI，游戏功能不受影响。

## 配置

主要配置文件位于：`game-common/src/main/resources/application.properties`

可以自定义：
- AI 引擎路径
- 思考时间
- 游戏参数
- 界面设置

## 项目结构

```
chinese-chess-game/
├── chinese-chess/          # 中国象棋游戏
├── international-chess/    # 国际象棋游戏
├── go-game/               # 围棋游戏
├── gomoku/                # 五子棋游戏
├── flight-chess/          # 飞行棋游戏
├── monopoly/              # 大富翁游戏
├── tank-battle-game/      # 坦克大战游戏
├── army-chess/            # 军棋游戏
├── game-common/           # 通用组件和配置
├── game-launcher/         # 游戏启动器
└── PIKAFISH_INSTALL.md    # Pikafish 安装指南
```

## 许可证

MIT License

# 多游戏平台

一个基于Java和JavaFX的多游戏平台，包含中国象棋、五子棋、围棋、国际象棋和飞行棋等多种棋类游戏。

## 快速开始

### 环境要求
- Java 11 或更高版本
- Maven 3.6 或更高版本

### 启动游戏
```bash
# 启动游戏中心（可选择所有游戏）
./start-game.sh

# 或直接启动大富翁游戏
./start-monopoly.sh
```

## 项目结构

```
chinese-chess-game/
├── game-common/          # 公共模块（工具类、音效等）
├── chinese-chess/        # 中国象棋模块
├── gomoku/              # 五子棋模块
├── go-game/             # 围棋模块
├── international-chess/ # 国际象棋模块
├── flight-chess/        # 飞行棋模块
├── monopoly/            # 大富翁模块
├── tank-battle-game/    # 坦克大战模块
├── army-chess/          # 军棋模块
├── game-launcher/       # 游戏启动器
├── documentation/       # 项目文档
├── scripts/            # 脚本文件
├── tools/              # 开发工具
├── logs/               # 日志文件
├── external/           # 外部资源
└── start-game.sh       # 主启动脚本
```

## 游戏特色

- **中国象棋**: 支持AI对战，多种难度级别
- **五子棋**: 智能AI，支持人机对战
- **围棋**: 经典围棋游戏
- **国际象棋**: 支持标准国际象棋规则
- **飞行棋**: 多人飞行棋游戏
- **大富翁**: 经典策略棋盘游戏，支持地产购买和4人对战
- **坦克大战**: 经典的坦克对战游戏
- **军棋**: 策略性军棋游戏

## 开发说明

本项目采用Maven多模块架构：
- 每个游戏都是独立的Maven模块
- `game-common`模块包含所有游戏的公共代码
- `game-launcher`提供统一的游戏选择界面

## 许可证

本项目采用开源许可证，详见各模块的许可证文件。