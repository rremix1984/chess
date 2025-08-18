package com.example.gomoku.ui;

import com.example.gomoku.core.GameState;
import com.example.gomoku.core.GomokuBoard;
import com.example.gomoku.ChatPanel;
import audio.SoundManager;
import static audio.SoundManager.Event.*;
import static audio.SoundManager.SoundProfile.*;
import com.example.gomoku.ai.GomokuZeroAI;
// AI类已在同一个ui包中，无需导入

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
    private GomokuAdvancedAI ai;
    private GomokuZeroAI gomokuZeroAI;
    
    // 移动历史记录（用于悔棋功能）
    private java.util.List<GomokuMoveRecord> moveHistory = new java.util.ArrayList<>();

    // 棋盘绘制相关常量
    private static final int MARGIN = 30; // 棋盘边距
    private static final int CELL_SIZE = 40; // 格子大小
    private static final int PIECE_SIZE = 34; // 棋子大小

    // 落子动画
    private int animRow = -1;
    private int animCol = -1;
    private char animPiece;
    private int animStartY;
    private int animEndY;
    private double animProgress;
    private Timer dropTimer;
    
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
                SoundManager.play(STONE, PIECE_DROP);

                // 动画与状态更新
                startDropAnimation(row, col, board.isBlackTurn() ? GomokuBoard.WHITE : GomokuBoard.BLACK);
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
        int[] move = null;
        String thinking = "";
        
        // 根据AI类型选择不同的AI引擎
        if ("GomokuZero".equals(aiType) && gomokuZeroAI != null) {
            // 在后台线程中运行GomokuZero AI计算
            SwingUtilities.invokeLater(() -> {
                // 显示思考状态
                if (chatPanel != null) {
                    chatPanel.addChatMessage("GomokuZero", "🧠 正在使用蒙特卡洛树搜索分析局面...");
                }
                
                // 在新线程中计算，避免阻塞UI
                new Thread(() -> {
                    int[] aiMove = gomokuZeroAI.getBestMove(board);
                    String aiThinking = gomokuZeroAI.getThinkingProcess();
                    
                    SwingUtilities.invokeLater(() -> {
                        if (aiMove != null && aiMove.length == 2) {
                            executeAIMove(aiMove[0], aiMove[1]);
                            
                            // 显示GomokuZero的思考过程
                            if (chatPanel != null && aiThinking != null && !aiThinking.isEmpty()) {
                                chatPanel.addChatMessage("GomokuZero", aiThinking);
                            }
                        }
                    });
                }).start();
            });
            
        } else if (ai != null) {
            // 传统AI
            move = ai.getNextMove(board);
            if (move != null && move.length == 2) {
                executeAIMove(move[0], move[1]);
                
                // 显示传统AI的思考过程
                if (aiType.equals("大模型AI") || aiType.equals("混合AI")) {
                    thinking = ai.getThinking();
                    if (chatPanel != null && thinking != null && !thinking.isEmpty()) {
                        chatPanel.addChatMessage("AI", thinking);
                    }
                }
            }
        }
    }
    
    /**
     * 执行AI走法
     */
    private void executeAIMove(int row, int col) {
        boolean success = board.placePiece(row, col);
        if (success) {
            // 记录AI移动历史（用于悔棋）
            moveHistory.add(new GomokuMoveRecord(row, col, board.isBlackTurn() ? GomokuBoard.WHITE : GomokuBoard.BLACK));

            // 播放落子音效
            SoundManager.play(STONE, PIECE_DROP);

            // 动画与状态更新
            startDropAnimation(row, col, board.isBlackTurn() ? GomokuBoard.WHITE : GomokuBoard.BLACK);
            updateStatus();
        }
    }

    private void startDropAnimation(int row, int col, char piece) {
        animRow = row;
        animCol = col;
        animPiece = piece;
        animEndY = MARGIN + row * CELL_SIZE;
        animStartY = animEndY - CELL_SIZE * 3;
        animProgress = 0;
        if (dropTimer != null && dropTimer.isRunning()) {
            dropTimer.stop();
        }
        dropTimer = new Timer(15, e -> {
            animProgress += 0.1;
            if (animProgress >= 1) {
                animProgress = 1;
                dropTimer.stop();
                animRow = -1;
            }
            repaint();
        });
        dropTimer.start();
        repaint();
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
                    SoundManager.play(STONE, WIN);
                    showVictoryAnimation(status);
                    break;
                case RED_WINS: // 在五子棋中表示白方获胜
                    status = "⚪ 白方获胜！";
                    SoundManager.play(STONE, WIN);
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
            
            // 简化胜利显示，不使用复杂动画
            JLabel victoryLabel = new JLabel("游戏结束！", SwingConstants.CENTER);
            victoryLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
            victoryLabel.setForeground(Color.RED);
            victoryLabel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
            
            // 添加到玻璃面板
            Component glassPane = frame.getGlassPane();
            if (glassPane instanceof JComponent) {
                JComponent glass = (JComponent) glassPane;
                glass.setLayout(null);
                glass.add(victoryLabel);
                glass.setVisible(true);
            } else {
                // 创建新的玻璃面板
                JPanel glass = new JPanel();
                glass.setLayout(null);
                glass.setOpaque(false);
                glass.add(victoryLabel);
                frame.setGlassPane(glass);
                glass.setVisible(true);
            }
            
            // 简化胜利显示，直接显示标签
            victoryLabel.setText(message);
            victoryLabel.setVisible(true);
            
            // 5秒后自动关闭动画
            Timer closeTimer = new Timer(5000, e -> {
                victoryLabel.setVisible(false);
                if (glassPane instanceof JComponent) {
                    ((JComponent) glassPane).remove(victoryLabel);
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
                    if (dropTimer != null && dropTimer.isRunning() && row == animRow && col == animCol) {
                        continue; // 动画棋子稍后绘制
                    }
                    drawPiece(g2d, row, col, piece);
                }
            }
        }

        // 绘制动画棋子
        if (dropTimer != null && dropTimer.isRunning()) {
            int centerX = MARGIN + animCol * CELL_SIZE;
            int currentY = (int) (animStartY + (animEndY - animStartY) * (1 - Math.pow(1 - animProgress, 3)));
            drawPieceAt(g2d, centerX, currentY, animPiece);
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
     * 绘制棋子（专业立体效果 - 参考国际象棋和围棋）
     */
    private void drawPiece(Graphics2D g2d, int row, int col, char piece) {
        int centerX = MARGIN + col * CELL_SIZE;
        int centerY = MARGIN + row * CELL_SIZE;
        drawPieceAt(g2d, centerX, centerY, piece);
    }

    private void drawPieceAt(Graphics2D g2d, int centerX, int centerY, char piece) {
        int pieceRadius = PIECE_SIZE / 2;
        int x = centerX - pieceRadius;
        int y = centerY - pieceRadius;

        // 保存原始状态
        Composite originalComposite = g2d.getComposite();
        Stroke originalStroke = g2d.getStroke();
        Paint originalPaint = g2d.getPaint();

        // 绘制环境阴影（多层柔和阴影）
        drawEnvironmentShadow(g2d, centerX, centerY, PIECE_SIZE);

        if (piece == GomokuBoard.BLACK) {
            drawProfessionalBlackPiece(g2d, centerX, centerY, pieceRadius);
        } else {
            drawProfessionalWhitePiece(g2d, centerX, centerY, pieceRadius);
        }

        // 绘制表面光照效果
        drawSurfaceLighting(g2d, centerX, centerY, pieceRadius, piece == GomokuBoard.WHITE);

        // 恢复原始状态
        g2d.setComposite(originalComposite);
        g2d.setStroke(originalStroke);
        g2d.setPaint(originalPaint);
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
    
    /**
     * 绘制专业黑子（改进的深灰色而非纯黑）
     */
    private void drawProfessionalBlackPiece(Graphics2D g2d, int centerX, int centerY, int radius) {
        int size = radius * 2;
        int x = centerX - radius;
        int y = centerY - radius;
        
        // 绘制深度阴影
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillOval(x + 3, y + 3, size, size);
        
        // 绘制次级阴影
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval(x + 1, y + 1, size, size);
        
        // 主体渐变（改善的黑棋颜色）
        RadialGradientPaint blackGradient = new RadialGradientPaint(
            centerX - radius/3, centerY - radius/3, radius,
            new float[]{0f, 0.4f, 0.8f, 1f},
            new Color[]{
                new Color(130, 130, 135),  // 高光
                new Color(100, 100, 105),  // 中间调
                new Color(70, 70, 75),     // 暗部
                new Color(50, 50, 55)      // 最暗部
            }
        );
        g2d.setPaint(blackGradient);
        g2d.fillOval(x, y, size, size);
        
        // 内部高光圈
        g2d.setStroke(new BasicStroke(1f));
        g2d.setColor(new Color(160, 160, 165, 100));
        g2d.drawOval(x + 4, y + 4, size - 8, size - 8);
        
        // 主边框
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(40, 40, 45));
        g2d.drawOval(x, y, size, size);
        
        // 外边框高亮
        g2d.setStroke(new BasicStroke(1f));
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.drawOval(x - 1, y - 1, size + 2, size + 2);
    }
    
    /**
     * 绘制专业白子
     */
    private void drawProfessionalWhitePiece(Graphics2D g2d, int centerX, int centerY, int radius) {
        int size = radius * 2;
        int x = centerX - radius;
        int y = centerY - radius;
        
        // 绘制深度阴影
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillOval(x + 3, y + 3, size, size);
        
        // 绘制次级阴影
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval(x + 1, y + 1, size, size);
        
        // 主体渐变
        RadialGradientPaint whiteGradient = new RadialGradientPaint(
            centerX - radius/3, centerY - radius/3, radius,
            new float[]{0f, 0.4f, 0.8f, 1f},
            new Color[]{
                Color.WHITE,                    // 高光
                new Color(248, 248, 248),      // 中间调
                new Color(235, 235, 235),      // 暗部
                new Color(220, 220, 220)       // 最暗部
            }
        );
        g2d.setPaint(whiteGradient);
        g2d.fillOval(x, y, size, size);
        
        // 内部高光圈
        g2d.setStroke(new BasicStroke(1f));
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.drawOval(x + 4, y + 4, size - 8, size - 8);
        
        // 主边框
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(160, 160, 160));
        g2d.drawOval(x, y, size, size);
        
        // 外边框高亮
        g2d.setStroke(new BasicStroke(1f));
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.drawOval(x - 1, y - 1, size + 2, size + 2);
    }
    
    /**
     * 绘制环境阴影（柔和的远距离阴影）
     */
    private void drawEnvironmentShadow(Graphics2D g2d, int centerX, int centerY, int size) {
        int radius = size / 2;
        int shadowOffset = 4;
        int shadowSize = size + 8;
        
        // 绘制多层环境阴影，创造柔和的阴影效果
        for (int i = 3; i >= 0; i--) {
            int shadowAlpha = 8 + i * 6; // 递减的透明度
            int currentOffset = shadowOffset + i;
            int currentSize = shadowSize + i * 2;
            
            g2d.setColor(new Color(0, 0, 0, shadowAlpha));
            g2d.fillOval(
                centerX - currentSize / 2 + currentOffset,
                centerY - currentSize / 2 + currentOffset,
                currentSize,
                currentSize
            );
        }
    }
    
    /**
     * 绘制表面光照效果
     */
    private void drawSurfaceLighting(Graphics2D g2d, int centerX, int centerY, int radius, boolean isWhite) {
        // 光源位置（左上方）
        int lightX = centerX - radius / 2;
        int lightY = centerY - radius / 2;
        
        // 绘制主要高光
        RadialGradientPaint highlight = new RadialGradientPaint(
            lightX, lightY, radius / 3,
            new float[]{0f, 0.7f, 1f},
            new Color[]{
                new Color(255, 255, 255, isWhite ? 200 : 140),
                new Color(255, 255, 255, isWhite ? 100 : 60),
                new Color(255, 255, 255, 0)
            }
        );
        g2d.setPaint(highlight);
        g2d.fillOval(
            lightX - radius / 3,
            lightY - radius / 3,
            (radius * 2) / 3,
            (radius * 2) / 3
        );
        
        // 绘制边缘光晕
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(255, 255, 255, isWhite ? 80 : 40));
        g2d.drawOval(centerX - radius + 2, centerY - radius + 2, radius * 2 - 4, radius * 2 - 4);
        
        // 绘制反射光（底部右侧）
        int reflectX = centerX + radius / 3;
        int reflectY = centerY + radius / 3;
        g2d.setColor(new Color(255, 255, 255, isWhite ? 60 : 30));
        g2d.fillOval(
            reflectX - radius / 6,
            reflectY - radius / 6,
            radius / 3,
            radius / 3
        );
        
        // 重置画笔
        g2d.setStroke(new BasicStroke(1f));
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
        
        // 只在棋盘不为空且当前是AI回合时，让AI走棋
        if (enabled && !isBoardEmpty() && 
            ((isPlayerBlack && !board.isBlackTurn()) || (!isPlayerBlack && board.isBlackTurn()))) {
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
        if ("GomokuZero".equals(aiType)) {
            // 创建GomokuZero AI
            int difficultyLevel = getDifficultyLevel(difficulty);
            gomokuZeroAI = new GomokuZeroAI(difficultyLevel);
            System.out.println("🏆 GomokuZero AI 初始化完成，难度: " + difficulty + " (级别: " + difficultyLevel + ")");
        } else {
            // 使用传统AI
            ai = new GomokuAdvancedAI(difficulty);
            gomokuZeroAI = null;
        }
    }
    
    /**
     * 将难度字符串转换为数值级别
     */
    private int getDifficultyLevel(String difficulty) {
        switch (difficulty) {
            case "简单": return 2;
            case "中等": return 4;
            case "困难": return 6;
            case "专家": return 8;
            case "大师": return 10;
            default: return 5;
        }
    }
    
    /**
     * 检查棋盘是否为空
     */
    private boolean isBoardEmpty() {
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) != ' ') {
                    return false;
                }
            }
        }
        return true;
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
        
        // 重置时不自动让AI走棋，等待玩家先手
        // 注意：五子棋黑棋先手，所以重置后应该是黑方下棋
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