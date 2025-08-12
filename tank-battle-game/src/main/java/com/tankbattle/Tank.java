package com.tankbattle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 坦克类
 */
public class Tank {
    private int x, y;
    private int width, height;
    private Direction direction;
    private Color color;
    private int speed;
    private int health;
    private boolean isPlayer;
    private String playerId;
    private List<Bullet> bullets;
    private long lastFireTime;
    private static final int FIRE_COOLDOWN = 200; // 射击冷却时间（毫秒）
    private AIState aiState;
    private long lastDecisionTime;
    private static final long MIN_DECISION_INTERVAL = 200; // AI最小决策间隔：0.2秒
    private static final long MAX_DECISION_INTERVAL = 500; // AI最大决策间隔：0.5秒
    private long currentDecisionInterval;
    
    // 道具效果相关
    private boolean invisible = false;
    private boolean invincible = false;
    private boolean hasShield = false;
    private boolean rapidFire = false;
    private boolean speedBoost = false;
    private long invisibilityEndTime = 0;
    private long invincibilityEndTime = 0;
    private long shieldEndTime = 0;
    private long rapidFireEndTime = 0;
    private long speedBoostEndTime = 0;
    private int shieldHealth = 0;
    private float speedMultiplier = 1.0f;
    private int originalSpeed;
    
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    
    public Tank(int x, int y, Color color, boolean isPlayer, String playerId) {
        this.x = x;
        this.y = y;
        this.width = 40;
        this.height = 40;
        this.direction = Direction.UP;
        this.color = color;
this.speed = isPlayer ? 4 : 6; // 玩家速度4，AI速度6
        this.health = 100;
        this.isPlayer = isPlayer;
        this.playerId = playerId;
        this.bullets = new ArrayList<>();
        this.lastFireTime = 0;
        this.lastDecisionTime = 0;
        this.currentDecisionInterval = generateRandomDecisionInterval();
        this.originalSpeed = this.speed;
    }
    
    /**
     * 移动坦克
     */
    public void move(Direction newDirection) {
        this.direction = newDirection;
        
        int newX = x;
        int newY = y;
        
        switch (direction) {
            case UP:
                newY -= speed;
                break;
            case DOWN:
                newY += speed;
                break;
            case LEFT:
                newX -= speed;
                break;
            case RIGHT:
                newX += speed;
                break;
        }
        
        // 边界检查
        if (newX >= 0 && newX <= 960 - width && newY >= 0 && newY <= 760 - height) {
            this.x = newX;
            this.y = newY;
        }
    }
    
    /**
     * 尝试移动坦克（带障碍物检测）
     */
    public boolean tryMove(Direction newDirection, java.util.List<Obstacle> obstacles) {
        this.direction = newDirection;
        
        int newX = x;
        int newY = y;
        
        switch (direction) {
            case UP:
                newY -= speed;
                break;
            case DOWN:
                newY += speed;
                break;
            case LEFT:
                newX -= speed;
                break;
            case RIGHT:
                newX += speed;
                break;
        }
        
        // 边界检查
        if (newX < 0 || newX > 960 - width || newY < 0 || newY > 760 - height) {
            return false;
        }
        
        // 障碍物碰撞检查 - 改进版本，防止穿越
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isDestroyed()) continue; // 跳过已摧毁的障碍物
            
            // 精确的碰撞检测，留出小的缓冲区避免过于严格的碰撞
            Rectangle tankBounds = new Rectangle(newX + 2, newY + 2, width - 4, height - 4);
            Rectangle obstacleBounds = new Rectangle(
                obstacle.getX(), obstacle.getY(), 
                obstacle.getWidth(), obstacle.getHeight()
            );
            
