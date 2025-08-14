package com.example.common.sound;

/**
 * 音效播放器（简化版）
 */
public class SoundPlayer {
    private static SoundPlayer instance = new SoundPlayer();
    
    public static SoundPlayer getInstance() {
        return instance;
    }
    
    public void playSound(String soundName) {
        // 简化实现，不实际播放音效
        System.out.println("🔊 播放音效: " + soundName);
    }
}
