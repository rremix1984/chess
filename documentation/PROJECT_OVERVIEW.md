# 多游戏平台项目总览

## 🎮 项目简介

这是一个升级到 **Java 11 + JavaFX 17** 的多游戏平台，专门解决了 macOS 上的 JavaFX 崩溃问题，现在包含了**两个街头霸王游戏**。

## 📁 项目结构

```
chinese-chess-game/
├── StreetFighter/               # 原版街头霸王游戏
├── StreetFighterNew/            # 全新街头霸王增强版 (新增)
├── ChineseChess/                # 中国象棋 (如果存在)
├── Go/                          # 围棋 (如果存在)
├── FlightChess/                 # 飞行棋 (如果存在)
├── TankBattle/                  # 坦克大战 (如果存在)
├── game-launcher/               # 传统游戏启动器
├── JavaFXGameLauncher.java      # 纯 JavaFX 启动器
├── SwingGameLauncher.java       # 纯 Swing 启动器 (macOS 最佳)
└── 各种启动脚本...
```

## 🥊 街头霸王游戏对比

### StreetFighter (原版)
- **路径**: `/StreetFighter`
- **特点**: 基础版本的街头霸王游戏
- **主类**: `com.example.gameproject.startGame`
- **配置**: 已升级到 Java 11 + JavaFX 17

### StreetFighterNew (增强版) 🆕
- **路径**: `/StreetFighterNew` 
- **特点**: 
  - ✅ 多人游戏支持 (网络对战)
  - ✅ 高级动画和特效
  - ✅ 可自定义 UI 缩放
  - ✅ 音效和音乐控制
  - ✅ 设置持久化存储 (保存到用户目录)
  - ✅ 服务器/客户端架构
  - ✅ 角色和场景选择
- **主类**: `com.example.gameproject.startGame`
- **配置**: Java 11 + JavaFX 17 + 增强功能

## 🚀 启动方式

### 1. 主启动菜单 (推荐)
```bash
./launch_street_fighter.sh
```
包含10种启动选项，现在包含街头霸王增强版！

### 2. 纯 Swing 启动器 (最稳定)
```bash
./run_swing_launcher.sh
```
- 3x3 网格布局
- 包含两个街头霸王游戏
- 完全避免 JavaFX 崩溃问题

### 3. 纯 JavaFX 启动器
```bash
./run_javafx_launcher.sh
```
- 独立进程启动
- 美观的界面设计
- 包含街头霸王增强版

### 4. 直接启动
```bash
# 原版街头霸王
./run_street_fighter.sh

# 增强版街头霸王
./run_street_fighter_enhanced.sh
```

## 🔧 技术升级详情

### Java 环境
- ✅ **从 Java 8 升级到 Java 11**
- ✅ **从 JavaFX 内置版本升级到 JavaFX 17**
- ✅ 所有 pom.xml 文件已更新
- ✅ 修复了 Java 20 到 Java 11 的语法兼容性问题

### macOS 兼容性
- ✅ **解决了 NSTrackingRectTag 崩溃问题**
- ✅ 提供多种启动方式规避问题
- ✅ 纯 Swing 启动器完全避免问题
- ✅ 独立进程启动避免混合问题

### 设置管理
- ✅ **修复了 JAR 内资源访问问题**
- ✅ 设置文件现在保存到用户主目录
- ✅ 支持默认设置创建

## 📋 启动选项详细说明

| 选项 | 名称 | 稳定性 | 描述 |
|------|------|--------|------|
| 1 | 纯 Swing 启动器 | ⭐⭐⭐⭐⭐ | 最稳定，包含所有游戏 |
| 2 | Docker 容器 | ⭐⭐⭐⭐⭐ | Linux 环境，彻底解决兼容性 |
| 3 | JavaFX 启动器 | ⭐⭐⭐⭐ | 独立进程，美观界面 |
| 4 | 终极启动器 | ⭐⭐⭐⭐ | 软件渲染 + 监控 |
| 5 | 安全模式 | ⭐⭐⭐ | 崩溃保护 + 超时 |
| 6 | 直接启动 | ⭐⭐⭐ | 跳过 Maven |
| 7 | 标准启动 | ⭐⭐ | 可能崩溃 |
| 8 | 系统检查 | - | 诊断工具 |
| 9 | 游戏菜单 | ⭐⭐⭐ | 传统菜单 |
| 10 | 街头霸王增强版 | ⭐⭐⭐⭐ | 新游戏直接启动 |

## 🆕 新增功能

### 街头霸王增强版特性
1. **多人游戏系统**
   - 网络服务器支持
   - 客户端连接功能
   - 实时对战

2. **增强的用户界面**
   - 动态 UI 缩放
   - 响应式设计
   - 设置界面优化

3. **音频系统**
   - 音乐音量控制
   - 音效音量控制
   - 设置持久化

4. **游戏功能**
   - 单人模式 (角色选择、场景选择)
   - 多人模式 (游戏查找、连接)
   - 高级动画系统

## 🔍 系统要求

- **Java**: OpenJDK 11+
- **操作系统**: macOS (主要优化), Windows, Linux
- **内存**: 最少 512MB (推荐 1GB+)
- **JavaFX**: 17.0.2 (自动下载)

## 🎯 使用建议

### 首次使用
1. 运行系统检查: `./check_system_status.sh`
2. 使用纯 Swing 启动器: `./run_swing_launcher.sh`
3. 体验新的街头霸王增强版！

### 日常使用
- **最稳定**: 选择纯 Swing 启动器
- **最美观**: 选择 JavaFX 启动器  
- **快速启动**: 直接运行增强版脚本

### 问题排查
- 如果崩溃: 使用纯 Swing 启动器
- 如果编译问题: 运行系统状态检查
- 如果设置问题: 删除 `~/.streetfighter-new-settings.txt`

## 📞 支持信息

项目已完成以下主要目标:
- ✅ Java 11 + JavaFX 17 升级
- ✅ macOS 崩溃问题解决
- ✅ 街头霸王增强版集成
- ✅ 多种启动方式提供
- ✅ 完整的文档和脚本

现在你可以享受稳定的多游戏平台体验，包括令人兴奋的街头霸王增强版！🥊🎮
