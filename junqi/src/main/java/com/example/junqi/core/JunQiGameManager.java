package com.example.junqi.core;

import com.example.junqi.ai.JunQiAIEngine;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 军棋游戏管理器
 * 统一管理玩家对玩家、玩家对AI两种游戏模式
 */
public class JunQiGameManager {
    
    public enum GameMode {
        PLAYER_VS_PLAYER("玩家对玩家"),
        PLAYER_VS_AI("玩家对AI");
        
        public final String displayName;
        
        GameMode(String displayName) {
            this.displayName = displayName;
        }
        
        public static GameMode fromString(String modeStr) {
            for (GameMode mode : values()) {
                if (mode.displayName.equals(modeStr)) {
                    return mode;
                }
            }
            return PLAYER_VS_PLAYER;
        }
    }
    
    public enum PlayerType {
        HUMAN("玩家"),
        AI("AI");
        
        public final String displayName;
        
        PlayerType(String displayName) {
            this.displayName = displayName;
        }
    }
    
    // 游戏状态
    private GameMode currentMode;
    private JunQiBoard board;
    private PlayerType redPlayer;
    private PlayerType blackPlayer;
    private JunQiAIEngine redAI;
    private JunQiAIEngine blackAI;
    private boolean isGameRunning;
    private boolean isGamePaused;
    private final AtomicBoolean isAIThinking = new AtomicBoolean(false);
    
    // 当前选中的棋子位置
    private int selectedRow = -1;
    private int selectedCol = -1;
    
    // 回调接口
    public interface GameCallback {
        void onGameStateChanged(GameState newState, String winner);
        void onTurnChanged(boolean isRedTurn, PlayerType currentPlayerType);
        void onAIThinking(String message);
        void onAIMove(int fromRow, int fromCol, int toRow, int toCol, String analysis);
        void onPieceFlipped(int row, int col, JunQiPiece piece);
        void onGameModeChanged(GameMode newMode);
        void onError(String error);
        void onPieceSelected(int row, int col);
        void onPieceDeselected();
    }
    
    private GameCallback gameCallback;
    
    public JunQiGameManager() {
        this.board = new JunQiBoard();
        this.currentMode = GameMode.PLAYER_VS_PLAYER;
        this.redPlayer = PlayerType.HUMAN;
        this.blackPlayer = PlayerType.HUMAN;
        this.isGameRunning = false;
        this.isGamePaused = false;
    }
    
    /**
     * 设置游戏模式
     */
    public void setGameMode(GameMode mode, String aiType, String difficulty, String modelName) {
        this.currentMode = mode;
        
        // 清理现有AI
        shutdownAIs();
        
        switch (mode) {
            case PLAYER_VS_PLAYER:
                redPlayer = PlayerType.HUMAN;
                blackPlayer = PlayerType.HUMAN;
                break;
                
            case PLAYER_VS_AI:
                redPlayer = PlayerType.HUMAN;
                blackPlayer = PlayerType.AI;
                blackAI = createAI(aiType, difficulty, modelName, "黑方AI");
                break;
        }
        
        if (gameCallback != null) {
            gameCallback.onGameModeChanged(mode);
        }
        
        System.out.println("🎮 军棋游戏模式已设置为: " + mode.displayName);
    }
    
    /**
     * 设置游戏模式（简化版）
     */
    public void setGameMode(GameMode mode) {
        setGameMode(mode, "高级AI", "普通", "qwen2.5:7b");
    }
    
    /**
     * 创建AI引擎
     */
    private JunQiAIEngine createAI(String aiType, String difficulty, String modelName, String aiName) {
        JunQiAIEngine ai = new JunQiAIEngine(aiType, difficulty, modelName);
        
        // 设置AI思考回调
        ai.setThinkingCallback(new JunQiAIEngine.ThinkingCallback() {
            @Override
            public void onThinking(String message) {
                if (gameCallback != null) {
                    gameCallback.onAIThinking(aiName + ": " + message);
                }
            }
            
            @Override
            public void onMoveDecision(int fromRow, int fromCol, int toRow, int toCol, String analysis) {
                if (gameCallback != null) {
                    gameCallback.onAIMove(fromRow, fromCol, toRow, toCol, aiName + ": " + analysis);
                }
            }
        });
        
        System.out.println("🤖 创建军棋AI: " + aiName + " (类型: " + aiType + ", 难度: " + difficulty + ")");
        return ai;
    }
    
    /**
     * 开始游戏
     */
    public void startGame() {
        if (isGameRunning && !isGamePaused) {
            return;
        }
        
        if (!isGameRunning) {
            resetGame();
        }
        
        isGameRunning = true;
        isGamePaused = false;
        
        System.out.println("🚀 军棋游戏开始 - 模式: " + currentMode.displayName);
        
        // 通知回合变更
        notifyTurnChanged();
        
        // 如果红方是AI，立即开始AI思考
        if (redPlayer == PlayerType.AI) {
            executeAIMove();
        }
    }
    
    /**
     * 暂停游戏
     */
    public void pauseGame() {
        isGamePaused = true;
        System.out.println("⏸️ 军棋游戏已暂停");
    }
    
