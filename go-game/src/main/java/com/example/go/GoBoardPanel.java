package com.example.go;

import com.example.common.utils.ExceptionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 围棋棋盘UI面板
 */
public class GoBoardPanel extends JPanel {
    private static final int BOARD_SIZE = 19;
    private static final int CELL_SIZE = 30;
    private static final int MARGIN = 40;
    private static final int STONE_RADIUS = 12;
    
    private GoGame game;
    private GoAI ai;
    private KataGoAI kataGoAI;
    private boolean aiEnabled = false;
    private int aiPlayer = GoGame.WHITE;
    private int difficulty = 3;
    
    // AI对AI模式相关
    private boolean aiVsAIMode = false;
    private GoAI blackAI;
    private GoAI whiteAI;
    private KataGoAI blackKataGoAI;
    private KataGoAI whiteKataGoAI;
    private Timer aiVsAITimer;
    private boolean useKataGo = false;
    
    // UI状态
    private GoPosition lastMove;
    private boolean showCoordinates = true;
    private boolean thinking = false;
    
    // 回调接口
    public interface GameStateCallback {
        void onGameStateChanged(String status);
        void onMoveCount(int blackCaptured, int whiteCaptured);
        void onTitleUpdateNeeded();
    }
    
    private GameStateCallback callback;
    
