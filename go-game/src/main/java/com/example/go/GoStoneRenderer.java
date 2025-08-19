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
        int off = 2;
        int size = diameter + off * 2;
        BufferedImage shadow = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics();
        enableAA(sg);
        sg.setColor(new Color(0, 0, 0, (int) (76 * alphaFactor)));
        sg.fillOval(off, off, diameter, diameter);
        sg.dispose();
        BufferedImage blurred = gaussianBlur(shadow, 1f);
        int r = diameter / 2;
        g.drawImage(blurred, cx - r - off, cy - r - off, null);
    }

    private static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static void paintStone(Graphics2D g, int cx, int cy, int r, boolean white) {
        Shape stone = new Ellipse2D.Float(cx - r, cy - r, 2f * r, 2f * r);
        if (white) {
            // 白子主体：中心白色，边缘淡灰
            RadialGradientPaint body = new RadialGradientPaint(
                    new Point2D.Float(cx, cy), r,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0xFFFFFF), new Color(0xE0E0E0)});
            g.setPaint(body);
            g.fill(stone);

            // 内部阴影以增加厚度
            g.setColor(new Color(0, 0, 0, 30));
            g.setStroke(new BasicStroke(r * 0.1f));
            g.draw(stone);
        } else {
            // 黑子主体：中心略亮
            RadialGradientPaint body = new RadialGradientPaint(
                    new Point2D.Float(cx, cy), r,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0x2B2B2B), new Color(0x000000)});
            g.setPaint(body);
            g.fill(stone);

            // 边缘高光
            g.setColor(new Color(255, 255, 255, 40));
            g.setStroke(new BasicStroke(r * 0.08f));
            g.draw(stone);
        }

        // 左上高光
        Shape old = g.getClip();
        g.setClip(stone);
        double hw = r * 0.8;
        double hh = r * 0.6;
        Ellipse2D highlight = new Ellipse2D.Double(cx - r * 0.8, cy - r * 0.8, hw, hh);
        g.setColor(new Color(255, 255, 255, white ? 153 : 128));
        g.fill(highlight);
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
