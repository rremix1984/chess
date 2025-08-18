package com.example.go;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * Renders a Go stone with wood texture, soft shadow, rim and specular highlight.
 */
public class GoStoneRenderer {
    private GoStoneRenderer() {}

    public static void draw(Graphics2D g, int cx, int cy, int diameter, boolean white) {
        int rOuter = diameter / 2;
        int rimW = Math.max(3, Math.round(diameter * 0.10f));
        int rInner = rOuter - rimW;
        try {
            BufferedImage img = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gg = img.createGraphics();
            enableAA(gg);
            paintDropShadow(gg, rOuter, rOuter, rOuter, diameter);
            paintRim(gg, rOuter, rOuter, rOuter, rimW);
            paintFace(gg, rOuter, rOuter, rInner);
            paintSpecular(gg, rOuter, rOuter, rInner);
            paintGlyph(gg, white ? "白" : "黑", white, rOuter, rOuter, rInner);
            gg.dispose();
            g.drawImage(img, cx - rOuter, cy - rOuter, null);
        } catch (Exception e) {
            g.setColor(white ? Color.WHITE : Color.BLACK);
            g.fillOval(cx - rOuter, cy - rOuter, diameter, diameter);
            g.setColor(Color.BLACK);
            g.drawOval(cx - rOuter, cy - rOuter, diameter, diameter);
        }
    }

    private static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static void paintDropShadow(Graphics2D g, int cx, int cy, int rOuter, int d) {
        int w = Math.round(rOuter * 1.4f);
        int h = Math.round(rOuter * 0.35f);
        int y = cy + rOuter / 2;

        BufferedImage shadow = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics();
        enableAA(sg);
        sg.setColor(new Color(0, 0, 0, 180));
        sg.fill(new Ellipse2D.Float(cx - w / 2f, y, w, h));
        sg.dispose();

        BufferedImage blurred = gaussianBlur(shadow, Math.max(2f, d / 60f));
        g.drawImage(blurred, 0, 0, null);
    }

    private static void paintRim(Graphics2D g, int cx, int cy, int rOuter, int rimW) {
        float[] dist = {0f, 0.6f, 1f};
        Color[] cols = {new Color(0x8F6B4B), new Color(0xB99367), new Color(0xD7B487)};
        RadialGradientPaint rg = new RadialGradientPaint(new Point2D.Float(cx, cy), rOuter, dist, cols);
        Shape outer = new Ellipse2D.Float(cx - rOuter, cy - rOuter, 2f * rOuter, 2f * rOuter);
        Paint old = g.getPaint();
        Composite bak = g.getComposite();
        g.setPaint(rg);
        g.fill(outer);
        int rInnerEdge = rOuter - rimW;
        g.setComposite(AlphaComposite.Clear);
        g.fill(new Ellipse2D.Float(cx - rInnerEdge, cy - rInnerEdge, 2f * rInnerEdge, 2f * rInnerEdge));
        g.setComposite(bak);
        g.setPaint(old);
    }

    private static void paintFace(Graphics2D g, int cx, int cy, int rInner) {
        Shape face = new Ellipse2D.Float(cx - rInner, cy - rInner, 2f * rInner, 2f * rInner);
        LinearGradientPaint lg = new LinearGradientPaint(
                cx - rInner, cy - rInner, cx + rInner, cy + rInner,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(0xEAD1AD), new Color(0xD9B98F), new Color(0xEAD1AD)});
        Paint old = g.getPaint();
        g.setPaint(lg);
        g.fill(face);
        drawFineWoodLines(g, cx, cy, rInner, face);
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Float(cx, cy), rInner,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 45)});
        g.setPaint(vignette);
        g.fill(face);
        g.setPaint(old);
    }

    private static void drawFineWoodLines(Graphics2D g, int cx, int cy, int rInner, Shape clip) {
        Shape oldClip = g.getClip();
        g.setClip(clip);
        g.setColor(new Color(0, 0, 0, 15));
        for (int i = -rInner; i <= rInner; i += 4) {
            g.drawLine(cx - rInner, cy + i, cx + rInner, cy + i);
        }
        g.setClip(oldClip);
    }

    private static void paintSpecular(Graphics2D g, int cx, int cy, int rInner) {
        int ox = Math.round(rInner * 0.35f);
        int oy = Math.round(rInner * 0.35f);
        RadialGradientPaint gloss = new RadialGradientPaint(
                new Point2D.Float(cx - ox, cy - oy), rInner * 0.9f,
                new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(255, 255, 255, 115), new Color(255, 255, 255, 35), new Color(255, 255, 255, 0)});
        Shape face = new Ellipse2D.Float(cx - rInner, cy - rInner, 2f * rInner, 2f * rInner);
        Shape clip = g.getClip();
        Paint old = g.getPaint();
        g.setClip(face);
        g.setPaint(gloss);
        g.fill(face);
        g.setClip(clip);
        g.setPaint(old);
    }

    private static void paintGlyph(Graphics2D g, String text, boolean red, int cx, int cy, int rInner) {
        Color main = red ? new Color(0xD83A3A) : new Color(0x222222);
        int fontSize = Math.max(22, Math.round(rInner * 1.15f));
        g.setFont(g.getFont().deriveFont(Font.BOLD, fontSize));
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

    private static BufferedImage gaussianBlur(BufferedImage img, float radius) {
        if (radius <= 0) return img;
        float[] kernelData = createGaussianKernel(radius);
        int k = kernelData.length;
        BufferedImage tmp = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        ConvolveOp op = new ConvolveOp(new Kernel(k, 1, kernelData), ConvolveOp.EDGE_NO_OP, null);
        op.filter(img, tmp);
        BufferedImage dst = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        op = new ConvolveOp(new Kernel(1, k, kernelData), ConvolveOp.EDGE_NO_OP, null);
        op.filter(tmp, dst);
        return dst;
    }

    private static float[] createGaussianKernel(float radius) {
        int r = (int) Math.ceil(radius);
        int size = r * 2 + 1;
        float[] data = new float[size];
        float sigma = radius / 3f;
        float sum = 0f;
        for (int i = 0; i < size; i++) {
            int x = i - r;
            float v = (float) Math.exp(-(x * x) / (2f * sigma * sigma));
            data[i] = v;
            sum += v;
        }
        for (int i = 0; i < size; i++) {
            data[i] /= sum;
        }
        return data;
    }
}
