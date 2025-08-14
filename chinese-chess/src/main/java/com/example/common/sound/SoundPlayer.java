package com.example.common.sound;

import com.example.common.config.GameConfig;

/**
 * 音效播放工具类
 * 提供游戏音效播放功能
 */
public class SoundPlayer {
    
    private static SoundPlayer instance;
    private boolean enabled = GameConfig.ENABLE_SOUND_EFFECTS;
    private float volume = GameConfig.SOUND_VOLUME;
    
    private SoundPlayer() {
        // 私有构造函数，实现单例模式
    }
    
    /**
     * 获取音效播放器实例
     * @return SoundPlayer实例
     */
    public static synchronized SoundPlayer getInstance() {
        if (instance == null) {
            instance = new SoundPlayer();
        }
        return instance;
    }
    
    /**
     * 播放音效
     * @param soundName 音效名称
     */
    public void playSound(String soundName) {
        if (!enabled) {
            return;
        }
        
        try {
            // 这里应该实现实际的音效播放逻辑
            // 目前只是输出调试信息
            System.out.println("🔊 [音效] 播放音效: " + soundName + " (音量: " + volume + ")");
            
            // 根据音效名称播放不同的音效
            switch (soundName.toLowerCase()) {
                case "piece_drop":
                    playPieceDropSound();
                    break;
                case "piece_capture":
                    playPieceCaptureSound();
                    break;
                case "game_win":
                    playGameWinSound();
                    break;
                case "game_start":
                    playGameStartSound();
                    break;
                case "check":
                    playCheckSound();
                    break;
                case "checkmate":
                    playCheckmateSound();
                    break;
                case "move_illegal":
                    playIllegalMoveSound();
                    break;
                default:
                    playDefaultSound();
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("❌ [音效] 播放音效失败: " + soundName + " - " + e.getMessage());
        }
    }
    
    /**
     * 播放落子音效
     */
    private void playPieceDropSound() {
        // 模拟落子音效
        System.out.println("♟️ 棋子落下");
    }
    
    /**
     * 播放吃子音效
     */
    private void playPieceCaptureSound() {
        // 模拟吃子音效
        System.out.println("⚔️ 吃子");
    }
    
    /**
     * 播放游戏胜利音效
     */
    private void playGameWinSound() {
        // 模拟胜利音效
        System.out.println("🎉 游戏胜利");
    }
    
    /**
     * 播放游戏开始音效
     */
    private void playGameStartSound() {
        // 模拟游戏开始音效
        System.out.println("🎮 游戏开始");
    }
    
    /**
     * 播放将军音效
     */
    private void playCheckSound() {
        // 模拟将军音效
        System.out.println("⚡ 将军");
    }
    
    /**
     * 播放将死音效
     */
    private void playCheckmateSound() {
        // 模拟将死音效
        System.out.println("💀 将死");
    }
    
    /**
     * 播放非法移动音效
     */
    private void playIllegalMoveSound() {
        // 模拟非法移动音效
        System.out.println("🚫 非法移动");
    }
    
    /**
     * 播放默认音效
     */
    private void playDefaultSound() {
        // 模拟默认音效
        System.out.println("🔔 默认音效");
    }
    
    /**
     * 启用或禁用音效
     * @param enabled 是否启用音效
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        System.out.println("🔊 [音效] 音效播放" + (enabled ? "已启用" : "已禁用"));
    }
    
    /**
     * 检查音效是否启用
     * @return 是否启用音效
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 设置音效音量
     * @param volume 音量 (0.0 - 1.0)
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        System.out.println("🔊 [音效] 音量设置为: " + this.volume);
    }
    
    /**
     * 获取当前音量
     * @return 当前音量
     */
    public float getVolume() {
        return volume;
    }
    
    /**
     * 停止所有音效播放
     */
    public void stopAllSounds() {
        System.out.println("🔇 [音效] 停止所有音效播放");
        // 这里应该实现停止所有音效播放的逻辑
    }
    
    /**
     * 预加载音效资源
     */
    public void preloadSounds() {
        System.out.println("📦 [音效] 预加载音效资源...");
        // 这里应该实现音效资源预加载逻辑
        System.out.println("✅ [音效] 音效资源预加载完成");
    }
    
    /**
     * 释放音效资源
     */
    public void dispose() {
        System.out.println("🧹 [音效] 释放音效资源");
        // 这里应该实现音效资源释放逻辑
    }
}
