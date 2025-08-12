package com.tankbattle;

import java.awt.*;

/**
 * 爆炸效果类
 */
public class Explosion {
    private int x, y;
    private int size;
    private int maxSize;
    private int duration;
    private int currentTime;
    private boolean active;
    
    public Explosion(int x, int y) {
        this.x = x;
        this.y = y;
        this.size = 5;
        this.maxSize = 25;
        this.duration = 20; // 持续帧数
        this.currentTime = 0;
        this.active = true;
    }
    
    /**
     * 更新爆炸效果
     */
    public void update() {
        if (!active) return;
        
        currentTime++;
        size = (int) (maxSize * ((double) currentTime / duration));
        
        if (currentTime >= duration) {
            active = false;
        }
    }
    
    /**
     * 绘制爆炸效果
     */
    public void draw(Graphics g) {
        if (!active) return;
        
        // 外层橙色圆圈
        g.setColor(Color.ORANGE);
        g.fillOval(x - size, y - size, size * 2, size * 2);
        
        // 内层红色圆圈
        g.setColor(Color.RED);
        int innerSize = size * 2 / 3;
        g.fillOval(x - innerSize, y - innerSize, innerSize * 2, innerSize * 2);
        
        // 中心黄色圆圈
        g.setColor(Color.YELLOW);
        int centerSize = size / 3;
        g.fillOval(x - centerSize, y - centerSize, centerSize * 2, centerSize * 2);
    }
    
    public boolean isActive() {
        return active;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
}
