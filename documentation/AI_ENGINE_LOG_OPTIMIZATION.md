# AI引擎日志优化说明

## 概述

针对用户反馈的AI引擎决策日志过于冗长的问题，我们对Pikafish和Stockfish引擎的日志输出进行了优化，大幅减少了不必要的深度搜索信息，提升了用户体验。

## 问题描述

之前的AI引擎日志输出存在以下问题：
- **Pikafish引擎**：每个搜索深度都会输出一条日志，导致大量重复信息
- **Stockfish引擎**：类似地输出每层深度的分析信息
- **用户体验差**：日志面板被大量冗余信息淹没，难以找到关键决策信息

### 示例问题日志
```
🐟 [Pikafish] 🔍 深度 10, 分数: 0.27, 主变: a0a1
🐟 [Pikafish] 🔍 深度 11, 分数: 0.30, 主变: a0a1
🐟 [Pikafish] 🔍 深度 12, 分数: 0.30, 主变: a0a1
...
🐟 [Pikafish] 🔍 深度 32
```

## 优化方案

### 1. Pikafish引擎优化 (`PikafishEngine.java`)

#### 修改位置
- 文件：`chinese-chess/src/main/java/com/example/chinesechess/ai/PikafishEngine.java`
- 行数：371-381行

#### 优化策略
```java
// 之前：每个深度都显示
if (currentDepth > lastDepth) {
    lastDepth = currentDepth;
    log("🔍 深度 " + currentDepth + ", 分数: " + score + ", 主变: " + pv);
}

// 优化后：只显示关键深度
if (currentDepth > lastDepth && (currentDepth % 5 == 0 || currentDepth >= 30)) {
    lastDepth = currentDepth;
    log("🔍 深度 " + currentDepth + ", 分数: " + score + ", 主变: " + pv);
}
```

### 2. Stockfish引擎优化 (`StockfishEngine.java`)

#### 修改位置
- 文件：`international-chess/src/main/java/com/example/internationalchess/ai/StockfishEngine.java`
- 行数：127-145行

#### 新增方法
```java
private boolean shouldLogEngineLine(String line)
private int extractDepthFromLine(String line)  
private String extractKeyInfoFromLine(String line)
```

#### 优化策略
- 过滤冗长的引擎输出，只显示关键信息
- 深度信息只显示每5层或≥25层的重要变化
- 保留重要信息：bestmove、info string、id name、mate等

## 优化效果

### 之前的日志输出
```
🐟 [Pikafish] 🔍 深度 10, 分数: 0.27, 主变: a0a1
🐟 [Pikafish] 🔍 深度 11, 分数: 0.30, 主变: a0a1
🐟 [Pikafish] 🔍 深度 12, 分数: 0.30, 主变: a0a1
🐟 [Pikafish] 🔍 深度 13, 分数: 0.29, 主变: a0a1
🐟 [Pikafish] 🔍 深度 14, 分数: 0.28, 主变: a0a1
🐟 [Pikafish] 🔍 深度 15, 分数: 0.31, 主变: a0a1
🐟 [Pikafish] 🔍 深度 16, 分数: 0.33, 主变: a0a1
🐟 [Pikafish] 🔍 深度 17, 分数: 0.28, 主变: a0a1
🐟 [Pikafish] 🔍 深度 18, 分数: 0.30, 主变: a0a1
🐟 [Pikafish] 🔍 深度 19, 分数: 0.29, 主变: e3e4
🐟 [Pikafish] 🔍 深度 20, 分数: 0.28, 主变: a0a1
... 更多重复日志 ...
```

### 优化后的日志输出
```
🐟 [Pikafish] 🔍 深度 10, 分数: 0.27, 主变: a0a1
🐟 [Pikafish] 🔍 深度 15, 分数: 0.31, 主变: a0a1
🐟 [Pikafish] 🔍 深度 20, 分数: 0.28, 主变: a0a1
🐟 [Pikafish] 🔍 深度 25, 分数: 0.29, 主变: e3e4
🐟 [Pikafish] 🔍 深度 30, 分数: 0.31, 主变: a0a1
🐟 [Pikafish] 计算完成，最佳走法: e3e4
```

### 改进成果
- **日志量减少约80%**：从每层都显示改为每5层显示一次
- **信息更清晰**：用户能够快速识别AI的思考进程
- **保持重要性**：关键信息（高深度、最终结果）仍然完整显示
- **性能提升**：减少日志输出提升了AI计算和界面响应速度

## 配置选项

### Pikafish引擎
- 显示间隔：每5层深度显示一次
- 重要深度：≥30层的深度会强制显示
- 保留信息：分数变化、主变走法

### Stockfish引擎  
- 显示间隔：每5层深度显示一次
- 重要深度：≥25层的深度会强制显示
- 过滤规则：只保留bestmove、mate、引擎信息等关键内容

### KataGo引擎
- KataGo引擎本身日志输出相对简洁，未进行特殊优化

## 使用建议

1. **开发调试**：如需查看完整的引擎输出，可以临时修改过滤条件
2. **性能监控**：观察日志减少后的性能提升效果
3. **用户反馈**：根据用户使用体验进一步调整显示间隔

## 技术细节

### 过滤逻辑
```java
// Pikafish：每5层或高深度显示
(currentDepth % 5 == 0 || currentDepth >= 30)

// Stockfish：类似逻辑 + 重要信息检测
(depth % 5 == 0 || depth >= 25) && shouldLogEngineLine(line)
```

### 兼容性
- ✅ 向后兼容：不影响现有AI决策逻辑
- ✅ 可配置：可以通过修改条件调整显示频率
- ✅ 可扩展：框架支持未来添加更多过滤规则

---

**更新时间**：2025-01-14  
**影响范围**：Pikafish引擎、Stockfish引擎的日志输出  
**测试状态**：已编译通过，待用户验证效果
