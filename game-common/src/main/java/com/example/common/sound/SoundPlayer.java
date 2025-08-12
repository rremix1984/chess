package com.example.common.sound;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 音效播放器类
 * 负责加载和播放游戏音效
 */
public class SoundPlayer {
    
    // 单例模式
    private static SoundPlayer instance;
    
    // 音效缓存
    private Map<String, Clip> soundCache = new HashMap<>();
    
    // 是否启用音效
    private boolean soundEnabled = true;
    
    // 私有构造函数
    private SoundPlayer() {
        // 预加载所有音效
        preloadSounds();
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized SoundPlayer getInstance() {
        if (instance == null) {
            instance = new SoundPlayer();
        }
        return instance;
    }
    
    /**
     * 预加载所有音效
     */
    private void preloadSounds() {
        try {
            // 尝试加载外部音效文件
            loadSound("piece_drop", "/sounds/piece_drop.wav");
            loadSound("piece_capture", "/sounds/piece_capture.wav");
            loadSound("game_start", "/sounds/game_start.wav");
            loadSound("game_win", "/sounds/game_win.wav");
            loadSound("game_lose", "/sounds/game_lose.wav");
            
            // 如果外部音效文件加载失败，使用生成的音效
            if (!soundCache.containsKey("piece_drop")) {
                loadGeneratedSound("piece_drop", SimpleSound.generatePieceDropSound());
            }
            
            if (!soundCache.containsKey("game_win")) {
                loadGeneratedSound("game_win", SimpleSound.generateWinSound());
            }
        } catch (Exception e) {
            System.err.println("预加载音效失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 加载音效
     * @param soundName 音效名称
     * @param resourcePath 资源路径
     */
    private void loadSound(String soundName, String resourcePath) {
        try {
            // 尝试多种路径加载资源
            InputStream inputStream = null;
            String[] pathsToTry = {
                resourcePath,                                                // 原始路径 /sounds/xxx.wav
                resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath, // 不带前导斜杠 sounds/xxx.wav
                "sounds/" + (resourcePath.contains("/") ? resourcePath.substring(resourcePath.lastIndexOf('/') + 1) : resourcePath), // 直接文件名 sounds/xxx.wav
                "/sounds/" + (resourcePath.contains("/") ? resourcePath.substring(resourcePath.lastIndexOf('/') + 1) : resourcePath) // 带前导斜杠 /sounds/xxx.wav
            };
            
            // 尝试使用不同的类加载器和路径格式
            for (String path : pathsToTry) {
                // 使用当前类的getResourceAsStream
                inputStream = getClass().getResourceAsStream(path);
                if (inputStream != null) {
                    System.out.println("成功通过getClass().getResourceAsStream加载音效: " + path);
                    break;
                }
                
                // 使用类加载器
                inputStream = getClass().getClassLoader().getResourceAsStream(path);
                if (inputStream != null) {
                    System.out.println("成功通过getClassLoader().getResourceAsStream加载音效: " + path);
                    break;
                }
                
                // 使用系统类加载器
                inputStream = ClassLoader.getSystemResourceAsStream(path);
                if (inputStream != null) {
                    System.out.println("成功通过ClassLoader.getSystemResourceAsStream加载音效: " + path);
                    break;
                }
            }
            
            // 如果仍然找不到，尝试从文件系统直接加载
            if (inputStream == null) {
                try {
                    java.io.File file = new java.io.File("src/main/resources" + resourcePath);
                    if (file.exists()) {
                        inputStream = new java.io.FileInputStream(file);
                        System.out.println("成功从文件系统加载音效: " + file.getAbsolutePath());
                    } else {
                        file = new java.io.File("target/classes" + resourcePath);
                        if (file.exists()) {
                            inputStream = new java.io.FileInputStream(file);
                            System.out.println("成功从target/classes加载音效: " + file.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("从文件系统加载音效失败: " + e.getMessage());
                }
            }
            
            // 如果仍然找不到，报错并返回
            if (inputStream == null) {
                System.err.println("未找到音效: " + soundName);
                return;
            }
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            soundCache.put(soundName, clip);
            System.out.println("成功加载音效: " + soundName);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("加载音效失败: " + resourcePath + "，错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 加载生成的音效
     * @param soundName 音效名称
     * @param audioInputStream 音频输入流
     */
    private void loadGeneratedSound(String soundName, AudioInputStream audioInputStream) {
        try {
            if (audioInputStream == null) {
                System.err.println("生成音效失败: " + soundName);
                return;
            }
            
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            soundCache.put(soundName, clip);
            System.out.println("成功加载生成的音效: " + soundName);
        } catch (LineUnavailableException | IOException e) {
            System.err.println("加载生成的音效失败: " + soundName + "，错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 播放音效
     * @param soundName 音效名称
     */
    public void playSound(String soundName) {
        if (!soundEnabled) {
            return;
        }
        
        Clip clip = soundCache.get(soundName);
        if (clip == null) {
            System.err.println("未找到音效: " + soundName);
            return;
        }
        
        // 如果音效正在播放，停止并重置
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }
    
    /**
     * 设置音效启用状态
     * @param enabled 是否启用音效
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    /**
     * 获取音效启用状态
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * 释放资源
     */
    public void dispose() {
        for (Clip clip : soundCache.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.close();
        }
        soundCache.clear();
    }
}