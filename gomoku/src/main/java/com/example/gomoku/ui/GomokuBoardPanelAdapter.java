package com.example.gomoku.ui;

import com.example.gomoku.core.GameState;
import com.example.gomoku.core.GomokuBoard;
import com.example.gomoku.core.GomokuGameManager;
import com.example.gomoku.ChatPanel;
import audio.SoundManager;
import static audio.SoundManager.Event.*;
import static audio.SoundManager.SoundProfile.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.AlphaComposite;
import java.awt.RadialGradientPaint;
import java.util.function.Consumer;

/**
 * 五子棋棋盘面板适配器
 * 与GomokuGameManager集成，用于新的游戏架构
 */
public class GomokuBoardPanelAdapter extends JPanel {

    private GomokuGameManager gameManager;
    private ChatPanel chatPanel;
    private Consumer<String> statusUpdateCallback;
    
    // 棋盘绘制相关常量
    private static final int MARGIN = 30; // 棋盘边距
    private static final int CELL_SIZE = 40; // 格子大小
    private static final int PIECE_SIZE = 34; // 棋子大小
    
    /**
     * 构造函数
     */
    public GomokuBoardPanelAdapter(GomokuGameManager gameManager) {
        this.gameManager = gameManager;
        setPreferredSize(new Dimension(
                MARGIN * 2 + CELL_SIZE * (GomokuBoard.BOARD_SIZE - 1),
                MARGIN * 2 + CELL_SIZE * (GomokuBoard.BOARD_SIZE - 1)));
        setBackground(new Color(249, 214, 91)); // 浅黄色背景，模拟木质棋盘
        
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
        if (gameManager == null || !gameManager.isGameRunning() || gameManager.isGamePaused()) {
            return;
        }
        
        // 计算点击的棋盘坐标
        int col = Math.round((float) (e.getX() - MARGIN) / CELL_SIZE);
        int row = Math.round((float) (e.getY() - MARGIN) / CELL_SIZE);
        
        // 确保坐标在有效范围内
        if (row >= 0 && row < GomokuBoard.BOARD_SIZE && col >= 0 && col < GomokuBoard.BOARD_SIZE) {
            // 尝试通过GameManager落子
            boolean success = gameManager.makePlayerMove(row, col);
            if (success) {
                // 播放落子音效
                SoundManager.play(STONE, PIECE_DROP);
                
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
        
        // 启用抗锯齿和高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        
        if (gameManager == null || gameManager.getBoard() == null) {
            g2d.setColor(Color.BLACK);
            g2d.drawString("棋盘加载中...", getWidth()/2 - 50, getHeight()/2);
            return;
        }
        
        GomokuBoard board = gameManager.getBoard();
        System.out.println("🎨 paintComponent 被调用，棋盘状态: " + board.getGameState());
        
        // 绘制坐标标签
        drawCoordinates(g2d);
        
        // 绘制棋盘网格
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        
        // 绘制横线
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            g2d.drawLine(
                    MARGIN, MARGIN + row * CELL_SIZE,
                    MARGIN + (GomokuBoard.BOARD_SIZE - 1) * CELL_SIZE, MARGIN + row * CELL_SIZE);
        }
        
        // 绘制竖线
        for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
            g2d.drawLine(
                    MARGIN + col * CELL_SIZE, MARGIN,
                    MARGIN + col * CELL_SIZE, MARGIN + (GomokuBoard.BOARD_SIZE - 1) * CELL_SIZE);
        }
        
        // 绘制天元和星位
        drawStar(g2d, 7, 7); // 天元
        
        // 四角星位
        drawStar(g2d, 3, 3);
        drawStar(g2d, 3, 11);
        drawStar(g2d, 11, 3);
        drawStar(g2d, 11, 11);
        
        // 绘制棋子
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                char piece = board.getPiece(row, col);
                if (piece != ' ') {
                    drawPiece(g2d, row, col, piece);
                }
            }
        }
        
        // 绘制最后一步棋的标记
        int lastRow = board.getLastMoveRow();
        int lastCol = board.getLastMoveCol();
        if (lastRow >= 0 && lastCol >= 0) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(1.5f));
            int x = MARGIN + lastCol * CELL_SIZE;
            int y = MARGIN + lastRow * CELL_SIZE;
            int markSize = 6;
            g2d.drawLine(x - markSize, y - markSize, x + markSize, y + markSize);
            g2d.drawLine(x + markSize, y - markSize, x - markSize, y + markSize);
        }
    }
    
    /**
     * 绘制坐标标签
     */
    private void drawCoordinates(Graphics2D g2d) {
        g2d.setColor(new Color(80, 80, 80));
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        
        // 绘制列坐标（A-O）
        for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
            String label = String.valueOf((char)('A' + col));
            int x = MARGIN + col * CELL_SIZE;
            int stringWidth = fm.stringWidth(label);
            
            // 上方坐标
            g2d.drawString(label, x - stringWidth / 2, MARGIN - 8);
            // 下方坐标
            g2d.drawString(label, x - stringWidth / 2, MARGIN + (GomokuBoard.BOARD_SIZE - 1) * CELL_SIZE + 20);
        }
        
        // 绘制行坐标（1-15）
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            String label = String.valueOf(row + 1);
            int y = MARGIN + row * CELL_SIZE;
            int stringWidth = fm.stringWidth(label);
            int stringHeight = fm.getAscent();
            
            // 左侧坐标
            g2d.drawString(label, MARGIN - stringWidth - 8, y + stringHeight / 2);
            // 右侧坐标
            g2d.drawString(label, MARGIN + (GomokuBoard.BOARD_SIZE - 1) * CELL_SIZE + 8, y + stringHeight / 2);
        }
    }
    
    /**
     * 绘制星位
     */
    private void drawStar(Graphics2D g2d, int row, int col) {
        int x = MARGIN + col * CELL_SIZE;
        int y = MARGIN + row * CELL_SIZE;
        int size = 5;
        g2d.fillOval(x - size/2, y - size/2, size, size);
    }
    
    /**
     * 绘制棋子
     */
    private void drawPiece(Graphics2D g2d, int row, int col, char piece) {
        int x = MARGIN + col * CELL_SIZE - PIECE_SIZE / 2;
        int y = MARGIN + row * CELL_SIZE - PIECE_SIZE / 2;
        
        // 保存原始状态
        Composite originalComposite = g2d.getComposite();
        Stroke originalStroke = g2d.getStroke();
        
        // 绘制棋子阴影
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(x + 2, y + 2, PIECE_SIZE, PIECE_SIZE);
        
        // 设置棋子基本颜色
        if (piece == GomokuBoard.BLACK) {
            // 黑子 - 使用渐变色增加立体感
            Paint originalPaint = g2d.getPaint();
            RadialGradientPaint blackGradient = new RadialGradientPaint(
                x + PIECE_SIZE / 3, y + PIECE_SIZE / 3, PIECE_SIZE,
                new float[]{0.1f, 0.3f, 1.0f},
                new Color[]{new Color(90, 90, 90), new Color(40, 40, 40), Color.BLACK}
            );
            g2d.setPaint(blackGradient);
            g2d.fillOval(x, y, PIECE_SIZE, PIECE_SIZE);
            
            // 添加高光
            g2d.setColor(new Color(255, 255, 255, 80));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            g2d.fillOval(x + PIECE_SIZE / 5, y + PIECE_SIZE / 5, PIECE_SIZE / 4, PIECE_SIZE / 4);
            
            // 恢复原始画笔
            g2d.setPaint(originalPaint);
        } else {
            // 白子 - 使用渐变色增加立体感
            Paint originalPaint = g2d.getPaint();
            RadialGradientPaint whiteGradient = new RadialGradientPaint(
                x + PIECE_SIZE / 3, y + PIECE_SIZE / 3, PIECE_SIZE,
                new float[]{0.1f, 0.3f, 1.0f},
                new Color[]{Color.WHITE, new Color(240, 240, 240), new Color(210, 210, 210)}
            );
            g2d.setPaint(whiteGradient);
            g2d.fillOval(x, y, PIECE_SIZE, PIECE_SIZE);
            
            // 为白子添加边框
            g2d.setColor(new Color(120, 120, 120));
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawOval(x, y, PIECE_SIZE, PIECE_SIZE);
            
            // 恢复原始画笔
            g2d.setPaint(originalPaint);
        }
        
        // 恢复原始状态
        g2d.setComposite(originalComposite);
        g2d.setStroke(originalStroke);
    }
    
    /**
     * 设置状态更新回调
     */
    public void setStatusUpdateCallback(Consumer<String> callback) {
        this.statusUpdateCallback = callback;
    }
    
    /**
     * 设置聊天面板
     */
    public void setChatPanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }
    
    /**
     * 获取棋盘对象
     */
    public GomokuBoard getBoard() {
        return gameManager != null ? gameManager.getBoard() : null;
    }
    
    /**
     * 设置棋盘对象（对于外部调用兼容）
     */
    public void setBoard(GomokuBoard board) {
        // 由于Board由GameManager管理，这里只需要重绘界面
        repaint();
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
    
    /**
     * 悔棋功能 - 委托给GameManager
     */
    public void undoLastMove() {
        if (gameManager != null) {
            boolean success = gameManager.undoMove();
            if (success) {
                repaint();
            } else {
                JOptionPane.showMessageDialog(this, "无法悔棋！", "悔棋", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * 设置AI类型
     */
    public void setAIType(String aiType, String difficulty, String modelName) {
        if (gameManager != null) {
            // 将参数传递给GameManager处理
            System.out.println("设置AI类型: " + aiType + ", 难度: " + difficulty + ", 模型: " + modelName);
        }
    }
    
    /**
     * 设置AI启用状态
     */
    public void setAIEnabled(boolean enabled) {
        if (gameManager != null) {
            System.out.println("AI启用状态: " + enabled);
        }
    }
    
    /**
     * 设置玩家颜色
     */
    public void setPlayerColor(boolean isPlayerBlack) {
        if (gameManager != null) {
            System.out.println("设置玩家颜色 - 玩家是黑方: " + isPlayerBlack);
        }
    }
}
