package com.tankbattle;

import java.awt.*;

/**
 * 子弹类
 */
public class Bullet {
    private int x, y;
    private int width, height;
    private Tank.Direction direction;
    private int speed;
    private boolean active;
    private String ownerId;
    private int damage;
    
    public Bullet(int x, int y, Tank.Direction direction, String ownerId) {
        this.x = x;
        this.y = y;
this.width = 6;
        this.height = 6;
        this.direction = direction;
        this.speed = 8;
        this.active = true;
        this.ownerId = ownerId;
        this.damage = 20;
    }
    
    /**
     * 更新子弹位置
     */
    public void update() {
        if (!active) return;
        
        switch (direction) {
            case UP:
                y -= speed;
                break;
            case DOWN:
                y += speed;
                break;
            case LEFT:
                x -= speed;
                break;
            case RIGHT:
                x += speed;
                break;
        }
        
        // 边界检查
        if (x < 0 || x > 1000 || y < 0 || y > 800) {
            active = false;
        }
    }
    
    /**
     * 绘制子弹
     */
    public void draw(Graphics g) {
        if (!active) return;
        
g.setColor(Color.YELLOW);
        g.fillOval(x, y, width, height);
        g.setColor(Color.ORANGE);
        g.fillOval(x - 2, y - 2, width + 4, height + 4);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, width, height);
    }
    
    /**
     * 检查与坦克的碰撞
     */
    public boolean checkCollision(Tank tank) {
        if (!active || tank.getPlayerId().equals(ownerId)) {
            return false; // 不与自己的坦克碰撞
        }
        
        return x < tank.getX() + tank.getWidth() &&
               x + width > tank.getX() &&
               y < tank.getY() + tank.getHeight() &&
               y + height > tank.getY();
    }
    
    /**
     * 销毁子弹
     */
    public void destroy() {
        active = false;
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Tank.Direction getDirection() { return direction; }
    public boolean isActive() { return active; }
    public String getOwnerId() { return ownerId; }
    public int getDamage() { return damage; }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
