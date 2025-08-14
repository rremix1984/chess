package com.example.junqi.ui;

import com.example.junqi.core.*;
import com.example.common.sound.SoundPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * 军棋棋盘面板适配器
 * 与JunQiGameManager集成，用于新的游戏架构
 */
public class JunQiBoardPanelAdapter extends JPanel {

    private JunQiGameManager gameManager;
    private Consumer<String> statusUpdateCallback;
    
    // 棋盘绘制相关常量
    private static final int MARGIN = 40; // 棋盘边距
    private static final int CELL_SIZE = 80; // 格子大小
    private static final int PIECE_SIZE = 70; // 棋子大小
    
    // 颜色配置
    private static final Color BOARD_COLOR = new Color(245, 222, 179); // 棋盘背景色
    private static final Color LINE_COLOR = Color.BLACK; // 网格线颜色
    private static final Color RED_PIECE_COLOR = new Color(220, 20, 60); // 红方棋子颜色
    private static final Color BLACK_PIECE_COLOR = new Color(70, 70, 70); // 黑方棋子颜色
    private static final Color HIDDEN_PIECE_COLOR = new Color(139, 69, 19); // 暗棋颜色
    private static final Color SELECTED_COLOR = new Color(255, 215, 0, 100); // 选中高亮色
    private static final Color VALID_MOVE_COLOR = new Color(50, 205, 50, 100); // 可移动位置颜色
    
    /**
     * 构造函数
     */
    public JunQiBoardPanelAdapter(JunQiGameManager gameManager) {
        this.gameManager = gameManager;
        
        // 计算面板大小
        int panelWidth = MARGIN * 2 + CELL_SIZE * JunQiBoard.BOARD_WIDTH;
        int panelHeight = MARGIN * 2 + CELL_SIZE * JunQiBoard.BOARD_HEIGHT;
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setBackground(BOARD_COLOR);
        
        // 添加鼠标事件监听
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
    }
    
