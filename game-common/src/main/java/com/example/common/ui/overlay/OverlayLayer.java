package com.example.common.ui.overlay;

import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 覆盖层，可在棋盘上方绘制横幅文本和简单烟花粒子效果。
 */
public class OverlayLayer extends JComponent {
    public enum Style { VICTORY, ALERT, ALERT_BRUSH }

    private String bannerText;
    private Style bannerStyle;
    private float bannerAlpha;
    private Timer bannerTimer;

    private final List<Particle> particles = new ArrayList<>();
    private final List<Ring> rings = new ArrayList<>();
    private Timer particleTimer;
    private final Random random = new Random();
    private double viewScale = 1.0;

    /** 显示横幅文字 */
    public void showBanner(String text, Style style, int durationMs) {
        this.bannerText = text;
        this.bannerStyle = style;
        long start = System.currentTimeMillis();
        long fade = (style == Style.ALERT_BRUSH) ? 200 : 400;
        long end = start + durationMs;
        if (bannerTimer != null) {
            bannerTimer.stop();
        }
        bannerTimer = new Timer(16, e -> {
            long now = System.currentTimeMillis();
            long elapsed = now - start;
            if (elapsed < fade) {
                bannerAlpha = elapsed / (float) fade;
            } else if (now > end - fade) {
                bannerAlpha = Math.max(0f, (end - now) / (float) fade);
            } else {
                bannerAlpha = 1f;
            }
            if (now >= end) {
                bannerTimer.stop();
            }
            repaint();
        });
        bannerTimer.start();
    }

    /** 播放简单烟花粒子效果 */
    public void playFireworks(int durationMs) {
        long start = System.currentTimeMillis();
        if (particleTimer != null) {
            particleTimer.stop();
        }
        particleTimer = new Timer(16, e -> {
            long now = System.currentTimeMillis();
            if (now - start > durationMs) {
                particleTimer.stop();
            }
            // 更新粒子
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                p.x += p.vx;
                p.y += p.vy;
                p.vy += 0.1f;
                if (--p.life <= 0) {
                    it.remove();
                }
            }
            // 生成新粒子
            for (int i = 0; i < 3; i++) {
                Particle p = new Particle();
                p.x = random.nextInt(Math.max(1, getWidth()));
                p.y = random.nextInt(Math.max(1, getHeight() / 2));
                double angle = random.nextDouble() * Math.PI * 2;
                double speed = 2 + random.nextDouble() * 3;
                p.vx = (float) (Math.cos(angle) * speed);
                p.vy = (float) (Math.sin(angle) * speed);
                p.life = 60 + random.nextInt(40);
                p.color = Color.getHSBColor(random.nextFloat(), 1f, 1f);
                particles.add(p);
            }
            repaint();
        });
        particleTimer.start();
    }

    /** 播放冲击波环效果 */
    public void playImpactRing(int x, int y) {
        rings.add(new Ring(x, y));
        if (impactTimer == null) {
            impactTimer = new Timer(16, e -> {
                Iterator<Ring> it = rings.iterator();
                while (it.hasNext()) {
                    Ring r = it.next();
                    r.age += 16;
                    if (r.age > 200) {
                        it.remove();
                    }
                }
                if (rings.isEmpty()) {
                    impactTimer.stop();
                    impactTimer = null;
                }
                repaint();
            });
            impactTimer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform old = g2d.getTransform();
        g2d.scale(viewScale, viewScale);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制粒子
        for (Particle p : particles) {
            g2d.setColor(p.color);
            g2d.fillOval((int) p.x, (int) p.y, 4, 4);
        }

        // 绘制横幅
        if (bannerText != null && bannerAlpha > 0f) {
            Composite old = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bannerAlpha));
            if (bannerStyle == Style.ALERT_BRUSH) {
                Font base = getFont().deriveFont(Font.BOLD, 72f);
                Font font = base;
                GlyphVector gv = font.createGlyphVector(g2d.getFontRenderContext(), bannerText);
                Shape shape = gv.getOutline();
                Rectangle bounds = shape.getBounds();
                int x = (getWidth() - bounds.width) / 2 - bounds.x;
                int y = bounds.height + 10;
                g2d.translate(x, y);
                g2d.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(Color.WHITE);
                g2d.draw(shape);
                g2d.setColor(new Color(0xC00000));
                g2d.fill(shape);
                g2d.translate(-x, -y);
            } else {
                g2d.setFont(getFont().deriveFont(Font.BOLD, 64f));
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(bannerText)) / 2;
                int y = getHeight() / 2;
                g2d.setColor(Color.RED);
                g2d.drawString(bannerText, x, y);
            }
            g2d.setComposite(old);
        }

        // 绘制冲击波环
        for (Ring r : rings) {
            float alpha = 1f - r.age / 200f;
            if (alpha <= 0f) continue;
            Composite old = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int radius = (int)(r.age * 0.4);
            g2d.setColor(new Color(255, 0, 0));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawOval(r.x - radius, r.y - radius, radius * 2, radius * 2);
            g2d.setComposite(old);
        }
        g2d.setTransform(old);
    }

    private static class Particle {
        float x, y, vx, vy;
        int life;
        Color color;
    }

    private static class Ring {
        int x, y;
        int age;
        Ring(int x, int y) { this.x = x; this.y = y; }
    }

    private Timer impactTimer;

    public void setViewScale(double viewScale) {
        this.viewScale = viewScale;
    }
}
