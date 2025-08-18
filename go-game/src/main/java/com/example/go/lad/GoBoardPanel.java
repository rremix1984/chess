package com.example.go.lad;

import com.example.go.GoStoneRenderer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import audio.SoundManager;
import static audio.SoundManager.Event.*;
import static audio.SoundManager.SoundProfile.*;

/**
 * Minimal board panel capable of drawing a position and notifying on clicks.
 */
public class GoBoardPanel extends JPanel {
    private GoGame game = new GoGame(9);
    private Consumer<GoPoint> moveListener;
    private GoPoint lastMove;

    // 落子动画
    private GoPoint animStone;
    private GoColor animColor;
    private int animStartY;
    private int animEndY;
    private double animProgress;
    private Timer dropTimer;
    private long animStartTime;
    private static final int DROP_DURATION = 600;

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
        GoColor color = game.get(pt.x, pt.y);
        startDropAnimation(pt.x, pt.y, color);
    }

    private void startDropAnimation(int x, int y, GoColor color) {
        int size = game.getSize();
        int margin = 10;
        int cell = (Math.min(getWidth(), getHeight()) - 2 * margin) / (size - 1);
        int sy = margin + (y - 1) * cell;
        animStone = new GoPoint(x, y);
        animColor = color;
        animEndY = sy;
        animStartY = -cell * 5;
        animProgress = 0;
        animStartTime = System.currentTimeMillis();
        if (dropTimer != null && dropTimer.isRunning()) {
            dropTimer.stop();
        }
        dropTimer = new Timer(15, e -> {
            long elapsed = System.currentTimeMillis() - animStartTime;
            animProgress = Math.min(1.0, elapsed / (double) DROP_DURATION);
            if (animProgress >= 1) {
                dropTimer.stop();
                animStone = null;
                SoundManager.play(STONE, PIECE_DROP);
            }
            repaint();
        });
        dropTimer.start();
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
        Graphics2D g2 = (Graphics2D) g;
        int size = game.getSize();
        int margin = 10;
        int cell = (Math.min(getWidth(), getHeight()) - 2 * margin) / (size - 1);
        int boardSize = cell * (size - 1);
        int originX = margin;
        int originY = margin;

        g2.setColor(Color.BLACK);
        for (int i = 0; i < size; i++) {
            int x = originX + i * cell;
            int y = originY + i * cell;
            g2.drawLine(originX, y, originX + boardSize, y);
            g2.drawLine(x, originY, x, originY + boardSize);
        }

        // draw stones with simple 3D shading and shadow
        for (int x = 1; x <= size; x++) {
            for (int y = 1; y <= size; y++) {
                GoColor c = game.get(x, y);
                if (c != null) {
                    if (animStone != null && dropTimer != null && dropTimer.isRunning()
                            && animStone.x == x && animStone.y == y) {
                        continue; // 动画棋子稍后绘制
                    }
                    int sx = originX + (x - 1) * cell;
                    int sy = originY + (y - 1) * cell;
                    drawStone(g2, sx, sy, c, cell);
                }
            }
        }

        if (animStone != null && dropTimer != null && dropTimer.isRunning()) {
            int sx = originX + (animStone.x - 1) * cell;
            double eased = easeOutBounce(animProgress);
            int sy = (int) (animStartY + (animEndY - animStartY) * eased);
            double scale = 0.6 + 0.4 * eased;
            drawStone(g2, sx, sy, animColor, (int) (cell * scale));
        }

        if (lastMove != null) {
            int sx = originX + (lastMove.x - 1) * cell;
            int sy = originY + (lastMove.y - 1) * cell;
            g2.setColor(Color.RED);
            g2.drawRect(sx - cell / 2, sy - cell / 2, cell, cell);
        }
    }

    private void drawStone(Graphics2D g2, int centerX, int centerY, GoColor c, int cell) {
        GoStoneRenderer.draw(g2, centerX, centerY, cell, c == GoColor.WHITE);
    }

    private double easeOutBounce(double t) {
        if (t < 1 / 2.75) {
            return 7.5625 * t * t;
        } else if (t < 2 / 2.75) {
            t -= 1.5 / 2.75;
            return 7.5625 * t * t + 0.75;
        } else if (t < 2.5 / 2.75) {
            t -= 2.25 / 2.75;
            return 7.5625 * t * t + 0.9375;
        } else {
            t -= 2.625 / 2.75;
            return 7.5625 * t * t + 0.984375;
        }
    }
}
