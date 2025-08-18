package com.example.chinesechess.ui.render;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 中国象棋棋子渲染器（木质立体风格）
 * - 外沿立体（外深内浅），内盘木纹，柔和高光，贴地阴影，文字浮雕
 * - 尺寸从直径 d 推导，高 DPI 友好
 * - 可选木纹贴图；无贴图时程序化木纹回退
 * - 渲染结果带缓存；异常返回占位图，不抛错
 */
public final class PieceRenderer {

    // ===== 枚举 =====
    public enum Side { RED, BLACK }
    public enum PieceType { CHE, MA, PAO, SHUAI, JIANG, SHI, XIANG, BING, ZU }

    // ===== 色板（可微调）=====
    private static final Color WOOD_DEEP   = new Color(0x8F6B4B);
    private static final Color WOOD_MID    = new Color(0xB99367);
    private static final Color WOOD_LIGHT  = new Color(0xD7B487);
    private static final Color FACE_L0     = new Color(0xEAD1AD);
    private static final Color FACE_L1     = new Color(0xD9B98F);
    private static final Color RED_GLYPH   = new Color(0xD83A3A);
    private static final Color BLACK_GLYPH = new Color(0x222222);

    // 可选：木纹贴图（若存在优先使用）
    private static Image WOOD_TEX;

    // 结果缓存：type|side|diameter|uiScale|hasTexture
    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

    static {
        // 尝试从资源加载木纹贴图（可选）
        try {
            URL url = PieceRenderer.class.getResource("/assets/wood/wood_a.jpg");
            if (url != null) WOOD_TEX = new ImageIcon(url).getImage();
        } catch (Throwable ignore) {}
    }

    private PieceRenderer() {}

    /** 外部可注入自定义木纹贴图（可选）。 */
    public static void setWoodTexture(Image img) { WOOD_TEX = img; CACHE.clear(); }

    /** 渲染一枚棋子（带缓存）。 */
    public static BufferedImage render(PieceType type, Side side, int diameterPx, float uiScale) {
        String key = type + "|" + side + "|" + diameterPx + "|" + uiScale + "|" + (WOOD_TEX != null);
        return CACHE.computeIfAbsent(key, k -> {
            try {
                return drawOne(type, side, diameterPx, uiScale);
            } catch (Throwable t) {
                System.err.println("[PieceRenderer] render failed: " + t);
                return placeholder(type, side, diameterPx);
            }
        });
    }

    // ====== 核心渲染 ======
    static BufferedImage drawOne(PieceType type, Side side, int d, float uiScale) {
        int margin = Math.max(2, Math.round(d * 0.04f));
        int cx = d / 2, cy = d / 2;
        int rOuter = d / 2 - margin;
        int rimW   = Math.max(3, Math.round(d * 0.10f));
        int rInner = rOuter - rimW;

        BufferedImage img = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        enableAA(g);

        // 1) 贴地阴影（先绘）
        paintDropShadow(g, cx, cy, rOuter, d);
        // 2) 立体外沿（环）
        paintRim(g, cx, cy, rOuter, rimW);
        // 3) 内盘 + 木纹/回退
        paintFace(g, cx, cy, rInner);
        // 4) 错位深色内圈
        paintInnerRimOffset(g, cx, cy, rInner, d);
        // 5) 内盘暗角
        paintVignette(g, cx, cy, rInner);
        // 6) 顶部柔和高光
        paintSpecular(g, cx, cy, rInner);
        // 7) 文字浮雕
        paintGlyph(g, toGlyph(type, side), side, cx, cy, rInner);

        g.dispose();
        return img;
    }

    // ====== 各绘制步骤 ======

    private static void paintDropShadow(Graphics2D g, int cx, int cy, int rOuter, int d) {
        int w = Math.round(rOuter * 1.4f);
        int h = Math.round(rOuter * 0.35f);
        float offset = d * 0.05f;
        int x = Math.round(cx - w / 2f + offset);
        int y = Math.round(cy - h / 2f + rOuter * 0.6f + offset);

        BufferedImage shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics(); enableAA(sg);
        int alpha = Math.min(200, 80 + d / 2);
        RadialGradientPaint rg = new RadialGradientPaint(
                new Point2D.Float(w / 2f, h / 2f), Math.max(w, h) / 2f,
                new float[]{0f, 1f}, new Color[]{new Color(0, 0, 0, alpha), new Color(0, 0, 0, 0)});
        sg.setPaint(rg);
        sg.fill(new Ellipse2D.Float(0, 0, w, h));
        sg.dispose();

        BufferedImage blurred = gaussianBlur(shadow, Math.max(2f, d / 60f));
        g.drawImage(blurred, x, y, null);

        // 额外的月牙形阴影以增强立体感
        g.setColor(new Color(0, 0, 0, 60));
        int crescentW = Math.round(rOuter * 1.2f);
        int crescentH = Math.round(rOuter * 0.6f);
        int crescentX = cx - crescentW / 2;
        int crescentY = cy + rOuter - crescentH / 2;
        g.fillArc(crescentX, crescentY, crescentW, crescentH, 0, 180);
    }

