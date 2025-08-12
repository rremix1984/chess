package com.example.chinesechess.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 胜利动画组件
 * 包含烟花、散花、彩带等动画效果
 */
public class VictoryAnimation extends JPanel {
    private static final int ANIMATION_DURATION = 5000; // 动画持续时间5秒
    private static final int FIREWORK_COUNT = 8; // 烟花数量
    private static final int CONFETTI_COUNT = 100; // 彩纸数量
    private static final int PARTICLE_COUNT = 50; // 每个烟花的粒子数量
    
    private Timer animationTimer;
    private List<Firework> fireworks;
    private List<Confetti> confettiList;
    private List<Particle> particles;
    private Random random;
    private long startTime;
    private String victoryMessage;
    private Color winnerColor;
    private Font messageFont;
    private boolean animationRunning;
    
    public VictoryAnimation() {
        setOpaque(false);
        random = new Random();
        fireworks = new ArrayList<>();
        confettiList = new ArrayList<>();
        particles = new ArrayList<>();
        messageFont = new Font("Microsoft YaHei", Font.BOLD, 48);
        animationRunning = false;
    }
    
    /**
     * 开始胜利动画
     * @param message 胜利消息
     * @param color 获胜方颜色
     */
    public void startVictoryAnimation(String message, Color color) {
        this.victoryMessage = message;
        this.winnerColor = color;
        this.startTime = System.currentTimeMillis();
        this.animationRunning = true;
        
        // 清空之前的动画元素
        fireworks.clear();
        confettiList.clear();
        particles.clear();
        
        // 初始化烟花
        initializeFireworks();
        
        // 初始化彩纸
        initializeConfetti();
        
        // 启动动画定时器
        if (animationTimer != null) {
            animationTimer.stop();
        }
        
        animationTimer = new Timer(16, new ActionListener() { // 约60FPS
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAnimation();
                repaint();
                
                // 检查动画是否结束
                if (System.currentTimeMillis() - startTime > ANIMATION_DURATION) {
                    stopAnimation();
                }
            }
        });
        
