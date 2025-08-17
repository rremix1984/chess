package com.example.chinesechess.ui;

import javax.swing.Timer;
import java.util.Random;

/**
 * 负责在落子冲击时让周围棋子产生抖动效果。
 */
public class ImpactAnimator {
    private final double[][] offsetX = new double[10][9];
    private final double[][] offsetY = new double[10][9];
    private Timer timer;
    private final Random random = new Random();
    private final Runnable repaintCallback;

    public ImpactAnimator(Runnable repaintCallback) {
        this.repaintCallback = repaintCallback;
    }

    /**
     * 在指定棋盘坐标触发冲击波。
     * @param centerRow  行坐标
     * @param centerCol  列坐标
     * @param radiusCells 影响半径（格子数）
     * @param maxShakePx 最大抖动像素
     * @param durationMs 动画持续时间
     */
    public void blastAt(int centerRow, int centerCol, double radiusCells, double maxShakePx, int durationMs) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                double dist = Math.hypot(r - centerRow, c - centerCol);
                if (dist <= radiusCells) {
                    double amp = maxShakePx * (1 - dist / radiusCells);
                    double angle = random.nextDouble() * Math.PI * 2;
                    offsetX[r][c] = Math.cos(angle) * amp;
                    offsetY[r][c] = Math.sin(angle) * amp;
                }
            }
        }
        if (timer != null) {
            timer.stop();
        }
        final int steps = Math.max(1, durationMs / 16);
        final int[] counter = {steps};
        timer = new Timer(16, e -> {
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 9; c++) {
                    offsetX[r][c] *= 0.8;
                    offsetY[r][c] *= 0.8;
                }
            }
            repaintCallback.run();
            if (--counter[0] <= 0) {
                timer.stop();
            }
        });
        timer.start();
    }

    public int getOffsetX(int row, int col) {
        return (int)Math.round(offsetX[row][col]);
    }

    public int getOffsetY(int row, int col) {
        return (int)Math.round(offsetY[row][col]);
    }
}