    public GoBoardPanel() {
        this.game = new GoGame();
        setPreferredSize(new Dimension(
            BOARD_SIZE * CELL_SIZE + 2 * MARGIN,
            BOARD_SIZE * CELL_SIZE + 2 * MARGIN + 50
        ));
        setBackground(new Color(220, 179, 92)); // 棋盘颜色
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 只有在非AI对AI模式下，且不是在思考中，且游戏未结束时才处理鼠标点击
                if (!aiVsAIMode && !thinking && !game.isGameEnded()) {
                    handleMouseClick(e.getX(), e.getY());
                }
            }
        });
    }
    
    /**
     * 设置游戏状态回调
     */
    public void setGameStateCallback(GameStateCallback callback) {
        this.callback = callback;
    }
    
    /**
     * 启用/禁用AI
     */
    public void setAIEnabled(boolean enabled, int aiPlayer, int difficulty) {
        setAIEnabled(enabled, aiPlayer, difficulty, false);
    }
    
    /**
     * 启用/禁用AI - 支持KataGo模式
     */
    public void setAIEnabled(boolean enabled, int aiPlayer, int difficulty, boolean useKataGo) {
        System.out.println("🎯 GoBoardPanel.setAIEnabled - enabled: " + enabled + ", aiPlayer: " + (aiPlayer == GoGame.BLACK ? "黑棋" : "白棋") + ", difficulty: " + difficulty + ", useKataGo: " + useKataGo);
        
        // 先禁用AI对AI模式
        if (aiVsAIMode) {
            disableAIvsAI();
        }
        
        this.aiEnabled = enabled;
        this.aiPlayer = aiPlayer;
        this.difficulty = difficulty;
        
        if (enabled) {
            if (useKataGo && kataGoAI != null) {
                System.out.println("✅ 使用KataGo AI作为主要AI");
                // 使用KataGo AI，不创建传统AI
                this.ai = null;
            } else {
                System.out.println("⚙️ 使用传统AI");
                // 使用传统AI
                this.ai = new GoAI(aiPlayer, difficulty);
            }
            
            // 如果AI是黑棋且是开局，让AI先走
            if (aiPlayer == GoGame.BLACK && game.getMoveHistory().isEmpty()) {
                SwingUtilities.invokeLater(this::makeAIMove);
            }
        } else {
            this.ai = null;
        }
        
        updateGameState();
    }
    
    /**
     * 重新开始游戏
     */
    public void restartGame() {
        game.restart();
        lastMove = null;
        thinking = false;
        
        // 如果AI是黑棋，让AI先走
        if (aiEnabled && aiPlayer == GoGame.BLACK) {
            SwingUtilities.invokeLater(this::makeAIMove);
        }
        
        updateGameState();
        repaint();
    }
    
    /**
     * 悔棋
     */
    public void undoMove() {
        if (game.undoMove()) {
            // 如果启用了AI，可能需要再悔一步（悔掉AI的移动）
            if (aiEnabled && !game.getMoveHistory().isEmpty()) {
                List<GoMove> history = game.getMoveHistory();
                GoMove lastMove = history.get(history.size() - 1);
                if (lastMove.player == aiPlayer) {
                    game.undoMove();
                }
            }
            
            updateLastMove();
            updateGameState();
            repaint();
        }
    }
    
    /**
     * 弃权
     */
    public void pass() {
        if (!game.isGameEnded()) {
            game.pass();
            updateGameState();
            
            // 如果启用AI且轮到AI，让AI走
            if (aiEnabled && game.getCurrentPlayer() == aiPlayer && !game.isGameEnded()) {
                SwingUtilities.invokeLater(this::makeAIMove);
            }
            
            repaint();
        }
    }
    
    /**
     * 处理鼠标点击
     */
    private void handleMouseClick(int x, int y) {
        GoPosition pos = getPositionFromCoordinates(x, y);
        if (pos != null && game.isValidMove(pos.row, pos.col)) {
            if (game.makeMove(pos.row, pos.col)) {
                lastMove = pos;
                updateGameState();
                repaint();
                
                // 如果启用AI且轮到AI，让AI走
                if (aiEnabled && game.getCurrentPlayer() == aiPlayer && !game.isGameEnded()) {
                    SwingUtilities.invokeLater(this::makeAIMove);
                }
            }
        }
    }
    
    /**
     * AI移动
     */
    private void makeAIMove() {
        // 检查AI是否可用（传统AI或KataGo AI）
        if (!aiEnabled || game.isGameEnded() || game.getCurrentPlayer() != aiPlayer) {
            System.out.println("❌ makeAIMove 被阻止 - aiEnabled: " + aiEnabled + ", 游戏结束: " + game.isGameEnded() + ", 当前玩家: " + (game.getCurrentPlayer() == GoGame.BLACK ? "黑棋" : "白棋") + ", AI玩家: " + (aiPlayer == GoGame.BLACK ? "黑棋" : "白棋"));
            return;
        }
        
        // 检查是否有可用的AI引擎
        if (ai == null && kataGoAI == null) {
            System.out.println("❌ 没有可用的AI引擎");
            return;
        }
        
        System.out.println("🤖 AI开始移动 - 使用: " + (kataGoAI != null && ai == null ? "KataGo AI" : "传统AI"));
        
        thinking = true;
        updateGameState();
        
        // 在后台线程中计算AI移动
        SwingWorker<GoPosition, Void> worker = new SwingWorker<GoPosition, Void>() {
            @Override
            protected GoPosition doInBackground() throws Exception {
                Thread.sleep(500); // 模拟思考时间
                
                // 优先使用KataGo AI（如果可用且没有传统AI）
                if (kataGoAI != null && ai == null) {
                    System.out.println("✅ 使用KataGo AI计算移动");
                    return kataGoAI.calculateBestMove(game.getBoard(), game.getCurrentPlayer());
                } else if (ai != null) {
                    System.out.println("⚙️ 使用传统AI计算移动");
                    return ai.calculateMove(game);
                } else {
                    System.out.println("❌ 没有AI引擎可用");
                    return null;
                }
            }
            
            @Override
            protected void done() {
                try {
                    GoPosition aiMove = get();
                    thinking = false;
                    
                    if (aiMove != null) {
                        if (game.makeMove(aiMove.row, aiMove.col)) {
                            lastMove = aiMove;
                            // 显示数字坐标
                            int displayRow = GoGame.BOARD_SIZE - aiMove.row;
                            int displayCol = aiMove.col + 1;
                            System.out.println("✅ AI落子成功: (" + displayRow + "," + displayCol + ")");
                        } else {
                            System.out.println("❌ AI落子失败");
                        }
                    } else {
                        // AI选择弃权
                        game.pass();
                        lastMove = null;
                        System.out.println("🏳️ AI选择弃权");
                    }
                    
                    updateGameState();
                    repaint();
                } catch (Exception e) {
                    thinking = false;
                    updateGameState();
                    System.err.println("AI移动异常: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 从屏幕坐标获取棋盘位置
     */
    private GoPosition getPositionFromCoordinates(int x, int y) {
        int col = (x - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
        int row = (y - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
        
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            return new GoPosition(row, col);
        }
        return null;
    }
    
    /**
     * 更新最后一步移动
     */
    private void updateLastMove() {
        List<GoMove> history = game.getMoveHistory();
        if (!history.isEmpty()) {
            GoMove lastMoveRecord = history.get(history.size() - 1);
            if (!lastMoveRecord.isPass()) {
                lastMove = lastMoveRecord.position;
            } else {
                lastMove = null;
            }
        } else {
            lastMove = null;
        }
    }
    
    /**
     * 更新游戏状态
     */
    private void updateGameState() {
        if (callback != null) {
            String status;
            if (game.isGameEnded()) {
                status = "游戏结束";
            } else if (thinking) {
                if (aiVsAIMode) {
                    String currentPlayerName = (game.getCurrentPlayer() == GoGame.BLACK) ? "黑棋" : "白棋";
                    status = currentPlayerName + " AI思考中...";
                } else {
                    status = "AI思考中...";
                }
            } else {
                String currentPlayerName = (game.getCurrentPlayer() == GoGame.BLACK) ? "黑棋" : "白棋";
                if (aiVsAIMode) {
                    status = "AI对AI模式 - " + currentPlayerName + " AI 回合";
                } else if (aiEnabled && game.getCurrentPlayer() == aiPlayer) {
                    status = currentPlayerName + " (AI) 回合";
                } else {
                    status = currentPlayerName + " 回合";
                }
            }
            
            callback.onGameStateChanged(status);
            callback.onMoveCount(game.getBlackCaptured(), game.getWhiteCaptured());
            callback.onTitleUpdateNeeded();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawBoard(g2d);
        drawStones(g2d);
        drawCoordinates(g2d);
        
        if (lastMove != null) {
            drawLastMoveMarker(g2d);
        }
        
        g2d.dispose();
    }
    
    /**
     * 绘制棋盘
     */
    private void drawBoard(Graphics2D g2d) {
        // 绘制棋盘背景
        g2d.setColor(new Color(220, 179, 92));
        g2d.fillRect(MARGIN - 15, MARGIN - 15, 
                    BOARD_SIZE * CELL_SIZE + 30, 
                    BOARD_SIZE * CELL_SIZE + 30);
        
        // 绘制棋盘线条
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        
        // 绘制横线
        for (int i = 0; i < BOARD_SIZE; i++) {
            int y = MARGIN + i * CELL_SIZE;
            g2d.drawLine(MARGIN, y, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE, y);
        }
        
        // 绘制竖线
        for (int i = 0; i < BOARD_SIZE; i++) {
            int x = MARGIN + i * CELL_SIZE;
            g2d.drawLine(x, MARGIN, x, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE);
        }
        
        // 绘制星位点
        drawStarPoints(g2d);
    }
    
    /**
     * 绘制星位点
     */
    private void drawStarPoints(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        int[] starPositions = {3, 9, 15}; // 19路棋盘的星位
        
        for (int row : starPositions) {
            for (int col : starPositions) {
                int x = MARGIN + col * CELL_SIZE;
                int y = MARGIN + row * CELL_SIZE;
                g2d.fillOval(x - 3, y - 3, 6, 6);
            }
        }
        
        // 天元
        int centerX = MARGIN + 9 * CELL_SIZE;
        int centerY = MARGIN + 9 * CELL_SIZE;
        g2d.fillOval(centerX - 3, centerY - 3, 6, 6);
    }
    
    /**
     * 绘制棋子
     */
    private void drawStones(Graphics2D g2d) {
        int[][] board = game.getBoard();
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] != GoGame.EMPTY) {
                    int x = MARGIN + col * CELL_SIZE;
                    int y = MARGIN + row * CELL_SIZE;
                    
                    // 绘制棋子阴影
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.fillOval(x - STONE_RADIUS + 2, y - STONE_RADIUS + 2, 
                               STONE_RADIUS * 2, STONE_RADIUS * 2);
                    
                    // 绘制棋子
                    if (board[row][col] == GoGame.BLACK) {
                        // 黑棋渐变
                        RadialGradientPaint gradient = new RadialGradientPaint(
                            x - 3, y - 3, STONE_RADIUS,
                            new float[]{0.0f, 1.0f},
                            new Color[]{new Color(80, 80, 80), Color.BLACK}
                        );
                        g2d.setPaint(gradient);
                    } else {
                        // 白棋渐变
                        RadialGradientPaint gradient = new RadialGradientPaint(
                            x - 3, y - 3, STONE_RADIUS,
                            new float[]{0.0f, 1.0f},
                            new Color[]{Color.WHITE, new Color(200, 200, 200)}
                        );
                        g2d.setPaint(gradient);
                    }
                    
                    g2d.fillOval(x - STONE_RADIUS, y - STONE_RADIUS, 
                               STONE_RADIUS * 2, STONE_RADIUS * 2);
                    
                    // 绘制棋子边框
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1.0f));
                    g2d.drawOval(x - STONE_RADIUS, y - STONE_RADIUS, 
                               STONE_RADIUS * 2, STONE_RADIUS * 2);
                }
            }
        }
    }
    
    /**
     * 绘制坐标
     */
    private void drawCoordinates(Graphics2D g2d) {
        if (!showCoordinates) return;
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        
        // 绘制列坐标 (1-19) - 横坐标
        for (int i = 0; i < BOARD_SIZE; i++) {
            String label = String.valueOf(i + 1); // 1-19
            int x = MARGIN + i * CELL_SIZE;
            int labelWidth = fm.stringWidth(label);
            
            // 上方坐标
            g2d.drawString(label, x - labelWidth / 2, MARGIN - 10);
            // 下方坐标
            g2d.drawString(label, x - labelWidth / 2, 
                          MARGIN + BOARD_SIZE * CELL_SIZE + 20);
        }
        
        // 绘制行坐标 (1-19) - 纵坐标
        for (int i = 0; i < BOARD_SIZE; i++) {
            String label = String.valueOf(BOARD_SIZE - i); // 19-1 (从上到下)
            int y = MARGIN + i * CELL_SIZE;
            int labelWidth = fm.stringWidth(label);
            
            // 左侧坐标
            g2d.drawString(label, MARGIN - labelWidth - 10, y + 4);
            // 右侧坐标
            g2d.drawString(label, MARGIN + BOARD_SIZE * CELL_SIZE + 10, y + 4);
        }
    }
    
    /**
     * 绘制最后一步移动标记
     */
    private void drawLastMoveMarker(Graphics2D g2d) {
        if (lastMove == null) return;
        
        int x = MARGIN + lastMove.col * CELL_SIZE;
        int y = MARGIN + lastMove.row * CELL_SIZE;
        
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(x - STONE_RADIUS - 2, y - STONE_RADIUS - 2, 
                   (STONE_RADIUS + 2) * 2, (STONE_RADIUS + 2) * 2);
    }
    
    // Getter方法
    public GoGame getGame() {
        return game;
    }
    
    public boolean isAIEnabled() {
        return aiEnabled;
    }
    
    public int getAIPlayer() {
        return aiPlayer;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setShowCoordinates(boolean show) {
        this.showCoordinates = show;
        repaint();
    }
    
    /**
     * 设置KataGo AI引擎
     */
    public void setKataGoAI(KataGoAI kataGoAI) {
        this.kataGoAI = kataGoAI;
        ExceptionHandler.logInfo("GoBoardPanel", "🎯 设置KataGo AI引擎: " + (kataGoAI != null ? "已设置" : "空值"));
    }
    
    /**
     * 设置标准Go AI引擎
     */
    public void setGoAI(GoAI goAI) {
        this.ai = goAI;
    }
    
    /**
     * 启用AI对AI模式
     */
    public void enableAIvsAI(int difficulty, String aiType) {
        System.out.println("🚀 开始启用AI对AI模式 - 雾度: " + difficulty + ", AI类型: " + aiType);
        
        // 不重置游戏状态，保持当前棋局继续
        thinking = false;
        
        aiVsAIMode = true;
        aiEnabled = false; // 禁用单个AI模式
        
        // 根据AI类型创建双方AI
        if ("KataGo AI".equals(aiType)) {
            useKataGo = true;
            System.out.println("⚙️ 尝试使用KataGo AI - kataGoAI存在: " + (kataGoAI != null));
            // 如果有KataGo引擎，创建两个实例
            if (kataGoAI != null) {
                blackKataGoAI = new KataGoAI(difficulty);
                whiteKataGoAI = new KataGoAI(difficulty);
                blackKataGoAI.initializeEngine();
                whiteKataGoAI.initializeEngine();
                System.out.println("✅ 创建了两个KataGo AI实例");
            } else {
                // 回退到传统AI
                System.out.println("⚠️ KataGo不可用，回退到传统AI");
                blackAI = new GoAI(GoGame.BLACK, difficulty);
                whiteAI = new GoAI(GoGame.WHITE, difficulty);
                useKataGo = false;
            }
        } else {
            useKataGo = false;
            System.out.println("⚙️ 使用传统AI");
            // 传统AI
            blackAI = new GoAI(GoGame.BLACK, difficulty);
            whiteAI = new GoAI(GoGame.WHITE, difficulty);
        }
        
        System.out.println("🎮 AI对AI模式设置完成 - aiVsAIMode: " + aiVsAIMode + ", useKataGo: " + useKataGo);
        System.out.println("🎮 AI实例 - blackAI: " + (blackAI != null) + ", whiteAI: " + (whiteAI != null));
        
        // 开始AI对AI对弈
        startAIvsAIGame();
    }
    
    /**
     * 禁用AI对AI模式
     */
    public void disableAIvsAI() {
        aiVsAIMode = false;
        
        if (aiVsAITimer != null) {
            aiVsAITimer.stop();
            aiVsAITimer = null;
        }
        
        // 清理AI实例
        blackAI = null;
        whiteAI = null;
        
        if (blackKataGoAI != null) {
            blackKataGoAI.shutdownEngine();
            blackKataGoAI = null;
        }
        if (whiteKataGoAI != null) {
            whiteKataGoAI.shutdownEngine();
            whiteKataGoAI = null;
        }
        
        thinking = false;
        updateGameState();
    }
    
    /**
     * 开始AI对AI游戏
     */
    private void startAIvsAIGame() {
        System.out.println("🎮 尝试开始AI对AI游戏 - aiVsAIMode: " + aiVsAIMode + ", 游戏结束: " + game.isGameEnded());
        
        if (!aiVsAIMode || game.isGameEnded()) {
            System.out.println("❌ 无法开始AI对AI游戏 - 条件不满足");
            return;
        }
        
        System.out.println("✅ AI对AI游戏条件满足，开始游戏");
        System.out.println("📊 棋局历史长度: " + game.getMoveHistory().size());
        
        // 如果是开局，黑棋先走
        if (game.getMoveHistory().isEmpty()) {
            System.out.println("🏁 开局状态，黑棋先手");
            SwingUtilities.invokeLater(() -> executeAIvsAIMove());
        } else {
            System.out.println("🔄 继续游戏");
            SwingUtilities.invokeLater(() -> executeAIvsAIMove());
        }
    }
    
    /**
     * 执行AI对AI移动
     */
    private void executeAIvsAIMove() {
        if (!aiVsAIMode || game.isGameEnded()) {
            return;
        }
        
        thinking = true;
        updateGameState();
        
        // 确定当前AI
        int currentPlayer = game.getCurrentPlayer();
        boolean isBlackTurn = (currentPlayer == GoGame.BLACK);
        
        // 在后台线程中计算AI移动
        SwingWorker<GoPosition, Void> worker = new SwingWorker<GoPosition, Void>() {
            @Override
            protected GoPosition doInBackground() throws Exception {
                Thread.sleep(1000); // AI思考时间
                
                GoPosition move = null;
                if (useKataGo) {
                    // 使用KataGo
                    KataGoAI currentKataGoAI = isBlackTurn ? blackKataGoAI : whiteKataGoAI;
                    if (currentKataGoAI != null) {
                        move = currentKataGoAI.calculateBestMove(game.getBoard(), currentPlayer);
                    }
                } else {
                    // 使用传统AI
                    GoAI currentAI = isBlackTurn ? blackAI : whiteAI;
                    if (currentAI != null) {
                        move = currentAI.calculateMove(game);
                    }
                }
                
                return move;
            }
            
            @Override
            protected void done() {
                try {
                    GoPosition aiMove = get();
                    thinking = false;
                    
                    if (aiMove != null) {
                        if (game.makeMove(aiMove.row, aiMove.col)) {
                            lastMove = aiMove;
                            // 使用数字坐标显示移动
                            int displayRow = GoGame.BOARD_SIZE - aiMove.row; // 19-1 (从上到下)
                            int displayCol = aiMove.col + 1; // 1-19 (从左到右)
                            System.out.println("AI移动: " + (isBlackTurn ? "黑棋" : "白棋") + " -> 位置(" + displayRow + "," + displayCol + ")");
                        }
                    } else {
                        // AI选择弃权
                        game.pass();
                        lastMove = null;
                        System.out.println("AI弃权: " + (isBlackTurn ? "黑棋" : "白棋"));
                    }
                    
                    updateGameState();
                    repaint();
                    
                    // 如果游戏未结束，继续下一步AI移动
                    if (!game.isGameEnded() && aiVsAIMode) {
                        // 延迟后继续下一步
                        Timer continueTimer = new Timer(1500, e -> {
                            SwingUtilities.invokeLater(() -> executeAIvsAIMove());
                        });
                        continueTimer.setRepeats(false);
                        continueTimer.start();
                    } else if (game.isGameEnded()) {
                        System.out.println("AI对AI游戏结束");
                        disableAIvsAI();
                    }
                    
                } catch (Exception e) {
                    thinking = false;
                    updateGameState();
                    System.err.println("AI对AI移动失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 检查是否处于AI对AI模式
     */
    public boolean isAIvsAIMode() {
        return aiVsAIMode;
    }
    
    /**
     * 重置AI对AI游戏
     */
    public void restartAIvsAIGame() {
        if (aiVsAIMode) {
            game.restart();
            lastMove = null;
            thinking = false;
            updateGameState();
            repaint();
            
            // 重新开始AI对弈
            SwingUtilities.invokeLater(() -> executeAIvsAIMove());
        }
    }
}
