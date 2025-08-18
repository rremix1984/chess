package com.example.chinesechess.ui;

import javax.swing.Timer;
import java.util.Random;
import java.awt.Rectangle;
import java.util.function.Consumer;
import com.example.chinesechess.config.ChineseChessConfig;

/**
 * 负责在落子冲击时让周围棋子产生抖动效果。
 */
public class ImpactAnimator {
    private final double[][] offsetX = new double[10][9];
    private final double[][] offsetY = new double[10][9];
    private Timer timer;
    private final Random random = new Random();
    private final Consumer<Rectangle> repaintCallback;
    private Rectangle repaintArea;

    public ImpactAnimator(Consumer<Rectangle> repaintCallback) {
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
        int cell = ChineseChessConfig.BOARD_CELL_SIZE;
        int margin = ChineseChessConfig.BOARD_MARGIN;
        int rCells = (int) Math.ceil(radiusCells);
        int minRow = Math.max(0, centerRow - rCells - 1);
        int maxRow = Math.min(9, centerRow + rCells + 1);
        int minCol = Math.max(0, centerCol - rCells - 1);
        int maxCol = Math.min(8, centerCol + rCells + 1);
        int x = margin + minCol * cell - (int) maxShakePx - 2;
        int y = margin + minRow * cell - (int) maxShakePx - 2;
        int w = (maxCol - minCol + 1) * cell + (int) maxShakePx * 2 + 4;
        int h = (maxRow - minRow + 1) * cell + (int) maxShakePx * 2 + 4;
        repaintArea = new Rectangle(x, y, w, h);
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
            repaintCallback.accept(repaintArea);
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
