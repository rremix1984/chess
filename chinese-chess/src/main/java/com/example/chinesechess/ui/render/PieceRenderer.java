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

    // base colours for wood texture and glyphs
    private static final Color WOOD_LIGHT = new Color(0xE6C8A0);
    private static final Color WOOD_MID   = new Color(0xD5B58B);
    private static final Color WOOD_DARK  = new Color(0x9C7A54);
    private static final Color RED_TEXT   = new Color(0xD83A3A);
    private static final Color BLACK_TEXT = new Color(0x222222);

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
        int margin = Math.round(scaledD * 0.04f);
        BufferedImage img = new BufferedImage(scaledD, scaledD, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(img, 0, 0, d, d, null);
            g2.dispose();
            return scaled;
        }
        return img;
    }

    private static void paintDropShadow(Graphics2D g, int cx, int cy, int r, int d) {
        int w = Math.round(r * 1.4f);
        int h = Math.round(r * 0.35f);
        int offsetY = Math.round(r * 0.5f);
        int blur = Math.max(1, Math.round(d / 60f));

        BufferedImage shadow = new BufferedImage(w + blur * 2, h + blur * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics();
        sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        sg.setColor(new Color(0, 0, 0, 180));
        sg.fillOval(blur, blur, w, h);
        sg.dispose();

        BufferedImage blurred = applyGaussianBlur(shadow, blur);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        g.drawImage(blurred, cx - w / 2 - blur, cy + offsetY - blur, null);
        g.setComposite(old);
    }

    private static void paintRim(Graphics2D g, int cx, int cy, int rOuter, int rimW) {
        int rInnerEdge = rOuter - rimW;
        float innerRatio = rInnerEdge / (float) rOuter;
        RadialGradientPaint rg = new RadialGradientPaint(
                new Point2D.Float(cx, cy),
                rOuter,
                new float[]{0f, innerRatio, 1f},
                new Color[]{WOOD_LIGHT, WOOD_MID, WOOD_DARK});
        Shape ring = new Arc2D.Double(cx - rOuter, cy - rOuter, rOuter * 2, rOuter * 2, 0, 360, Arc2D.CHORD);
        g.setPaint(rg);
        g.fill(ring);

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
                    new Color[]{WOOD_LIGHT, WOOD_MID, WOOD_LIGHT});
            g.setPaint(lg);
            g.fill(face);
            RadialGradientPaint soft = new RadialGradientPaint(
                    new Point2D.Float(cx, cy), rInner,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 255, 255, 40), new Color(0, 0, 0, 30)});
            g.setPaint(soft);
            g.fill(face);
            // subtle wood grain lines
            g.setColor(new Color(WOOD_DARK.getRed(), WOOD_DARK.getGreen(), WOOD_DARK.getBlue(), 40));
            Stroke oldS = g.getStroke();
            g.setStroke(new BasicStroke(Math.max(1f, rInner / 40f)));
            for (int i = -rInner; i < rInner; i += Math.max(3, rInner / 8)) {
                int y = cy + i;
                g.drawLine(cx - rInner, y, cx + rInner, y);
            }
            g.setStroke(oldS);
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
        Color main = (side == Side.RED) ? RED_TEXT : BLACK_TEXT;

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

    /**
     * Applies a simple Gaussian blur to the given image.
     */
    private static BufferedImage applyGaussianBlur(BufferedImage img, int radius) {
        if (radius <= 0) return img;
        int size = radius * 2 + 1;
        float sigma = radius / 3f;
        float[] data = new float[size * size];
        float sum = 0f;
        int idx = 0;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                float value = (float) Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                data[idx++] = value;
                sum += value;
            }
        }
        for (int i = 0; i < data.length; i++) {
            data[i] /= sum;
        }
        Kernel kernel = new Kernel(size, size, data);
        BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
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
