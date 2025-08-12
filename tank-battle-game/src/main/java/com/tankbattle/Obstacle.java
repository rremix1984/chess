package com.tankbattle;

import java.awt.*;

/**
 * 障碍物类
 */
public class Obstacle {
    private int x, y;
    private int width, height;
    private Type type;
    private int health;
    private boolean destroyed;
    
    public enum Type {
        BRICK, STEEL, WATER
    }
    
    public Obstacle(int x, int y, int width, int height, Type type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.destroyed = false;
        
        // 根据类型设置血量
        switch (type) {
            case BRICK:
                this.health = 40;
                break;
            case STEEL:
                this.health = Integer.MAX_VALUE; // 钢铁障碍物不可摧毁
                break;
            case WATER:
                this.health = Integer.MAX_VALUE; // 水域不可摧毁
                break;
        }
    }
    
    /**
     * 绘制障碍物
     */
    public void draw(Graphics g) {
        if (destroyed) return;
        
        switch (type) {
            case BRICK:
                // 绘制砖块
                g.setColor(new Color(139, 69, 19)); // 棕色
                g.fillRect(x, y, width, height);
                
                // 绘制砖块纹理
                g.setColor(new Color(160, 82, 45));
                for (int i = 0; i < width; i += 10) {
                    for (int j = 0; j < height; j += 10) {
                        g.drawRect(x + i, y + j, 10, 10);
                    }
                }
                break;
                
            case STEEL:
                // 绘制钢铁块
                g.setColor(Color.GRAY);
                g.fillRect(x, y, width, height);
                
                // 绘制钢铁纹理
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, width, height);
                g.drawRect(x + 2, y + 2, width - 4, height - 4);
                
                // 添加反光效果
                g.setColor(Color.WHITE);
                g.drawLine(x + 5, y + 5, x + 15, y + 5);
                g.drawLine(x + 5, y + 5, x + 5, y + 15);
                break;
                
            case WATER:
                // 绘制水域
                g.setColor(new Color(0, 100, 200)); // 深蓝色
                g.fillRect(x, y, width, height);
                
                // 绘制水波纹效果
                g.setColor(new Color(100, 150, 255)); // 浅蓝色
                for (int i = 0; i < width; i += 8) {
                    for (int j = 0; j < height; j += 8) {
                        if ((i + j) % 16 == 0) {
                            g.drawLine(x + i, y + j, x + i + 4, y + j);
                            g.drawLine(x + i, y + j + 2, x + i + 6, y + j + 2);
                        }
                    }
                }
                
                // 添加水面反光
                g.setColor(new Color(200, 220, 255, 100));
                g.fillOval(x + width/4, y + height/4, width/2, height/2);
                break;
        }
        
        // 如果血量不满，显示损坏效果
        if (health < getMaxHealth() && type == Type.BRICK) {
            g.setColor(Color.BLACK);
            // 绘制裂纹
            int damage = getMaxHealth() - health;
            for (int i = 0; i < damage / 10; i++) {
                int crackX = x + (int)(Math.random() * width);
                int crackY = y + (int)(Math.random() * height);
                g.drawLine(crackX, crackY, crackX + 5, crackY + 5);
            }
        }
    }
    
    /**
     * 检查与子弹的碰撞
     */
    public boolean checkCollision(Bullet bullet) {
        if (destroyed || !bullet.isActive()) return false;
        
        return bullet.getX() < x + width &&
               bullet.getX() + bullet.getWidth() > x &&
               bullet.getY() < y + height &&
               bullet.getY() + bullet.getHeight() > y;
    }
    
    /**
     * 受到伤害
     */
    public void takeDamage(int damage) {
        if (type == Type.STEEL || type == Type.WATER) return; // 钢铁和水域不受伤害
        
        health -= damage;
        if (health <= 0) {
            destroyed = true;
        }
    }
    
    /**
     * 获取最大血量
     */
    private int getMaxHealth() {
        switch (type) {
            case BRICK:
                return 40;
            case STEEL:
                return Integer.MAX_VALUE;
            default:
                return 40;
        }
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Type getType() { return type; }
    public int getHealth() { return health; }
    public boolean isDestroyed() { return destroyed; }
}
