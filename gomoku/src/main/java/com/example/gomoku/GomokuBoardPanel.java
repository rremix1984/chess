package com.example.gomoku.ui;

import com.example.gomoku.core.GameState;
import com.example.gomoku.core.GomokuBoard;
import com.example.common.sound.SoundPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.AlphaComposite;
import java.awt.RadialGradientPaint;
import java.util.function.Consumer;
import javax.swing.Timer;

/**
 * 五子棋棋盘面板
 * 负责绘制棋盘和处理用户交互
 */
public class GomokuBoardPanel extends JPanel {

    private GomokuBoard board;
    private ChatPanel chatPanel;
    private Consumer<String> statusUpdateCallback;
    private boolean aiEnabled = false;
    private String aiType = "传统AI";
    private String difficulty = "普通";
    private String modelName = "deepseek-r1:7b";
    private boolean isPlayerBlack = true; // 玩家默认执黑
    private GomokuAI ai;
    
    // 移动历史记录（用于悔棋功能）
    private java.util.List<GomokuMoveRecord> moveHistory = new java.util.ArrayList<>();
    
    // 棋盘绘制相关常量
    private static final int MARGIN = 30; // 棋盘边距
    private static final int CELL_SIZE = 40; // 格子大小
    private static final int PIECE_SIZE = 34; // 棋子大小
    
    /**
     * 构造函数
     */
    public GomokuBoardPanel() {
        board = new GomokuBoard();
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
        // 如果游戏已结束，不处理点击
        if (board.getGameState() != GameState.PLAYING) {
            return;
        }
        
        // 如果AI已启用且当前不是玩家回合，不处理点击
        if (aiEnabled && ((isPlayerBlack && !board.isBlackTurn()) || (!isPlayerBlack && board.isBlackTurn()))) {
            return;
        }
        
        // 计算点击的棋盘坐标
        int col = Math.round((float) (e.getX() - MARGIN) / CELL_SIZE);
        int row = Math.round((float) (e.getY() - MARGIN) / CELL_SIZE);
        
        // 确保坐标在有效范围内
        if (row >= 0 && row < GomokuBoard.BOARD_SIZE && col >= 0 && col < GomokuBoard.BOARD_SIZE) {
            // 尝试落子
            boolean success = board.placePiece(row, col);
            if (success) {
                // 记录移动历史（用于悔棋）
                moveHistory.add(new GomokuMoveRecord(row, col, board.isBlackTurn() ? GomokuBoard.WHITE : GomokuBoard.BLACK));
                
                // 播放落子音效
                SoundPlayer.getInstance().playSound("piece_drop");
                
                // 更新界面
                repaint();
                updateStatus();
                
                // 如果游戏未结束且AI已启用，让AI走棋
                if (board.getGameState() == GameState.PLAYING && aiEnabled) {
                    SwingUtilities.invokeLater(this::makeAIMove);
                }
            }
        }
    }
    
    /**
     * 让AI走棋
     */
    private void makeAIMove() {
        if (ai == null) {
            return;
        }
        
        // 获取AI的走法
        int[] move = ai.getNextMove(board);
        if (move != null && move.length == 2) {
            int row = move[0];
            int col = move[1];
            
            // 执行AI的走法
            boolean success = board.placePiece(row, col);
            if (success) {
                // 记录AI移动历史（用于悔棋）
                moveHistory.add(new GomokuMoveRecord(row, col, board.isBlackTurn() ? GomokuBoard.WHITE : GomokuBoard.BLACK));
                
                // 播放落子音效
                SoundPlayer.getInstance().playSound("piece_drop");
                
                // 更新界面
                repaint();
                updateStatus();
                
                // 如果使用大模型AI，显示AI的思考过程
                if (aiType.equals("大模型AI") || aiType.equals("混合AI")) {
                    String thinking = ai.getThinking();
                    if (chatPanel != null && thinking != null && !thinking.isEmpty()) {
                        chatPanel.addChatMessage("AI", thinking);
                    }
                }
            }
        }
    }
    
    /**
     * 更新游戏状态
     */
    private void updateStatus() {
        if (statusUpdateCallback != null) {
            String status;
            switch (board.getGameState()) {
                case BLACK_WINS:
                    status = "⚫ 黑方获胜！";
                    SoundPlayer.getInstance().playSound("game_win");
                    showVictoryAnimation(status);
                    break;
                case RED_WINS: // 在五子棋中表示白方获胜
                    status = "⚪ 白方获胜！";
                    SoundPlayer.getInstance().playSound("game_win");
                    showVictoryAnimation(status);
                    break;
                case DRAW:
                    status = "🤝 和棋！";
                    showVictoryAnimation(status);
                    break;
                default:
                    status = board.isBlackTurn() ? "⚫ 当前玩家: 黑方" : "⚪ 当前玩家: 白方";
                    break;
            }
            statusUpdateCallback.accept(status);
        }
    }
    
