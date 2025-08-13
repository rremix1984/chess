package com.example.internationalchess.ui;

import com.example.internationalchess.core.PieceColor;
import com.example.internationalchess.core.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

/**
 * 胜利动画类
 * 用于显示游戏结束时的胜利动画效果
 */
public class VictoryAnimation extends JPanel {
    
    private GameState gameState;
    private Timer animationTimer;
    private int animationFrame = 0;
    private final int maxFrames = 60;
    private boolean animationRunning = false;
    
    // 动画参数
    private float alpha = 0.0f;
    private float scale = 0.5f;
    private float rotation = 0.0f;
    
    /**
     * 构造函数
     */
    public VictoryAnimation() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 400));
        
        // 初始化动画定时器
        animationTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAnimation();
            }
        });
    }
    
    /**
     * 开始胜利动画
     */
    public void startVictoryAnimation(GameState gameState) {
        this.gameState = gameState;
        this.animationFrame = 0;
        this.animationRunning = true;
        this.alpha = 0.0f;
        this.scale = 0.5f;
        this.rotation = 0.0f;
        
        animationTimer.start();
        setVisible(true);
    }
    
    /**
     * 停止动画
     */
    public void stopAnimation() {
        animationTimer.stop();
        animationRunning = false;
        setVisible(false);
    }
    
    /**
     * 更新动画帧
     */
    private void updateAnimation() {
        if (!animationRunning) {
            return;
        }
        
        animationFrame++;
        
        // 计算动画参数
        float progress = (float) animationFrame / maxFrames;
        
        // 淡入效果
        if (progress <= 0.3f) {
            alpha = progress / 0.3f;
        } else {
            alpha = 1.0f;
        }
        
        // 缩放效果
        if (progress <= 0.5f) {
            scale = 0.5f + (progress / 0.5f) * 0.7f; // 从0.5缩放到1.2
        } else {
            scale = 1.2f - ((progress - 0.5f) / 0.5f) * 0.2f; // 从1.2缩放到1.0
        }
        
        // 旋转效果
        rotation = progress * 360.0f;
        
        repaint();
        
        // 动画结束
        if (animationFrame >= maxFrames) {
            animationTimer.stop();
            // 保持显示3秒后自动隐藏
            Timer hideTimer = new Timer(3000, e -> stopAnimation());
            hideTimer.setRepeats(false);
            hideTimer.start();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (!animationRunning || gameState == null) {
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 设置透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        // 保存原始变换
        AffineTransform originalTransform = g2d.getTransform();
        
        // 应用缩放和旋转变换
        g2d.translate(centerX, centerY);
        g2d.scale(scale, scale);
        g2d.rotate(Math.toRadians(rotation));
        g2d.translate(-centerX, -centerY);
        
        // 绘制背景
        drawBackground(g2d);
        
        // 绘制胜利文本
        drawVictoryText(g2d);
        
        // 绘制装饰效果
        drawDecorations(g2d);
        
        // 恢复变换
        g2d.setTransform(originalTransform);
        g2d.dispose();
    }
    
    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g2d) {
        // 绘制半透明背景
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // 绘制胜利背景框
        int boxWidth = 400;
        int boxHeight = 200;
        int x = (getWidth() - boxWidth) / 2;
        int y = (getHeight() - boxHeight) / 2;
        
        // 渐变背景
        Color startColor, endColor;
        if (gameState.getWinner() == PieceColor.WHITE) {
            startColor = new Color(255, 255, 255, 200);
            endColor = new Color(240, 240, 240, 150);
        } else if (gameState.getWinner() == PieceColor.BLACK) {
            startColor = new Color(50, 50, 50, 200);
            endColor = new Color(30, 30, 30, 150);
        } else {
            startColor = new Color(100, 100, 100, 200);
            endColor = new Color(80, 80, 80, 150);
        }
        
        GradientPaint gradient = new GradientPaint(x, y, startColor, x + boxWidth, y + boxHeight, endColor);
        g2d.setPaint(gradient);
        g2d.fillRoundRect(x, y, boxWidth, boxHeight, 20, 20);
        
        // 绘制边框
        g2d.setColor(new Color(255, 215, 0, 180)); // 金色边框
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(x, y, boxWidth, boxHeight, 20, 20);
    }
    
    /**
     * 绘制胜利文本
     */
    private void drawVictoryText(Graphics2D g2d) {
        String mainText = getMainText();
        String subText = getSubText();
        
        // 主文本
        Font mainFont = new Font("微软雅黑", Font.BOLD, 48);
        g2d.setFont(mainFont);
        FontMetrics mainFm = g2d.getFontMetrics();
        
        int mainTextX = (getWidth() - mainFm.stringWidth(mainText)) / 2;
        int mainTextY = getHeight() / 2 - 20;
        
        // 文本阴影
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(mainText, mainTextX + 2, mainTextY + 2);
        
        // 主文本颜色
        if (gameState.getWinner() == PieceColor.WHITE) {
            g2d.setColor(new Color(255, 255, 255));
        } else if (gameState.getWinner() == PieceColor.BLACK) {
            g2d.setColor(new Color(50, 50, 50));
        } else {
            g2d.setColor(new Color(100, 100, 100));
        }
        g2d.drawString(mainText, mainTextX, mainTextY);
        
        // 副文本
        Font subFont = new Font("微软雅黑", Font.PLAIN, 24);
        g2d.setFont(subFont);
        FontMetrics subFm = g2d.getFontMetrics();
        
        int subTextX = (getWidth() - subFm.stringWidth(subText)) / 2;
        int subTextY = mainTextY + 50;
        
        g2d.setColor(new Color(255, 215, 0)); // 金色
        g2d.drawString(subText, subTextX, subTextY);
    }
    
    /**
     * 绘制装饰效果
     */
    private void drawDecorations(Graphics2D g2d) {
        // 绘制星星装饰
        g2d.setColor(new Color(255, 215, 0, 150));
        
        for (int i = 0; i < 8; i++) {
            double angle = (animationFrame * 2 + i * 45) * Math.PI / 180;
            int starX = (int) (getWidth() / 2 + Math.cos(angle) * 150);
            int starY = (int) (getHeight() / 2 + Math.sin(angle) * 100);
            
            drawStar(g2d, starX, starY, 8, 4);
        }
    }
    
    /**
     * 绘制星星
     */
    private void drawStar(Graphics2D g2d, int centerX, int centerY, int outerRadius, int innerRadius) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5;
            int radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = (int) (centerX + Math.cos(angle) * radius);
            yPoints[i] = (int) (centerY + Math.sin(angle) * radius);
        }
        
        g2d.fillPolygon(xPoints, yPoints, 10);
    }
    
    /**
     * 获取主要文本
     */
    private String getMainText() {
        if (gameState == null) {
            return "游戏结束";
        }
        
        switch (gameState) {
            case WHITE_WIN:
            case WHITE_CHECKMATE:
                return "白方获胜！";
            case BLACK_WIN:
            case BLACK_CHECKMATE:
                return "黑方获胜！";
            case DRAW:
                return "平局！";
            case STALEMATE:
                return "僵局！";
            default:
                return "游戏结束";
        }
    }
    
    /**
     * 获取副文本
     */
    private String getSubText() {
        if (gameState == null) {
            return "";
        }
        
        switch (gameState) {
            case WHITE_CHECKMATE:
                return "黑王被将死";
            case BLACK_CHECKMATE:
                return "白王被将死";
            case STALEMATE:
                return "无法移动但未被将军";
            case DRAW:
                return "双方同意和棋";
            default:
                return "恭喜获胜方！";
        }
    }
    
    /**
     * 判断动画是否正在运行
     */
    public boolean isAnimationRunning() {
        return animationRunning;
    }
}