        animationTimer.start();
        setVisible(true);
    }
    
    /**
     * 停止动画
     */
    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        animationRunning = false;
        setVisible(false);
    }
    
    /**
     * 初始化烟花
     */
    private void initializeFireworks() {
        for (int i = 0; i < FIREWORK_COUNT; i++) {
            int x = random.nextInt(getWidth() > 0 ? getWidth() : 800);
            int y = random.nextInt(getHeight() > 0 ? getHeight() / 2 : 300) + 100;
            int delay = random.nextInt(3000); // 随机延迟0-3秒
            
            Color[] colors = {
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, 
                Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK
            };
            Color fireworkColor = colors[random.nextInt(colors.length)];
            
            fireworks.add(new Firework(x, y, fireworkColor, delay));
        }
    }
    
    /**
     * 初始化彩纸
     */
    private void initializeConfetti() {
        for (int i = 0; i < CONFETTI_COUNT; i++) {
            int x = random.nextInt(getWidth() > 0 ? getWidth() : 800);
            int y = -random.nextInt(200); // 从屏幕上方开始
            
            Color[] colors = {
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, 
                Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK,
                winnerColor != null ? winnerColor : new Color(255, 215, 0)
            };
            Color confettiColor = colors[random.nextInt(colors.length)];
            
            confettiList.add(new Confetti(x, y, confettiColor));
        }
    }
    
    /**
     * 更新动画
     */
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;
        
        // 更新烟花
        for (Firework firework : fireworks) {
            firework.update(elapsed);
        }
        
        // 更新彩纸
        for (Confetti confetti : confettiList) {
            confetti.update();
        }
        
        // 更新粒子
        particles.removeIf(particle -> !particle.isAlive());
        for (Particle particle : particles) {
            particle.update();
        }
        
        // 检查烟花爆炸
        for (Firework firework : fireworks) {
            if (firework.shouldExplode(elapsed)) {
                explodeFirework(firework);
            }
        }
    }
    
    /**
     * 烟花爆炸效果
     */
    private void explodeFirework(Firework firework) {
        if (firework.hasExploded()) return;
        
        firework.explode();
        
        // 创建爆炸粒子
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = (2 * Math.PI * i) / PARTICLE_COUNT;
            double speed = 2 + random.nextDouble() * 4;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            particles.add(new Particle(
                firework.getX(), firework.getY(),
                vx, vy, firework.getColor()
            ));
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (!animationRunning) return;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制半透明背景
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // 绘制彩纸
        for (Confetti confetti : confettiList) {
            confetti.draw(g2d);
        }
        
        // 绘制烟花
        for (Firework firework : fireworks) {
            firework.draw(g2d);
        }
        
        // 绘制粒子
        for (Particle particle : particles) {
            particle.draw(g2d);
        }
        
        // 绘制胜利消息
        drawVictoryMessage(g2d);
        
        // 绘制装饰元素
        drawDecorations(g2d);
    }
    
    /**
     * 绘制胜利消息
     */
    private void drawVictoryMessage(Graphics2D g2d) {
        if (victoryMessage == null) return;
        
        long elapsed = System.currentTimeMillis() - startTime;
        
        // 文字闪烁效果
        float alpha = 0.8f + 0.2f * (float) Math.sin(elapsed * 0.01);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // 设置字体
        g2d.setFont(messageFont);
        FontMetrics fm = g2d.getFontMetrics();
        
        int textWidth = fm.stringWidth(victoryMessage);
        int textHeight = fm.getHeight();
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2;
        
        // 绘制文字阴影
        g2d.setColor(Color.BLACK);
        g2d.drawString(victoryMessage, x + 3, y + 3);
        
        // 绘制主文字
        if (winnerColor != null) {
            g2d.setColor(winnerColor);
        } else {
            g2d.setColor(new Color(255, 215, 0)); // Gold color
        }
        g2d.drawString(victoryMessage, x, y);
        
        // 绘制文字边框
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.WHITE);
        g2d.drawString(victoryMessage, x, y);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    /**
     * 绘制装饰元素
     */
    private void drawDecorations(Graphics2D g2d) {
        long elapsed = System.currentTimeMillis() - startTime;
        
        // 绘制星星
        drawStars(g2d, elapsed);
        
        // 绘制光环
        drawHalo(g2d, elapsed);
    }
    
    /**
     * 绘制星星
     */
    private void drawStars(Graphics2D g2d, long elapsed) {
        g2d.setColor(Color.YELLOW);
        
        for (int i = 0; i < 20; i++) {
            double angle = (elapsed * 0.001 + i * 0.3) % (2 * Math.PI);
            int radius = 150 + i * 10;
            int x = getWidth() / 2 + (int) (Math.cos(angle) * radius);
            int y = getHeight() / 2 + (int) (Math.sin(angle) * radius);
            
            if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
                drawStar(g2d, x, y, 8);
            }
        }
    }
    
    /**
     * 绘制单个星星
     */
    private void drawStar(Graphics2D g2d, int x, int y, int size) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5;
            int radius = (i % 2 == 0) ? size : size / 2;
            xPoints[i] = x + (int) (Math.cos(angle) * radius);
            yPoints[i] = y + (int) (Math.sin(angle) * radius);
        }
        
        g2d.fillPolygon(xPoints, yPoints, 10);
    }
    
    /**
     * 绘制光环
     */
    private void drawHalo(Graphics2D g2d, long elapsed) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        for (int i = 0; i < 3; i++) {
            float alpha = 0.3f - i * 0.1f;
            int radius = 100 + i * 50 + (int) (Math.sin(elapsed * 0.005) * 20);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(winnerColor != null ? winnerColor : new Color(255, 215, 0));
            g2d.setStroke(new BasicStroke(5));
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    /**
     * 烟花类
     */
    private static class Firework {
        private int x, y;
        private Color color;
        private long delay;
        private boolean exploded;
        private long explodeTime;
        
        public Firework(int x, int y, Color color, long delay) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.delay = delay;
            this.exploded = false;
        }
        
        public void update(long elapsed) {
            // 烟花逻辑更新
        }
        
        public boolean shouldExplode(long elapsed) {
            return elapsed > delay && !exploded;
        }
        
        public void explode() {
            exploded = true;
            explodeTime = System.currentTimeMillis();
        }
        
        public boolean hasExploded() {
            return exploded;
        }
        
        public void draw(Graphics2D g2d) {
            if (!exploded) {
                // 绘制上升的烟花
                g2d.setColor(color);
                g2d.fillOval(x - 3, y - 3, 6, 6);
            }
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public Color getColor() { return color; }
    }
    
    /**
     * 彩纸类
     */
    private static class Confetti {
        private double x, y;
        private double vx, vy;
        private Color color;
        private double rotation;
        private double rotationSpeed;
        private int width, height;
        
        public Confetti(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.vx = (Math.random() - 0.5) * 2;
            this.vy = Math.random() * 2 + 1;
            this.rotation = Math.random() * 2 * Math.PI;
            this.rotationSpeed = (Math.random() - 0.5) * 0.2;
            this.width = 8 + (int)(Math.random() * 8);
            this.height = 4 + (int)(Math.random() * 4);
        }
        
        public void update() {
            x += vx;
            y += vy;
            vy += 0.1; // 重力
            rotation += rotationSpeed;
            
            // 边界检查
            if (y > 1000) { // 重置到顶部
                y = -10;
                x = Math.random() * 800;
            }
        }
        
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            
            // 保存变换
            AffineTransform oldTransform = g2d.getTransform();
            
            // 应用旋转
            g2d.translate(x, y);
            g2d.rotate(rotation);
            
            // 绘制彩纸
            g2d.fillRect(-width/2, -height/2, width, height);
            
            // 恢复变换
            g2d.setTransform(oldTransform);
        }
    }
    
    /**
     * 粒子类
     */
    private static class Particle {
        private double x, y;
        private double vx, vy;
        private Color color;
        private int life;
        private int maxLife;
        
        public Particle(double x, double y, double vx, double vy, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.maxLife = 60 + (int)(Math.random() * 60);
            this.life = maxLife;
        }
        
        public void update() {
            x += vx;
            y += vy;
            vy += 0.1; // 重力
            vx *= 0.99; // 阻力
            vy *= 0.99;
            life--;
        }
        
        public boolean isAlive() {
            return life > 0;
        }
        
        public void draw(Graphics2D g2d) {
            float alpha = (float) life / maxLife;
            Color drawColor = new Color(
                color.getRed(), color.getGreen(), color.getBlue(),
                (int)(255 * alpha)
            );
            g2d.setColor(drawColor);
            g2d.fillOval((int)x - 2, (int)y - 2, 4, 4);
        }
    }
}