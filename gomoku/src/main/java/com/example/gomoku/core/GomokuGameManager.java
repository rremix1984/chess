package com.example.gomoku.core;

import com.example.gomoku.ai.GomokuAIEngine;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 五子棋游戏管理器
 * 统一管理玩家对玩家、玩家对AI、AI对AI三种游戏模式
 */
public class GomokuGameManager {
    
    public enum GameMode {
        PLAYER_VS_PLAYER("玩家对玩家"),
        PLAYER_VS_AI("玩家对AI"),
        AI_VS_AI("AI对AI");
        
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
    private GomokuBoard board;
    private PlayerType blackPlayer;
    private PlayerType whitePlayer;
    private GomokuAIEngine blackAI;
    private GomokuAIEngine whiteAI;
    private boolean isGameRunning;
    private boolean isGamePaused;
    private final AtomicBoolean isAIThinking = new AtomicBoolean(false);
    
    // 回调接口
    public interface GameCallback {
        void onGameStateChanged(GameState newState, String winner);
        void onTurnChanged(boolean isBlackTurn, PlayerType currentPlayerType);
        void onAIThinking(String message);
        void onAIMove(int row, int col, String analysis);
        void onGameModeChanged(GameMode newMode);
        void onError(String error);
    }
    
    private GameCallback gameCallback;
    
    public GomokuGameManager() {
        this.board = new GomokuBoard();
        this.currentMode = GameMode.PLAYER_VS_PLAYER;
        this.blackPlayer = PlayerType.HUMAN;
        this.whitePlayer = PlayerType.HUMAN;
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
                blackPlayer = PlayerType.HUMAN;
                whitePlayer = PlayerType.HUMAN;
                break;
                
            case PLAYER_VS_AI:
                blackPlayer = PlayerType.HUMAN;
                whitePlayer = PlayerType.AI;
                whiteAI = createAI(aiType, difficulty, modelName, "白方AI");
                break;
                
            case AI_VS_AI:
                blackPlayer = PlayerType.AI;
                whitePlayer = PlayerType.AI;
                blackAI = createAI(aiType, difficulty, modelName, "黑方AI");
                whiteAI = createAI(aiType, difficulty, modelName, "白方AI");
                break;
        }
        
        if (gameCallback != null) {
            gameCallback.onGameModeChanged(mode);
        }
        
        System.out.println("🎮 游戏模式已设置为: " + mode.displayName);
    }
    
    /**
     * 创建AI引擎
     */
    private GomokuAIEngine createAI(String aiType, String difficulty, String modelName, String aiName) {
        GomokuAIEngine ai = new GomokuAIEngine(aiType, difficulty, modelName);
        
        // 设置AI思考回调
        ai.setThinkingCallback(new GomokuAIEngine.ThinkingCallback() {
            @Override
            public void onThinking(String message) {
                if (gameCallback != null) {
                    gameCallback.onAIThinking(aiName + ": " + message);
                }
            }
            
            @Override
            public void onMoveDecision(int[] move, String analysis) {
                if (gameCallback != null) {
                    gameCallback.onAIMove(move[0], move[1], aiName + ": " + analysis);
                }
            }
        });
        
        System.out.println("🤖 创建AI: " + aiName + " (" + ai.getAIInfo() + ")");
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
        
        System.out.println("🚀 游戏开始 - 模式: " + currentMode.displayName);
        
        // 通知回合变更
        notifyTurnChanged();
        
        // 如果黑方是AI且是AI对AI模式，立即开始AI思考
        if (currentMode == GameMode.AI_VS_AI && blackPlayer == PlayerType.AI) {
            System.out.println("🤖 AI对AI模式，准备开始第一步AI移动...");
            // 延迟一下让UI完全更新
            SwingUtilities.invokeLater(() -> executeAIMove());
        }
    }
    
    /**
     * 暂停游戏
     */
    public void pauseGame() {
        isGamePaused = true;
        System.out.println("⏸️ 游戏已暂停");
    }
    
    /**
     * 继续游戏
     */
    public void resumeGame() {
        if (!isGameRunning || !isGamePaused) {
            return;
        }
        
        isGamePaused = false;
        System.out.println("▶️ 游戏继续");
        
        // 如果当前是AI回合，继续AI思考
        if (getCurrentPlayerType() == PlayerType.AI) {
            executeAIMove();
        }
    }
    
    /**
     * 重置游戏
     */
    public void resetGame() {
        board.initializeBoard();
        isGameRunning = false;
        isGamePaused = false;
        isAIThinking.set(false);
        
        System.out.println("🔄 游戏已重置");
        
        // 通知游戏状态变更
        if (gameCallback != null) {
            gameCallback.onGameStateChanged(GameState.PLAYING, null);
            gameCallback.onTurnChanged(true, blackPlayer);
        }
    }
    