    private static void paintRim(Graphics2D g, int cx, int cy, int rOuter, int rimW) {
        float[] dist = {0f, 0.6f, 1f};
        Color[] cols = {WOOD_DEEP, WOOD_MID, WOOD_LIGHT};
        RadialGradientPaint rg = new RadialGradientPaint(
                new Point2D.Float(cx, cy), rOuter, dist, cols);
        Shape outer = new Ellipse2D.Float(cx - rOuter, cy - rOuter, rOuter * 2f, rOuter * 2f);

        Composite bak = g.getComposite();
        Paint old = g.getPaint();

        g.setPaint(rg);
        g.fill(outer);

        // 挖空形成环
        int rInnerEdge = rOuter - rimW;
        g.setComposite(AlphaComposite.Clear);
        g.fill(new Ellipse2D.Float(cx - rInnerEdge, cy - rInnerEdge, rInnerEdge * 2f, rInnerEdge * 2f));

        g.setComposite(bak);
        g.setPaint(old);
    }

    private static void paintFace(Graphics2D g, int cx, int cy, int rInner) {
        Shape face = new Ellipse2D.Float(cx - rInner, cy - rInner, rInner * 2f, rInner * 2f);
        Paint old = g.getPaint();

        if (WOOD_TEX != null) {
            TexturePaint tp = new TexturePaint(
                    toBuffered(WOOD_TEX),
                    new Rectangle(cx - rInner, cy - rInner, Math.max(12, rInner), Math.max(12, rInner)));
            g.setPaint(tp);
            g.fill(face);
        } else {
            // 线性渐变：浅→中→浅
            LinearGradientPaint lg = new LinearGradientPaint(
                    cx - rInner, cy - rInner, cx + rInner, cy + rInner,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{FACE_L0, FACE_L1, FACE_L0});
            g.setPaint(lg);
            g.fill(face);
            // 程序化细木纹
            drawFineWoodLines(g, cx, cy, rInner, face);
        }

        g.setPaint(old);
    }

