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
        try {
            drawShadow(g, cx, cy, diameter, 1f);
            drawWithoutShadow(g, cx, cy, diameter, white);
        } catch (Exception e) {
            int r = diameter / 2;
            g.setColor(white ? Color.WHITE : Color.BLACK);
            g.fillOval(cx - r, cy - r, diameter, diameter);
        }
    }

    /**
     * Draws a stone without the built-in shadow. Useful when the caller wants to
     * control shadow rendering separately (e.g. during animations).
     */
    public static void drawWithoutShadow(Graphics2D g, int cx, int cy, int diameter, boolean white) {
        int r = diameter / 2;
        try {
            enableAA(g);
            paintStone(g, cx, cy, r, white);
        } catch (Exception e) {
            g.setColor(white ? Color.WHITE : Color.BLACK);
            g.fillOval(cx - r, cy - r, diameter, diameter);
        }
    }

    /**
     * Draws a soft blurred shadow offset to the bottom-right.
     */
    public static void drawShadow(Graphics2D g, int cx, int cy, int diameter, float alphaFactor) {
        int offX = Math.round(diameter * 0.10f);
        int offY = Math.round(diameter * 0.12f);
        int size = diameter + Math.max(offX, offY) * 2;
        BufferedImage shadow = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics();
        enableAA(sg);
        sg.setColor(new Color(0, 0, 0, (int) (85 * alphaFactor)));
        sg.fillOval(offX, offY, diameter, diameter);
        sg.dispose();
        BufferedImage blurred = gaussianBlur(shadow, Math.max(1f, diameter / 50f));
        int r = diameter / 2;
        g.drawImage(blurred, cx - r - offX, cy - r - offY, null);
    }

    private static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static void paintStone(Graphics2D g, int cx, int cy, int r, boolean white) {
        Shape stone = new Ellipse2D.Float(cx - r, cy - r, 2f * r, 2f * r);
        Color[] cols = white
                ? new Color[]{new Color(0xFFFFFF), new Color(0xEDE7DA), new Color(0xD7CBB4)}
                : new Color[]{new Color(0x2B2B2B), new Color(0x1A1A1A), new Color(0x111111)};
        float[] dist = {0f, 0.65f, 1f};
        RadialGradientPaint body = new RadialGradientPaint(new Point2D.Float(cx, cy), r, dist, cols);
        g.setPaint(body);
        g.fill(stone);

        int ox = Math.round(r * 0.35f);
        int oy = Math.round(r * 0.35f);
        RadialGradientPaint gloss = new RadialGradientPaint(
                new Point2D.Float(cx - ox, cy - oy), r * 0.9f,
                new float[]{0f, 0.6f, 1f},
                new Color[]{
                        new Color(255, 255, 255, white ? 130 : 90),
                        new Color(255, 255, 255, 35),
                        new Color(255, 255, 255, 0)
                });
        Shape old = g.getClip();
        g.setClip(stone);
        g.setPaint(gloss);
        g.fill(stone);
        g.setClip(old);
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