    /**
     * 继续游戏
     */
    public void resumeGame() {
        if (!isGameRunning || !isGamePaused) {
            return;
        }
        
        isGamePaused = false;
        System.out.println("▶️ 军棋游戏继续");
        
        // 如果当前是AI回合，继续AI思考
        if (getCurrentPlayerType() == PlayerType.AI) {
            executeAIMove();
        }
    }
    
    /**
     * 重置游戏
     */
    public void resetGame() {
        board.reset();
        isGameRunning = false;
        isGamePaused = false;
        isAIThinking.set(false);
        selectedRow = -1;
        selectedCol = -1;
        
        System.out.println("🔄 军棋游戏已重置");
        
        // 通知游戏状态变更
        if (gameCallback != null) {
            gameCallback.onGameStateChanged(GameState.PLAYING, null);
            gameCallback.onTurnChanged(true, redPlayer);
        }
    }
    
    /**
     * 处理玩家点击
     */
    public boolean handlePlayerClick(int row, int col) {
        if (!isGameRunning || isGamePaused || isAIThinking.get()) {
            return false;
        }
        
        // 检查当前是否轮到玩家
        PlayerType currentPlayerType = getCurrentPlayerType();
        if (currentPlayerType != PlayerType.HUMAN) {
            if (gameCallback != null) {
                gameCallback.onError("现在轮到AI行棋，请等待");
            }
            return false;
        }
        
        JunQiPiece clickedPiece = board.getPiece(row, col);
        
        // 如果点击了暗棋，尝试翻棋
        if (clickedPiece != null && !clickedPiece.isVisible() && clickedPiece.isAlive()) {
            boolean success = board.flipPiece(row, col);
            if (success) {
                System.out.println("🔄 翻棋: " + clickedPiece.getDisplayName());
                if (gameCallback != null) {
                    gameCallback.onPieceFlipped(row, col, clickedPiece);
                }
                
                // 检查游戏状态
                checkGameState();
                
                // 如果下一回合是AI，执行AI移动
                if (board.getGameState() == GameState.PLAYING && getCurrentPlayerType() == PlayerType.AI) {
                    SwingUtilities.invokeLater(() -> executeAIMove());
                }
                
                return true;
            }
            return false;
        }
        
        // 如果没有选中棋子，尝试选中当前点击的棋子
        if (selectedRow == -1 || selectedCol == -1) {
            if (clickedPiece != null && clickedPiece.isAlive() && clickedPiece.getType().canMove()) {
                // 检查是否是当前玩家的棋子
                if ((board.isRedTurn() && clickedPiece.isRed()) || 
                    (!board.isRedTurn() && !clickedPiece.isRed())) {
                    selectedRow = row;
                    selectedCol = col;
                    if (gameCallback != null) {
                        gameCallback.onPieceSelected(row, col);
                    }
                    return true;
                }
            }
            return false;
        }
        
        // 如果已经选中了棋子，尝试移动
        boolean success = board.makeMove(selectedRow, selectedCol, row, col);
        if (success) {
            JunQiPiece movingPiece = board.getPiece(row, col);
            System.out.println("💥 移动: (" + selectedRow + "," + selectedCol + ") -> (" + row + "," + col + ")");
            
            // 取消选择
            selectedRow = -1;
            selectedCol = -1;
            if (gameCallback != null) {
                gameCallback.onPieceDeselected();
            }
            
            // 检查游戏状态
            checkGameState();
            
            // 如果游戏继续且下一回合是AI，执行AI移动
            if (board.getGameState() == GameState.PLAYING && getCurrentPlayerType() == PlayerType.AI) {
                SwingUtilities.invokeLater(() -> executeAIMove());
            }
            
            return true;
        } else {
            // 移动失败，尝试重新选择
            if (clickedPiece != null && clickedPiece.isAlive() && clickedPiece.getType().canMove()) {
                // 检查是否是当前玩家的棋子
                if ((board.isRedTurn() && clickedPiece.isRed()) || 
                    (!board.isRedTurn() && !clickedPiece.isRed())) {
                    selectedRow = row;
                    selectedCol = col;
                    if (gameCallback != null) {
                        gameCallback.onPieceSelected(row, col);
                    }
                    return true;
                }
            }
            
            // 取消选择
            selectedRow = -1;
            selectedCol = -1;
            if (gameCallback != null) {
                gameCallback.onPieceDeselected();
            }
        }
        
        return false;
    }
    
