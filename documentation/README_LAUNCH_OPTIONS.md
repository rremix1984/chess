# 街头霸王启动选项指南 - macOS 优化版

## 概述

由于 macOS 上 JavaFX 与系统窗口管理存在已知冲突（NSTrackingRectTag 崩溃），我们提供了多种启动方式来确保游戏能稳定运行。

## 🚀 启动选项（按推荐程度排序）

### 选项 A: 纯 JavaFX 启动器 ⭐⭐⭐⭐⭐（最推荐）
```bash
./run_javafx_launcher.sh
```
- **描述**: 使用纯 JavaFX 图形界面启动器
- **优势**: 完全避免 Swing+JavaFX 混合，最稳定
- **工作原理**: 街头霸王在独立进程中运行，避免窗口管理冲突
- **适用场景**: 日常使用的首选方式

### 选项 B: 安全模式启动器 ⭐⭐⭐⭐
```bash
./run_street_fighter_safe.sh
```
- **描述**: 带有崩溃保护和超时检测的启动器
- **优势**: 包含 macOS 特定优化参数和监控
- **工作原理**: 30秒启动超时保护，优化的 JVM 参数
- **适用场景**: 当选项 A 不可用时的备选方案

### 选项 C: 直接启动 ⭐⭐⭐
```bash
./run_street_fighter_direct.sh
```
- **描述**: 跳过 Maven，直接使用 Java 命令
- **优势**: 避免 Maven 相关问题，启动速度更快
- **工作原理**: 直接构建 classpath 并调用 Java
- **适用场景**: Maven 出现问题时的替代方案

### 选项 D: 标准启动 ⭐⭐
```bash
./run_street_fighter.sh
```
- **描述**: 使用 Maven javafx:run 的标准方式
- **缺点**: 在 macOS 上可能遇到窗口管理崩溃
- **适用场景**: 仅用于测试或非 macOS 环境

## 🛠️ 系统工具

### 系统状态检查
```bash
./check_system_status.sh
```
检查 Java 环境、项目编译状态和兼容性

### 游戏菜单
```bash
./game_menu.sh
```
交互式菜单，包含所有启动选项

## 📋 故障排除

### 如果遇到崩溃问题：

1. **首先尝试选项 A**（纯 JavaFX 启动器）
2. **检查 Java 版本**：确保使用 Java 11
3. **重新编译项目**：
   ```bash
   cd StreetFighter
   mvn clean compile
   ```
4. **查看系统状态**：
   ```bash
   ./check_system_status.sh
   ```

### 常见错误码：
- **退出代码 134**: macOS JavaFX 窗口管理崩溃 (SIGABRT)
- **退出代码 124**: 启动超时（安全模式）
- **退出代码 1**: 编译或依赖问题

## 🧪 技术细节

### macOS 优化参数
我们使用了以下 JVM 参数来减少 macOS 兼容性问题：

```bash
# Apple 特定参数
-Dapple.awt.application.name=StreetFighter
-Dcom.apple.macos.useScreenMenuBar=true
-Djava.awt.Window.locationByPlatform=true

# JavaFX 优化
-Djavafx.embed.singleThread=true
-Dprism.verbose=false
-Dcom.sun.javafx.isEmbedded=false

# 内存和 GC 优化
-Xms256m -Xmx1024m
-XX:+UseG1GC
```

### 独立进程启动
纯 JavaFX 启动器使用以下方式启动游戏：
```java
ProcessBuilder pb = new ProcessBuilder(
    "bash", "-c", 
    "cd /path/to/StreetFighter && mvn javafx:run"
);
pb.inheritIO();
Process process = pb.start();
```

## 📝 更新日志

- **v1.0**: 基础 Maven javafx:run 启动
- **v1.1**: 添加 macOS 优化参数
- **v1.2**: 创建纯 JavaFX 启动器，避免 Swing 混合
- **v1.3**: 添加安全模式和直接启动选项
- **v1.4**: 完善错误处理和用户指导

## 🎮 使用建议

1. **日常使用**: 选择选项 A（纯 JavaFX 启动器）
2. **开发测试**: 选择选项 C（直接启动）用于快速测试
3. **问题诊断**: 使用系统状态检查工具
4. **批量操作**: 使用游戏菜单进行多次启动

---

**注意**: 这些优化专门针对 macOS 上的 JavaFX 窗口管理问题。在其他操作系统上，标准启动方式通常就足够了。
