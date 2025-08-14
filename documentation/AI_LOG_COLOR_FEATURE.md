# 象棋AI决策日志颜色区分功能

## 🎯 功能概述

为中国象棋游戏的AI分析面板添加了颜色区分功能，让不同方（红方、黑方）的决策日志用不同颜色来体现：

- **🔴 红方AI决策** - 红色字体
- **⚫ 黑方AI决策** - 黑色字体  
- **🟢 一般信息** - 绿色字体

## ✅ 已实现功能

### 1. 颜色区分显示
- 红方AI决策显示为红色字体
- 黑方AI决策显示为黑色字体
- 系统信息和其他内容显示为绿色字体

### 2. 智能颜色检测
- 自动检测消息中的"红方"、"黑方"等关键词
- 根据关键词自动应用对应的颜色样式
- 无需手动指定颜色，提升使用便利性

### 3. 多种使用方式
提供多个方法来满足不同使用场景：

```java
// 方式1：明确指定红方AI决策
aiLogPanel.addRedPlayerDecision("我选择炮二平五，控制中路");

// 方式2：明确指定黑方AI决策  
aiLogPanel.addBlackPlayerDecision("我选择炮2平5，与红方炮对峙");

// 方式3：智能颜色检测（推荐）
aiLogPanel.addAIDecisionWithColorDetection("红方AI正在分析当前局面...");
aiLogPanel.addAIDecisionWithColorDetection("黑方AI考虑反击策略");

// 方式4：一般信息
aiLogPanel.addGeneralInfo("游戏开始，双方布阵完毕");
```

## 🚀 快速体验

### 运行演示程序
```bash
./run_ai_log_color_demo.sh
```

演示程序将展示：
- 红方AI决策的红色显示效果
- 黑方AI决策的黑色显示效果
- 智能颜色检测功能
- 一般信息的绿色显示效果

### 界面操作
- **红方AI思考** - 查看红色AI决策日志
- **黑方AI思考** - 查看黑色AI决策日志
- **智能颜色检测** - 测试自动颜色识别
- **一般信息** - 查看绿色系统信息
- **清空日志** - 清除所有日志记录

## 💡 集成到游戏

### 在现有代码中使用

1. **获取AI日志面板实例**：
```java
AILogPanel aiLogPanel = gameFrame.getAILogPanel(); // 假设已有获取方法
```

2. **在AI决策代码中添加日志**：
```java
// 在红方AI决策时
if (currentPlayer == PieceColor.RED) {
    aiLogPanel.addRedPlayerDecision("分析局面：" + analysisResult);
    aiLogPanel.addRedPlayerDecision("最终选择：" + finalMove);
}

// 在黑方AI决策时
if (currentPlayer == PieceColor.BLACK) {
    aiLogPanel.addBlackPlayerDecision("分析局面：" + analysisResult);
    aiLogPanel.addBlackPlayerDecision("最终选择：" + finalMove);
}

// 或者使用智能检测（推荐）
String playerName = (currentPlayer == PieceColor.RED) ? "红方" : "黑方";
aiLogPanel.addAIDecisionWithColorDetection(playerName + "AI选择了：" + finalMove);
```

3. **添加游戏状态信息**：
```java
// 游戏开始
aiLogPanel.addGeneralInfo("新游戏开始，双方准备就绪");

// 游戏状态变化
aiLogPanel.addGeneralInfo("当前回合：第" + roundNumber + "手");
aiLogPanel.addGeneralInfo("游戏时间：" + gameTime);

// 游戏结束
aiLogPanel.addGeneralInfo("游戏结束，" + winner + "获胜！");
```

## 🔧 技术特点

### 1. 颜色定义
```java
Color.RED              // 红方AI - 红色
Color.BLACK            // 黑方AI - 黑色  
new Color(0, 153, 0)   // 一般信息 - 绿色
```

### 2. 智能检测逻辑
```java
if (message.contains("红方") || message.contains("红")) {
    // 使用红色
} else if (message.contains("黑方") || message.contains("黑")) {
    // 使用黑色
} else {
    // 使用绿色
}
```

### 3. 线程安全
- 使用 `SwingUtilities.invokeLater()` 确保UI更新的线程安全
- 支持多线程环境下的日志记录

## 📋 API 参考

### AILogPanel 新增方法

```java
// 智能颜色检测
public void addAIDecisionWithColorDetection(String message)

// 明确指定红方AI决策
public void addRedPlayerDecision(String message)

// 明确指定黑方AI决策  
public void addBlackPlayerDecision(String message)

// 添加一般信息
public void addGeneralInfo(String message)
```

### 使用示例

```java
AILogPanel logPanel = new AILogPanel();
logPanel.setEnabled(true);

// 各种使用方式
logPanel.addRedPlayerDecision("炮二平五，控制中路要点");
logPanel.addBlackPlayerDecision("马8进7，巩固右翼防守");
logPanel.addAIDecisionWithColorDetection("红方AI深度分析中...");
logPanel.addGeneralInfo("双方进入中局阶段");
```

## 🎮 用户体验

### 视觉效果
- **清晰区分**：不同颜色让用户一眼就能区分红方和黑方的决策
- **提升可读性**：颜色编码减少了阅读负担
- **专业感**：符合象棋传统的红黑配色方案

### 实际效果预览
```
[14:23:15] 红方AI: 开局选择中炮对屏风马        (红色)
[14:23:18] 黑方AI: 应对中炮，采用屏风马防守    (黑色)
[14:23:20] 系统信息: 双方进入经典开局模式      (绿色)
[14:23:25] 红方AI: 马二进三，发展子力          (红色)
[14:23:28] 黑方AI: 马8进7，巩固防守           (黑色)
```

## 📁 相关文件

- `AILogPanel.java` - 主要功能实现
- `AILogColorDemo.java` - 功能演示程序
- `run_ai_log_color_demo.sh` - 演示程序启动脚本

## 🔄 版本信息

- **版本**: v1.0
- **兼容性**: 与现有AI决策日志面板完全兼容
- **性能**: 无额外性能开销，保持原有效率

## 🎯 下一步计划

1. **更多颜色选项** - 考虑支持用户自定义颜色
2. **主题支持** - 适配深色/浅色主题
3. **导出功能增强** - 在导出时保留颜色信息
4. **配置选项** - 允许用户启用/禁用颜色区分功能

---

现在您可以在象棋AI分析中使用颜色区分功能，让红方、黑方的决策日志更加直观和易读！🎮
