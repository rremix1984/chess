package com.tankbattle;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 背景渲染器 - 创建丰富多彩的动态背景
 */
public class BackgroundRenderer {
    private final int width;
    private final int height;
    private final Random random;
    
    // 背景元素
    private List<Star> stars;
    private List<Cloud> clouds;
    private List<Particle> particles;
    private List<GradientZone> gradientZones;
    private List<GeometricShape> geometricShapes;
    
    // 动画时间
    private long startTime;
    private float animationTime;
    
    public BackgroundRenderer(int width, int height) {
        this.width = width;
        this.height = height;
        this.random = new Random();
        this.startTime = System.currentTimeMillis();
        
        initializeBackgroundElements();
    }
    
    private void initializeBackgroundElements() {
        // 初始化星星（增加数量和多样性）
        stars = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            stars.add(new Star(
                random.nextFloat() * width,
                random.nextFloat() * height,
                random.nextFloat() * 3 + 1,
                new Color(
                    random.nextInt(100) + 155,
                    random.nextInt(100) + 155,
                    random.nextInt(100) + 155,
                    random.nextInt(150) + 50
                ),
                random.nextFloat() * 0.02f + 0.005f
            ));
        }
        
        // 初始化云朵（增加数量和变化）
        clouds = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            clouds.add(new Cloud(
                random.nextFloat() * width,
                random.nextFloat() * height * 0.6f,
                random.nextFloat() * 80 + 40,
                new Color(
                    clamp(random.nextInt(80) + 175, 0, 255),
                    clamp(random.nextInt(80) + 175, 0, 255),
                    clamp(random.nextInt(100) + 200, 0, 255),
                    random.nextInt(60) + 40
                ),
                random.nextFloat() * 0.5f + 0.2f
            ));
        }
        
        // 初始化粒子效果（增加数量）
        particles = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            particles.add(new Particle(
                random.nextFloat() * width,
                random.nextFloat() * height,
                random.nextFloat() * 4 - 2,
                random.nextFloat() * 4 - 2,
                random.nextFloat() * 6 + 2,
                new Color(
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(80) + 20
                )
            ));
        }
        
        // 初始化渐变区域（增加数量和随机性）
        gradientZones = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            gradientZones.add(new GradientZone(
                random.nextFloat() * width,
                random.nextFloat() * height,
                random.nextFloat() * 200 + 100,
                new Color(
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(30) + 10
                ),
                new Color(
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(255),
                    5
                )
            ));
        }
        
        // 初始化几何图形（增加数量和多样性）
        geometricShapes = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            geometricShapes.add(new GeometricShape(
                random.nextFloat() * width,
                random.nextFloat() * height,
                random.nextFloat() * 50 + 20,
                new Color(
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(60) + 20
                ),
                random.nextInt(6) + 3, // 3-8边形
                random.nextFloat() * 0.02f + 0.005f
            ));
        }
    }
    
    public void update() {
        animationTime = (System.currentTimeMillis() - startTime) / 1000.0f;
        
        // 更新所有背景元素
        for (Star star : stars) {
            star.update(animationTime);
        }
        
        for (Cloud cloud : clouds) {
            cloud.update(width);
        }
        
        for (Particle particle : particles) {
            particle.update(width, height);
        }
        
        for (GradientZone zone : gradientZones) {
            zone.update(animationTime);
        }
        
        for (GeometricShape shape : geometricShapes) {
            shape.update(animationTime);
        }
    }
    
    public void render(Graphics2D g2d) {
        // 创建主背景渐变
        createMainBackground(g2d);
        
        // 添加随机颜色溢出效果
        renderColorBurst(g2d);
        
        // 渲染各层背景元素
        renderGradientZones(g2d);
        renderStars(g2d);
        renderClouds(g2d);
        renderGeometricShapes(g2d);
        renderParticles(g2d);
        
        // 添加动态光效
        renderDynamicLighting(g2d);
        
        // 添加闪电效果
        renderLightning(g2d);
        
        // 添加彩虹条纹
        renderRainbowStripes(g2d);
    }
    
    private void createMainBackground(Graphics2D g2d) {
        // 创建多层渐变背景
        float time = animationTime * 0.5f;
        
        // 第一层：基础渐变
        Color color1 = new Color(
            clamp((int)(Math.sin(time) * 30 + 60), 0, 255),
            clamp((int)(Math.sin(time + 1) * 40 + 80), 0, 255),
            clamp((int)(Math.sin(time + 2) * 50 + 120), 0, 255)
        );
        Color color2 = new Color(
            clamp((int)(Math.cos(time) * 20 + 40), 0, 255),
            clamp((int)(Math.cos(time + 1.5) * 30 + 60), 0, 255),
            clamp((int)(Math.cos(time + 2.5) * 35 + 85), 0, 255)
        );
        
        GradientPaint gradient = new GradientPaint(
            0, 0, color1,
            width, height, color2
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // 第二层：对角渐变
        Color color3 = new Color(
            clamp((int)(Math.sin(time + 3) * 25 + 45), 0, 255),
            clamp((int)(Math.sin(time + 4) * 35 + 75), 0, 255),
            clamp((int)(Math.sin(time + 5) * 40 + 100), 0, 255),
            100
        );
        Color color4 = new Color(
            clamp((int)(Math.cos(time + 3) * 30 + 50), 0, 255),
            clamp((int)(Math.cos(time + 4) * 25 + 65), 0, 255),
            clamp((int)(Math.cos(time + 5) * 30 + 90), 0, 255),
            100
        );
        
        GradientPaint diagonalGradient = new GradientPaint(
            width, 0, color3,
            0, height, color4
        );
        g2d.setPaint(diagonalGradient);
        g2d.fillRect(0, 0, width, height);
    }
    
    private void renderGradientZones(Graphics2D g2d) {
        for (GradientZone zone : gradientZones) {
            zone.render(g2d);
        }
    }
    
    private void renderStars(Graphics2D g2d) {
        for (Star star : stars) {
            star.render(g2d);
        }
    }
    
    private void renderClouds(Graphics2D g2d) {
        for (Cloud cloud : clouds) {
            cloud.render(g2d);
        }
    }
    
    private void renderGeometricShapes(Graphics2D g2d) {
        for (GeometricShape shape : geometricShapes) {
            shape.render(g2d);
        }
    }
    
    private void renderParticles(Graphics2D g2d) {
        for (Particle particle : particles) {
            particle.render(g2d);
        }
    }
    
    private void renderDynamicLighting(Graphics2D g2d) {
        // 创建动态光效
        float time = animationTime;
        
        // 光晕效果
        for (int i = 0; i < 3; i++) {
            float lightX = (float)(Math.sin(time * 0.7 + i * 2) * width * 0.3 + width * 0.5);
            float lightY = (float)(Math.cos(time * 0.5 + i * 1.5) * height * 0.3 + height * 0.5);
            
            RadialGradientPaint lightGradient = new RadialGradientPaint(
                lightX, lightY, 150,
                new float[]{0.0f, 0.7f, 1.0f},
                new Color[]{
                    new Color(255, 255, 200, 30),
                    new Color(255, 200, 100, 15),
                    new Color(255, 150, 50, 0)
                }
            );
            
            g2d.setPaint(lightGradient);
            g2d.fillOval((int)(lightX - 150), (int)(lightY - 150), 300, 300);
        }
    }
    
    // 内部类：星星
    private static class Star {
        float x, y, size;
        Color color;
        float twinkleSpeed;
        float initialAlpha;
        
        Star(float x, float y, float size, Color color, float twinkleSpeed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.twinkleSpeed = twinkleSpeed;
            this.initialAlpha = color.getAlpha() / 255.0f;
        }
        
        void update(float time) {
            // 闪烁效果
            float alpha = (float)(Math.sin(time * twinkleSpeed * 10) * 0.3 + 0.7) * initialAlpha;
            color = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                Math.max(0, Math.min(255, (int)(alpha * 255)))
            );
        }
        
        void render(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillOval((int)(x - size/2), (int)(y - size/2), (int)size, (int)size);
            
            // 添加光芒效果
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 3));
            g2d.fillOval((int)(x - size), (int)(y - size), (int)(size * 2), (int)(size * 2));
        }
    }
    
    // 内部类：云朵
    private static class Cloud {
        float x, y, size;
        Color color;
        float speed;
        
        Cloud(float x, float y, float size, Color color, float speed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.speed = speed;
        }
        
        void update(int screenWidth) {
            x += speed;
            if (x > screenWidth + size) {
                x = -size;
            }
        }
        
        void render(Graphics2D g2d) {
            g2d.setColor(color);
            // 绘制云朵形状
            g2d.fillOval((int)x, (int)y, (int)size, (int)(size * 0.6));
            g2d.fillOval((int)(x + size * 0.3), (int)(y - size * 0.2), (int)(size * 0.8), (int)(size * 0.7));
            g2d.fillOval((int)(x + size * 0.6), (int)y, (int)(size * 0.7), (int)(size * 0.5));
        }
    }
    
    // 内部类：粒子
    private static class Particle {
        float x, y, vx, vy, life, maxLife;
        Color color;
        
        Particle(float x, float y, float vx, float vy, float life, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
            this.color = color;
        }
        
        void update(int screenWidth, int screenHeight) {
            x += vx;
            y += vy;
            life -= 0.05f;
            
            // 重置粒子
            if (life <= 0 || x < 0 || x > screenWidth || y < 0 || y > screenHeight) {
                Random rand = new Random();
                x = rand.nextFloat() * screenWidth;
                y = rand.nextFloat() * screenHeight;
                vx = rand.nextFloat() * 4 - 2;
                vy = rand.nextFloat() * 4 - 2;
                life = maxLife;
                color = new Color(
                    rand.nextInt(255),
                    rand.nextInt(255),
                    rand.nextInt(255),
                    rand.nextInt(80) + 20
                );
            }
            
            // 更新透明度
            float alpha = (life / maxLife) * (color.getAlpha() / 255.0f);
            color = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                Math.max(0, Math.min(255, (int)(alpha * 255)))
            );
        }
        
        void render(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillOval((int)(x - 1), (int)(y - 1), 3, 3);
        }
    }
    
    // 内部类：渐变区域
    private static class GradientZone {
        float x, y, radius;
        Color centerColor, edgeColor;
        float pulseSpeed;
        
        GradientZone(float x, float y, float radius, Color centerColor, Color edgeColor) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.centerColor = centerColor;
            this.edgeColor = edgeColor;
            this.pulseSpeed = new Random().nextFloat() * 0.02f + 0.01f;
        }
        
        void update(float time) {
            // 脉冲效果
            float pulse = (float)(Math.sin(time * pulseSpeed * 20) * 0.2 + 0.8);
            radius = Math.abs(radius * pulse); // 确保半径为正数
        }
        
        void render(Graphics2D g2d) {
            RadialGradientPaint gradient = new RadialGradientPaint(
                x, y, radius,
                new float[]{0.0f, 1.0f},
                new Color[]{centerColor, edgeColor}
            );
            g2d.setPaint(gradient);
            g2d.fillOval((int)(x - radius), (int)(y - radius), (int)(radius * 2), (int)(radius * 2));
        }
    }
    
    // 内部类：几何图形
    private static class GeometricShape {
        float x, y, size;
        Color color;
        int sides;
        float rotationSpeed;
        float rotation;
        
        GeometricShape(float x, float y, float size, Color color, int sides, float rotationSpeed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.sides = sides;
            this.rotationSpeed = rotationSpeed;
            this.rotation = 0;
        }
        
        void update(float time) {
            rotation += rotationSpeed * 60; // 60 FPS approximation
        }
        
        void render(Graphics2D g2d) {
            AffineTransform oldTransform = g2d.getTransform();
            
            g2d.translate(x, y);
            g2d.rotate(Math.toRadians(rotation));
            
            // 创建多边形
            int[] xPoints = new int[sides];
            int[] yPoints = new int[sides];
            
            for (int i = 0; i < sides; i++) {
                double angle = 2 * Math.PI * i / sides;
                xPoints[i] = (int)(Math.cos(angle) * size);
                yPoints[i] = (int)(Math.sin(angle) * size);
            }
            
            Polygon polygon = new Polygon(xPoints, yPoints, sides);
            
            g2d.setColor(color);
            g2d.fill(polygon);
            
            // 添加边框
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, color.getAlpha() * 2)));
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(polygon);
            
            g2d.setTransform(oldTransform);
        }
    }
    
    /**
     * 渲染随机颜色溢出效果
     */
    private void renderColorBurst(Graphics2D g2d) {
        float time = animationTime * 2.0f;
        
        // 创建多个随机位置的颜色爆发
        for (int i = 0; i < 6; i++) {
            float burstX = (float)(Math.sin(time * 0.3 + i * 1.2) * width * 0.4 + width * 0.5);
            float burstY = (float)(Math.cos(time * 0.4 + i * 1.5) * height * 0.4 + height * 0.5);
            
            // 随机颜色
            Color burstColor = new Color(
                clamp((int)(Math.sin(time + i) * 128 + 127), 0, 255),
                clamp((int)(Math.sin(time + i + 2) * 128 + 127), 0, 255),
                clamp((int)(Math.sin(time + i + 4) * 128 + 127), 0, 255),
                40
            );
            
            // 创建放射状渐变
            float radius = (float)(Math.sin(time * 0.8 + i) * 80 + 120);
            RadialGradientPaint burstGradient = new RadialGradientPaint(
                burstX, burstY, Math.abs(radius),
                new float[]{0.0f, 0.5f, 1.0f},
                new Color[]{
                    burstColor,
                    new Color(burstColor.getRed(), burstColor.getGreen(), burstColor.getBlue(), 20),
                    new Color(burstColor.getRed(), burstColor.getGreen(), burstColor.getBlue(), 0)
                }
            );
            
            g2d.setPaint(burstGradient);
            g2d.fillOval((int)(burstX - Math.abs(radius)), (int)(burstY - Math.abs(radius)), 
                        (int)(Math.abs(radius) * 2), (int)(Math.abs(radius) * 2));
        }
    }
    
    /**
     * 渲染闪电效果
     */
    private void renderLightning(Graphics2D g2d) {
        float time = animationTime;
        
        // 每5秒随机出现一次闪电
        if (Math.sin(time * 0.2) > 0.95 && Math.random() < 0.3) {
            g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            // 创建闪电路径
            int startX = random.nextInt(width);
            int startY = 0;
            int currentX = startX;
            int currentY = startY;
            
            Color lightningColor = new Color(255, 255, 200, 180);
            g2d.setColor(lightningColor);
            
            // 绘制曲折的闪电
            while (currentY < height) {
                int nextX = currentX + random.nextInt(60) - 30;
                int nextY = currentY + random.nextInt(80) + 20;
                
                g2d.drawLine(currentX, currentY, nextX, nextY);
                
                // 添加光晕效果
                g2d.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.drawLine(currentX, currentY, nextX, nextY);
                
                // 恢复原始笔触
                g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(lightningColor);
                
                currentX = nextX;
                currentY = nextY;
                
                // 随机分叉
                if (random.nextFloat() < 0.3) {
                    int branchX = currentX + random.nextInt(40) - 20;
                    int branchY = currentY + random.nextInt(30) + 10;
                    g2d.drawLine(currentX, currentY, branchX, branchY);
                }
            }
        }
    }
    
    /**
     * 渲染彩虹条纹效果
     */
    private void renderRainbowStripes(Graphics2D g2d) {
        float time = animationTime * 0.1f;
        
        // 创建流动的彩虹条纹
        for (int i = 0; i < 5; i++) {
            float offset = (float)(Math.sin(time + i * 0.5) * height * 0.1);
            int stripeY = (int)(i * height / 5 + offset);
            
            // 彩虹颜色
            Color rainbowColor = Color.getHSBColor((time * 0.5f + i * 0.2f) % 1.0f, 0.8f, 0.9f);
            Color transparentColor = new Color(
                rainbowColor.getRed(),
                rainbowColor.getGreen(),
                rainbowColor.getBlue(),
                30
            );
            
            // 创建渐变条纹
            GradientPaint stripeGradient = new GradientPaint(
                0, stripeY, transparentColor,
                width, stripeY + 20, new Color(transparentColor.getRed(), transparentColor.getGreen(), transparentColor.getBlue(), 0)
            );
            
            g2d.setPaint(stripeGradient);
            g2d.fillRect(0, stripeY, width, 30);
        }
    }
    
    /**
     * 确保值在指定范围内
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