            if (tankBounds.intersects(obstacleBounds)) {
                // 根据障碍物类型决定是否可以通过 - 减少调试输出
                switch (obstacle.getType()) {
                    case STEEL:
                    case BRICK:
                    case WATER:
                        return false;
                }
            }
        }
        
        // 如果没有碰撞，执行移动
        this.x = newX;
        this.y = newY;
        return true;
    }
    
    /**
     * 开火
     */
    public Bullet fire() {
        long currentTime = System.currentTimeMillis();
        int actualCooldown = rapidFire ? FIRE_COOLDOWN / 3 : FIRE_COOLDOWN; // 快速射击时冷却时间减少
        if (currentTime - lastFireTime < actualCooldown) {
            return null; // 冷却中
        }
        
        lastFireTime = currentTime;
        
        int bulletX = x + width / 2 - 2;
        int bulletY = y + height / 2 - 2;
        
        // 根据坦克方向调整子弹起始位置
        switch (direction) {
            case UP:
                bulletY = y - 10;
                break;
            case DOWN:
                bulletY = y + height + 5;
                break;
            case LEFT:
                bulletX = x - 10;
                break;
            case RIGHT:
                bulletX = x + width + 5;
                break;
        }
        
        Bullet bullet = new Bullet(bulletX, bulletY, direction, playerId);
        bullets.add(bullet);
        return bullet;
    }
    
    /**
     * 初始化AI状态
     */
    public void initAIState() {
        this.aiState = new AIState();
    }
    
    /**
     * 检查AI状态是否存在
     */
    public boolean hasAIState() {
        return this.aiState != null;
    }

    /**
     * 更新AI状态
     */
    public void updateAIState() {
        if (aiState != null) {
            aiState.update();
        }
    }
    
    /**
     * 是否应该移动
     */
    public boolean shouldMove() {
        return aiState != null && aiState.shouldMove();
    }
    
    /**
     * 是否应该射击
     */
    public boolean shouldShoot() {
        return aiState != null && aiState.shouldShoot();
    }
    
    /**
     * 重置移动冷却
     */
    public void resetMoveCooldown(int cooldown) {
        if (aiState != null) {
            aiState.resetMoveCooldown(cooldown);
        }
    }
    
    /**
     * 重置射击冷却
     */
    public void resetShootCooldown(int cooldown) {
        if (aiState != null) {
            aiState.resetShootCooldown(cooldown);
        }
    }
    
    /**
     * 检查是否可以进行AI决策（基于随机时间间隔）
     */
    public boolean canMakeDecision() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastDecisionTime >= currentDecisionInterval;
    }
    
    /**
     * 更新上次决策时间并生成新的随机间隔
     */
    public void updateDecisionTime() {
        this.lastDecisionTime = System.currentTimeMillis();
        this.currentDecisionInterval = generateRandomDecisionInterval();
        System.out.println("[AI间隔] 坦克 " + playerId + " 下次决策间隔: " + (currentDecisionInterval / 1000.0) + "秒");
    }
    
    /**
     * 生成随机决策间隔（3-5秒）
     */
    private long generateRandomDecisionInterval() {
        return MIN_DECISION_INTERVAL + (long)(Math.random() * (MAX_DECISION_INTERVAL - MIN_DECISION_INTERVAL));
    }
    
    /**
     * 更新子弹
     */
    public void updateBullets() {
        bullets.removeIf(bullet -> !bullet.isActive());
        for (Bullet bullet : bullets) {
            bullet.update();
        }
    }
    
    /**
     * 更新道具效果
     */
    public void updatePowerUpEffects() {
        long currentTime = System.currentTimeMillis();
        
        // 更新隐身效果
        if (invisible && currentTime > invisibilityEndTime) {
            invisible = false;
        }
        
        // 更新无敌效果
        if (invincible && currentTime > invincibilityEndTime) {
            invincible = false;
        }
        
        // 更新护盾效果
        if (hasShield && (currentTime > shieldEndTime || shieldHealth <= 0)) {
            hasShield = false;
            shieldHealth = 0;
        }
        
        // 更新快速射击效果
        if (rapidFire && currentTime > rapidFireEndTime) {
            rapidFire = false;
        }
        
        // 更新加速效果
        if (speedBoost && currentTime > speedBoostEndTime) {
            speedBoost = false;
            speed = originalSpeed;
            speedMultiplier = 1.0f;
        }
    }
    
    /**
     * 绘制坦克
     */
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // 如果隐身，设置透明度
        if (invisible) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }
        
        // 绘制坦克主体
        Color tankColor = color;
        if (invincible) {
            // 无敌时闪烁金色
            tankColor = (System.currentTimeMillis() % 200 < 100) ? Color.YELLOW : color;
        }
        g.setColor(tankColor);
        g.fillRect(x, y, width, height);
        
        // 绘制坦克边框
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        
        // 绘制炮管
        g.setColor(Color.DARK_GRAY);
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        
        switch (direction) {
            case UP:
                g.fillRect(centerX - 3, y - 10, 6, 15);
                break;
            case DOWN:
                g.fillRect(centerX - 3, y + height - 5, 6, 15);
                break;
            case LEFT:
                g.fillRect(x - 10, centerY - 3, 15, 6);
                break;
            case RIGHT:
                g.fillRect(x + width - 5, centerY - 3, 15, 6);
                break;
        }
        
        // 绘制护盾
        if (hasShield && shieldHealth > 0) {
            g.setColor(Color.CYAN);
            g.drawOval(x - 5, y - 5, width + 10, height + 10);
            g.drawOval(x - 3, y - 3, width + 6, height + 6);
        }
        
        // 绘制血量条
        if (health < 100) {
            g.setColor(Color.RED);
            g.fillRect(x, y - 10, width, 5);
            g.setColor(Color.GREEN);
            g.fillRect(x, y - 10, (int)(width * (health / 100.0)), 5);
        }
        
        // 绘制护盾血量条
        if (hasShield && shieldHealth > 0) {
            g.setColor(Color.CYAN);
            g.fillRect(x, y - 15, width, 3);
            g.setColor(Color.WHITE);
            g.fillRect(x, y - 15, (int)(width * (shieldHealth / 100.0)), 3);
        }
        
        // 恢复透明度
        if (invisible) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        // 绘制子弹
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
    }
    
    /**
     * 检查碰撞
     */
    public boolean checkCollision(Tank other) {
        return x < other.x + other.width &&
               x + width > other.x &&
               y < other.y + other.height &&
               y + height > other.y;
    }
    
    /**
     * 受到伤害
     */
    public void takeDamage(int damage) {
        if (invincible) {
            return; // 无敌状态不受伤害
        }
        
        if (hasShield && shieldHealth > 0) {
            // 护盾先承受伤害
            int shieldDamage = Math.min(damage, shieldHealth);
            shieldHealth -= shieldDamage;
            damage -= shieldDamage;
        }
        
        if (damage > 0) {
            health -= damage;
            if (health < 0) {
                health = 0;
            }
        }
    }
    
    /**
     * 治疗
     */
    public void heal(int amount) {
        health = Math.min(100, health + amount);
    }
    
    /**
     * 是否已死亡
     */
    public boolean isDead() {
        return health <= 0;
    }
    
    // Getters and Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Direction getDirection() { return direction; }
    public Color getColor() { return color; }
    public int getHealth() { return health; }
    public boolean isPlayer() { return isPlayer; }
    public String getPlayerId() { return playerId; }
    public List<Bullet> getBullets() { return bullets; }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public void setHealth(int health) { this.health = health; }
    
    // 道具效果方法
    public void applyInvisibility(long duration) {
        this.invisible = true;
        this.invisibilityEndTime = System.currentTimeMillis() + duration;
    }
    
    public void applyInvincibility(long duration) {
        this.invincible = true;
        this.invincibilityEndTime = System.currentTimeMillis() + duration;
    }
    
    public void applyShield(long duration, int shieldAmount) {
        this.hasShield = true;
        this.shieldHealth = shieldAmount;
        this.shieldEndTime = System.currentTimeMillis() + duration;
    }
    
    public void applyRapidFire(long duration) {
        this.rapidFire = true;
        this.rapidFireEndTime = System.currentTimeMillis() + duration;
    }
    
    public void applySpeedBoost(long duration, float multiplier) {
        this.speedBoost = true;
        this.speedMultiplier = multiplier;
        this.speed = (int)(originalSpeed * multiplier);
        this.speedBoostEndTime = System.currentTimeMillis() + duration;
    }
    
    public boolean isInvisible() { return invisible; }
    public boolean isInvincible() { return invincible; }
    /**
     * 内部类：AI状态
     */
    private class AIState {
        private int moveCooldown;
        private int shootCooldown;

        public AIState() {
            this.moveCooldown = (int)(Math.random() * 100) + 50;
            this.shootCooldown = (int)(Math.random() * 50) + 25;
        }

        public void update() {
            if (moveCooldown > 0) moveCooldown--;
            if (shootCooldown > 0) shootCooldown--;
        }

        public boolean shouldMove() {
            return moveCooldown <= 0;
        }

        public boolean shouldShoot() {
            return shootCooldown <= 0;
        }
        
        public void resetMoveCooldown(int frames) {
            this.moveCooldown = frames;
        }
        
        public void resetShootCooldown(int frames) {
            this.shootCooldown = frames;
        }
    }
}