    private static void paintVignette(Graphics2D g, int cx, int cy, int rInner) {
        Shape face = new Ellipse2D.Float(cx - rInner, cy - rInner, rInner * 2f, rInner * 2f);
        Paint old = g.getPaint();
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Float(cx, cy), rInner,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 50)});
        g.setPaint(vignette);
        g.fill(face);
        g.setPaint(old);
    }

    // 错位深色内圈：圆心向右下偏移制造厚度错觉
    private static void paintInnerRimOffset(Graphics2D g, int cx, int cy, int rInner, int d) {
        int offset = Math.max(2, Math.round(d * 0.04f)); // 错位量
        int ringR = Math.max(1, Math.round(d * 0.02f));  // 圆环粗细

        int ox = cx + offset;
        int oy = cy + offset;

        Stroke bak = g.getStroke();
        Paint old = g.getPaint();

        g.setStroke(new BasicStroke(ringR, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setPaint(new Color(0x7A, 0x5A, 0x3B));
        g.draw(new Ellipse2D.Float(ox - rInner, oy - rInner, rInner * 2f, rInner * 2f));

        g.setStroke(bak);
        g.setPaint(old);
    }

    private static void paintSpecular(Graphics2D g, int cx, int cy, int rInner) {
        int ox = Math.round(rInner * 0.35f), oy = Math.round(rInner * 0.35f);
        RadialGradientPaint gloss = new RadialGradientPaint(
                new Point2D.Float(cx - ox, cy - oy), rInner * 0.9f,
                new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(255, 255, 255, 115),
                            new Color(255, 255, 255, 35),
                            new Color(255, 255, 255, 0)});
        Shape face = new Ellipse2D.Float(cx - rInner, cy - rInner, rInner * 2f, rInner * 2f);
        Paint old = g.getPaint();
        Shape clipBak = g.getClip();

        g.setClip(face);
        g.setPaint(gloss);
        g.fill(face);

        g.setClip(clipBak);
        g.setPaint(old);
    }

    private static void paintGlyph(Graphics2D g, String text, Side side, int cx, int cy, int rInner) {
        Color main = (side == Side.RED) ? RED_GLYPH : BLACK_GLYPH;
        int fontSize = Math.max(24, Math.round(rInner * 1.15f));
        Font font = g.getFont().deriveFont(Font.BOLD, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        int ty = cy + (fm.getAscent() - fm.getDescent()) / 2;

        // 阴影
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(text, tx + 2, ty + 2);
        // 白描边（八方向 1px）
        g.setColor(Color.WHITE);
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
            if (dx == 0 && dy == 0) continue;
            g.drawString(text, tx + dx, ty + dy);
        }
        // 主体
        g.setColor(main);
        g.drawString(text, tx, ty);
    }

    // ===== 小工具 =====

    private static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    /** 简易程序化木纹：低强度横线，破坏镜面感即可。 */
    private static void drawFineWoodLines(Graphics2D g, int cx, int cy, int rInner, Shape clip) {
        Stroke bak = g.getStroke();
        Shape oldClip = g.getClip();
        g.setClip(clip);
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        int top = cy - rInner, bottom = cy + rInner, left = cx - rInner, right = cx + rInner;
        int step = Math.max(3, rInner / 8);
        for (int y = top + step / 2; y < bottom; y += step) {
            g.setColor(new Color(120, 90, 60, 18));
            g.drawLine(left, y, right, y);
        }
        g.setClip(oldClip);
        g.setStroke(bak);
    }

    /** 高斯模糊：分离卷积实现（横向 + 纵向） */
    private static BufferedImage gaussianBlur(BufferedImage src, float radius) {
        if (radius < 1f) return src;

        int size = (int) Math.ceil(radius * 3) * 2 + 1; // 6σ+1
        float[] kernelData = new float[size];
        float sigma = radius;
        float sum = 0f;
        int mid = size / 2;

        for (int i = 0; i < size; i++) {
            float x = i - mid;
            kernelData[i] = (float) Math.exp(-(x * x) / (2 * sigma * sigma));
            sum += kernelData[i];
        }
        for (int i = 0; i < size; i++) kernelData[i] /= sum;

        Kernel kernelH = new Kernel(size, 1, kernelData);
        ConvolveOp opH = new ConvolveOp(kernelH, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage tmp = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        opH.filter(src, tmp);

        Kernel kernelV = new Kernel(1, size, kernelData);
        ConvolveOp opV = new ConvolveOp(kernelV, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        opV.filter(tmp, dst);

        return dst;
    }

    private static BufferedImage toBuffered(Image img) {
        if (img instanceof BufferedImage) return (BufferedImage) img;
        BufferedImage out = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        enableAA(g);
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return out;
    }

    private static String toGlyph(PieceType t, Side s) {
        switch (t) {
            case CHE:
                return "車";
            case MA:
                return "馬";
            case PAO:
                return "炮";
            case SHUAI:
                return "帥";
            case JIANG:
                return "將";
            case SHI:
                return s == Side.RED ? "仕" : "士";
            case XIANG:
                return s == Side.RED ? "相" : "象";
            case BING:
                return "兵";
            case ZU:
                return "卒";
            default:
                throw new IllegalArgumentException("Unknown piece type: " + t);
        }
    }

    private static BufferedImage placeholder(PieceType type, Side side, int d) {
        BufferedImage img = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        enableAA(g);
        g.setColor(new Color(230, 230, 230));
        g.fillOval(2, 2, d - 4, d - 4);
        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(Math.max(2, d * 0.04f)));
        g.drawOval(2, 2, d - 4, d - 4);
        g.setColor(side == Side.RED ? RED_GLYPH : BLACK_GLYPH);
        String text = toGlyph(type, side);
        int fs = Math.max(18, (int) (d * 0.45));
        g.setFont(g.getFont().deriveFont(Font.BOLD, fs));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (d - fm.stringWidth(text)) / 2, (d + fm.getAscent() - fm.getDescent()) / 2);
        g.dispose();
        return img;
    }

    // ===== 便捷 Demo：导出不同尺寸的棋子 PNG（可删）=====
    public static void main(String[] args) throws IOException {
        int[] sizes = {64, 96, 128, 192, 256};
        for (int d : sizes) {
            BufferedImage red = render(PieceType.SHUAI, Side.RED, d, 1.0f);
            BufferedImage blk = render(PieceType.JIANG, Side.BLACK, d, 1.0f);
            File f1 = new File("piece_RED_" + d + ".png");
            File f2 = new File("piece_BLACK_" + d + ".png");
            ImageIO.write(red, "PNG", f1);
            ImageIO.write(blk, "PNG", f2);
            System.out.println("Exported: " + f1.getAbsolutePath() + " , " + f2.getAbsolutePath());
        }
    }
}

