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
        g2d.setFont(new Font("Arial Unicode MS", Font.BOLD, 36));
        FontMetrics fm = g2d.getFontMetrics();
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String piece = board.getPiece(row, col);
                if (piece != null && !piece.trim().isEmpty()) {
                    String symbol = getPieceSymbol(piece);
                    if (!symbol.isEmpty()) {
                        int x = col * CELL_SIZE + (CELL_SIZE - fm.stringWidth(symbol)) / 2;
                        int y = row * CELL_SIZE + (CELL_SIZE + fm.getAscent()) / 2;
                        
                        // 设置棋子颜色
                        if (piece.charAt(0) == 'W') {
                            g2d.setColor(Color.WHITE);
                            // 绘制黑色边框
                            g2d.drawString(symbol, x-1, y-1);
                            g2d.drawString(symbol, x-1, y+1);
                            g2d.drawString(symbol, x+1, y-1);
                            g2d.drawString(symbol, x+1, y+1);
                            g2d.setColor(Color.BLACK);
                        } else {
                            g2d.setColor(Color.BLACK);
                        }
                        g2d.drawString(symbol, x, y);
                    }
                }
            }
        }
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
    
    /**
     * 让AI走棋
     */
    private void makeAIMove() {
        if (!aiEnabled || board.getGameState() != GameState.PLAYING) {
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
        new SwingWorker<int[], Void>() {
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
        }.execute();
    }
    
    /**
     * AI vs AI 移动
     */
    private void makeAIvsAIMove() {
        if (!isAIvsAIMode || board.getGameState() != GameState.PLAYING) {
            return;
        }
        
        boolean isWhiteTurn = board.isWhiteTurn();
        String currentPlayer = isWhiteTurn ? "白方AI" : "黑方AI";
        updateStatus("🤖🆚🤖 " + currentPlayer + "正在思考...");
        
        new SwingWorker<int[], Void>() {
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
                        
                        // 继续AI vs AI游戏循环
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
        }.execute();
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
     * 生成移动描述
     */
    private String generateMoveDescription(String piece, int fromRow, int fromCol, int toRow, int toCol, boolean isCapture, String targetPiece) {
        String pieceNameCh = getPieceNameChinese(piece.charAt(1));
        String colorName = piece.charAt(0) == 'W' ? "白方" : "黑方";
        
        char fromFile = (char) ('a' + fromCol);
        int fromRank = 8 - fromRow;
        char toFile = (char) ('a' + toCol);
        int toRank = 8 - toRow;
        
        String moveStr = "" + fromFile + fromRank + "→" + toFile + toRank;
        
        if (isCapture) {
            String capturedPiece = getPieceNameChinese(targetPiece.charAt(1));
            return String.format("🤖 %s%s %s 吃掉%s", colorName, pieceNameCh, moveStr, capturedPiece);
        } else {
            return String.format("🤖 %s%s %s", colorName, pieceNameCh, moveStr);
        }
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
        if (aiEnabled) {
            initializeAI(); // 重新初始化AI
        }
    }
    
    // 设置AI类型
    public void setAIType(String aiType, int difficulty, String modelName) {
        this.aiType = aiType;
        this.difficulty = difficulty;
        if (aiEnabled) {
            initializeAI(); // 重新初始化AI
        }
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
}
