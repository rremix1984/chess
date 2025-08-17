package com.example.chinesechess.ui.render;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(PieceRenderer.class.getName());

    // ===== 色板 =====
    private static final Color WOOD_DEEP   = new Color(0x8F6B4B);
    private static final Color WOOD_MID    = new Color(0xB99367);
    private static final Color WOOD_LIGHT  = new Color(0xD7B487);
    private static final Color FACE_L0     = new Color(0xEAD1AD);
    private static final Color FACE_L1     = new Color(0xD9B98F);
    private static final Color RED_GLYPH   = new Color(0xD83A3A);
    private static final Color BLACK_GLYPH = new Color(0x222222);

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

    // 高 DPI 抗锯齿统一开启
    private static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /**
     * Returns cached image or renders a new one when missing.
     */
    public static BufferedImage render(PieceType type, Side side, int diameterPx, float uiScale) {
        String key = type + "|" + side + "|" + diameterPx + "|" + uiScale + "|" + (WOOD_TEX != null);
        return CACHE.computeIfAbsent(key, k -> {
            try {
                return drawOne(type, side, diameterPx, uiScale);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to render piece " + k, ex);
                return placeholder(diameterPx);
            }
        });
    }

    private static BufferedImage drawOne(PieceType type, Side side, int d, float uiScale) {
        int scaledD = Math.max(1, Math.round(d * uiScale));
        int margin = Math.max(2, Math.round(scaledD * 0.04f));
        BufferedImage img = new BufferedImage(scaledD, scaledD, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        enableAA(g);

        int cx = scaledD / 2;
        int cy = scaledD / 2;
        int rOuter = scaledD / 2 - margin;
        int rimW = Math.round(Math.max(3f, scaledD * 0.08f));
        int rInner = rOuter - rimW;

        // 1) drop shadow
        paintDropShadow(g, cx, cy, rOuter, scaledD);

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

        if (uiScale != 1f) {
            BufferedImage scaled = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            enableAA(g2);
            g2.drawImage(img, 0, 0, d, d, null);
            g2.dispose();
            return scaled;
        }
        return img;
    }

    // 半透明椭圆 + 模糊，位置略低；让棋子“贴地”
    private static void paintDropShadow(Graphics2D g, int cx, int cy, int rOuter, int d) {
        int w = Math.round(rOuter * 1.4f);
        int h = Math.round(rOuter * 0.35f);
        int y = cy + rOuter / 2;
        BufferedImage shadow = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics();
        enableAA(sg);
        sg.setColor(new Color(0, 0, 0, 180));
        sg.fillOval(cx - w / 2, y, w, h);
        sg.dispose();
        float radius = Math.max(2, d / 60f);
        g.drawImage(blur(shadow, radius), 0, 0, null);
    }

    // 外深内浅的径向渐变，再挖空形成环
    private static void paintRim(Graphics2D g, int cx, int cy, int rOuter, int rimW) {
        float[] dist = {0f, 0.5f, 1f};
        Color[] cols = {WOOD_DEEP, WOOD_MID, WOOD_LIGHT};
        RadialGradientPaint rg = new RadialGradientPaint(new Point2D.Float(cx, cy), rOuter, dist, cols);
        Shape outer = new Ellipse2D.Float(cx - rOuter, cy - rOuter, rOuter * 2f, rOuter * 2f);
        Composite bak = g.getComposite();
        g.setPaint(rg);
        g.fill(outer);
        int rInnerEdge = rOuter - rimW;
        g.setComposite(AlphaComposite.Clear);
        g.fill(new Ellipse2D.Float(cx - rInnerEdge, cy - rInnerEdge, rInnerEdge * 2f, rInnerEdge * 2f));
        g.setComposite(bak);
    }

    // 内盘：木纹贴图优先；无贴图时程序化木纹 + 暗角
    private static void paintFace(Graphics2D g, int cx, int cy, int rInner) {
        Shape face = new Ellipse2D.Float(cx - rInner, cy - rInner, rInner * 2f, rInner * 2f);
        Paint bak = g.getPaint();
        if (WOOD_TEX != null) {
            TexturePaint tp = new TexturePaint(toBuffered(WOOD_TEX),
                    new Rectangle(cx - rInner, cy - rInner, Math.max(12, rInner), Math.max(12, rInner)));
            g.setPaint(tp);
            g.fill(face);
        } else {
            LinearGradientPaint lg = new LinearGradientPaint(
                    cx - rInner, cy - rInner, cx + rInner, cy + rInner,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{FACE_L0, FACE_L1, FACE_L0});
            g.setPaint(lg);
            g.fill(face);
            drawFineWoodLines(g, cx, cy, rInner, face);
            overlayNoise(g, cx, cy, rInner, face);
        }
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Float(cx, cy), rInner,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 40)});
        g.setPaint(vignette);
        g.fill(face);
        g.setPaint(bak);
    }

    // 顶部柔和高光：中心偏左上，透明度随半径衰减
    private static void paintSpecular(Graphics2D g, int cx, int cy, int rInner) {
        int ox = Math.round(rInner * 0.35f);
        int oy = Math.round(rInner * 0.35f);
        RadialGradientPaint gloss = new RadialGradientPaint(
                new Point2D.Float(cx - ox, cy - oy), rInner * 0.9f,
                new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(255, 255, 255, 115), new Color(255, 255, 255, 35), new Color(255, 255, 255, 0)});
        Shape face = new Ellipse2D.Float(cx - rInner, cy - rInner, rInner * 2f, rInner * 2f);
        Paint bak = g.getPaint();
        g.setPaint(gloss);
        g.fill(face);
        g.setPaint(bak);
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
        Color main = (side == Side.RED) ? RED_GLYPH : BLACK_GLYPH;

        int fontSize = Math.round(rInner * 1.2f);
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

    // === 小工具：程序化木纹线 + 噪声 + 简易模糊 ===
    private static void drawFineWoodLines(Graphics2D g, int cx, int cy, int rInner, Shape clip) {
        Stroke bakS = g.getStroke();
        Shape bakClip = g.getClip();
        g.setClip(clip);
        g.setStroke(new BasicStroke(1f));
        int top = cy - rInner, bottom = cy + rInner;
        int left = cx - rInner, right = cx + rInner;
        int step = Math.max(3, rInner / 8);
        for (int y = top + step / 2; y < bottom; y += step) {
            g.setColor(new Color(120, 90, 60, 18));
            g.drawLine(left, y, right, y);
        }
        g.setClip(bakClip);
        g.setStroke(bakS);
    }

    private static void overlayNoise(Graphics2D g, int cx, int cy, int rInner, Shape clip) {
        int size = rInner * 2;
        BufferedImage noise = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        java.util.Random rand = new java.util.Random();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int gray = 120 + rand.nextInt(40);
                int alpha = 8 + rand.nextInt(11); // 8-18
                int rgb = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                noise.setRGB(x, y, rgb);
            }
        }
        noise = blur(noise, 1f);
        Shape bak = g.getClip();
        g.setClip(clip);
        g.drawImage(noise, cx - rInner, cy - rInner, null);
        g.setClip(bak);
    }

    /** 非严格的快速模糊（高斯核） */
    private static BufferedImage blur(BufferedImage img, float radius) {
        int r = Math.max(1, Math.round(radius));
        int size = r * 2 + 1;
        float sigma = r / 3f;
        float[] data = new float[size * size];
        float sum = 0f;
        int idx = 0;
        for (int y = -r; y <= r; y++) {
            for (int x = -r; x <= r; x++) {
                float value = (float) Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                data[idx++] = value;
                sum += value;
            }
        }
        for (int i = 0; i < data.length; i++) {
            data[i] /= sum;
        }
        Kernel kernel = new Kernel(size, size, data);
        BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
        BufferedImage dst = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        op.filter(img, dst);
        return dst;
    }

    /**
     * Generates a simple placeholder piece when rendering fails.
     */
    private static BufferedImage placeholder(int d) {
        BufferedImage img = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(220, 220, 220));
        g.fillOval(0, 0, d, d);
        g.setColor(Color.GRAY);
        g.drawOval(0, 0, d - 1, d - 1);
        g.dispose();
        return img;
    }
}