    /**
     * 处理鼠标点击事件
     */
    private void handleMouseClick(MouseEvent e) {
        if (gameManager == null) {
            return;
        }
        
        // 计算点击的棋盘坐标
        int col = (e.getX() - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
        int row = (e.getY() - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
        
        // 确保坐标在有效范围内
        if (row >= 0 && row < JunQiBoard.BOARD_HEIGHT && col >= 0 && col < JunQiBoard.BOARD_WIDTH) {
            // 通过GameManager处理点击
            boolean success = gameManager.handlePlayerClick(row, col);
            if (success) {
                // 播放音效
                SoundPlayer.getInstance().playSound("piece_drop");
                
                // 更新界面
                repaint();
            }
        }
    }
    
    /**
     * 绘制棋盘和棋子
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (gameManager == null || gameManager.getBoard() == null) {
            return;
        }
        
        JunQiBoard board = gameManager.getBoard();
        
        // 绘制坐标标签
        drawCoordinates(g2d);
        
        // 绘制棋盘网格
        drawGrid(g2d);
        
        // 绘制选中位置和可移动位置的高亮
        drawHighlights(g2d);
        
        // 绘制棋子
        drawPieces(g2d, board);
    }
    
    /**
     * 绘制坐标标签
     */
    private void drawCoordinates(Graphics2D g2d) {
        g2d.setColor(new Color(80, 80, 80));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        
        // 绘制列坐标（A-E）
        for (int col = 0; col < JunQiBoard.BOARD_WIDTH; col++) {
            String label = String.valueOf((char)('A' + col));
            int x = MARGIN + col * CELL_SIZE + CELL_SIZE / 2;
            int stringWidth = fm.stringWidth(label);
            
            // 上方坐标
            g2d.drawString(label, x - stringWidth / 2, MARGIN - 10);
            // 下方坐标
            g2d.drawString(label, x - stringWidth / 2, 
                          MARGIN + JunQiBoard.BOARD_HEIGHT * CELL_SIZE + 25);
        }
        
        // 绘制行坐标（1-6）
        for (int row = 0; row < JunQiBoard.BOARD_HEIGHT; row++) {
            String label = String.valueOf(row + 1);
            int y = MARGIN + row * CELL_SIZE + CELL_SIZE / 2;
            int stringWidth = fm.stringWidth(label);
            int stringHeight = fm.getAscent();
            
            // 左侧坐标
            g2d.drawString(label, MARGIN - stringWidth - 15, y + stringHeight / 2);
            // 右侧坐标
            g2d.drawString(label, MARGIN + JunQiBoard.BOARD_WIDTH * CELL_SIZE + 10, 
                          y + stringHeight / 2);
        }
    }
    
    /**
     * 绘制棋盘网格
     */
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(LINE_COLOR);
        g2d.setStroke(new BasicStroke(2.0f));
        
        // 绘制横线
        for (int row = 0; row <= JunQiBoard.BOARD_HEIGHT; row++) {
            int y = MARGIN + row * CELL_SIZE;
            g2d.drawLine(MARGIN, y, MARGIN + JunQiBoard.BOARD_WIDTH * CELL_SIZE, y);
        }
        
        // 绘制竖线
        for (int col = 0; col <= JunQiBoard.BOARD_WIDTH; col++) {
            int x = MARGIN + col * CELL_SIZE;
            g2d.drawLine(x, MARGIN, x, MARGIN + JunQiBoard.BOARD_HEIGHT * CELL_SIZE);
        }
        
        // 绘制中线（分隔双方阵营）
        g2d.setStroke(new BasicStroke(4.0f));
        g2d.setColor(new Color(139, 69, 19));
        int middleY = MARGIN + (JunQiBoard.BOARD_HEIGHT / 2) * CELL_SIZE;
        g2d.drawLine(MARGIN, middleY, MARGIN + JunQiBoard.BOARD_WIDTH * CELL_SIZE, middleY);
    }
    
    /**
     * 绘制选中位置和可移动位置的高亮
     */
    private void drawHighlights(Graphics2D g2d) {
        int selectedRow = gameManager.getSelectedRow();
        int selectedCol = gameManager.getSelectedCol();
        
        // 绘制选中位置
        if (selectedRow >= 0 && selectedCol >= 0) {
            g2d.setColor(SELECTED_COLOR);
            int x = MARGIN + selectedCol * CELL_SIZE + 5;
            int y = MARGIN + selectedRow * CELL_SIZE + 5;
            g2d.fillRoundRect(x, y, CELL_SIZE - 10, CELL_SIZE - 10, 10, 10);
            
            // 绘制可移动位置
            JunQiBoard board = gameManager.getBoard();
            java.util.List<int[]> validMoves = board.getValidMoves(selectedRow, selectedCol);
            
            g2d.setColor(VALID_MOVE_COLOR);
            for (int[] move : validMoves) {
                int moveX = MARGIN + move[1] * CELL_SIZE + 10;
                int moveY = MARGIN + move[0] * CELL_SIZE + 10;
                g2d.fillOval(moveX, moveY, CELL_SIZE - 20, CELL_SIZE - 20);
            }
        }
    }
    
    /**
     * 绘制棋子
     */
    private void drawPieces(Graphics2D g2d, JunQiBoard board) {
        for (int row = 0; row < board.getBoardHeight(); row++) {
            for (int col = 0; col < board.getBoardWidth(); col++) {
                JunQiPiece piece = board.getPiece(row, col);
                if (piece != null && piece.isAlive()) {
                    drawPiece(g2d, row, col, piece);
                }
            }
        }
    }
    
    /**
     * 绘制单个棋子
     */
    private void drawPiece(Graphics2D g2d, int row, int col, JunQiPiece piece) {
        int x = MARGIN + col * CELL_SIZE + (CELL_SIZE - PIECE_SIZE) / 2;
        int y = MARGIN + row * CELL_SIZE + (CELL_SIZE - PIECE_SIZE) / 2;
        
        // 保存原始状态
        Composite originalComposite = g2d.getComposite();
        Stroke originalStroke = g2d.getStroke();
        
        // 绘制棋子阴影
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillOval(x + 3, y + 3, PIECE_SIZE, PIECE_SIZE);
        
        // 确定棋子颜色
        Color pieceColor;
        if (!piece.isVisible()) {
            pieceColor = HIDDEN_PIECE_COLOR; // 暗棋
        } else {
            pieceColor = piece.isRed() ? RED_PIECE_COLOR : BLACK_PIECE_COLOR;
        }
        
        // 绘制棋子底色
        g2d.setColor(pieceColor);
        g2d.fillOval(x, y, PIECE_SIZE, PIECE_SIZE);
        
        // 绘制棋子边框
        g2d.setColor(pieceColor.darker());
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(x, y, PIECE_SIZE, PIECE_SIZE);
        
        // 绘制棋子文字
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("宋体", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        
        String symbol = piece.getDisplaySymbol();
        int stringWidth = fm.stringWidth(symbol);
        int stringHeight = fm.getAscent();
        
        int textX = x + (PIECE_SIZE - stringWidth) / 2;
        int textY = y + (PIECE_SIZE + stringHeight) / 2 - 2;
        
        // 绘制文字阴影
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(symbol, textX + 1, textY + 1);
        
        // 绘制文字
        g2d.setColor(Color.WHITE);
        g2d.drawString(symbol, textX, textY);
        
        // 如果是特殊棋子，添加标记
        if (piece.isVisible()) {
            if (piece.getType() == PieceType.FLAG) {
                // 军旗添加星形标记
                drawStar(g2d, x + PIECE_SIZE / 2, y + PIECE_SIZE / 4, 8, Color.YELLOW);
            } else if (piece.getType() == PieceType.MINE) {
                // 地雷添加圆点标记
                g2d.setColor(Color.ORANGE);
                g2d.fillOval(x + PIECE_SIZE / 2 - 3, y + PIECE_SIZE / 4 - 3, 6, 6);
            }
        }
        
        // 恢复原始状态
        g2d.setComposite(originalComposite);
        g2d.setStroke(originalStroke);
    }
    
    /**
     * 绘制星形标记
     */
    private void drawStar(Graphics2D g2d, int centerX, int centerY, int size, Color color) {
        g2d.setColor(color);
        
        // 简化的星形绘制
        int[] xPoints = {
            centerX, centerX + size/3, centerX + size, centerX + size/3, 
            centerX + size*2/3, centerX, centerX - size*2/3, centerX - size/3, 
            centerX - size, centerX - size/3
        };
        int[] yPoints = {
            centerY - size, centerY - size/3, centerY - size/3, centerY, 
            centerY + size*2/3, centerY + size/2, centerY + size*2/3, centerY, 
            centerY - size/3, centerY - size/3
        };
        
        g2d.fillPolygon(xPoints, yPoints, 10);
    }
    
    /**
     * 设置状态更新回调
     */
    public void setStatusUpdateCallback(Consumer<String> callback) {
        this.statusUpdateCallback = callback;
    }
    
    /**
     * 获取棋盘对象
     */
    public JunQiBoard getBoard() {
        return gameManager != null ? gameManager.getBoard() : null;
    }
    
    /**
     * 重置游戏
     */
    public void resetGame() {
        if (gameManager != null) {
            gameManager.resetGame();
        }
        repaint();
    }
}
