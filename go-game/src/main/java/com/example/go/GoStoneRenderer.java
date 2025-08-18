package com.example.go;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * Renders a glossy Go stone with soft shadow.
 */
public class GoStoneRenderer {
    private GoStoneRenderer() {}

    public static void draw(Graphics2D g, int cx, int cy, int diameter, boolean white) {
        int r = diameter / 2;
        try {
            BufferedImage img = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gg = img.createGraphics();
            enableAA(gg);
            paintShadow(gg, r, r, r, diameter);
            paintStone(gg, r, r, r, white);
            gg.dispose();
            g.drawImage(img, cx - r, cy - r, null);
        } catch (Exception e) {
            g.setColor(white ? Color.WHITE : Color.BLACK);
            g.fillOval(cx - r, cy - r, diameter, diameter);
        }
    }

    private static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static void paintShadow(Graphics2D g, int cx, int cy, int r, int d) {
        int w = Math.round(r * 1.4f);
        int h = Math.round(r * 0.35f);
        float ox = r * 0.25f;
        float oy = r * 0.25f;
        float x = cx - w / 2f + ox;
        float y = cy + r / 2f + oy;
        BufferedImage shadow = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics();
        enableAA(sg);
        sg.setColor(new Color(0, 0, 0, 90));
        sg.fill(new Ellipse2D.Float(x, y, w, h));
        sg.dispose();
        BufferedImage blurred = gaussianBlur(shadow, Math.max(2f, d / 60f));
        g.drawImage(blurred, 0, 0, null);
    }

    private static void paintStone(Graphics2D g, int cx, int cy, int r, boolean white) {
        float hx = cx - r * 0.35f;
        float hy = cy - r * 0.35f;
        Color mid = white ? new Color(0xF0F0F0) : new Color(0x333333);
        Color edge = white ? new Color(0xC8C8C8) : new Color(0x111111);
        Shape stone = new Ellipse2D.Float(cx - r, cy - r, 2f * r, 2f * r);

        RadialGradientPaint body = new RadialGradientPaint(
                new Point2D.Float(hx, hy), r,
                new float[]{0f, 0.6f, 1f},
                new Color[]{Color.WHITE, mid, edge});
        g.setPaint(body);
        g.fill(stone);

        LinearGradientPaint shade = new LinearGradientPaint(
                cx - r, cy - r, cx + r, cy + r,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 80)});
        g.setPaint(shade);
        g.fill(stone);

        RadialGradientPaint spec = new RadialGradientPaint(
                new Point2D.Float(hx, hy), r * 0.9f,
                new float[]{0f, 0.6f, 1f},
                new Color[]{
                        new Color(255, 255, 255, 115),
                        new Color(255, 255, 255, 35),
                        new Color(255, 255, 255, 0)
                });
        g.setPaint(spec);
        g.fill(stone);
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
