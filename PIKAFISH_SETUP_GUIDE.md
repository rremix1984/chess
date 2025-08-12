# Pikafish 引擎设置指南

## 当前状态

🎮 **游戏可以正常运行！** 虽然 Pikafish 引擎由于缺少神经网络文件而无法使用，但系统会自动降级使用**增强 AI**，您仍然可以享受高质量的象棋对弈。

## 关于 Pikafish

Pikafish 是一个强大的中国象棋引擎，需要神经网络文件才能正常工作。

## 快速开始（不需要 Pikafish）

您可以立即开始游戏，系统将使用：
- **增强 AI**: 具有开局库和高级算法的智能 AI
- **DeepSeek LLM**: 提供自然语言象棋分析（如果可用）

## 如何安装完整的 Pikafish 支持

### 步骤 1: 下载神经网络文件

```bash
# 从官方源下载（约 30-50MB）
curl -L -o pikafish.nnue "https://github.com/official-pikafish/Networks/releases/download/master-net/pikafish.nnue"
```

或手动下载：
1. 访问 https://github.com/official-pikafish/Networks/releases
2. 下载最新的 `pikafish.nnue` 文件
3. 将文件放置在项目根目录：`/Users/wangxiaozhe/workspace/chinese-chess-game/pikafish.nnue`

### 步骤 2: 验证文件

```bash
# 检查文件是否存在且不为空
ls -la pikafish.nnue
```

文件大小应该约为 30-50MB。

### 步骤 3: 重新启动游戏

```bash
sh start-game.sh
```

## 故障排除

### 1. 下载失败
如果网络下载失败，可以：
- 尝试使用 VPN
- 从其他镜像源下载
- 继续使用增强 AI（游戏完全可用）

### 2. 文件损坏
如果引擎仍然报错：
```bash
# 删除损坏的文件
rm pikafish.nnue
# 重新下载
curl -L -o pikafish.nnue "https://github.com/official-pikafish/Networks/releases/download/master-net/pikafish.nnue"
```

### 3. 权限问题
确保引擎文件有执行权限：
```bash
chmod +x pikafish_engine/MacOS/pikafish-apple-silicon
```

## 性能对比

| AI 类型 | 强度 | 响应速度 | 功能 |
|---------|------|----------|------|
| **增强 AI** | ⭐⭐⭐⭐ | 快速 | 开局库 + 高级算法 |
| **Pikafish** | ⭐⭐⭐⭐⭐ | 中等 | 神经网络 + 深度搜索 |
| **DeepSeek+Pikafish** | ⭐⭐⭐⭐⭐ | 较慢 | 完整 AI 分析 |

## 推荐设置

对于不同用户：

- **休闲玩家**: 使用增强 AI（当前可用）
- **认真棋手**: 安装完整 Pikafish 支持
- **AI 研究**: 配置 DeepSeek + Pikafish 组合

## 支持的功能（当前可用）

✅ 完整的象棋游戏  
✅ 增强 AI 对弈  
✅ 开局库支持  
✅ 音效和动画  
✅ 游戏记录  
✅ 多难度级别  

⏳ Pikafish 引擎（需要神经网络文件）  
⏳ 深度 AI 分析  

## 联系支持

如果遇到问题，请查看日志输出或联系开发团队。

---

**重要提醒**: 即使没有 Pikafish，游戏也完全可用且有趣！增强 AI 提供了很好的对弈体验。
