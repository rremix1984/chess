package com.example.common.sound;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;

/**
 * 简单音效生成器
 * 用于生成简单的音效
 */
public class SimpleSound {
    
    /**
     * 生成一个简单的下棋音效
     * @return 音效的音频输入流
     */
    public static AudioInputStream generatePieceDropSound() {
        try {
            // 音频格式参数
            float sampleRate = 44100.0f;
            int sampleSizeInBits = 8;
            int channels = 1;
            boolean signed = true;
            boolean bigEndian = false;
            
            AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
            
            // 生成0.1秒的音频数据
            int duration = (int)(sampleRate * 0.1);
            byte[] buffer = new byte[duration];
            
            // 生成一个简单的音调
            for (int i = 0; i < duration; i++) {
                double angle = i / (sampleRate / 440) * 2.0 * Math.PI; // 440Hz音调
                buffer[i] = (byte)(Math.sin(angle) * 100);
            }
            
            return new AudioInputStream(new ByteArrayInputStream(buffer), format, buffer.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 生成一个简单的胜利音效
     * @return 音效的音频输入流
     */
    public static AudioInputStream generateWinSound() {
        try {
            // 音频格式参数
            float sampleRate = 44100.0f;
            int sampleSizeInBits = 8;
            int channels = 1;
            boolean signed = true;
            boolean bigEndian = false;
            
            AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
            
            // 生成0.5秒的音频数据
            int duration = (int)(sampleRate * 0.5);
            byte[] buffer = new byte[duration];
            
            // 生成一个上升的音调
            for (int i = 0; i < duration; i++) {
                double frequency = 440 + (i / (double)duration) * 440; // 从440Hz上升到880Hz
                double angle = i / (sampleRate / frequency) * 2.0 * Math.PI;
                buffer[i] = (byte)(Math.sin(angle) * 100);
            }
            
            return new AudioInputStream(new ByteArrayInputStream(buffer), format, buffer.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
