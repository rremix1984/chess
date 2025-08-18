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
        int shadowDiameter = Math.round(diameter * 1.1f);
        int radius = shadowDiameter / 2;
        int offset = Math.round(diameter * 0.25f);
        int blur = Math.max(2, diameter / 6);
        int size = shadowDiameter + blur * 2;

        BufferedImage shadow = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics();
        enableAA(sg);
        sg.setColor(new Color(0, 0, 0, (int) (80 * alphaFactor)));
        sg.fillOval(blur, blur, shadowDiameter, shadowDiameter);
        sg.dispose();

        BufferedImage blurred = gaussianBlur(shadow, blur);
        g.drawImage(blurred, cx - radius - blur + offset, cy - radius - blur + offset, null);
    }

    private static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static void paintStone(Graphics2D g, int cx, int cy, int r, boolean white) {
        float hx = cx - r * 0.35f;
        float hy = cy - r * 0.35f;
        Color mid = white ? new Color(250, 250, 250, 230) : new Color(70, 70, 70);
        Color edge = white ? new Color(210, 210, 210, 230) : new Color(15, 15, 15);
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
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 90)});
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