    /**
     * 显示胜利动画
     */
    private void showVictoryAnimation(String message) {
        // 获取顶层容器
        Container topContainer = getTopLevelAncestor();
        if (topContainer instanceof JFrame) {
            JFrame frame = (JFrame) topContainer;
            
            // 创建动画层
            VictoryAnimation animation = new VictoryAnimation();
            animation.setBounds(0, 0, frame.getWidth(), frame.getHeight());
            
            // 添加到玻璃面板
            Component glassPane = frame.getGlassPane();
            if (glassPane instanceof JComponent) {
                JComponent glass = (JComponent) glassPane;
                glass.setLayout(null);
                glass.add(animation);
                glass.setVisible(true);
            } else {
                // 创建新的玻璃面板
                JPanel glass = new JPanel();
                glass.setLayout(null);
                glass.setOpaque(false);
                glass.add(animation);
                frame.setGlassPane(glass);
                glass.setVisible(true);
            }
            
            // 确定获胜方颜色
            Color winnerColor = new Color(255, 215, 0); // Gold color
            if (message.contains("黑方")) {
                winnerColor = Color.BLACK;
            } else if (message.contains("白方")) {
                winnerColor = Color.WHITE;
            }
            
            // 开始动画
            animation.startVictoryAnimation(message, winnerColor);
            
            // 5秒后自动关闭动画
            Timer closeTimer = new Timer(5000, e -> {
                animation.stopAnimation();
                if (glassPane instanceof JComponent) {
                    ((JComponent) glassPane).remove(animation);
                    glassPane.setVisible(false);
                }
            });
            closeTimer.setRepeats(false);
            closeTimer.start();
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
        updateStatus(); // 初始化状态
    }
    
    /**
     * 设置聊天面板
     */
    public void setChatPanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }
    
    /**
     * 启用或禁用AI
     */
    public void setAIEnabled(boolean enabled) {
        this.aiEnabled = enabled;
        if (enabled && ai == null) {
            initializeAI();
        }
        
        // 如果启用AI且当前是AI回合，让AI走棋
        if (enabled && ((isPlayerBlack && !board.isBlackTurn()) || (!isPlayerBlack && board.isBlackTurn()))) {
            SwingUtilities.invokeLater(this::makeAIMove);
        }
    }
    
    /**
     * 设置AI类型
     */
    public void setAIType(String aiType, String difficulty, String modelName) {
        this.aiType = aiType;
        this.difficulty = difficulty;
        this.modelName = modelName;
        
        // 重新初始化AI
        if (aiEnabled) {
            initializeAI();
        }
    }
    
    /**
     * 设置玩家颜色
     */
    public void setPlayerColor(boolean isBlack) {
        this.isPlayerBlack = isBlack;
        
        // 如果AI已启用且当前是AI回合，让AI走棋
        if (aiEnabled && ((isPlayerBlack && !board.isBlackTurn()) || (!isPlayerBlack && board.isBlackTurn()))) {
            SwingUtilities.invokeLater(this::makeAIMove);
        }
    }
    
    /**
     * 初始化AI
     */
    private void initializeAI() {
        switch (aiType) {
            case "传统AI":
                ai = new GomokuAI(difficulty);
                break;
            case "大模型AI":
                ai = new GomokuLLMAI(difficulty, modelName);
                break;
            case "混合AI":
                ai = new GomokuHybridAI(difficulty, modelName);
                break;
            default:
                ai = new GomokuAI(difficulty);
                break;
        }
    }
    
    /**
     * 获取棋盘对象
     */
    public GomokuBoard getBoard() {
        return board;
    }
    
    /**
     * 重置游戏
     */
    public void resetGame() {
        board = new GomokuBoard();
        moveHistory.clear(); // 清空移动历史
        repaint();
        updateStatus();
        
        // 如果AI已启用且当前是AI回合，让AI走棋
        if (aiEnabled && ((isPlayerBlack && !board.isBlackTurn()) || (!isPlayerBlack && board.isBlackTurn()))) {
            SwingUtilities.invokeLater(this::makeAIMove);
        }
    }
    
    /**
     * 悔棋功能 - 撤销上一步移动
     */
    public void undoLastMove() {
        if (moveHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可以撤销的移动！", "悔棋", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 获取最后一步移动记录
        GomokuMoveRecord lastMove = moveHistory.remove(moveHistory.size() - 1);
        
        // 恢复棋盘状态
        board.removePiece(lastMove.getRow(), lastMove.getCol());
        
        // 切换回合（因为removePiece不会自动切换回合）
        board.switchTurn();
        
        repaint();
        updateStatus();
        
        System.out.println("🔄 悔棋成功，已撤销上一步移动");
    }
    
    /**
     * 五子棋移动记录类
     */
    private static class GomokuMoveRecord {
        private final int row;
        private final int col;
        private final char piece;
        
        public GomokuMoveRecord(int row, int col, char piece) {
            this.row = row;
            this.col = col;
            this.piece = piece;
        }
        
        public int getRow() { return row; }
        public int getCol() { return col; }
        public char getPiece() { return piece; }
    }
}