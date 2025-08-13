package com.example.internationalchess.ui;

import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.core.PieceColor;
import com.example.internationalchess.core.GameState;
import com.example.internationalchess.ai.InternationalChessAI;
import com.example.internationalchess.ai.StockfishAIAdapter;
import com.example.internationalchess.ui.StockfishLogPanel;
import com.example.common.sound.SoundPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * 国际象棋棋盘面板
 */
public class InternationalBoardPanel extends JPanel {
    
    private static final int BOARD_SIZE = 8;
    private static final int CELL_SIZE = 70;
    private static final Color LIGHT_COLOR = new Color(240, 217, 181);
    private static final Color DARK_COLOR = new Color(181, 136, 99);
    private static final Color SELECTED_COLOR = new Color(255, 255, 0, 128);
    private static final Color POSSIBLE_MOVE_COLOR = new Color(0, 255, 0, 128);
    
    private InternationalChessBoard board;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean aiEnabled = false;
    private char humanPlayer = 'W'; // 默认人类玩家为白方
    private Consumer<String> statusUpdateCallback;
    private ChatPanel chatPanel;
    private StockfishLogPanel stockfishLogPanel;
    
    // AI相关
    private InternationalChessAI ai;
    private StockfishAIAdapter stockfishAI;
    private String aiType = "Stockfish";
    private int difficulty = 2; // 默认中等难度
    
    public InternationalBoardPanel() {
        this.board = new InternationalChessBoard();
        setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawBoard(g2d);
        drawPieces(g2d);
        drawSelection(g2d);
    }
    
    private void drawBoard(Graphics2D g2d) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Color cellColor = (row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
                g2d.setColor(cellColor);
                g2d.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                
                // 绘制边框
                g2d.setColor(Color.BLACK);
                g2d.drawRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }
    