    /**
     * 玩家落子
     */
    public boolean makePlayerMove(int row, int col) {
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
        
        // 尝试落子
        if (board.placePiece(row, col)) {
            System.out.println("👤 玩家落子: (" + row + ", " + col + ")");
            
            // 检查游戏状态
            checkGameState();
            
            // 如果游戏继续且下一回合是AI，执行AI移动
            if (board.getGameState() == GameState.PLAYING && getCurrentPlayerType() == PlayerType.AI) {
                // 延迟一下让UI更新
                SwingUtilities.invokeLater(() -> executeAIMove());
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 执行AI移动
     */
    private void executeAIMove() {
        System.out.println("🚀 executeAIMove() 被调用 - 游戏状态: " + (isGameRunning ? "运行中" : "未运行"));
        
        if (!isGameRunning || isGamePaused || board.getGameState() != GameState.PLAYING) {
            System.out.println("⚠️ executeAIMove() 退出: 游戏状态不合适");
            return;
        }
        
        PlayerType currentPlayerType = getCurrentPlayerType();
        System.out.println("🔄 当前玩家类型: " + currentPlayerType.displayName + ", 轮到: " + (board.isBlackTurn() ? "黑方" : "白方"));
        
        if (currentPlayerType != PlayerType.AI) {
            System.out.println("⚠️ executeAIMove() 退出: 当前不是AI回合");
            return;
        }
        
        if (!isAIThinking.compareAndSet(false, true)) {
            System.out.println("⚠️ executeAIMove() 退出: AI已在思考中");
            return;
        }
        
        GomokuAIEngine currentAI = board.isBlackTurn() ? blackAI : whiteAI;
        String aiName = board.isBlackTurn() ? "黑方AI" : "白方AI";
        System.out.println("🤖 开始" + aiName + "思考... AI引擎: " + (currentAI != null ? "已初始化" : "null"));
        
        if (currentAI == null) {
            isAIThinking.set(false);
            System.out.println("❌ AI引擎未初始化!");
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
                        if (board.placePiece(move[0], move[1])) {
                            String aiName2 = board.isBlackTurn() ? "白方AI" : "黑方AI";
                            System.out.println("🤖 " + aiName2 + " 落子: (" + move[0] + ", " + move[1] + ")");
                            
                            // 通知UI AI已落子（触发界面刷新）
                            if (gameCallback != null) {
                                gameCallback.onAIMove(move[0], move[1], aiName2 + "落子");
                            }
                            
                            // 检查游戏状态
                            checkGameState();
                            
                            // 如果是AI对AI模式且游戏继续，安排下一步AI移动
                            if (currentMode == GameMode.AI_VS_AI && 
                                board.getGameState() == GameState.PLAYING) {
                                
                                Timer timer = new Timer(1000, e -> executeAIMove());
                                timer.setRepeats(false);
                                timer.start();
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
                case BLACK_WINS:
                    winner = "黑方获胜";
                    break;
                case RED_WINS: // 在五子棋中代表白方
                    winner = "白方获胜";
                    break;
                case DRAW:
                    winner = "平局";
                    break;
            }
            
            System.out.println("🏁 游戏结束: " + winner);
            
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
            gameCallback.onTurnChanged(board.isBlackTurn(), getCurrentPlayerType());
        }
    }
    
    /**
     * 获取当前玩家类型
     */
    private PlayerType getCurrentPlayerType() {
        return board.isBlackTurn() ? blackPlayer : whitePlayer;
    }
    
    /**
     * 悔棋（仅在玩家对AI模式下允许）
     */
    public boolean undoMove() {
        if (currentMode != GameMode.PLAYER_VS_AI || !isGameRunning || isAIThinking.get()) {
            return false;
        }
        
        // TODO: 实现悔棋逻辑
        // 需要记录历史走法并能够回退
        
        System.out.println("🔙 悔棋功能暂未实现");
        return false;
    }
    
    /**
     * 获取游戏统计信息
     */
    public String getGameStats() {
        int moveCount = 0;
        for (int row = 0; row < GomokuBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuBoard.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) != ' ') {
                    moveCount++;
                }
            }
        }
        
        return String.format("手数: %d, 模式: %s, 状态: %s", 
                           moveCount, 
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
        if (blackAI != null) {
            blackAI.shutdown();
            blackAI = null;
        }
        if (whiteAI != null) {
            whiteAI.shutdown();
            whiteAI = null;
        }
    }
    
    // Getters
    public GameMode getCurrentMode() { return currentMode; }
    public GomokuBoard getBoard() { return board; }
    public boolean isGameRunning() { return isGameRunning; }
    public boolean isGamePaused() { return isGamePaused; }
    public boolean isAIThinking() { return isAIThinking.get(); }
    public PlayerType getBlackPlayer() { return blackPlayer; }
    public PlayerType getWhitePlayer() { return whitePlayer; }
    
    // Setters
    public void setGameCallback(GameCallback callback) {
        this.gameCallback = callback;
    }
    
    /**
     * 设置游戏模式（简化版）
     */
    public void setGameMode(GameMode mode) {
        setGameMode(mode, "高级AI", "普通", "qwen2.5:7b");
    }
    
    /**
     * 设置玩家颜色（仅在玩家对AI模式下有效）
     */
    public void setPlayerColor(boolean isPlayerBlack) {
        if (currentMode == GameMode.PLAYER_VS_AI) {
            if (isPlayerBlack) {
                blackPlayer = PlayerType.HUMAN;
                whitePlayer = PlayerType.AI;
            } else {
                blackPlayer = PlayerType.AI;
                whitePlayer = PlayerType.HUMAN;
            }
            
            // 重新创建AI（如果需要）
            if (whitePlayer == PlayerType.AI && whiteAI == null) {
                // TODO: 使用当前配置创建AI
            }
            if (blackPlayer == PlayerType.AI && blackAI == null) {
                // TODO: 使用当前配置创建AI
            }
        }
    }
}
