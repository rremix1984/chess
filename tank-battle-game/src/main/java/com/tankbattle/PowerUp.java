package com.tankbattle;

import java.awt.*;
import java.util.Random;

/**
 * 道具类
 */
public class PowerUp {
    public enum Type {
        BOMB("炸弹", Color.RED, "B"),
        TIME_STOP("时间停止", Color.BLUE, "T"),
        INVISIBILITY("隐身", Color.LIGHT_GRAY, "I"),
        INVINCIBILITY("无敌", Color.YELLOW, "U"),
        SPEED_BOOST("加速", Color.GREEN, "S"),
        HEALTH_PACK("生命包", Color.PINK, "H"),
        RAPID_FIRE("连发", Color.ORANGE, "R"),
        SHIELD("护盾", Color.CYAN, "D");
        
        private final String name;
        private final Color color;
        private final String symbol;
        
        Type(String name, Color color, String symbol) {
            this.name = name;
            this.color = color;
            this.symbol = symbol;
        }
        
        public String getName() { return name; }
        public Color getColor() { return color; }
        public String getSymbol() { return symbol; }
    }
    
    private int x, y;
    private int width = 30;
    private int height = 30;
    private Type type;
    private boolean active = true;
    private long spawnTime;
    private static final long LIFETIME = 15000; // 15秒后消失
    private float pulse = 0; // 脉冲动画效果
    
    public PowerUp(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
    }
    
    public static PowerUp createRandom(int x, int y) {
        Type[] types = Type.values();
        Type randomType = types[new Random().nextInt(types.length)];
        return new PowerUp(x, y, randomType);
    }
    
    public void update() {
        // 检查生命周期
        if (System.currentTimeMillis() - spawnTime > LIFETIME) {
            active = false;
        }
        
        // 更新脉冲动画
        pulse += 0.1f;
        if (pulse > Math.PI * 2) {
            pulse = 0;
        }
    }
    
    public void draw(Graphics g) {
        if (!active) return;
        
        Graphics2D g2d = (Graphics2D) g;
        
        // 脉冲效果
        float alpha = 0.7f + 0.3f * (float) Math.sin(pulse);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // 绘制道具背景
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - 2, y - 2, width + 4, height + 4);
        
        // 绘制道具主体
        g2d.setColor(type.getColor());
        g2d.fillOval(x, y, width, height);
        
        // 绘制道具符号
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(type.getSymbol())) / 2;
        int textY = y + (height + fm.getAscent()) / 2;
        g2d.drawString(type.getSymbol(), textX, textY);
        
        // 恢复透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    public boolean checkCollision(Tank tank) {
        if (!active) return false;
        
        Rectangle powerUpBounds = new Rectangle(x, y, width, height);
        Rectangle tankBounds = new Rectangle(tank.getX(), tank.getY(), tank.getWidth(), tank.getHeight());
        
        return powerUpBounds.intersects(tankBounds);
    }
    
    /**
     * 应用道具效果
     */
    public void applyEffect(Tank tank, GamePanel gamePanel) {
        if (!active) return;
        
        switch (type) {
            case BOMB:
                // 在坦克周围创建爆炸伤害
                gamePanel.createExplosionAt(tank.getX(), tank.getY(), 100, 50);
                break;
                
            case TIME_STOP:
                // 停止所有敌人5秒
                gamePanel.applyTimeStop(5000);
                break;
                
            case INVISIBILITY:
                // 隐身8秒
                tank.applyInvisibility(8000);
                break;
                
            case INVINCIBILITY:
                // 无敌6秒
                tank.applyInvincibility(6000);
                break;
                
            case SPEED_BOOST:
                // 加速10秒
                tank.applySpeedBoost(10000, 2.0f);
                break;
                
            case HEALTH_PACK:
                // 恢复50点生命值
                tank.heal(50);
                break;
                
            case RAPID_FIRE:
                // 快速射击8秒
                tank.applyRapidFire(8000);
                break;
                
            case SHIELD:
                // 护盾10秒，吸收100点伤害
                tank.applyShield(10000, 100);
                break;
        }
        
        active = false; // 使用后消失
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Type getType() { return type; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