    /**
     * 执行AI移动
     */
    private void executeAIMove() {
        if (!isGameRunning || isGamePaused || board.getGameState() != GameState.PLAYING) {
            return;
        }
        
        PlayerType currentPlayerType = getCurrentPlayerType();
        if (currentPlayerType != PlayerType.AI) {
            return;
        }
        
        if (!isAIThinking.compareAndSet(false, true)) {
            // AI已在思考中
            return;
        }
        
        JunQiAIEngine currentAI = board.isRedTurn() ? redAI : blackAI;
        if (currentAI == null) {
            isAIThinking.set(false);
            if (gameCallback != null) {
                gameCallback.onError("AI引擎未初始化");
            }
            return;
        }
        
        // 异步执行AI思考
        currentAI.getNextMoveAsync(board).thenAccept(move -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    if (move != null && board.getGameState() == GameState.PLAYING) {
                        int[] moveData = move.getMoveData();
                        if (moveData.length == 4) {
                            // 移动棋子
                            if (board.makeMove(moveData[0], moveData[1], moveData[2], moveData[3])) {
                                String aiName = board.isRedTurn() ? "黑方AI" : "红方AI";
                                System.out.println("🤖 " + aiName + " 移动: (" + moveData[0] + "," + moveData[1] + 
                                                ") -> (" + moveData[2] + "," + moveData[3] + ")");
                                
                                // 检查游戏状态
                                checkGameState();
                            }
                        } else if (moveData.length == 2) {
                            // 翻棋
                            if (board.flipPiece(moveData[0], moveData[1])) {
                                JunQiPiece piece = board.getPiece(moveData[0], moveData[1]);
                                String aiName = board.isRedTurn() ? "黑方AI" : "红方AI";
                                System.out.println("🤖 " + aiName + " 翻棋: (" + moveData[0] + "," + moveData[1] + 
                                                ") " + (piece != null ? piece.getDisplayName() : ""));
                                
                                if (gameCallback != null && piece != null) {
                                    gameCallback.onPieceFlipped(moveData[0], moveData[1], piece);
                                }
                                
                                // 检查游戏状态
                                checkGameState();
                            }
                        }
                    }
                } finally {
                    isAIThinking.set(false);
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                isAIThinking.set(false);
                if (gameCallback != null) {
                    gameCallback.onError("AI计算出错: " + throwable.getMessage());
                }
            });
            return null;
        });
    }
    
    /**
     * 检查游戏状态
     */
    private void checkGameState() {
        GameState gameState = board.getGameState();
        
        if (gameState != GameState.PLAYING) {
            isGameRunning = false;
            String winner = null;
            
            switch (gameState) {
                case RED_WINS:
                    winner = "红方获胜";
                    break;
                case BLACK_WINS:
                    winner = "黑方获胜";
                    break;
                case DRAW:
                    winner = "平局";
                    break;
            }
            
            System.out.println("🏁 军棋游戏结束: " + winner);
            
            if (gameCallback != null) {
                gameCallback.onGameStateChanged(gameState, winner);
            }
        } else {
            // 通知回合变更
            notifyTurnChanged();
        }
    }
    
    /**
     * 通知回合变更
     */
    private void notifyTurnChanged() {
        if (gameCallback != null) {
            gameCallback.onTurnChanged(board.isRedTurn(), getCurrentPlayerType());
        }
    }
    
    /**
     * 获取当前玩家类型
     */
    private PlayerType getCurrentPlayerType() {
        return board.isRedTurn() ? redPlayer : blackPlayer;
    }
    
    /**
     * 设置玩家颜色（仅在玩家对AI模式下有效）
     */
    public void setPlayerColor(boolean isPlayerRed) {
        if (currentMode == GameMode.PLAYER_VS_AI) {
            if (isPlayerRed) {
                redPlayer = PlayerType.HUMAN;
                blackPlayer = PlayerType.AI;
                // 重新创建黑方AI
                if (blackAI != null) {
                    blackAI.shutdown();
                }
                blackAI = createAI("高级AI", "普通", "qwen2.5:7b", "黑方AI");
                redAI = null;
            } else {
                redPlayer = PlayerType.AI;
                blackPlayer = PlayerType.HUMAN;
                // 重新创建红方AI
                if (redAI != null) {
                    redAI.shutdown();
                }
                redAI = createAI("高级AI", "普通", "qwen2.5:7b", "红方AI");
                blackAI = null;
            }
        }
    }
    
    /**
     * 获取游戏统计信息
     */
    public String getGameStats() {
        return String.format("步数: %d, 模式: %s, 状态: %s", 
                           board.getMoveCount(), 
                           currentMode.displayName, 
                           isGameRunning ? (isGamePaused ? "暂停" : "进行中") : "未开始");
    }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        isGameRunning = false;
        shutdownAIs();
    }
    
    /**
     * 关闭AI引擎
     */
    private void shutdownAIs() {
        if (redAI != null) {
            redAI.shutdown();
            redAI = null;
        }
        if (blackAI != null) {
            blackAI.shutdown();
            blackAI = null;
        }
    }
    
    // Getters
    public GameMode getCurrentMode() { return currentMode; }
    public JunQiBoard getBoard() { return board; }
    public boolean isGameRunning() { return isGameRunning; }
    public boolean isGamePaused() { return isGamePaused; }
    public boolean isAIThinking() { return isAIThinking.get(); }
    public PlayerType getRedPlayer() { return redPlayer; }
    public PlayerType getBlackPlayer() { return blackPlayer; }
    public int getSelectedRow() { return selectedRow; }
    public int getSelectedCol() { return selectedCol; }
    
    // Setters
    public void setGameCallback(GameCallback callback) {
        this.gameCallback = callback;
    }
}