    private void drawPieces(Graphics2D g2d) {
        g2d.setFont(new Font("Arial Unicode MS", Font.BOLD, 40));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fm = g2d.getFontMetrics();
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String piece = board.getPiece(row, col);
                if (piece != null && !piece.trim().isEmpty()) {
                    String symbol = getPieceSymbol(piece);
                    if (!symbol.isEmpty()) {
                        int centerX = col * CELL_SIZE + CELL_SIZE / 2;
                        int centerY = row * CELL_SIZE + CELL_SIZE / 2;
                        
                        // 绘制3D立体棋子
                        draw3DPiece(g2d, symbol, centerX, centerY, piece.charAt(0) == 'W', fm);
                    }
                }
            }
        }
    }
    
    /**
     * 绘制3D立体效果的棋子
     */
    private void draw3DPiece(Graphics2D g2d, String symbol, int centerX, int centerY, boolean isWhite, FontMetrics fm) {
        int symbolWidth = fm.stringWidth(symbol);
        int symbolHeight = fm.getHeight();
        int x = centerX - symbolWidth / 2;
        int y = centerY + fm.getAscent() / 2;
        
        // 阴影偏移量
        int shadowOffset = 3;
        
        // 绘制阴影（右下角）
        g2d.setColor(new Color(0, 0, 0, 60)); // 半透明黑色阴影
        g2d.drawString(symbol, x + shadowOffset, y + shadowOffset);
        
        if (isWhite) {
            // 白棋：绘制立体效果
            // 1. 绘制深色轮廓（左上角高光的反面）
            g2d.setColor(new Color(120, 120, 120));
            g2d.drawString(symbol, x + 1, y + 1);
            
            // 2. 绘制主体（白色）
            g2d.setColor(new Color(250, 250, 250));
            g2d.drawString(symbol, x, y);
            
            // 3. 绘制高光（左上角）
            g2d.setColor(Color.WHITE);
            g2d.drawString(symbol, x - 1, y - 1);
            
            // 4. 绘制最终轮廓
            g2d.setColor(new Color(80, 80, 80));
            g2d.setStroke(new BasicStroke(0.5f));
            // 使用细线描边增强立体感
            drawOutlineText(g2d, symbol, x, y, fm);
        } else {
            // 黑棋：绘制立体效果
            // 1. 绘制深色基底
            g2d.setColor(new Color(40, 40, 40));
            g2d.drawString(symbol, x + 1, y + 1);
            
            // 2. 绘制主体（黑色）
            g2d.setColor(new Color(50, 50, 50));
            g2d.drawString(symbol, x, y);
            
            // 3. 绘制高光（左上角）
            g2d.setColor(new Color(120, 120, 120));
            g2d.drawString(symbol, x - 1, y - 1);
            
            // 4. 绘制最终轮廓
            g2d.setColor(Color.BLACK);
            drawOutlineText(g2d, symbol, x, y, fm);
        }
    }
    
    /**
     * 绘制文字轮廓
     */
    private void drawOutlineText(Graphics2D g2d, String text, int x, int y, FontMetrics fm) {
        // 绘制细致的轮廓线以增强立体效果
        Color originalColor = g2d.getColor();
        Stroke originalStroke = g2d.getStroke();
        
        g2d.setStroke(new BasicStroke(1.0f));
        
        // 8个方向的轮廓
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        
        for (int i = 0; i < dx.length; i++) {
            g2d.drawString(text, x + dx[i], y + dy[i]);
        }
        
        g2d.setStroke(originalStroke);
    }
    
    private void drawSelection(Graphics2D g2d) {
        if (selectedRow >= 0 && selectedCol >= 0) {
            g2d.setColor(SELECTED_COLOR);
            g2d.fillRect(selectedCol * CELL_SIZE, selectedRow * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }
    
    private String getPieceSymbol(String piece) {
        if (piece == null || piece.length() < 2) return "";
        
        char color = piece.charAt(0);
        char type = piece.charAt(1);
        
        switch (type) {
            case 'K': return color == 'W' ? "♔" : "♚"; // 王
            case 'Q': return color == 'W' ? "♕" : "♛"; // 后
            case 'R': return color == 'W' ? "♖" : "♜"; // 车
            case 'B': return color == 'W' ? "♗" : "♝"; // 象
            case 'N': return color == 'W' ? "♘" : "♞"; // 马
            case 'P': return color == 'W' ? "♙" : "♟"; // 兵
            default: return "";
        }
    }
    
    private void handleMouseClick(int x, int y) {
        // 如果游戏暂停，禁用所有操作
        if (isPaused) {
            updateStatus("⏸️ 游戏已暂停，请先继续游戏");
            return;
        }
        
        // 如果AI启用且当前不是人类玩家回合，忽略点击
        if (aiEnabled && !isHumanTurn()) {
            updateStatus("等待AI走棋...");
            return;
        }
        
        int col = x / CELL_SIZE;
        int row = y / CELL_SIZE;
        
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return;
        }
        
        if (selectedRow == -1 && selectedCol == -1) {
            // 选择棋子
            String piece = board.getPiece(row, col);
            if (piece != null && !piece.trim().isEmpty()) {
                // 检查是否是当前玩家的棋子
                char pieceColor = piece.charAt(0);
                boolean isWhiteTurn = board.isWhiteTurn();
                if ((isWhiteTurn && pieceColor == 'W') || (!isWhiteTurn && pieceColor == 'B')) {
                    selectedRow = row;
                    selectedCol = col;
                    updateStatus("已选择棋子: " + piece);
                    repaint();
                } else {
                    updateStatus("不能选择对方的棋子！");
                }
            }
        } else {
            // 尝试移动
            if (board.isValidMove(selectedRow, selectedCol, row, col)) {
                if (board.movePiece(selectedRow, selectedCol, row, col)) {
                    SoundPlayer.getInstance().playSound("piece_drop");
                    updateStatus("移动成功");
                    
                    // 检查游戏状态
                    checkGameState();
                    
                    // 如果AI启用且游戏仍在进行，让AI走棋
                    if (aiEnabled && board.getGameState() == GameState.PLAYING) {
                        SwingUtilities.invokeLater(this::makeAIMove);
                    }
                } else {
                    SoundPlayer.getInstance().playSound("invalid");
                    updateStatus("移动失败");
                }
            } else {
                SoundPlayer.getInstance().playSound("invalid");
                updateStatus("无效移动");
            }
            
            // 清除选择
            selectedRow = -1;
            selectedCol = -1;
            repaint();
        }
    }
    
    /**
     * 检查是否是人类玩家的回合
     */
    private boolean isHumanTurn() {
        boolean isWhiteTurn = board.isWhiteTurn();
        return (humanPlayer == 'W' && isWhiteTurn) || (humanPlayer == 'B' && !isWhiteTurn);
    }
    
    // AI vs AI 相关变量
    private boolean isAIvsAIMode = false;
    private Timer aiVsAiTimer;
    private InternationalChessAI whiteAI;
    private InternationalChessAI blackAI;
    private StockfishAIAdapter whiteStockfishAI;
    private StockfishAIAdapter blackStockfishAI;
    
    // 暂停状态管理
    private boolean isPaused = false;
    private SwingWorker<int[], Void> currentAIWorker = null;
    
    /**
     * 让AI走棋
     */
    private void makeAIMove() {
        if (!aiEnabled || board.getGameState() != GameState.PLAYING || isPaused) {
            return;
        }
        
        // AI vs AI 模式
        if (isAIvsAIMode) {
            makeAIvsAIMove();
            return;
        }
        
        // 单个AI模式
        if ((!("Stockfish".equals(aiType)) && ai == null) || 
            ("Stockfish".equals(aiType) && stockfishAI == null)) {
            return;
        }
        
        updateStatus("🤖 AI正在思考...");
        
        // 在新线程中计算AI移动，避免阻塞UI
        currentAIWorker = new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                try {
                    if ("Stockfish".equals(aiType) && stockfishAI != null) {
                        int[] move = stockfishAI.calculateNextMove(board);
                        if (move != null && stockfishLogPanel != null) {
                            logAIDecision("Stockfish", move, "Stockfish引擎计算的最佳移动");
                        }
                        return move;
                    } else if (ai != null) {
                        int[] move = ai.calculateNextMove(board);
                        if (move != null && stockfishLogPanel != null) {
                            logAIDecision("传统AI", move, "基于评估函数的最佳移动");
                        }
                        return move;
                    }
                } catch (Exception e) {
                    System.err.println("AI计算移动时出错: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    int[] move = get();
                    if (move != null && move.length == 4) {
                        executeAIMove(move);
                    } else {
                        updateStatus("❌ AI无法找到有效移动");
                        if (stockfishLogPanel != null) {
                            stockfishLogPanel.addGameEvent("AI无法找到有效移动，可能是游戏结束");
                        }
                    }
                } catch (Exception e) {
                    updateStatus("❌ AI计算出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        currentAIWorker.execute();
    }
    
    /**
     * AI vs AI 移动
     */
    private void makeAIvsAIMove() {
        if (!isAIvsAIMode || board.getGameState() != GameState.PLAYING || isPaused) {
            return;
        }
        
        boolean isWhiteTurn = board.isWhiteTurn();
        String currentPlayer = isWhiteTurn ? "白方AI" : "黑方AI";
        updateStatus("🤖🆚🤖 " + currentPlayer + "正在思考...");
        
        currentAIWorker = new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                try {
                    int[] move = null;
                    if (isWhiteTurn) {
                        // 白方AI移动
                        if ("Stockfish".equals(aiType) && whiteStockfishAI != null) {
                            move = whiteStockfishAI.calculateNextMove(board);
                        } else if (whiteAI != null) {
                            move = whiteAI.calculateNextMove(board);
                        }
                    } else {
                        // 黑方AI移动
                        if ("Stockfish".equals(aiType) && blackStockfishAI != null) {
                            move = blackStockfishAI.calculateNextMove(board);
                        } else if (blackAI != null) {
                            move = blackAI.calculateNextMove(board);
                        }
                    }
                    
                    if (move != null && stockfishLogPanel != null) {
                        logAIDecision(currentPlayer, move, "AI vs AI 模式下的计算移动");
                    }
                    return move;
                } catch (Exception e) {
                    System.err.println("AI vs AI计算移动时出错: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            
            @Override
            protected void done() {
                try {
                    int[] move = get();
                    if (move != null && move.length == 4) {
                        executeAIMove(move);
                        
                        // 继续 AI vs AI游戏循环
                        if (isAIvsAIMode && board.getGameState() == GameState.PLAYING) {
                            // 延迟1.5秒后进行下一步
                            Timer nextMoveTimer = new Timer(1500, e -> makeAIvsAIMove());
                            nextMoveTimer.setRepeats(false);
                            nextMoveTimer.start();
                        }
                    } else {
                        updateStatus("❌ " + currentPlayer + "无法找到有效移动");
                        pauseAIvsAI();
                    }
                } catch (Exception e) {
                    updateStatus("❌ " + currentPlayer + "计算出错: " + e.getMessage());
                    e.printStackTrace();
                    pauseAIvsAI();
                }
            }
        };
        currentAIWorker.execute();
    }
    
    /**
     * 暂停AI vs AI模式
     */
    private void pauseAIvsAI() {
        if (aiVsAiTimer != null) {
            aiVsAiTimer.stop();
        }
        updateStatus("AI vs AI游戏已暂停");
    }
    
    /**
     * 执行AI移动
     */
    private void executeAIMove(int[] move) {
        int fromRow = move[0];
        int fromCol = move[1];
        int toRow = move[2];
        int toCol = move[3];
        
        // 获取移动的棋子信息
        String piece = board.getPiece(fromRow, fromCol);
        String targetPiece = board.getPiece(toRow, toCol);
        boolean isCapture = targetPiece != null;
        
        if (board.movePiece(fromRow, fromCol, toRow, toCol)) {
            SoundPlayer.getInstance().playSound("piece_drop");
            
            // 生成移动描述
            String moveDescription = generateMoveDescription(piece, fromRow, fromCol, toRow, toCol, isCapture, targetPiece);
            updateStatus("✅ " + moveDescription);
            
            // 在Stockfish日志面板中显示AI建议
            logDetailedAIRecommendation(moveDescription, piece, fromRow, fromCol, toRow, toCol, isCapture, targetPiece);
            
            if (stockfishLogPanel != null) {
                stockfishLogPanel.addGameEvent("移动执行: " + moveDescription);
            }
            
            repaint();
            
            // 检查游戏状态
            checkGameState();
            
            // 如果是单AI模式且游戏仍在进行，等待玩家移动
            if (!isAIvsAIMode && aiEnabled && board.getGameState() == GameState.PLAYING) {
                updateStatus("请进行您的移动");
            }
        } else {
            updateStatus("❌ AI移动执行失败");
        }
    }
    
    /**
     * 记录详细的AI建议到日志面板
     */
    private void logDetailedAIRecommendation(String moveDescription, String piece, int fromRow, int fromCol, int toRow, int toCol, boolean isCapture, String targetPiece) {
        if (stockfishLogPanel == null) return;
        
        // 添加分隔线
        stockfishLogPanel.addAIDecision("═══════════════════════════════════════");
        stockfishLogPanel.addAIDecision("🤖 AI移动建议详情");
        stockfishLogPanel.addAIDecision("═══════════════════════════════════════");
        
        // 棋子信息
        String pieceNameCh = getPieceNameChinese(piece.charAt(1));
        String colorName = piece.charAt(0) == 'W' ? "白方" : "黑方";
        stockfishLogPanel.addAIDecision("♟️ 棋子: " + colorName + pieceNameCh);
        
        // 坐标信息
        char fromFile = (char) ('a' + fromCol);
        int fromRank = 8 - fromRow;
        char toFile = (char) ('a' + toCol);
        int toRank = 8 - toRow;
        stockfishLogPanel.addAIDecision("📍 起始位置: " + fromFile + fromRank);
        stockfishLogPanel.addAIDecision("🎯 目标位置: " + toFile + toRank);
        
        // 移动描述
        stockfishLogPanel.addAIDecision("📋 移动: " + moveDescription.replace("🤖 ", ""));
        
        // 战术分析
        if (isCapture) {
            String capturedPiece = getPieceNameChinese(targetPiece.charAt(1));
            int captureValue = getPieceValue(targetPiece.charAt(1));
            stockfishLogPanel.addAIDecision("⚔️ 战术分析: 吃掉对方" + capturedPiece + " (价值: " + captureValue + "分)");
        } else {
            stockfishLogPanel.addAIDecision("📊 战术分析: 位置移动");
        }
        
        // 位置价值分析
        StringBuilder positionAnalysis = new StringBuilder();
        if ((toRow == 3 || toRow == 4) && (toCol == 3 || toCol == 4)) {
            positionAnalysis.append("控制中心; ");
        }
        
        if (piece.charAt(1) != 'P' && fromRow >= 6) {
            positionAnalysis.append("开发后排棋子; ");
        }
        
        // 检查是否威胁对方棋子
        if (isThreateningMove(toRow, toCol, piece.charAt(0))) {
            positionAnalysis.append("威胁对方棋子; ");
        }
        
        // 检查是否改善棋子安全性
        if (isSaferPosition(fromRow, fromCol, toRow, toCol, piece.charAt(0))) {
            positionAnalysis.append("提升棋子安全; ");
        }
        
        String positionValue = positionAnalysis.length() > 0 ? positionAnalysis.toString() : "常规移动";
        stockfishLogPanel.addAIDecision("🎯 战略价值: " + positionValue);
        
        // 总体评估
        String evaluation = evaluateMove(isCapture, targetPiece, toRow, toCol, piece);
        stockfishLogPanel.addAIDecision("💡 综合评估: " + evaluation);
        
        stockfishLogPanel.addAIDecision("═══════════════════════════════════════");
    }
    
    /**
     * 检查移动是否威胁对方棋子
     */
    private boolean isThreateningMove(int toRow, int toCol, char pieceColor) {
        // 简化版本：检查周围是否有对方棋子
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int checkRow = toRow + dr;
                int checkCol = toCol + dc;
                if (checkRow >= 0 && checkRow < 8 && checkCol >= 0 && checkCol < 8) {
                    String neighborPiece = board.getPiece(checkRow, checkCol);
                    if (neighborPiece != null && neighborPiece.charAt(0) != pieceColor) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 检查新位置是否更安全
     */
    private boolean isSaferPosition(int fromRow, int fromCol, int toRow, int toCol, char pieceColor) {
        // 简化版本：移动到边缘通常更安全（对于某些棋子）
        boolean wasOnEdge = (fromRow == 0 || fromRow == 7 || fromCol == 0 || fromCol == 7);
        boolean isOnEdge = (toRow == 0 || toRow == 7 || toCol == 0 || toCol == 7);
        
        // 如果从中央移动到边缘，通常是为了安全
        return !wasOnEdge && isOnEdge;
    }
    
    /**
     * 评估移动质量
     */
    private String evaluateMove(boolean isCapture, String targetPiece, int toRow, int toCol, String piece) {
        if (isCapture) {
            int captureValue = getPieceValue(targetPiece.charAt(1));
            if (captureValue >= 5) {
                return "优秀 - 吃掉高价值棋子";
            } else if (captureValue >= 3) {
                return "良好 - 吃掉中等价值棋子";
            } else {
                return "一般 - 吃掉低价值棋子";
            }
        }
        
        // 检查是否控制中心
        if ((toRow == 3 || toRow == 4) && (toCol == 3 || toCol == 4)) {
            return "良好 - 控制棋盘中心";
        }
        
        // 检查是否发展棋子
        if (piece.charAt(1) != 'P' && toRow < 6) {
            return "良好 - 积极发展棋子";
        }
        
        return "标准 - 稳妥的位置调整";
    }
    
    /**
     * 生成移动描述
     */
    private String generateMoveDescription(String piece, int fromRow, int fromCol, int toRow, int toCol, boolean isCapture, String targetPiece) {
        String pieceNameCh = getPieceNameChinese(piece.charAt(1));
        String colorName = piece.charAt(0) == 'W' ? "白方" : "黑方";
        
        char fromFile = (char) ('a' + fromCol);
        int fromRank = 8 - fromRow;
        char toFile = (char) ('a' + toCol);
        int toRank = 8 - toRow;
        
        // 生成具体的移动描述
        StringBuilder description = new StringBuilder();
        description.append("🤖 ").append(colorName).append(pieceNameCh);
        description.append(" 从 ").append(fromFile).append(fromRank);
        description.append(" 移动到 ").append(toFile).append(toRank);
        
        if (isCapture) {
            String capturedPiece = getPieceNameChinese(targetPiece.charAt(1));
            description.append(" (吃掉").append(capturedPiece).append(")");
        }
        
        return description.toString();
    }
    
    /**
     * 记录AI决策信息
     */
    private void logAIDecision(String aiName, int[] move, String reason) {
        if (stockfishLogPanel == null) return;
        
        char fromFile = (char) ('a' + move[1]);
        int fromRank = 8 - move[0];
        char toFile = (char) ('a' + move[3]);
        int toRank = 8 - move[2];
        String moveStr = "" + fromFile + fromRank + "→" + toFile + toRank;
        
        stockfishLogPanel.addAIDecision(aiName + "决策: " + moveStr);
        stockfishLogPanel.addAIDecision("原因: " + reason);
        
        // 分析移动价值
        String analysis = analyzeMoveValue(move);
        stockfishLogPanel.addAIDecision("移动分析: " + analysis);
    }
    
    /**
     * 分析移动价值
     */
    private String analyzeMoveValue(int[] move) {
        String piece = board.getPiece(move[0], move[1]);
        String targetPiece = board.getPiece(move[2], move[3]);
        
        StringBuilder analysis = new StringBuilder();
        
        if (targetPiece != null) {
            int captureValue = getPieceValue(targetPiece.charAt(1));
            analysis.append("吃子价值+").append(captureValue).append("; ");
        }
        
        // 检查是否控制中心
        if ((move[2] == 3 || move[2] == 4) && (move[3] == 3 || move[3] == 4)) {
            analysis.append("控制中心+2; ");
        }
        
        // 检查是否发展棋子
        if (piece != null && piece.charAt(1) != 'P') {
            analysis.append("棋子发展; ");
        }
        
        return analysis.length() > 0 ? analysis.toString() : "位置调整";
    }
    
    /**
     * 获取棋子价值
     */
    private int getPieceValue(char pieceType) {
        switch (pieceType) {
            case 'P': return 1;
            case 'N': case 'B': return 3;
            case 'R': return 5;
            case 'Q': return 9;
            case 'K': return 100;
            default: return 0;
        }
    }
    
    /**
     * 获取棋子中文名称
     */
    private String getPieceNameChinese(char pieceType) {
        switch (pieceType) {
            case 'K': return "王";
            case 'Q': return "后";
            case 'R': return "车";
            case 'B': return "象";
            case 'N': return "马";
            case 'P': return "兵";
            default: return "未知";
        }
    }
    
    /**
     * 检查游戏状态
     */
    private void checkGameState() {
        GameState gameState = board.getGameState();
        switch (gameState) {
            case WHITE_WIN:
            case WHITE_CHECKMATE:
                updateStatus("🎉 白方获胜！");
                SoundPlayer.getInstance().playSound("game_win");
                break;
            case BLACK_WIN:
            case BLACK_CHECKMATE:
                updateStatus("🎉 黑方获胜！");
                SoundPlayer.getInstance().playSound("game_win");
                break;
            case DRAW:
            case STALEMATE:
                updateStatus("🤝 和棋！");
                break;
            case WHITE_CHECK:
                updateStatus("⚠️ 白方被将军！");
                break;
            case BLACK_CHECK:
                updateStatus("⚠️ 黑方被将军！");
                break;
            case PLAYING:
            default:
                String currentPlayer = board.isWhiteTurn() ? "白方" : "黑方";
                if (aiEnabled) {
                    String aiPlayer = humanPlayer == 'W' ? "黑方(AI)" : "白方(AI)";
                    String humanPlayerStr = humanPlayer == 'W' ? "白方" : "黑方";
                    updateStatus("当前回合: " + (board.isWhiteTurn() ? 
                        (humanPlayer == 'W' ? humanPlayerStr : aiPlayer) : 
                        (humanPlayer == 'B' ? humanPlayerStr : aiPlayer)));
                } else {
                    updateStatus("当前回合: " + currentPlayer);
                }
                break;
        }
    }
    
    private void updateStatus(String message) {
        if (statusUpdateCallback != null) {
            statusUpdateCallback.accept(message);
        }
    }
    
    // 设置状态更新回调
    public void setStatusUpdateCallback(Consumer<String> callback) {
        this.statusUpdateCallback = callback;
    }
    
    // 设置AI启用状态
    public void setAIEnabled(boolean enabled) {
        this.aiEnabled = enabled;
        if (enabled) {
            initializeAI();
            // 如果启用AI且当前是AI回合，立即让AI走棋
            if (!isHumanTurn() && board.getGameState() == GameState.PLAYING) {
                SwingUtilities.invokeLater(this::makeAIMove);
            }
        }
    }
    
    // 设置人类玩家颜色
    public void setHumanPlayer(char color) {
        this.humanPlayer = color;
        // 注意: 不在这里重新初始化AI，由外部调用者控制
    }
    
    // 设置AI类型
    public void setAIType(String aiType, int difficulty, String modelName) {
        this.aiType = aiType;
        this.difficulty = difficulty;
        // 注意: 不在这里重新初始化AI，由外部调用者控制
        updateStatus("AI类型设置为: " + aiType + ", 难度: " + difficulty);
    }
    
    /**
     * 初始化AI
     */
    private void initializeAI() {
        if (!aiEnabled) return;
        
        // 确定AI的颜色（与人类玩家相反）
        char aiColor = (humanPlayer == 'W') ? 'B' : 'W';
        
        // 清理旧的AI实例
        if (stockfishAI != null) {
            stockfishAI.shutdown();
            stockfishAI = null;
        }
        
        // 根据AI类型创建不同的AI实例
        switch (aiType) {
            case "Stockfish":
                try {
                    if (stockfishLogPanel != null) {
                        this.stockfishAI = new StockfishAIAdapter(difficulty, aiColor, stockfishLogPanel);
                    } else {
                        this.stockfishAI = new StockfishAIAdapter(difficulty, aiColor);
                    }
                    updateStatus("🤖 Stockfish引擎已初始化 - 颜色: " + (aiColor == 'W' ? "白方" : "黑方") + ", 难度: " + difficulty);
                } catch (Exception e) {
                    updateStatus("❌ Stockfish初始化失败: " + e.getMessage());
                    System.err.println("Stockfish初始化失败: " + e.getMessage());
                    e.printStackTrace();
                    // 回退到传统AI
                    this.ai = new InternationalChessAI(difficulty, aiColor);
                    updateStatus("回退到传统AI - 颜色: " + (aiColor == 'W' ? "白方" : "黑方") + ", 难度: " + difficulty);
                }
                break;
            case "传统AI":
            case "增强AI":
            default:
                this.ai = new InternationalChessAI(difficulty, aiColor);
                updateStatus("传统AI已初始化 - 颜色: " + (aiColor == 'W' ? "白方" : "黑方") + ", 难度: " + difficulty);
                break;
            case "大模型AI":
            case "混合AI":
                // 暂时使用传统AI
                this.ai = new InternationalChessAI(difficulty, aiColor);
                updateStatus("AI已初始化 - 颜色: " + (aiColor == 'W' ? "白方" : "黑方") + ", 难度: " + difficulty);
                break;
        }
    }
    
    // 设置聊天面板
    public void setChatPanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }
    
    // 设置Stockfish日志面板
    public void setStockfishLogPanel(StockfishLogPanel stockfishLogPanel) {
        this.stockfishLogPanel = stockfishLogPanel;
        if (stockfishLogPanel != null) {
            stockfishLogPanel.addGameEvent("棋盘面板已连接Stockfish日志");
        }
    }
    
    // 检查是否可以悔棋
    public boolean canUndo() {
        // TODO: 实现悔棋检查逻辑
        return false;
    }
    
    // 悔棋
    public void undoMove() {
        // TODO: 实现悔棋逻辑
        updateStatus("悔棋功能暂未实现");
    }
    
    /**
     * 设置AI vs AI模式
     */
    public void setAIvsAIMode(boolean enabled) {
        this.isAIvsAIMode = enabled;
        if (enabled) {
            this.aiEnabled = true; // AI vs AI模式下自动启用AI
        }
        updateStatus(isAIvsAIMode ? "AI vs AI模式已启用" : "AI vs AI模式已禁用");
    }
    
    /**
     * 初始化AI vs AI模式的双方AI
     */
    public void initializeAIvsAI(String aiType, int difficulty, String modelName) {
        if (!isAIvsAIMode) return;
        
        this.aiType = aiType;
        this.difficulty = difficulty;
        
        // 清理旧的AI实例
        cleanupAIInstances();
        
        // 根据AI类型创建双方AI实例
        switch (aiType) {
            case "Stockfish":
                try {
                    // 白方Stockfish AI
                    if (stockfishLogPanel != null) {
                        this.whiteStockfishAI = new StockfishAIAdapter(difficulty, 'W', stockfishLogPanel);
                        this.blackStockfishAI = new StockfishAIAdapter(difficulty, 'B', stockfishLogPanel);
                    } else {
                        this.whiteStockfishAI = new StockfishAIAdapter(difficulty, 'W');
                        this.blackStockfishAI = new StockfishAIAdapter(difficulty, 'B');
                    }
                    updateStatus("🤖⚔️🤖 Stockfish AI vs AI已初始化 - 难度: " + difficulty);
                } catch (Exception e) {
                    updateStatus("❌ Stockfish初始化失败，回退到传统AI");
                    // 回退到传统AI
                    this.whiteAI = new InternationalChessAI(difficulty, 'W');
                    this.blackAI = new InternationalChessAI(difficulty, 'B');
                }
                break;
            case "传统AI":
            case "增强AI":
            default:
                this.whiteAI = new InternationalChessAI(difficulty, 'W');
                this.blackAI = new InternationalChessAI(difficulty, 'B');
                updateStatus("🤖⚔️🤖 传统AI vs AI已初始化 - 难度: " + difficulty);
                break;
            case "大模型AI":
            case "混合AI":
                // 暂时使用传统AI
                this.whiteAI = new InternationalChessAI(difficulty, 'W');
                this.blackAI = new InternationalChessAI(difficulty, 'B');
                updateStatus("🤖⚔️🤖 AI vs AI已初始化 - 难度: " + difficulty);
                break;
        }
    }
    
    /**
     * 开始AI vs AI游戏
     */
    public void startAIvsAI() {
        if (!isAIvsAIMode || board.getGameState() != GameState.PLAYING) {
            return;
        }
        
        updateStatus("🎮 AI对战即将开始...");
        
        // 延迟1秒后开始第一步
        Timer startTimer = new Timer(1000, e -> {
            if (isAIvsAIMode && board.getGameState() == GameState.PLAYING) {
                makeAIvsAIMove();
            }
        });
        startTimer.setRepeats(false);
        startTimer.start();
    }
    
    /**
     * 恢复AI vs AI游戏
     */
    public void resumeAIvsAI() {
        if (!isAIvsAIMode || board.getGameState() != GameState.PLAYING) {
            return;
        }
        
        updateStatus("🔄 AI对战继续...");
        
        // 延迟500毫秒后继续
        Timer resumeTimer = new Timer(500, e -> {
            if (isAIvsAIMode && board.getGameState() == GameState.PLAYING) {
                makeAIvsAIMove();
            }
        });
        resumeTimer.setRepeats(false);
        resumeTimer.start();
    }
    
    /**
     * 清理AI实例
     */
    private void cleanupAIInstances() {
        if (stockfishAI != null) {
            stockfishAI.shutdown();
            stockfishAI = null;
        }
        if (whiteStockfishAI != null) {
            whiteStockfishAI.shutdown();
            whiteStockfishAI = null;
        }
        if (blackStockfishAI != null) {
            blackStockfishAI.shutdown();
            blackStockfishAI = null;
        }
        
        ai = null;
        whiteAI = null;
        blackAI = null;
    }
    
    /**
     * 检查当前是否是白方回合
     */
    public boolean isWhiteTurn() {
        return board.isWhiteTurn();
    }
    
    /**
     * 获取当前游戏状态
     */
    public GameState getGameState() {
        return board.getGameState();
    }
    
    /**
     * 暂停游戏
     */
    public void pauseGame() {
        isPaused = true;
        
        // 取消当前AI计算任务
        if (currentAIWorker != null && !currentAIWorker.isDone()) {
            currentAIWorker.cancel(true);
            currentAIWorker = null;
        }
        
        // 清除选择状态
        selectedRow = -1;
        selectedCol = -1;
        repaint();
        
        updateStatus("⏸️ 游戏已暂停");
        
        if (stockfishLogPanel != null) {
            stockfishLogPanel.addGameEvent("游戏暂停");
        }
    }
    
    /**
     * 恢复游戏
     */
    public void resumeGame() {
        isPaused = false;
        
        if (isAIvsAIMode) {
            updateStatus("🔄 AI对战恢复...");
            // 延迟500毫秒后恢复AI vs AI
            Timer resumeTimer = new Timer(500, e -> {
                if (!isPaused && isAIvsAIMode && board.getGameState() == GameState.PLAYING) {
                    makeAIvsAIMove();
                }
            });
            resumeTimer.setRepeats(false);
            resumeTimer.start();
        } else if (aiEnabled && board.getGameState() == GameState.PLAYING) {
            // 检查当前是否轮到AI下棋
            boolean isWhiteTurn = board.isWhiteTurn();
            boolean isAITurn = (humanPlayer == 'W' && !isWhiteTurn) || (humanPlayer == 'B' && isWhiteTurn);
            
            if (isAITurn) {
                updateStatus("🔄 游戏恢复 - AI继续思考...");
                // 延迟300毫秒后让AI走棋
                Timer aiResumeTimer = new Timer(300, e -> {
                    if (!isPaused && aiEnabled) {
                        makeAIMove();
                    }
                });
                aiResumeTimer.setRepeats(false);
                aiResumeTimer.start();
            } else {
                String currentPlayer = isWhiteTurn ? "白方" : "黑方";
                updateStatus("▶️ 游戏恢复 - 当前回合: " + currentPlayer + "（请您下棋）");
            }
        } else {
            String currentPlayer = board.isWhiteTurn() ? "白方" : "黑方";
            updateStatus("▶️ 游戏恢复 - 当前回合: " + currentPlayer);
            System.out.println("ℹ️ 恢复游戏但不符合AI条件: aiEnabled=" + aiEnabled + ", gameState=" + board.getGameState());
        }
        
        if (stockfishLogPanel != null) {
            stockfishLogPanel.addGameEvent("游戏恢复");
        }
    }
    
    /**
     * 检查游戏是否处于暂停状态
     */
    public boolean isPaused() {
        return isPaused;
    }
}
