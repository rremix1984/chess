package com.example.go.lad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Minimal board panel capable of drawing a position and notifying on clicks.
 */
public class GoBoardPanel extends JPanel {
    private GoGame game = new GoGame(9);
    private Consumer<GoPoint> moveListener;
    private GoPoint lastMove;

    public GoBoardPanel() {
        setBackground(new Color(0xD69A45));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GoPoint pt = screenToPoint(e.getX(), e.getY());
                if (pt != null && moveListener != null) {
                    moveListener.accept(pt);
                    lastMove = pt;
                }
            }
        });
    }

    public void setMoveListener(Consumer<GoPoint> l) { this.moveListener = l; }
    public GoGame getGame() { return game; }

    public void showPosition(GoLifeAndDeathProblem p) {
        game = new GoGame(p.size);
        game.resetTo(p);
        repaint();
    }

    public void place(GoPoint pt) {
        game.play(pt);
        repaint();
    }

    private GoPoint screenToPoint(int x, int y) {
        int size = game.getSize();
        int margin = 10;
        int cell = (Math.min(getWidth(), getHeight()) - 2 * margin) / (size - 1);
        int bx = Math.round((x - margin) / (float) cell) + 1;
        int by = Math.round((y - margin) / (float) cell) + 1;
        if (bx >= 1 && bx <= size && by >= 1 && by <= size) {
            int sx = margin + (bx - 1) * cell;
            int sy = margin + (by - 1) * cell;
            if (Math.abs(x - sx) <= cell / 2 && Math.abs(y - sy) <= cell / 2) {
                return new GoPoint(bx, by);
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int size = game.getSize();
        int margin = 10;
        int cell = (Math.min(getWidth(), getHeight()) - 2 * margin) / (size - 1);
        int boardSize = cell * (size - 1);
        int originX = margin;
        int originY = margin;

        g.setColor(Color.BLACK);
        for (int i = 0; i < size; i++) {
            int x = originX + i * cell;
            int y = originY + i * cell;
            g.drawLine(originX, y, originX + boardSize, y);
            g.drawLine(x, originY, x, originY + boardSize);
        }

        // draw stones with simple 3D shading and shadow
        for (int x = 1; x <= size; x++) {
            for (int y = 1; y <= size; y++) {
                GoColor c = game.get(x, y);
                if (c != null) {
                    int sx = originX + (x - 1) * cell;
                    int sy = originY + (y - 1) * cell;
                    int ox = sx - cell / 2;
                    int oy = sy - cell / 2;

                    // shadow
                    g.setColor(new Color(0, 0, 0, 60));
                    g.fillOval(ox + 2, oy + 2, cell, cell);

                    // gradient for stone body
                    RadialGradientPaint rg = (c == GoColor.BLACK)
                            ? new RadialGradientPaint(new java.awt.geom.Point2D.Float(ox + cell / 3f, oy + cell / 3f), cell / 2f,
                            new float[]{0f, 1f}, new Color[]{new Color(80, 80, 80), Color.BLACK})
                            : new RadialGradientPaint(new java.awt.geom.Point2D.Float(ox + cell / 3f, oy + cell / 3f), cell / 2f,
                            new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(200, 200, 200)});
                    g.setPaint(rg);
                    g.fillOval(ox, oy, cell, cell);

                    g.setColor(Color.BLACK);
                    g.drawOval(ox, oy, cell, cell);
                }
            }
        }

        if (lastMove != null) {
            int sx = originX + (lastMove.x - 1) * cell;
            int sy = originY + (lastMove.y - 1) * cell;
            g.setColor(Color.RED);
            g.drawRect(sx - cell / 2, sy - cell / 2, cell, cell);
        }
    }
}
