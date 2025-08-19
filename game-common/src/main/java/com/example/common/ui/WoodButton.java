package com.example.common.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * A rounded wooden button with subtle grain and highlight. Text can be changed
 * at runtime via {@link #setTextLabel(String)}.
 */
public class WoodButton extends JComponent {
    private String text = "全屏 ⛶";
    private boolean hover;
    private boolean pressed;

    public WoodButton() {
        setOpaque(false);
        setPreferredSize(new Dimension(60, 60));
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setTextLabel(String t) {
        this.text = t;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        float r = 14f;
        Shape shape = new RoundRectangle2D.Float(2, 2, w - 4, h - 4, r, r);

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new RoundRectangle2D.Float(4, 5, w - 6, h - 6, r, r));

        // Wood gradient
        GradientPaint gp = new GradientPaint(0, 0, new Color(0xE7B972), 0, h, new Color(0xC88943));
        if (pressed) {
            gp = new GradientPaint(0, 0, new Color(0xD6A35C), 0, h, new Color(0xB87734));
        }
        g2.setPaint(gp);
        g2.fill(shape);

        // Grain lines
        g2.setClip(shape);
        g2.setStroke(new BasicStroke(1f));
        for (int y = 6, i = 0; y < h - 6; y += 6, i++) {
            int wobble = (i % 5) - 2;
            g2.setColor(new Color(120, 80, 40, hover && !pressed ? 28 : 22));
            g2.drawLine(8, y + wobble, w - 8, y + wobble);
        }

        // Highlight
        GradientPaint gloss = new GradientPaint(0, 2, new Color(255, 255, 255, 90),
                0, h / 2f, new Color(255, 255, 255, 0));
        g2.setPaint(gloss);
        g2.fill(new RoundRectangle2D.Float(4, 3, w - 8, (h - 6) / 2f, r, r));

        // Border
        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(new Color(0x6E3F1C));
        g2.draw(shape);

        // Text with shadow (supports multi-line)
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20f));
        FontMetrics fm = g2.getFontMetrics();
        String[] lines = text.split("\\n");
        int lineHeight = fm.getHeight();
        int totalHeight = lines.length * lineHeight;
        int y = (h - totalHeight) / 2 + fm.getAscent();
        for (String line : lines) {
            int tx = (w - fm.stringWidth(line)) / 2;
            g2.setColor(new Color(0, 0, 0, 60));
            g2.drawString(line, tx + 1, y + 1);
            g2.setColor(new Color(36, 23, 12));
            g2.drawString(line, tx, y);
            y += lineHeight;
        }

        g2.dispose();
    }

    private void fireAction() {
        for (ActionListener l : listenerList.getListeners(ActionListener.class)) {
            l.actionPerformed(new java.awt.event.ActionEvent(this, 0, "click"));
        }
    }

    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }

    private boolean hovered() {
        return hover && !pressed;
    }

    @Override
    protected void processMouseEvent(java.awt.event.MouseEvent e) {
        switch (e.getID()) {
            case java.awt.event.MouseEvent.MOUSE_ENTERED:
                hover = true; repaint(); break;
            case java.awt.event.MouseEvent.MOUSE_EXITED:
                hover = false; pressed = false; repaint(); break;
            case java.awt.event.MouseEvent.MOUSE_PRESSED:
                pressed = true; repaint(); break;
            case java.awt.event.MouseEvent.MOUSE_RELEASED:
                pressed = false; repaint();
                if (contains(e.getPoint())) fireAction();
                break;
        }
        super.processMouseEvent(e);
    }
}
