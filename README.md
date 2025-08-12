# 多游戏平台

一个基于Java和JavaFX的多游戏平台，包含中国象棋、五子棋、围棋、国际象棋和飞行棋等多种棋类游戏。

## 快速开始

### 环境要求
- Java 11 或更高版本
- Maven 3.6 或更高版本

### 启动游戏
```bash
./start-game.sh
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

## 开发说明

本项目采用Maven多模块架构：
- 每个游戏都是独立的Maven模块
- `game-common`模块包含所有游戏的公共代码
- `game-launcher`提供统一的游戏选择界面

## 许可证

本项目采用开源许可证，详见各模块的许可证文件。