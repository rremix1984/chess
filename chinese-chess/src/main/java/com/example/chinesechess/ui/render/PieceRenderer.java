package com.example.chinesechess.ui.render;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders Chinese chess pieces with realistic wood style.
 * Results are cached based on piece properties to avoid recomputation.
 */
public final class PieceRenderer {
    public enum Side { RED, BLACK }
    public enum PieceType {
        CHE, MA, PAO, SHUAI, JIANG, SHI, XIANG, BING, ZU
    }

    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();
    private static Image WOOD_TEX;

    static {
        // Optional wood texture loading
        try {
            URL url = PieceRenderer.class.getResource("/assets/wood/wood_a.jpg");
            if (url != null) {
                WOOD_TEX = new ImageIcon(url).getImage();
            }
        } catch (Exception ignore) {}
    }

    private PieceRenderer() {}

    /**
     * Returns cached image or renders a new one when missing.
     */
    public static BufferedImage render(PieceType type, Side side, int diameterPx, float uiScale) {
        String key = type + "|" + side + "|" + diameterPx + "|" + uiScale + "|" + (WOOD_TEX != null);
        return CACHE.computeIfAbsent(key, k -> drawOne(type, side, diameterPx, uiScale));
    }

    private static BufferedImage drawOne(PieceType type, Side side, int d, float uiScale) {
        int margin = Math.max(2, Math.round(d * 0.04f));
        BufferedImage img = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = d / 2;
        int cy = d / 2;
        int rOuter = d / 2 - margin;
        int rimW = Math.max(3, Math.round(d * 0.08f));
        int rInner = rOuter - rimW;

        // 1) drop shadow
        paintDropShadow(g, cx, cy, rOuter, d);

        // 2) rim
        paintRim(g, cx, cy, rOuter, rimW);

        // 3) face
        paintFace(g, cx, cy, rInner);

        // 4) specular highlight
        paintSpecular(g, cx, cy, rInner);

        // 5) glyph
        String text = toGlyph(type, side);
        paintGlyph(g, text, side, cx, cy, rInner);

        g.dispose();
        return img;
    }

    private static void paintDropShadow(Graphics2D g, int cx, int cy, int r, int d) {
        Composite old = g.getComposite();
        float alpha = 0.28f;
        int w = Math.round(r * 1.4f);
        int h = Math.round(r * 0.35f);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(new Color(0, 0, 0, 180));
        g.fillOval(cx - w / 2, cy + r / 2, w, h);
        g.setComposite(old);
    }

    private static void paintRim(Graphics2D g, int cx, int cy, int rOuter, int rimW) {
        float[] dist = {0f, 0.5f, 1f};
        Color[] cols = {
                new Color(110, 85, 60),
                new Color(150, 120, 90),
                new Color(190, 160, 120)
        };
        RadialGradientPaint rg = new RadialGradientPaint(
                new Point2D.Float(cx, cy),
                rOuter,
                dist, cols,
                MultipleGradientPaint.CycleMethod.NO_CYCLE);
        Shape ring = new Arc2D.Double(cx - rOuter, cy - rOuter, rOuter * 2, rOuter * 2, 0, 360, Arc2D.CHORD);
        g.setPaint(rg);
        g.fill(ring);

        int rInnerEdge = rOuter - rimW;
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.Clear);
        g.fill(new Ellipse2D.Double(cx - rInnerEdge, cy - rInnerEdge, rInnerEdge * 2, rInnerEdge * 2));
        g.setComposite(old);
    }

    private static void paintFace(Graphics2D g, int cx, int cy, int rInner) {
        Shape face = new Ellipse2D.Double(cx - rInner, cy - rInner, rInner * 2, rInner * 2);
        if (WOOD_TEX != null) {
            TexturePaint tp = new TexturePaint(toBuffered(WOOD_TEX),
                    new Rectangle(cx - rInner, cy - rInner, Math.max(8, rInner), Math.max(8, rInner)));
            g.setPaint(tp);
            g.fill(face);
            Paint old = g.getPaint();
            RadialGradientPaint vignette = new RadialGradientPaint(
                    new Point2D.Float(cx, cy), rInner,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 255, 255, 0), new Color(0, 0, 0, 40)});
            g.setPaint(vignette);
            g.fill(face);
            g.setPaint(old);
        } else {
            LinearGradientPaint lg = new LinearGradientPaint(
                    cx - rInner, cy - rInner, cx + rInner, cy + rInner,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{new Color(205, 170, 125), new Color(185, 150, 105), new Color(215, 180, 135)});
            g.setPaint(lg);
            g.fill(face);
            RadialGradientPaint soft = new RadialGradientPaint(
                    new Point2D.Float(cx, cy), rInner,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 255, 255, 40), new Color(0, 0, 0, 30)});
            g.setPaint(soft);
            g.fill(face);
        }
    }

    private static void paintSpecular(Graphics2D g, int cx, int cy, int rInner) {
        int ox = (int) (rInner * 0.35);
        int oy = (int) (rInner * 0.35);
        RadialGradientPaint gloss = new RadialGradientPaint(
                new Point2D.Float(cx - ox, cy - oy), (float) (rInner * 0.9),
                new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(255, 255, 255, 130), new Color(255, 255, 255, 30), new Color(255, 255, 255, 0)});
        g.setPaint(gloss);
        g.fill(new Ellipse2D.Double(cx - rInner, cy - rInner, rInner * 2, rInner * 2));
    }

    private static String toGlyph(PieceType t, Side s) {
        return switch (t) {
            case CHE -> "車";
            case MA -> "馬";
            case PAO -> "炮";
            case SHUAI -> "帥";
            case JIANG -> "將";
            case SHI -> (s == Side.RED ? "仕" : "士");
            case XIANG -> (s == Side.RED ? "相" : "象");
            case BING -> "兵";
            case ZU -> "卒";
        };
    }

    private static void paintGlyph(Graphics2D g, String text, Side side, int cx, int cy, int rInner) {
        Color main = (side == Side.RED) ? new Color(216, 58, 58) : new Color(34, 34, 34);

        int fontSize = Math.max((int) (rInner * 1.2), 24);
        Font font = g.getFont().deriveFont(Font.BOLD, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        int ty = cy + (fm.getAscent() - fm.getDescent()) / 2;

        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(text, tx + 2, ty + 2);

        g.setColor(Color.WHITE);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                g.drawString(text, tx + dx, ty + dy);
            }
        }

        g.setColor(main);
        g.drawString(text, tx, ty);
    }

    private static BufferedImage toBuffered(Image img) {
        BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return bi;
    }
}
