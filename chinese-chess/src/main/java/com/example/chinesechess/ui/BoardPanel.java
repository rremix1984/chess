package com.example.chinesechess.ui;

import com.example.chinesechess.VictoryAnimation;
import com.example.chinesechess.ui.ChatPanel;
import com.example.chinesechess.ui.AILogPanel;
import com.example.chinesechess.core.*;
import com.example.chinesechess.ai.ChessAI;
import com.example.chinesechess.ai.LLMChessAI;
import com.example.chinesechess.ai.HybridChessAI;
import com.example.chinesechess.ai.EnhancedChessAI;
import com.example.chinesechess.ai.DeepSeekPikafishAI;
import com.example.chinesechess.ai.FairyStockfishAI;
import com.example.chinesechess.ai.PikafishAI;
import com.example.chinesechess.core.Move;
import com.example.common.utils.ExceptionHandler;
import com.example.common.utils.PerformanceMonitor;
import com.example.common.utils.ResourceManager;
import com.example.common.config.GameConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import javax.swing.Timer;
import com.example.common.sound.SoundPlayer;

public class BoardPanel extends JPanel {

    private final Board board;
    private static final int CELL_SIZE = GameConfig.BOARD_CELL_SIZE;
    private static final int MARGIN = GameConfig.BOARD_MARGIN;
    
    // 游戏状态
    private Piece selectedPiece = null;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private PieceColor currentPlayer = PieceColor.RED; // 红方先行
    
    // 移动历史记录（用于悔棋功能）- 使用专门的BoardState类
    private java.util.List<com.example.chinesechess.core.BoardState> boardHistory = new java.util.ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 200; // 增加历史记录容量
    private int stateCounter = 0; // 状态计数器
    
    // 状态更新回调
    private Consumer<String> statusUpdateCallback;
    
    // 聊天面板引用
    private ChatPanel chatPanel;
    
    // AI决策日志面板引用
    private AILogPanel aiLogPanel;
    
    // 合法走位提示
    private List<Position> validMoves = new ArrayList<>();
    
    // AI相关
    private ChessAI ai;
    private LLMChessAI llmChessAI;
    private EnhancedChessAI enhancedAI;
    private HybridChessAI hybridAI;
    private DeepSeekPikafishAI deepSeekPikafishAI;
    private FairyStockfishAI fairyStockfishAI;
    private PikafishAI pikafishAI;
    private boolean isAIEnabled = false;
    private boolean useLLM = false;
    private boolean useEnhanced = false;
    private boolean useHybrid = false;
    private boolean useDeepSeekPikafish = false;
    private boolean useFairyStockfish = false;
    private boolean usePikafish = false;
    private PieceColor humanPlayer = PieceColor.RED; // 默认人类执红棋
    private boolean isAIThinking = false;
    private volatile boolean isGamePaused = false; // 游戏暂停标志

    public void setGamePaused(boolean isPaused) {
        this.isGamePaused = isPaused;
    }

    private GameState gameState = GameState.PLAYING;
    
    // 棋盘翻转状态
    private boolean isBoardFlipped = false;
    
    // 移动轨迹标记
    private Position lastMoveStart = null;
    private Position lastMoveEnd = null;
    
    // AI建议标记
    private Position aiSuggestionStart = null; // AI建议的起始位置
    private Position aiSuggestionEnd = null;   // AI建议的目标位置
    private boolean showAISuggestion = false;  // 是否显示AI建议
    private Timer aiSuggestionTimer = null;    // AI建议标记自动清除定时器
    
    // 残局功能相关
    private boolean isEndgameMode = false;
    private boolean isSettingUpEndgame = false;
    private PieceColor endgameAIColor = PieceColor.BLACK; // 残局中AI执子颜色
    private boolean isAIvsAIMode = false; // AI对AI模式
    private Object redAI; // 红方AI (支持多种引擎类型)
    private Object blackAI; // 黑方AI (支持多种引擎类型)
    
    // 残局棋子选择菜单相关
    private JPopupMenu pieceSelectionMenu;
    private int currentEndgameRow = -1;
    private int currentEndgameCol = -1;
    private int selectedPieceIndex = 0;
    private final String[] pieceOptions = {
        "红帅", "红仕", "红相", "红马", "红车", "红炮", "红兵",
        "黑将", "黑士", "黑象", "黑马", "黑车", "黑炮", "黑卒"
    };
    
    // 错误信息显示相关
    private JPanel errorInfoPanel;
    private JTextArea errorTextArea;
    private JScrollPane errorScrollPane;
    private Timer errorClearTimer;

    // AI Move Analysis Panel
    private JTextArea moveAnalysisTextArea;
    private JScrollPane moveAnalysisScrollPane;
    private JButton analyzeButton;

    public BoardPanel(Board board) {
        this.board = board;
        initializePieceSelectionMenu();
        initializeErrorInfoPanel();
        
        // 设置棋盘面板的首选大小和最小大小
        Dimension boardSize = calculateBoardSize();
        setPreferredSize(boardSize);
        setMinimumSize(boardSize);
        setSize(boardSize);
        
        // 设置背景色
        setBackground(new Color(245, 222, 179)); // 棋盘背景色

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isGamePaused) { // 如果游戏暂停，则不处理点击事件
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e) && isSettingUpEndgame) {
                    handleEndgameSetupRightClick(e.getX(), e.getY());
                } else {
                    handleMouseClick(e.getX(), e.getY());
                }
            }
        });
    }

    public void enableAI(PieceColor humanColor, int difficulty, boolean useLLM, String modelName) {
        this.humanPlayer = humanColor;
        this.useLLM = useLLM;
        this.useEnhanced = false;
        this.useHybrid = false;
        this.useDeepSeekPikafish = false;

        PieceColor aiColor = (humanColor == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;

        if (useLLM) {
            this.llmChessAI = new LLMChessAI(aiColor, modelName, difficulty);
            if (this.aiLogPanel != null) {
                this.llmChessAI.setAILogPanel(this.aiLogPanel);
            }
        } else {
            this.ai = new ChessAI(aiColor, difficulty);
        }

        this.isAIEnabled = true;

        // 添加调试信息
        String humanColorName = (humanColor == PieceColor.RED) ? "红方" : "黑方";
        String aiColorName = (aiColor == PieceColor.RED) ? "红方" : "黑方";
        System.out.println("🎮 AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName);
        addAILog("system", "AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName);

        // 如果当前轮到AI，立即开始AI回合
        if (aiColor == currentPlayer) {
            SwingUtilities.invokeLater(this::performAIMove);
        }

        updateStatus();
    }

    public void enableEnhancedAI(PieceColor humanColor, int difficulty) {
        this.humanPlayer = humanColor;
        this.useEnhanced = true;
        this.useLLM = false;
        this.useHybrid = false;

        PieceColor aiColor = (humanColor == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
        this.enhancedAI = new EnhancedChessAI(aiColor, difficulty);
        this.isAIEnabled = true;

        // 如果当前轮到AI，立即开始AI回合
        if (aiColor == currentPlayer) {
            SwingUtilities.invokeLater(this::performAIMove);
        }

        updateStatus();
    }
    
    /**
     * 启用混合AI对弈
     * @param humanColor 人类玩家颜色
     * @param difficulty AI难度 (1-5)
     * @param modelName LLM模型名称
     */
    public void enableHybridAI(PieceColor humanColor, int difficulty, String modelName) {
        this.humanPlayer = humanColor;
        this.useHybrid = true;
        this.useLLM = false;
        this.useEnhanced = false;

        PieceColor aiColor = (humanColor == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
        this.hybridAI = new HybridChessAI(aiColor, difficulty, modelName);
        this.isAIEnabled = true;

        // 添加调试信息
        String humanColorName = (humanColor == PieceColor.RED) ? "红方" : "黑方";
        String aiColorName = (aiColor == PieceColor.RED) ? "红方" : "黑方";
        System.out.println("🎮 AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName);
        addAILog("system", "AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName);

        // 如果当前轮到AI，立即开始AI回合
        if (aiColor == currentPlayer) {
            SwingUtilities.invokeLater(this::performAIMove);
        }

        updateStatus();
    }

    /**
     * 启用DeepSeek+Pikafish AI对弈
     * @param humanColor 人类玩家颜色
     * @param difficulty AI难度 (1-5)
     * @param modelName 模型名称 (例如 "deepseek-ai")
     */
    public void enableDeepSeekPikafishAI(PieceColor humanColor, int difficulty, String modelName) {
        this.humanPlayer = humanColor;
        this.useDeepSeekPikafish = true;
        this.useLLM = false;
        this.useEnhanced = false;
        this.useHybrid = false;

        PieceColor aiColor = (humanColor == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
        this.deepSeekPikafishAI = new DeepSeekPikafishAI(aiColor, difficulty, modelName);
        this.isAIEnabled = true;

        // 添加调试信息
        String humanColorName = (humanColor == PieceColor.RED) ? "红方" : "黑方";
        String aiColorName = (aiColor == PieceColor.RED) ? "红方" : "黑方";
        System.out.println("🎮 DeepSeek+Pikafish AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName);
        addAILog("system", "DeepSeek+Pikafish AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName);

        // 如果当前轮到AI，立即开始AI回合
        if (aiColor == currentPlayer) {
            SwingUtilities.invokeLater(this::performAIMove);
        }

        updateStatus();
    }
    
    /**
     * 启用Fairy-Stockfish AI对弈
     * @param humanColor 人类玩家颜色
     * @param difficulty AI难度 (1-10)
     */
    public void enableFairyStockfishAI(PieceColor humanColor, int difficulty) {
        enableFairyStockfishAI(humanColor, difficulty, null);
    }
    
    /**
     * 启用Fairy-Stockfish AI对弈（支持神经网络选择）
     * @param humanColor 人类玩家颜色
     * @param difficulty AI难度 (1-10)
     * @param neuralNetworkPath 神经网络文件路径（可为null）
     */
    public void enableFairyStockfishAI(PieceColor humanColor, int difficulty, String neuralNetworkPath) {
        this.humanPlayer = humanColor;
        this.useFairyStockfish = true;
        this.useLLM = false;
        this.useEnhanced = false;
        this.useHybrid = false;
        this.useDeepSeekPikafish = false;

        PieceColor aiColor = (humanColor == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
        this.fairyStockfishAI = new FairyStockfishAI(aiColor, difficulty, neuralNetworkPath);
        
        // 设置AI日志面板
        if (this.aiLogPanel != null) {
            this.fairyStockfishAI.setAILogPanel(this.aiLogPanel);
        }
        
        this.isAIEnabled = true;

        // 添加调试信息
        String humanColorName = (humanColor == PieceColor.RED) ? "红方" : "黑方";
        String aiColorName = (aiColor == PieceColor.RED) ? "红方" : "黑方";
        System.out.println("🧚 Fairy-Stockfish AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName);
        if (neuralNetworkPath != null && !neuralNetworkPath.isEmpty()) {
            System.out.println("   - 神经网络: " + neuralNetworkPath);
            addAILog("system", "Fairy-Stockfish AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName + ", NN=" + neuralNetworkPath);
        } else {
            addAILog("system", "Fairy-Stockfish AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName);
        }

        // 如果当前轮到AI，立即开始AI回合
        if (aiColor == currentPlayer) {
            SwingUtilities.invokeLater(this::performAIMove);
        }

        updateStatus();
    }
    
    /**
     * 启用纯 Pikafish AI对弈
     * @param humanColor 人类玩家颜色
     * @param difficulty AI难度 (1-10)
     */
    public void enablePikafishAI(PieceColor humanColor, int difficulty) {
        this.humanPlayer = humanColor;
        this.usePikafish = true;
        this.useLLM = false;
        this.useEnhanced = false;
        this.useHybrid = false;
        this.useDeepSeekPikafish = false;
        this.useFairyStockfish = false;

        PieceColor aiColor = (humanColor == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
        this.pikafishAI = new PikafishAI(aiColor, difficulty);
        
        // 设置AI日志面板
        if (this.aiLogPanel != null) {
            this.pikafishAI.setAILogPanel(this.aiLogPanel);
        }
        
        this.isAIEnabled = true;

        // 添加调试信息
        String humanColorName = (humanColor == PieceColor.RED) ? "红方" : "黑方";
        String aiColorName = (aiColor == PieceColor.RED) ? "红方" : "黑方";
        System.out.println("🐟 Pikafish AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName);
        addAILog("system", "Pikafish AI对弈设置: 玩家=" + humanColorName + ", AI=" + aiColorName);

        // 如果当前轮到AI，立即开始AI回合
        if (aiColor == currentPlayer) {
            SwingUtilities.invokeLater(this::performAIMove);
        }

        updateStatus();
    }
    
    /**
     * 禁用AI对弈
     */
    public void disableAI() {
        this.isAIEnabled = false;
        this.useLLM = false;
        this.useEnhanced = false;
        this.useHybrid = false;
        this.useDeepSeekPikafish = false;
        this.useFairyStockfish = false;
        this.usePikafish = false;
        
        // 清理AI实例
        this.ai = null;
        this.llmChessAI = null;
        this.enhancedAI = null;
        
        // 关闭混合AI资源
        if (this.hybridAI != null) {
            this.hybridAI.close();
            this.hybridAI = null;
        }
        
        // 关闭DeepSeekPikafishAI资源
        if (this.deepSeekPikafishAI != null) {
            // this.deepSeekPikafishAI.close(); // 该AI引擎可能不需要手动关闭资源
            this.deepSeekPikafishAI = null;
        }
        
        // 关闭FairyStockfishAI资源
        if (this.fairyStockfishAI != null) {
            this.fairyStockfishAI.cleanup();
            this.fairyStockfishAI = null;
        }
        
        // 关闭PikafishAI资源
        if (this.pikafishAI != null) {
            this.pikafishAI.cleanup();
            this.pikafishAI = null;
        }
        
        this.isAIThinking = false;
        updateStatus();
    }
    
    /**
     * 检查当前是否是AI回合
     */
    private boolean isAITurn() {
        // 在AI对AI模式下，总是AI回合
        if (isAIvsAIMode) {
            return true;
        }
        // 在玩家对玩家模式下，永远不是AI回合
        if (!isAIEnabled) {
            return false;
        }
        // 在玩家对AI模式下，检查当前玩家是否是AI
        return isAIEnabled && currentPlayer != humanPlayer;
    }
    
    private void updateStatus() {
        if (statusUpdateCallback != null) {
            String playerName = (currentPlayer == PieceColor.RED) ? "红方" : "黑方";
            String status = "当前玩家: " + playerName;
            
            if (isAIvsAIMode) {
                // AI vs AI模式
                status += isAIThinking ? " (AI思考中...)" : " (AI)";
                status = "🤖 AI vs AI对弈 - " + status;
            } else if (isAIEnabled) {
                if (isAITurn()) {
                    String aiType = "";
                    if (usePikafish) {
                        aiType = "Pikafish";
                    } else if (useFairyStockfish) {
                        aiType = "Fairy-Stockfish";
                    } else if (useDeepSeekPikafish) {
                        aiType = "DeepSeek+Pikafish";
                    } else if (useHybrid) {
                        aiType = "混合AI";
                    } else if (useEnhanced) {
                        aiType = "增强AI";
                    } else if (useLLM) {
                        aiType = "大模型AI";
                    } else {
                        aiType = "传统AI";
                    }
                    status += isAIThinking ? " (" + aiType + "思考中...)" : " (" + aiType + ")";
                } else {
                    status += " (人类)";
                }
            }
            
            statusUpdateCallback.accept(status);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawValidMoves(g);
        drawPieces(g);
        drawSelection(g);
        drawAISuggestion(g); // 绘制AI建议标记
    }
    

    
    private void handleMouseClick(int mouseX, int mouseY) {
        // 如果在残局设置模式下，忽略正常的鼠标点击
        if (isSettingUpEndgame) {
            return;
        }
        
        // 如果在AI对AI模式下，禁用用户点击
        if (isAIvsAIMode) {
            return;
        }
        
        // 如果游戏已结束、是AI回合或AI正在思考，忽略鼠标点击
        if (gameState == GameState.RED_WINS || gameState == GameState.BLACK_WINS || 
            gameState == GameState.DRAW || isAITurn() || isAIThinking) {
            return;
        }
        
        // 将鼠标坐标转换为显示坐标
        int displayCol = (mouseX - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
        int displayRow = (mouseY - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
        
        // 检查显示坐标是否在棋盘范围内
        if (displayRow < 0 || displayRow >= 10 || displayCol < 0 || displayCol >= 9) {
            return;
        }
        
        // 转换为逻辑坐标
        int row = getLogicalRow(displayRow);
        int col = getLogicalCol(displayCol);
        
        Piece clickedPiece = board.getPiece(row, col);
        
        if (selectedPiece == null) {
            // 没有选中棋子，尝试选择棋子
            if (clickedPiece != null && clickedPiece.getColor() == currentPlayer) {
                selectedPiece = clickedPiece;
                selectedRow = row;
                selectedCol = col;
                calculateValidMoves();
                repaint();
            }
        } else {
            // 已经选中棋子，尝试移动
            if (row == selectedRow && col == selectedCol) {
                // 点击同一个位置，取消选择
                clearSelection();
            } else if (clickedPiece != null && clickedPiece.getColor() == currentPlayer) {
                // 点击同色棋子，重新选择
                selectedPiece = clickedPiece;
                selectedRow = row;
                selectedCol = col;
                calculateValidMoves();
                repaint();
            } else {
                 // 尝试移动棋子
                 Position start = new Position(selectedRow, selectedCol);
                 Position end = new Position(row, col);
                 if (selectedPiece.isValidMove(board, start, end)) {
                     // 检查移动是否安全（不会导致己方将军被将军）
                     if (board.isMoveSafe(start, end, currentPlayer)) {
                         // 记录移动历史（用于悔棋）
                         Piece capturedPiece = board.getPiece(end.getX(), end.getY());
                             // 保存当前棋盘状态
    saveBoardState();
                         
                         // 记录移动标记
                         lastMoveStart = new Position(start.getX(), start.getY());
                         lastMoveEnd = new Position(end.getX(), end.getY());
                         
                         // 执行移动
                         board.movePiece(start, end);
                         
                         // 播放落子音效
                         SoundPlayer.getInstance().playSound("piece_drop");
                         
                         // 显示移动信息
                         String playerType = (selectedPiece.getColor() == humanPlayer) ? "玩家" : "AI";
                         String colorName = (selectedPiece.getColor() == PieceColor.RED) ? "红方" : "黑方";
                         System.out.println("🎯 " + playerType + "(" + colorName + ")移动: " + selectedPiece.getChineseName() + 
                                          " 从 (" + selectedRow + "," + selectedCol + ") 到 (" + row + "," + col + ")");
                         
                         // 切换玩家
                         currentPlayer = (currentPlayer == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
                         clearSelection();
                         
                         // 用户操作完成后，清除AI建议标记
                         if (showAISuggestion) {
                             clearAISuggestion();
                         }
                         
                         // 检查游戏状态
                         gameState = board.checkGameState(currentPlayer);
                         updateStatus(); // 更新状态显示
                         
                         // 通知聊天面板更新棋盘状态
                         notifyChatPanelBoardUpdate();
                         
                         // 移除自动评估功能，改为仅在玩家主动询问时提供建议
                         
                         // 检查游戏是否结束
                         if (gameState == GameState.RED_WINS) {
                             SoundPlayer.getInstance().playSound("game_win");
                             showGameEndDialog("红方获胜！");
                         } else if (gameState == GameState.BLACK_WINS) {
                             SoundPlayer.getInstance().playSound("game_win");
                             showGameEndDialog("黑方获胜！");
                         } else if (gameState == GameState.DRAW) {
                             showGameEndDialog("和棋！");
                         } else if (gameState == GameState.PLAYING || gameState == GameState.IN_CHECK) {
                             // 如果游戏未结束且启用了AI且现在是AI回合，触发AI移动
                             if (isAIvsAIMode) {
                                 // AI vs AI模式下，延迟执行下一步AI移动
                                 SwingUtilities.invokeLater(() -> {
                                     Timer timer = new Timer(1000, e -> performAIvsAIMove());
                                     timer.setRepeats(false);
                                     timer.start();
                                 });
                             } else if (isAITurn()) {
                                 SwingUtilities.invokeLater(this::performAIMove);
                             }
                         }
                     } else {
                         // 移动会导致己方将军被将军
                         System.out.println("无效移动: 此移动会导致己方将军被将军!");
                     }
                 } else {
                     // 无效移动，保持选择状态
                     System.out.println("无效移动!");
                 }
            }
        }
    }
    
    private void clearSelection() {
        selectedPiece = null;
        selectedRow = -1;
        selectedCol = -1;
        validMoves.clear();
        repaint();
    }
    
    private void calculateValidMoves() {
        validMoves.clear();
        if (selectedPiece != null) {
            Position start = new Position(selectedRow, selectedCol);
            // 遍历整个棋盘，检查每个位置是否是合法走位
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 9; col++) {
                    Position end = new Position(row, col);
                    if (selectedPiece.isValidMove(board, start, end)) {
                        // 同时检查移动是否安全（不会导致己方将军被将军）
                        if (board.isMoveSafe(start, end, currentPlayer)) {
                            validMoves.add(end);
                        }
                    }
                }
            }
        }
    }
    
    private void drawValidMoves(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for (Position pos : validMoves) {
            // 转换为显示坐标
            int displayRow = getDisplayRow(pos.getX());
            int displayCol = getDisplayCol(pos.getY());
            
            int centerX = MARGIN + displayCol * CELL_SIZE;
            int centerY = MARGIN + displayRow * CELL_SIZE;
            
            // 绘制3D合法移动提示
            draw3DValidMoveIndicator(g2d, centerX, centerY);
        }
    }
    
    /**
     * 绘制3D合法移动指示器
     */
    private void draw3DValidMoveIndicator(Graphics2D g2d, int centerX, int centerY) {
        int size = CELL_SIZE / 3;
        
        // 创建脉动效果
        long time = System.currentTimeMillis();
        float pulse = (float)(0.6 + 0.4 * Math.sin(time * 0.01));
        
        // 绘制外层光环
        drawValidMoveGlow(g2d, centerX, centerY, (int)(size * 1.8 * pulse));
        
        // 绘制主体圆点
        drawValidMoveCore(g2d, centerX, centerY, size);
    }
    
    /**
     * 绘制合法移动光环
     */
    private void drawValidMoveGlow(Graphics2D g2d, int centerX, int centerY, int size) {
        // 创建径向渐变光环
        RadialGradientPaint glowGradient = new RadialGradientPaint(
            centerX, centerY, size / 2,
            new float[]{0.0f, 0.8f, 1.0f},
            new Color[]{
                new Color(0, 255, 0, 120),
                new Color(0, 255, 0, 60),
                new Color(0, 255, 0, 0)
            }
        );
        
        g2d.setPaint(glowGradient);
        g2d.fillOval(centerX - size / 2, centerY - size / 2, size, size);
    }
    
    /**
     * 绘制合法移动核心圆点
     */
    private void drawValidMoveCore(Graphics2D g2d, int centerX, int centerY, int size) {
        // 绘制主体
        RadialGradientPaint coreGradient = new RadialGradientPaint(
            centerX - size / 4, centerY - size / 4, size / 2,
            new float[]{0.0f, 0.7f, 1.0f},
            new Color[]{
                new Color(150, 255, 150),
                new Color(0, 200, 0),
                new Color(0, 100, 0)
            }
        );
        
        g2d.setPaint(coreGradient);
        g2d.fillOval(centerX - size / 2, centerY - size / 2, size, size);
        
        // 绘制边框
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(new Color(0, 150, 0));
        g2d.drawOval(centerX - size / 2, centerY - size / 2, size, size);
        
        // 绘制高光
        int highlightSize = size / 3;
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.fillOval(centerX - size / 3, centerY - size / 3, highlightSize, highlightSize);
    }
    
    private void drawSelection(Graphics g) {
        if (selectedPiece != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 转换为显示坐标
            int displayRow = getDisplayRow(selectedRow);
            int displayCol = getDisplayCol(selectedCol);
            
            int centerX = MARGIN + displayCol * CELL_SIZE;
            int centerY = MARGIN + displayRow * CELL_SIZE;
            
            // 绘制3D选中效果
            draw3DSelectionEffect(g2d, centerX, centerY);
        }
    }
    
    /**
     * 绘制3D选中效果
     */
    private void draw3DSelectionEffect(Graphics2D g2d, int centerX, int centerY) {
        int baseSize = (int)(CELL_SIZE * 1.2);
        
        // 绘制外层光环
        drawSelectionGlow(g2d, centerX, centerY, baseSize + 20);
        
        // 绘制中层光环
        drawSelectionGlow(g2d, centerX, centerY, baseSize + 10);
        
        // 绘制内层边框
        drawSelectionBorder(g2d, centerX, centerY, baseSize);
    }
    
    /**
     * 绘制选中光环
     */
    private void drawSelectionGlow(Graphics2D g2d, int centerX, int centerY, int size) {
        // 创建脉动效果
        long time = System.currentTimeMillis();
        float pulse = (float)(0.5 + 0.3 * Math.sin(time * 0.008));
        
        // 创建径向渐变光环
        RadialGradientPaint glowGradient = new RadialGradientPaint(
            centerX, centerY, size / 2,
            new float[]{0.0f, 0.7f, 1.0f},
            new Color[]{
                new Color(255, 255, 0, 0),
                new Color(255, 255, 0, (int)(100 * pulse)),
                new Color(255, 255, 0, 0)
            }
        );
        
        g2d.setPaint(glowGradient);
        g2d.fillOval(centerX - size / 2, centerY - size / 2, size, size);
    }
    
    /**
     * 绘制选中边框
     */
    private void drawSelectionBorder(Graphics2D g2d, int centerX, int centerY, int size) {
        // 外边框
        g2d.setStroke(new BasicStroke(4));
        g2d.setColor(new Color(255, 215, 0)); // 金色
        g2d.drawOval(centerX - size / 2, centerY - size / 2, size, size);
        
        // 内边框
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(255, 255, 255, 200)); // 白色高光
        g2d.drawOval(centerX - size / 2 + 3, centerY - size / 2 + 3, size - 6, size - 6);
        
        // 最内层边框
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(new Color(255, 215, 0, 150));
        g2d.drawOval(centerX - size / 2 + 6, centerY - size / 2 + 6, size - 12, size - 12);
    }

    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // 绘制3D背景
        draw3DBackground(g2d);
        
        // 绘制3D棋盘
        draw3DChessBoard(g2d);
        
        // 绘制九宫格
        draw3DPalaceLines(g2d);
        
        // 绘制楚河汉界
        draw3DRiverText(g2d);
        
        // 绘制坐标
        drawCoordinates(g2d);
    }
    
    /**
     * 绘制3D背景效果
     */
    private void draw3DBackground(Graphics2D g2d) {
        // 绘制华丽的背景渐变
        drawLuxuriousBackground(g2d);
        
        // 添加装饰性边框
        drawDecorativeBorder(g2d);
        
        // 添加木纹纹理效果
        drawEnhancedWoodTexture(g2d);
        
        // 添加背景装饰图案
        drawBackgroundPattern(g2d);
    }
    
    /**
     * 绘制华丽背景渐变
     */
    private void drawLuxuriousBackground(Graphics2D g2d) {
        // 主背景渐变 - 从金黄到深褐色
        RadialGradientPaint mainGradient = new RadialGradientPaint(
            getWidth() * 0.3f, getHeight() * 0.3f, Math.max(getWidth(), getHeight()),
            new float[]{0.0f, 0.5f, 1.0f},
            new Color[]{
                new Color(255, 248, 220),  // 象牙白
                new Color(245, 222, 179),  // 浅木色
                new Color(160, 120, 90)    // 深木色
            }
        );
        g2d.setPaint(mainGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // 添加暖色调光晕效果
        RadialGradientPaint warmGlow = new RadialGradientPaint(
            getWidth() * 0.7f, getHeight() * 0.2f, getWidth() * 0.6f,
            new float[]{0.0f, 1.0f},
            new Color[]{
                new Color(255, 215, 0, 30),   // 金色光晕
                new Color(255, 215, 0, 0)
            }
        );
        g2d.setPaint(warmGlow);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    
    /**
     * 绘制装饰性边框
     */
    private void drawDecorativeBorder(Graphics2D g2d) {
        int borderWidth = 15;
        
        // 外边框 - 深色
        g2d.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GradientPaint borderGradient = new GradientPaint(
            0, 0, new Color(101, 67, 33),
            getWidth(), getHeight(), new Color(139, 69, 19)
        );
        g2d.setPaint(borderGradient);
        g2d.drawRect(borderWidth/2, borderWidth/2, 
                    getWidth() - borderWidth, getHeight() - borderWidth);
        
        // 内边框 - 金色装饰
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(255, 215, 0, 180));
        g2d.drawRect(borderWidth + 5, borderWidth + 5,
                    getWidth() - 2 * borderWidth - 10, getHeight() - 2 * borderWidth - 10);
        
        // 角落装饰
        drawCornerDecorations(g2d, borderWidth);
    }
    
    /**
     * 绘制角落装饰
     */
    private void drawCornerDecorations(Graphics2D g2d, int borderWidth) {
        g2d.setColor(new Color(255, 215, 0, 120));
        g2d.setStroke(new BasicStroke(2));
        
        int decorSize = 20;
        int offset = borderWidth + 10;
        
        // 四个角落的装饰图案
        int[][] corners = {{offset, offset}, {getWidth() - offset - decorSize, offset},
                          {offset, getHeight() - offset - decorSize}, 
                          {getWidth() - offset - decorSize, getHeight() - offset - decorSize}};
        
        for (int[] corner : corners) {
            int x = corner[0], y = corner[1];
            // 绘制花纹装饰
            g2d.drawArc(x, y, decorSize, decorSize, 0, 90);
            g2d.drawArc(x + 5, y + 5, decorSize - 10, decorSize - 10, 0, 90);
            
            // 添加小点装饰
            g2d.fillOval(x + decorSize/2 - 2, y + decorSize/2 - 2, 4, 4);
        }
    }
    
    /**
     * 绘制增强的木纹纹理
     */
    private void drawEnhancedWoodTexture(Graphics2D g2d) {
        // 水平木纹
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        for (int y = 0; y < getHeight(); y += 12) {
            // 变化的木纹颜色
            int colorVariation = (int)(Math.sin(y * 0.05) * 30);
            g2d.setColor(new Color(139 + colorVariation, 69 + colorVariation/2, 19 + colorVariation/3));
            
            // 波浪形木纹
            for (int x = 0; x < getWidth(); x += 8) {
                int waveY = y + (int)(Math.sin(x * 0.03 + y * 0.01) * 4);
                g2d.drawLine(x, waveY, x + 6, waveY);
            }
        }
        
        // 垂直纹理
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
        for (int x = 0; x < getWidth(); x += 20) {
            int colorVar = (int)(Math.cos(x * 0.02) * 20);
            g2d.setColor(new Color(120 + colorVar, 80 + colorVar/2, 40 + colorVar/3));
            g2d.drawLine(x, 0, x, getHeight());
        }
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    /**
     * 绘制背景装饰图案
     */
    private void drawBackgroundPattern(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
        g2d.setColor(new Color(139, 69, 19));
        
        // 在背景绘制传统图案
        int patternSize = 40;
        for (int x = patternSize; x < getWidth() - patternSize; x += patternSize * 2) {
            for (int y = patternSize; y < getHeight() - patternSize; y += patternSize * 2) {
                // 避开棋盘区域
                if (x > MARGIN - patternSize && x < MARGIN + 8 * CELL_SIZE + patternSize &&
                    y > MARGIN - patternSize && y < MARGIN + 9 * CELL_SIZE + patternSize) {
                    continue;
                }
                drawTraditionalPattern(g2d, x, y, patternSize);
            }
        }
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    /**
     * 绘制传统装饰图案
     */
    private void drawTraditionalPattern(Graphics2D g2d, int centerX, int centerY, int size) {
        int halfSize = size / 2;
        
        // 绘制传统云纹图案
        g2d.setStroke(new BasicStroke(1.5f));
        
        // 中心圆
        g2d.drawOval(centerX - halfSize/3, centerY - halfSize/3, size/3, size/3);
        
        // 四周装饰弧线
        g2d.drawArc(centerX - halfSize, centerY - halfSize/2, halfSize, halfSize, 0, 180);
        g2d.drawArc(centerX, centerY - halfSize/2, halfSize, halfSize, 180, 180);
        g2d.drawArc(centerX - halfSize/2, centerY - halfSize, halfSize, halfSize, 90, 180);
        g2d.drawArc(centerX - halfSize/2, centerY, halfSize, halfSize, 270, 180);
    }
    
    /**
     * 绘制3D棋盘
     */
    private void draw3DChessBoard(Graphics2D g2d) {
        // 绘制棋盘阴影
        drawBoardShadow(g2d);
        
        // 绘制棋盘主体
        drawBoardMain(g2d);
        
        // 绘制棋盘线条
        drawBoardLines(g2d);
    }
    
    /**
     * 绘制棋盘阴影
     */
    private void drawBoardShadow(Graphics2D g2d) {
        int shadowOffset = 8;
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRect(MARGIN + shadowOffset, MARGIN + shadowOffset, 
                    8 * CELL_SIZE, 9 * CELL_SIZE);
    }
    
    /**
     * 绘制棋盘主体
     */
    private void drawBoardMain(Graphics2D g2d) {
        // 创建棋盘渐变效果
        GradientPaint boardGradient = new GradientPaint(
            MARGIN, MARGIN, new Color(255, 248, 220),  // 象牙白
            MARGIN + 8 * CELL_SIZE, MARGIN + 9 * CELL_SIZE, new Color(245, 222, 179)  // 浅木色
        );
        g2d.setPaint(boardGradient);
        g2d.fillRect(MARGIN, MARGIN, 8 * CELL_SIZE, 9 * CELL_SIZE);
        
        // 绘制棋盘边框
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawRect(MARGIN - 2, MARGIN - 2, 8 * CELL_SIZE + 4, 9 * CELL_SIZE + 4);
    }
    
    /**
     * 绘制棋盘线条
     */
    private void drawBoardLines(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(139, 69, 19));
        
        // 绘制水平线
        for (int i = 0; i < 10; i++) {
            int y = MARGIN + i * CELL_SIZE;
            // 主线
            g2d.drawLine(MARGIN, y, MARGIN + 8 * CELL_SIZE, y);
            // 添加3D效果的高光线
            g2d.setColor(new Color(160, 82, 45, 100));
            g2d.drawLine(MARGIN, y + 1, MARGIN + 8 * CELL_SIZE, y + 1);
            g2d.setColor(new Color(139, 69, 19));
        }
        
        // 绘制垂直线
        for (int i = 0; i < 9; i++) {
            int x = MARGIN + i * CELL_SIZE;
            // 主线
            g2d.drawLine(x, MARGIN, x, MARGIN + 9 * CELL_SIZE);
            // 添加3D效果的高光线
            g2d.setColor(new Color(160, 82, 45, 100));
            g2d.drawLine(x + 1, MARGIN, x + 1, MARGIN + 9 * CELL_SIZE);
            g2d.setColor(new Color(139, 69, 19));
        }
    }
    
    /**
     * 绘制3D九宫格
     */
    private void draw3DPalaceLines(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(139, 69, 19));
        
        // 上方九宫格
        drawPalaceLine(g2d, MARGIN + 3 * CELL_SIZE, MARGIN, 
                      MARGIN + 5 * CELL_SIZE, MARGIN + 2 * CELL_SIZE);
        drawPalaceLine(g2d, MARGIN + 5 * CELL_SIZE, MARGIN, 
                      MARGIN + 3 * CELL_SIZE, MARGIN + 2 * CELL_SIZE);
        
        // 下方九宫格
        drawPalaceLine(g2d, MARGIN + 3 * CELL_SIZE, MARGIN + 7 * CELL_SIZE, 
                      MARGIN + 5 * CELL_SIZE, MARGIN + 9 * CELL_SIZE);
        drawPalaceLine(g2d, MARGIN + 5 * CELL_SIZE, MARGIN + 7 * CELL_SIZE, 
                      MARGIN + 3 * CELL_SIZE, MARGIN + 9 * CELL_SIZE);
    }
    
    /**
     * 绘制带3D效果的九宫格线条
     */
    private void drawPalaceLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        // 主线
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawLine(x1, y1, x2, y2);
        
        // 高光效果
        g2d.setColor(new Color(160, 82, 45, 100));
        g2d.drawLine(x1 + 1, y1 + 1, x2 + 1, y2 + 1);
    }
    
    /**
     * 绘制3D楚河汉界
     */
    private void draw3DRiverText(Graphics2D g2d) {
        // 绘制河流效果
        drawRiverEffect(g2d);
        
        // 绘制文字
        g2d.setFont(new Font("宋体", Font.BOLD, 36));
        
        // 计算文字位置，确保完全显示在河流中央
        int riverY = MARGIN + 4 * CELL_SIZE;
        int textY = riverY + CELL_SIZE/2 + 15; // 调整垂直位置使文字居中
        
        // 绘制"楚河"
        draw3DText(g2d, "楚河", MARGIN + CELL_SIZE + 10, textY);
        
        // 绘制"汉界"
        draw3DText(g2d, "汉界", MARGIN + 5 * CELL_SIZE + 10, textY);
    }
    
    /**
     * 绘制河流效果
     */
    private void drawRiverEffect(Graphics2D g2d) {
        // 河流区域
        int riverY = MARGIN + 4 * CELL_SIZE;
        int riverHeight = CELL_SIZE;
        
        // 绘制河流底层（深水区）
        drawRiverBase(g2d, MARGIN, riverY, 8 * CELL_SIZE, riverHeight);
        
        // 绘制水面反射效果
        drawWaterReflection(g2d, MARGIN, riverY, 8 * CELL_SIZE, riverHeight);
        
        // 绘制水波纹效果
        drawWaterRipples(g2d, MARGIN, riverY, 8 * CELL_SIZE, riverHeight);
    }
    
    /**
     * 绘制河流底层
     */
    private void drawRiverBase(Graphics2D g2d, int x, int y, int width, int height) {
        // 创建深邃的水底渐变
        RadialGradientPaint deepWaterGradient = new RadialGradientPaint(
            x + width / 2, y + height / 2, width,
            new float[]{0.0f, 0.6f, 1.0f},
            new Color[]{
                new Color(25, 75, 150, 200),   // 深蓝色中心
                new Color(65, 105, 225, 180),  // 中等蓝色
                new Color(100, 149, 237, 160)  // 浅蓝色边缘
            }
        );
        g2d.setPaint(deepWaterGradient);
        g2d.fillRect(x, y, width, height);
        
        // 添加水底纹理
        drawWaterBottomTexture(g2d, x, y, width, height);
    }
    
    /**
     * 绘制水底纹理
     */
    private void drawWaterBottomTexture(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        
        // 绘制水底的石块纹理
        for (int i = 0; i < 15; i++) {
            int stoneX = x + (int)(Math.random() * width);
            int stoneY = y + (int)(Math.random() * height);
            int stoneSize = 8 + (int)(Math.random() * 12);
            
            g2d.setColor(new Color(70, 70, 80, 100));
            g2d.fillOval(stoneX, stoneY, stoneSize, stoneSize / 2);
        }
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    /**
     * 绘制水面反射效果
     */
    private void drawWaterReflection(Graphics2D g2d, int x, int y, int width, int height) {
        // 创建水面反射渐变
        GradientPaint reflectionGradient = new GradientPaint(
            x, y, new Color(255, 255, 255, 40),
            x + width, y + height, new Color(173, 216, 230, 80)
        );
        g2d.setPaint(reflectionGradient);
        g2d.fillRect(x, y, width, height / 3);
        
        // 添加动态光斑效果
        long time = System.currentTimeMillis();
        for (int i = 0; i < 8; i++) {
            float phase = (float)((time * 0.003f + i * 0.8f) % (Math.PI * 2));
            int spotX = x + (int)(width * (0.2f + 0.6f * (i / 8.0f)));
            int spotY = y + (int)(height * 0.3f + Math.sin(phase) * height * 0.2f);
            int spotSize = 15 + (int)(Math.sin(phase + Math.PI) * 8);
            
            RadialGradientPaint lightSpot = new RadialGradientPaint(
                spotX, spotY, spotSize,
                new float[]{0.0f, 1.0f},
                new Color[]{
                    new Color(255, 255, 255, 60),
                    new Color(255, 255, 255, 0)
                }
            );
            g2d.setPaint(lightSpot);
            g2d.fillOval(spotX - spotSize, spotY - spotSize/2, spotSize * 2, spotSize);
        }
    }
    
    /**
     * 绘制水波纹效果
     */
    private void drawWaterRipples(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.setStroke(new BasicStroke(1.5f));
        
        // 获取当前时间用于动画效果
        long time = System.currentTimeMillis() / 100;
        
        // 绘制多条水波纹
        for (int i = 0; i < 8; i++) {
            int waveOffset = (int)(Math.sin((time + i * 20) * 0.05) * 5);
            int waveY = y + (height / 8) * i + waveOffset;
            g2d.drawLine(x, waveY, x + width, waveY);
        }
        
        // 绘制一些随机的小波点
        g2d.setColor(new Color(255, 255, 255, 60));
        for (int i = 0; i < 20; i++) {
            int dotX = x + (int)(Math.random() * width);
            int dotY = y + (int)(Math.random() * height);
            int dotSize = 2 + (int)(Math.random() * 3);
            g2d.fillOval(dotX, dotY, dotSize, dotSize);
        }
    }
    
    /**
     * 绘制3D文字效果
     */
    private void draw3DText(Graphics2D g2d, String text, int x, int y) {
        // 阴影
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(text, x + 2, y + 2);
        
        // 主文字
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawString(text, x, y);
        
        // 高光
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.drawString(text, x - 1, y - 1);
    }
    
    /**
     * 绘制棋盘坐标
     */
    private void drawCoordinates(Graphics2D g2d) {
        g2d.setFont(new Font("宋体", Font.BOLD, 16));
        
        // 绘制纵坐标 - 红方用中文数字（从右到左：一到九），黑方用阿拉伯数字（从左到右：1到9）
        String[] redNumbers = {"九", "八", "七", "六", "五", "四", "三", "二", "一"}; // 从右到左
        String[] blackNumbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9"}; // 从左到右
        
        for (int i = 0; i < 9; i++) {
            int x = MARGIN + i * CELL_SIZE;
            
            // 上方（黑方）用阿拉伯数字 - 调整位置避免被棋子遮挡
            draw3DCoordinateText(g2d, blackNumbers[i], x - 8, MARGIN - 35);
            
            // 下方（红方）用中文数字 - 调整位置避免被棋子遮挡
            draw3DCoordinateText(g2d, redNumbers[i], x - 8, MARGIN + 9 * CELL_SIZE + 45);
        }
        
        // 不再绘制左右两边的横坐标，只保留上下的纵坐标
    }
    
    /**
     * 绘制带3D效果的坐标文字
     */
    private void draw3DCoordinateText(Graphics2D g2d, String text, int x, int y) {
        // 阴影
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.drawString(text, x + 1, y + 1);
        
        // 主文字
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawString(text, x, y);
        
        // 高光
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.drawString(text, x - 1, y - 1);
    }

    private void drawPieces(Graphics g) {
        // 绘制移动标记
        drawMoveMarkers(g);
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                Piece piece = board.getPiece(i, j);
                if (piece != null) {
                    // 使用显示坐标绘制棋子
                    int displayRow = getDisplayRow(i);
                    int displayCol = getDisplayCol(j);
                    drawPiece(g, piece, displayRow, displayCol);
                }
            }
        }
    }
    
    /**
     * 绘制移动标记
     */
    private void drawMoveMarkers(Graphics g) {
        if (lastMoveStart != null && lastMoveEnd != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 转换为显示坐标
            int startDisplayRow = getDisplayRow(lastMoveStart.getX());
            int startDisplayCol = getDisplayCol(lastMoveStart.getY());
            int endDisplayRow = getDisplayRow(lastMoveEnd.getX());
            int endDisplayCol = getDisplayCol(lastMoveEnd.getY());
            
            // 计算屏幕坐标
            int startX = MARGIN + startDisplayCol * CELL_SIZE;
            int startY = MARGIN + startDisplayRow * CELL_SIZE;
            int endX = MARGIN + endDisplayCol * CELL_SIZE;
            int endY = MARGIN + endDisplayRow * CELL_SIZE;
            
            // 绘制起始位置标记（虚线圆圈）
            g2d.setColor(new Color(255, 165, 0, 180)); // 橙色
            float[] dashPattern = {5.0f, 5.0f}; // 虚线模式
            g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashPattern, 0));
            g2d.drawOval(startX - 18, startY - 18, 36, 36);
            
            // 绘制结束位置标记（实线圆圈）
            g2d.setColor(new Color(255, 0, 0, 200)); // 红色
            g2d.setStroke(new BasicStroke(4.0f));
            g2d.drawOval(endX - 20, endY - 20, 40, 40);
            
            // 添加内圈强调效果
            g2d.setColor(new Color(255, 0, 0, 100));
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(endX - 15, endY - 15, 30, 30);
        }
    }
    
    /**
     * 绘制AI建议标记
     */
    private void drawAISuggestion(Graphics g) {
        if (!showAISuggestion || aiSuggestionStart == null || aiSuggestionEnd == null) {
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 转换为显示坐标
        int startDisplayRow = getDisplayRow(aiSuggestionStart.getX());
        int startDisplayCol = getDisplayCol(aiSuggestionStart.getY());
        int endDisplayRow = getDisplayRow(aiSuggestionEnd.getX());
        int endDisplayCol = getDisplayCol(aiSuggestionEnd.getY());
        
        // 计算屏幕坐标
        int startX = MARGIN + startDisplayCol * CELL_SIZE;
        int startY = MARGIN + startDisplayRow * CELL_SIZE;
        int endX = MARGIN + endDisplayCol * CELL_SIZE;
        int endY = MARGIN + endDisplayRow * CELL_SIZE;
        
        // 绘制AI建议的起始位置标记（蓝色闪烁圆圈）
        drawAISuggestionStart(g2d, startX, startY);
        
        // 绘制AI建议的目标位置标记（绿色闪烁圆圈）
        drawAISuggestionEnd(g2d, endX, endY);
        
        // 绘制连接箭头
        drawAISuggestionArrow(g2d, startX, startY, endX, endY);
    }
    
    /**
     * 绘制AI建议的起始位置标记
     */
    private void drawAISuggestionStart(Graphics2D g2d, int centerX, int centerY) {
        // 创建脉动效果
        long time = System.currentTimeMillis();
        float pulse = (float)(0.5 + 0.5 * Math.sin(time * 0.006));
        
        // 外层蓝色光环
        int outerSize = (int)(50 * pulse);
        g2d.setColor(new Color(30, 144, 255, (int)(80 * pulse))); // 蓝色
        g2d.setStroke(new BasicStroke(4.0f));
        g2d.drawOval(centerX - outerSize/2, centerY - outerSize/2, outerSize, outerSize);
        
        // 内层实心圆
        int innerSize = 25;
        g2d.setColor(new Color(30, 144, 255, 150)); // 半透明蓝色
        g2d.fillOval(centerX - innerSize/2, centerY - innerSize/2, innerSize, innerSize);
        
        // 边框
        g2d.setColor(new Color(0, 100, 200));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(centerX - innerSize/2, centerY - innerSize/2, innerSize, innerSize);
        
        // 中心高光
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillOval(centerX - 6, centerY - 6, 12, 12);
    }
    
    /**
     * 绘制AI建议的目标位置标记
     */
    private void drawAISuggestionEnd(Graphics2D g2d, int centerX, int centerY) {
        // 创建脉动效果
        long time = System.currentTimeMillis();
        float pulse = (float)(0.5 + 0.5 * Math.sin(time * 0.008));
        
        // 外层绿色光环
        int outerSize = (int)(60 * pulse);
        g2d.setColor(new Color(34, 139, 34, (int)(100 * pulse))); // 森林绿
        g2d.setStroke(new BasicStroke(5.0f));
        g2d.drawOval(centerX - outerSize/2, centerY - outerSize/2, outerSize, outerSize);
        
        // 中层绿色圆环
        int middleSize = 35;
        g2d.setColor(new Color(34, 139, 34, 120));
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawOval(centerX - middleSize/2, centerY - middleSize/2, middleSize, middleSize);
        
        // 内层实心圆
        int innerSize = 28;
        g2d.setColor(new Color(50, 205, 50, 150)); // 半透明绿色
        g2d.fillOval(centerX - innerSize/2, centerY - innerSize/2, innerSize, innerSize);
        
        // 边框
        g2d.setColor(new Color(0, 100, 0));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(centerX - innerSize/2, centerY - innerSize/2, innerSize, innerSize);
        
        // 中心星形标记
        drawStar(g2d, centerX, centerY, 8, new Color(255, 255, 255, 200));
    }
    
    /**
     * 绘制AI建议的连接箭头
     */
    private void drawAISuggestionArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        // 创建渐变色箭头
        g2d.setColor(new Color(255, 165, 0, 200)); // 橙色
        g2d.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // 绘制箭头线（稍微偏移，避免与棋子重叠）
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int lineStartX = (int)(x1 + 25 * Math.cos(angle));
        int lineStartY = (int)(y1 + 25 * Math.sin(angle));
        int lineEndX = (int)(x2 - 30 * Math.cos(angle));
        int lineEndY = (int)(y2 - 30 * Math.sin(angle));
        
        g2d.drawLine(lineStartX, lineStartY, lineEndX, lineEndY);
        
        // 绘制箭头头部
        drawArrowHead(g2d, lineEndX, lineEndY, angle);
        
        // 添加箭头动画效果（虚线移动）
        long time = System.currentTimeMillis();
        float dashOffset = (float)((time / 10) % 20);
        float[] dashPattern = {10.0f, 10.0f};
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                                     0, dashPattern, dashOffset));
        g2d.setColor(new Color(255, 255, 0, 150)); // 黄色虚线
        g2d.drawLine(lineStartX, lineStartY, lineEndX, lineEndY);
    }
    
    /**
     * 绘制箭头头部
     */
    private void drawArrowHead(Graphics2D g2d, int x, int y, double angle) {
        int arrowLength = 20;
        double arrowAngle = Math.PI / 6;
        
        int x1 = (int) (x - arrowLength * Math.cos(angle - arrowAngle));
        int y1 = (int) (y - arrowLength * Math.sin(angle - arrowAngle));
        int x2 = (int) (x - arrowLength * Math.cos(angle + arrowAngle));
        int y2 = (int) (y - arrowLength * Math.sin(angle + arrowAngle));
        
        // 绘制实心箭头头部
        int[] xPoints = {x, x1, x2};
        int[] yPoints = {y, y1, y2};
        
        g2d.setColor(new Color(255, 165, 0, 200));
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        g2d.setColor(new Color(200, 120, 0));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawPolygon(xPoints, yPoints, 3);
    }
    
    /**
     * 绘制星形标记
     */
    private void drawStar(Graphics2D g2d, int centerX, int centerY, int radius, Color color) {
        g2d.setColor(color);
        
        // 绘制五角星
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5;
            int r = (i % 2 == 0) ? radius : radius / 2;
            xPoints[i] = (int)(centerX + r * Math.cos(angle - Math.PI / 2));
            yPoints[i] = (int)(centerY + r * Math.sin(angle - Math.PI / 2));
        }
        
        g2d.fillPolygon(xPoints, yPoints, 10);
    }
    
    /**
     * 绘制箭头
     */
    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        // 绘制箭头线
        g2d.drawLine(x1, y1, x2, y2);
        
        // 计算箭头头部
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowLength = 15;
        double arrowAngle = Math.PI / 6;
        
        int x3 = (int) (x2 - arrowLength * Math.cos(angle - arrowAngle));
        int y3 = (int) (y2 - arrowLength * Math.sin(angle - arrowAngle));
        int x4 = (int) (x2 - arrowLength * Math.cos(angle + arrowAngle));
        int y4 = (int) (y2 - arrowLength * Math.sin(angle + arrowAngle));
        
        g2d.drawLine(x2, y2, x3, y3);
        g2d.drawLine(x2, y2, x4, y4);
    }
    
    /**
     * 设置AI建议标记
     * @param startPos AI建议的起始位置
     * @param endPos AI建议的目标位置
     * @param autoHideDelayMs 自动隐藏延迟时间（毫秒），0表示不自动隐藏
     */
    public void setAISuggestion(Position startPos, Position endPos, int autoHideDelayMs) {
        this.aiSuggestionStart = startPos;
        this.aiSuggestionEnd = endPos;
        this.showAISuggestion = true;
        
        // 停止之前的定时器
        if (aiSuggestionTimer != null && aiSuggestionTimer.isRunning()) {
            aiSuggestionTimer.stop();
        }
        
        // 如果设置了自动隐藏延迟，启动定时器
        if (autoHideDelayMs > 0) {
            aiSuggestionTimer = new Timer(autoHideDelayMs, e -> clearAISuggestion());
            aiSuggestionTimer.setRepeats(false);
            aiSuggestionTimer.start();
        }
        
        // 立即重绘棋盘以显示标记
        repaint();
        
        // 添加日志
        String startNotation = convertPositionToNotation(startPos);
        String endNotation = convertPositionToNotation(endPos);
        addAILog("suggestion", "显示AI推荐走法: " + startNotation + " -> " + endNotation);
        System.out.println("💡 显示AI建议标记: (" + startPos.getX() + "," + startPos.getY() + ") -> (" + endPos.getX() + "," + endPos.getY() + ")");
    }
    
    /**
     * 设置AI建议标记（带默认自动隐藏延迟）
     * @param startPos AI建议的起始位置
     * @param endPos AI建议的目标位置
     */
    public void setAISuggestion(Position startPos, Position endPos) {
        setAISuggestion(startPos, endPos, 30000); // 默认30秒后自动隐藏
    }
    
    /**
     * 清除AI建议标记
     */
    public void clearAISuggestion() {
        // 停止自动隐藏定时器
        if (aiSuggestionTimer != null && aiSuggestionTimer.isRunning()) {
            aiSuggestionTimer.stop();
        }
        
        boolean wasShowing = showAISuggestion;
        this.aiSuggestionStart = null;
        this.aiSuggestionEnd = null;
        this.showAISuggestion = false;
        
        // 如果之前在显示建议，则重绘棋盘
        if (wasShowing) {
            repaint();
            addAILog("suggestion", "清除AI推荐走法标记");
            System.out.println("🔄 清除AI建议标记");
        }
    }
    
    /**
     * 检查当前是否在显示AI建议
     */
    public boolean isShowingAISuggestion() {
        return showAISuggestion;
    }
    
    /**
     * 将Position转换为棋谱记号
     */
    private String convertPositionToNotation(Position pos) {
        if (pos == null) {
            return "未知位置";
        }
        // 简单的坐标表示，可以根据需要改为标准象棋记号
        return "(" + (pos.getX() + 1) + "," + (pos.getY() + 1) + ")";
    }
    
    // AI重试相关字段
    private int aiRetryCount = 0;
    private static final int MAX_AI_RETRY_COUNT = 1;
    private static final int RETRY_DELAY_MS = 1000;
    
    /**
     * 执行AI移动
     */
    private void performAIMove() {
        if (isGamePaused) { // 如果游戏暂停，则不执行AI移动
            isAIThinking = false;
            updateStatus();
            return;
        }

        performAIMoveWithRetry(0);
    }
    
    /**
     * 带重试机制的AI移动执行
     */
    private void performAIMoveWithRetry(int retryCount) {
        if (!isAITurn()) {
            return;
        }
        
        isAIThinking = true;
        updateStatus();
        
        // 记录AI开始思考
        String aiType = getCurrentAIType();
        if (retryCount > 0) {
            addAILog("thinking", aiType + "重试计算中... (第" + retryCount + "次重试)");
        } else {
            addAILog("thinking", aiType + "开始分析当前局面...");
        }
        
        // 使用CompletableFuture进行异步AI计算，提供更好的性能监控和异常处理
        CompletableFuture<Move> aiMoveTask = CompletableFuture.supplyAsync(() -> {
            try {
                // 开始性能监控
                PerformanceMonitor.startTimer("AI_calculation");
                
                // 添加思考延迟（可配置）
                int thinkingDelay = GameConfig.getAIThinkingDelay();
                if (thinkingDelay > 0) {
                    Thread.sleep(thinkingDelay);
                }
                
                addAILog("thinking", "正在计算最佳走法...");
                
                Move move = calculateAIMove();
                
                // 结束性能监控
                PerformanceMonitor.endTimer("AI_calculation");
                
                return move;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("AI计算被中断", e);
            } catch (Exception e) {
                throw new RuntimeException("AI计算失败", e);
            }
        }, ResourceManager.getExecutorService());
        
        // 设置超时处理
        Timer timeoutTimer = new Timer(GameConfig.AI_CALCULATION_TIMEOUT_SECONDS * 1000, e -> {
            if (isAIThinking) {
                isAIThinking = false;
                aiMoveTask.cancel(true); // 取消任务
                SwingUtilities.invokeLater(() -> {
                    handleAITimeout(retryCount);
                });
            }
        });
        timeoutTimer.setRepeats(false);
        timeoutTimer.start();
        
        // 处理AI计算结果
        aiMoveTask.whenComplete((aiMove, throwable) -> {
            SwingUtilities.invokeLater(() -> {
                timeoutTimer.stop(); // 停止超时计时器
                isAIThinking = false;
                
                if (throwable != null) {
                     if (throwable instanceof CancellationException) {
                         // 任务被取消，已经在超时处理中处理了
                         return;
                     }
                    handleAIError(throwable, aiType, retryCount);
                } else if (aiMove != null) {
                    executeAIMove(aiMove, aiType);
                    aiRetryCount = 0; // 重置重试计数
                } else {
                    handleNoValidMove(retryCount);
                }
            });
        });
    }
    
    /**
     * 处理AI计算超时
     */
    private void handleAITimeout(int retryCount) {
        String aiType = getCurrentAIType();
        
        // 只在调试模式下记录详细日志，用户界面保持简洁
        if (retryCount < MAX_AI_RETRY_COUNT) {
            // 静默重试，不向用户显示超时信息
            System.out.println("🔄 AI计算超时，正在重试... (第" + (retryCount + 1) + "次尝试)");
            
            Timer retryTimer = new Timer(RETRY_DELAY_MS, e -> {
                performAIMoveWithRetry(retryCount + 1);
            });
            retryTimer.setRepeats(false);
            retryTimer.start();
        } else {
            // 重试次数用完，静默使用兜底方案
            System.out.println("🔄 AI计算多次超时，启用兜底方案");
            handleAIFallback();
        }
    }
    
    /**
     * 获取当前AI类型描述
     */
    private String getCurrentAIType() {
        if (usePikafish) return "Pikafish";
        if (useFairyStockfish) return "Fairy-Stockfish";
        if (useDeepSeekPikafish) return "DeepSeek+Pikafish";
        if (useHybrid) return "混合AI";
        if (useEnhanced) return "增强AI";
        if (useLLM) return "大模型AI";
        return "传统AI";
    }
    
    /**
     * 计算AI移动
     */
    private Move calculateAIMove() throws Exception {
        if (usePikafish && pikafishAI != null) {
            return pikafishAI.getBestMove(board);
        } else if (useFairyStockfish && fairyStockfishAI != null) {
            return fairyStockfishAI.getBestMove(board);
        } else if (useDeepSeekPikafish && deepSeekPikafishAI != null) {
            return deepSeekPikafishAI.getBestMove(board);
        } else if (useHybrid && hybridAI != null) {
            return hybridAI.getBestMove(board);
        } else if (useEnhanced && enhancedAI != null) {
            return enhancedAI.getBestMove(board);
        } else if (useLLM && llmChessAI != null) {
            return llmChessAI.getBestMove(board);
        } else if (ai != null) {
            return ai.getBestMove(board);
        }
        return null;
    }
    
    /**
     * 执行AI移动
     */
    private void executeAIMove(Move aiMove, String aiType) {
        try {
            PerformanceMonitor.startTimer("execute_move");
            
            Position start = aiMove.getStart();
            Position end = aiMove.getEnd();
            Piece movingPiece = board.getPiece(start.getX(), start.getY());
            
            // 记录AI决策
            String moveDescription = formatMoveDescription(movingPiece, start, end);
            addAILog("decision", "AI决定: " + moveDescription);
            ExceptionHandler.logInfo("AI移动: " + moveDescription, aiType);
            
            // 执行移动
            board.movePiece(start, end);
            
            // 设置移动痕迹标记（确保AI移动也能显示移动痕迹）
            lastMoveStart = new Position(start.getX(), start.getY());
            lastMoveEnd = new Position(end.getX(), end.getY());
            
            // 切换玩家
            currentPlayer = (currentPlayer == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
            
            // 检查游戏状态
            gameState = board.checkGameState(currentPlayer);
            
            // 更新状态显示
            updateStatus();
            
            // 通知聊天面板更新棋盘状态
            notifyChatPanelBoardUpdate();
            
            // 检查游戏是否结束
            checkGameEnd();
            
            // 重绘棋盘
            PerformanceMonitor.monitorUIOperation("board_repaint", this::repaint);
            
            PerformanceMonitor.endTimer("execute_move");
            
        } catch (Exception e) {
            ExceptionHandler.handleException(e, "执行AI移动");
        }
    }
    
    /**
     * 格式化移动描述
     */
    private String formatMoveDescription(Piece piece, Position start, Position end) {
        // 使用标准象棋语言描述走法
        String standardNotation = convertToStandardChessNotation(piece, start, end);
        if (standardNotation != null && !standardNotation.isEmpty()) {
            return standardNotation;
        }
        
        // 如果转换失败，使用原有格式作为备用
        if (piece != null) {
            return piece.getChineseName() + " 从 (" + start.getX() + "," + start.getY() + ") 到 (" + 
                   end.getX() + "," + end.getY() + ")";
        } else {
            return "从 (" + start.getX() + "," + start.getY() + ") 到 (" + 
                   end.getX() + "," + end.getY() + ")";
        }
    }
    
    /**
     * 将走法转换为标准象棋语言
     */
    private String convertToStandardChessNotation(Piece piece, Position start, Position end) {
        if (piece == null) {
            return null;
        }
        
        try {
            PieceColor color = piece.getColor();
            
            // 获取棋子名称（包含同线多子区分）
            String pieceName = getPieceNotationNameWithPosition(piece, start);
            
            // 计算起始和结束位置的坐标
            // 注意：Position中x代表行(0-9)，y代表列(0-8)
            int startFile = start.getY(); // 纵线（列，0-8）
            int startRank = start.getX(); // 横线（行，0-9）
            int endFile = end.getY();
            int endRank = end.getX();
            
            // 转换为象棋坐标系统
            String startPos, endPos;
            if (color == PieceColor.RED) {
                // 红方：纵线用中文数字，从右到左为一到九
                startPos = getRedFileNotation(startFile);
                endPos = getRedFileNotation(endFile);
            } else {
                // 黑方：纵线用阿拉伯数字，从左到右为1到9
                startPos = getBlackFileNotation(startFile);
                endPos = getBlackFileNotation(endFile);
            }
            
            // 判断移动方向
            String direction;
            if (startFile == endFile) {
                // 纵向移动
                if (color == PieceColor.RED) {
                    // 红方在下方，进是向上（行号减小），退是向下（行号增大）
                    direction = (endRank < startRank) ? "进" : "退";
                } else {
                    // 黑方在上方，进是向下（行号增大），退是向上（行号减小）
                    direction = (endRank > startRank) ? "进" : "退";
                }
                
                // 对于斜行棋子（马、士、象），数字表示落点所在纵线
                if (piece instanceof Horse || piece instanceof Advisor || piece instanceof Elephant) {
                    return pieceName + startPos + direction + endPos;
                } else {
                    // 对于直行棋子（车、炮、兵、帅/将），直行时数字代表步数
                    int steps = Math.abs(endRank - startRank);
                    String stepNotation = getStepNotation(steps, color);
                    return pieceName + startPos + direction + stepNotation;
                }
            } else {
                // 横向移动（平移）
                direction = "平";
                // 对于直行棋子，横行时数字代表目标纵线
                return pieceName + startPos + direction + endPos;
            }
            
        } catch (Exception e) {
            // 转换失败，返回null使用备用格式
            return null;
        }
    }
    
    /**
     * 获取棋子在棋谱中的名称
     */
    private String getPieceNotationName(Piece piece) {
        PieceColor color = piece.getColor();
        
        if (piece instanceof General) {
            return color == PieceColor.RED ? "帅" : "将";
        }
        if (piece instanceof Advisor) {
            return color == PieceColor.RED ? "仕" : "士";
        }
        if (piece instanceof Elephant) {
            return color == PieceColor.RED ? "相" : "象";
        }
        if (piece instanceof Horse) {
            return "马";
        }
        if (piece instanceof Chariot) {
            return "车";
        }
        if (piece instanceof Cannon) {
            return "炮";
        }
        if (piece instanceof Soldier) {
            return color == PieceColor.RED ? "兵" : "卒";
        }
        return piece.getChineseName();
    }
    
    /**
     * 获取棋子在棋谱中的名称（包含同线多子区分）
     */
    private String getPieceNotationNameWithPosition(Piece piece, Position position) {
        String baseName = getPieceNotationName(piece);
        PieceColor color = piece.getColor();
        int file = position.getY(); // 纵线
        
        // 查找同一纵线上的相同棋子
         List<Position> samePieces = new ArrayList<>();
         for (int row = 0; row < 10; row++) {
             Piece boardPiece = board.getPiece(row, file);
             if (boardPiece != null && 
                 boardPiece.getClass().equals(piece.getClass()) && 
                 boardPiece.getColor() == color) {
                 samePieces.add(new Position(row, file));
             }
         }
        
        // 如果只有一个棋子，直接返回基本名称
        if (samePieces.size() <= 1) {
            return baseName;
        }
        
        // 按行号排序（红方从下到上，黑方从上到下）
        samePieces.sort((p1, p2) -> {
            if (color == PieceColor.RED) {
                return Integer.compare(p2.getX(), p1.getX()); // 红方：行号大的在前（下方在前）
            } else {
                return Integer.compare(p1.getX(), p2.getX()); // 黑方：行号小的在前（上方在前）
            }
        });
        
        // 找到当前棋子的位置索引
        int currentIndex = -1;
        for (int i = 0; i < samePieces.size(); i++) {
            if (samePieces.get(i).getX() == position.getX() && 
                samePieces.get(i).getY() == position.getY()) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex == -1) {
            return baseName; // 未找到，返回基本名称
        }
        
        // 根据棋子数量和位置添加前缀
        if (samePieces.size() == 2) {
            // 两个棋子：前、后
            return (currentIndex == 0 ? "前" : "后") + baseName;
        } else if (samePieces.size() == 3) {
            // 三个棋子：前、中、后
            String[] prefixes = {"前", "中", "后"};
            return prefixes[currentIndex] + baseName;
        } else if (samePieces.size() >= 4) {
            // 四个或更多棋子：前、二、三、四...
            if (currentIndex == 0) {
                return "前" + baseName;
            } else {
                String[] numbers = {"二", "三", "四", "五"};
                if (currentIndex - 1 < numbers.length) {
                    return numbers[currentIndex - 1] + baseName;
                } else {
                    return String.valueOf(currentIndex + 1) + baseName;
                }
            }
        }
        
        return baseName;
    }
    
    /**
     * 获取红方纵线表示（中文数字，从右到左为一到九）
     */
    private String getRedFileNotation(int file) {
        // 红方纵线：从右到左为一到九（file=0对应九，file=8对应一）
        String[] redFiles = {"九", "八", "七", "六", "五", "四", "三", "二", "一"};
        return redFiles[file];
    }
    
    /**
     * 获取黑方纵线表示（阿拉伯数字，从左到右）
     */
    private String getBlackFileNotation(int file) {
        return String.valueOf(file + 1);
    }
    
    /**
     * 获取步数表示
     */
    private String getStepNotation(int steps, PieceColor color) {
        if (color == PieceColor.RED) {
            String[] redNumbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
            return steps > 0 && steps <= 9 ? redNumbers[steps - 1] : String.valueOf(steps);
        } else {
            return String.valueOf(steps);
        }
    }
    
    /**
      * 处理AI错误（带重试机制）
      */
     private void handleAIError(Throwable throwable, String aiType, int retryCount) {
         // 只在控制台记录详细错误，不向用户显示
         System.out.println("🔄 AI计算异常: " + throwable.getMessage());
         
         if (retryCount < MAX_AI_RETRY_COUNT) {
             // 静默重试
             System.out.println("🔄 准备重试AI计算... (第" + (retryCount + 1) + "次重试)");
             Timer retryTimer = new Timer(RETRY_DELAY_MS, e -> {
                 performAIMoveWithRetry(retryCount + 1);
             });
             retryTimer.setRepeats(false);
             retryTimer.start();
         } else {
             // 重试次数用完，静默记录错误并使用兜底方案
             System.out.println("🔄 AI计算多次失败，启用兜底方案");
             
             // 只在严重错误时记录到异常处理器，不显示给用户
             if (throwable.getCause() instanceof InterruptedException) {
                 ExceptionHandler.handleException((Exception) throwable.getCause(), aiType + "计算", false);
             } else if (useLLM) {
                 ExceptionHandler.handleAIException(new Exception(throwable), aiType);
             } else {
                 ExceptionHandler.handleException(new Exception(throwable), aiType + "计算");
             }
             
             handleAIFallback();
         }
     }
    
    /**
     * 处理无有效移动情况（带重试机制）
     */
    private void handleNoValidMove(int retryCount) {
        if (retryCount < MAX_AI_RETRY_COUNT) {
            // 静默重试
            System.out.println("🔄 AI未找到有效移动，准备重试...");
            Timer retryTimer = new Timer(RETRY_DELAY_MS, e -> {
                performAIMoveWithRetry(retryCount + 1);
            });
            retryTimer.setRepeats(false);
            retryTimer.start();
        } else {
            System.out.println("🔄 AI多次未找到有效移动，处理游戏结束逻辑...");
            ExceptionHandler.logWarning("AI无法移动，检查游戏结束条件", "游戏逻辑");
            handleAINoValidMoveGameEnd();
        }
    }
    
    /**
     * 处理AI无法找到有效走法时的游戏结束逻辑
     */
    private void handleAINoValidMoveGameEnd() {
        System.out.println("🎯 AI无法找到有效走法，检查游戏结束条件...");
        addAILog("system", "AI无法找到有效走法，正在检查游戏状态...");
        
        try {
            // 首先检查当前游戏状态
            gameState = board.checkGameState(currentPlayer);
            
            // 如果游戏状态表明游戏已经结束，直接处理
            if (gameState != GameState.PLAYING && gameState != GameState.IN_CHECK) {
                System.out.println("📋 游戏状态已确定: " + gameState);
                addAILog("system", "游戏结束状态: " + gameState);
                
                // 播放胜利音效
                SoundPlayer.getInstance().playSound("game_win");
                
                // 检查并显示游戏结束画面
                checkGameEnd();
                return;
            }
            
            // 如果游戏状态显示还在进行，但AI无法找到走法，说明AI可能遇到问题
            // 这种情况下，宣布AI败负
            System.out.println("🏆 AI无法找到有效走法，判定对方获胜");
            String winnerColorName = (currentPlayer == PieceColor.RED) ? "黑方" : "红方";
            PieceColor winnerColor = (currentPlayer == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
            
            // 设置游戏状态为对方获胜
            gameState = (winnerColor == PieceColor.RED) ? GameState.RED_WINS : GameState.BLACK_WINS;
            
            // 记录游戏结束原因
            String aiColorName = (currentPlayer == PieceColor.RED) ? "红方" : "黑方";
            addAILog("game_end", aiColorName + "AI无法找到有效走法，" + winnerColorName + "获胜！");
            System.out.println("🎊 游戏结束: " + aiColorName + "AI无法走棋，" + winnerColorName + "获胜！");
            
            // 播放胜利音效
            SoundPlayer.getInstance().playSound("game_win");
            
            // 显示游戏结束画面
            SwingUtilities.invokeLater(() -> {
                showGameEndDialog(winnerColorName + "获胜！");
                updateStatus();
            });
            
        } catch (Exception e) {
            System.err.println("❌ 处理AI无效走法游戏结束逻辑失败: " + e.getMessage());
            ExceptionHandler.handleException(e, "AI游戏结束处理");
            
            // 作为最后的兜底，尝试原有的兜底方案
            handleAIFallback();
        }
    }
    
    /**
     * AI兜底方案 - 当AI多次失败时的处理
     */
    private void handleAIFallback() {
        System.out.println("🔄 启用AI兜底方案...");
        
        try {
            // 尝试使用简单的随机移动作为兜底
            Move fallbackMove = generateRandomValidMove();
            
            if (fallbackMove != null) {
                System.out.println("✅ 使用随机移动作为兜底方案");
                executeAIMove(fallbackMove, "AI");
                // 向用户显示AI已完成思考，不暴露是兜底方案
                addAILog("success", "AI移动完成");
            } else {
                // 如果连随机移动都找不到，说明游戏真的已经结束
                System.out.println("⚠️ 无法生成任何有效移动，处理游戏结束");
                handleAINoValidMoveGameEnd();
            }
        } catch (Exception e) {
            System.out.println("❌ 兜底方案执行失败: " + e.getMessage());
            ExceptionHandler.handleException(e, "AI兜底方案");
        }
    }
    
    /**
     * 生成随机有效移动
     */
    private Move generateRandomValidMove() {
        try {
            List<Move> validMoves = new ArrayList<>();
            
            // 遍历所有己方棋子
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 9; col++) {
                    Piece piece = board.getPiece(row, col);
                    if (piece != null && piece.getColor() == currentPlayer) {
                        Position start = new Position(row, col);
                        
                        // 检查该棋子的所有可能移动
                        for (int targetRow = 0; targetRow < 10; targetRow++) {
                            for (int targetCol = 0; targetCol < 9; targetCol++) {
                                Position end = new Position(targetRow, targetCol);
                                
                                if (piece.isValidMove(board, start, end) && 
                                    board.isMoveSafe(start, end, currentPlayer)) {
                                    validMoves.add(new Move(start, end));
                                }
                            }
                        }
                    }
                }
            }
            
            // 随机选择一个有效移动
            if (!validMoves.isEmpty()) {
                int randomIndex = (int)(Math.random() * validMoves.size());
                return validMoves.get(randomIndex);
            }
            
        } catch (Exception e) {
            addAILog("error", "生成随机移动失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 检查游戏结束
     */
    private void checkGameEnd() {
        switch (gameState) {
            case RED_WINS:
                showGameEndDialog("红方获胜！");
                break;
            case BLACK_WINS:
                showGameEndDialog("黑方获胜！");
                break;
            case DRAW:
                showGameEndDialog("和棋！");
                break;
            default:
                // 游戏继续
                break;
        }
    }

    private void drawPiece(Graphics g, Piece piece, int row, int col) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = MARGIN + col * CELL_SIZE;
        int centerY = MARGIN + row * CELL_SIZE;
        int pieceSize = (int)(CELL_SIZE * 0.75); // 减小棋子大小，使其更紧凑
        int x = centerX - pieceSize / 2;
        int y = centerY - pieceSize / 2;
        
        // 绘制3D棋子
        draw3DPieceBase(g2d, piece, centerX, centerY, pieceSize);
        
        // 绘制棋子文字
        draw3DPieceText(g2d, piece, centerX, centerY, pieceSize);
    }
    
    /**
     * 绘制3D棋子底座
     */
    private void draw3DPieceBase(Graphics2D g2d, Piece piece, int centerX, int centerY, int size) {
        // 绘制棋子阴影
        drawPieceShadow(g2d, centerX, centerY, size);
        
        // 绘制棋子主体
        drawPieceBody(g2d, piece, centerX, centerY, size);
        
        // 绘制棋子边框
        drawPieceBorder(g2d, piece, centerX, centerY, size);
        
        // 绘制棋子高光
        drawPieceHighlight(g2d, centerX, centerY, size);
    }
    
    /**
     * 绘制棋子阴影
     */
    private void drawPieceShadow(Graphics2D g2d, int centerX, int centerY, int size) {
        int shadowOffset = 6;
        int shadowSize = size + 4;
        
        // 创建阴影渐变
        RadialGradientPaint shadowGradient = new RadialGradientPaint(
            centerX + shadowOffset, centerY + shadowOffset, shadowSize / 2,
            new float[]{0.0f, 1.0f},
            new Color[]{new Color(0, 0, 0, 80), new Color(0, 0, 0, 0)}
        );
        
        g2d.setPaint(shadowGradient);
        g2d.fillOval(centerX - shadowSize / 2 + shadowOffset, 
                    centerY - shadowSize / 2 + shadowOffset, 
                    shadowSize, shadowSize);
    }
    
    /**
     * 绘制棋子主体
     */
    private void drawPieceBody(Graphics2D g2d, Piece piece, int centerX, int centerY, int size) {
        Color baseColor;
        Color lightColor;
        Color darkColor;
        
        if (piece.getColor() == com.example.chinesechess.core.PieceColor.RED) {
            // 红方棋子：鲜明的红色
            baseColor = new Color(220, 20, 20);
            lightColor = new Color(255, 100, 100);
            darkColor = new Color(150, 0, 0);
        } else {
            // 黑方棋子：深黑色，增强对比度
            baseColor = new Color(20, 20, 20);
            lightColor = new Color(80, 80, 80);
            darkColor = new Color(0, 0, 0);
        }
        
        // 创建球形渐变效果
        RadialGradientPaint bodyGradient = new RadialGradientPaint(
            centerX - size / 6, centerY - size / 6, size / 2,
            new float[]{0.0f, 0.7f, 1.0f},
            new Color[]{lightColor, baseColor, darkColor}
        );
        
        g2d.setPaint(bodyGradient);
        g2d.fillOval(centerX - size / 2, centerY - size / 2, size, size);
    }
    
    /**
     * 绘制棋子边框
     */
    private void drawPieceBorder(Graphics2D g2d, Piece piece, int centerX, int centerY, int size) {
        g2d.setStroke(new BasicStroke(2));
        
        Color borderColor;
        if (piece.getColor() == com.example.chinesechess.core.PieceColor.RED) {
            // 红方棋子：深红色边框
            borderColor = new Color(100, 0, 0);
        } else {
            // 黑方棋子：深灰色边框，增强对比
            borderColor = new Color(120, 120, 120);
        }
        
        g2d.setColor(borderColor);
        g2d.drawOval(centerX - size / 2, centerY - size / 2, size, size);
        
        // 内边框
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.drawOval(centerX - size / 2 + 2, centerY - size / 2 + 2, size - 4, size - 4);
    }
    
    /**
     * 绘制棋子高光
     */
    private void drawPieceHighlight(Graphics2D g2d, int centerX, int centerY, int size) {
        int highlightSize = size / 3;
        int highlightX = centerX - size / 4;
        int highlightY = centerY - size / 4;
        
        // 创建高光渐变
        RadialGradientPaint highlightGradient = new RadialGradientPaint(
            highlightX, highlightY, highlightSize / 2,
            new float[]{0.0f, 1.0f},
            new Color[]{new Color(255, 255, 255, 180), new Color(255, 255, 255, 0)}
        );
        
        g2d.setPaint(highlightGradient);
        g2d.fillOval(highlightX - highlightSize / 2, highlightY - highlightSize / 2, 
                    highlightSize, highlightSize);
    }
    
    /**
     * 绘制3D棋子文字
     */
    private void draw3DPieceText(Graphics2D g2d, Piece piece, int centerX, int centerY, int size) {
        String text = piece.getChineseName();
        Font font = new Font("宋体", Font.BOLD, size / 2);
        g2d.setFont(font);
        
        FontMetrics fm = g2d.getFontMetrics();
        int stringWidth = fm.stringWidth(text);
        int stringHeight = fm.getAscent();
        
        int textX = centerX - stringWidth / 2;
        int textY = centerY + stringHeight / 2 - 2;
        
        // 绘制文字阴影
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(text, textX + 1, textY + 1);
        
        // 绘制主文字
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, textX, textY);
        
        // 绘制文字高光
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawString(text, textX - 1, textY - 1);
    }
    
    /**
     * 显示游戏结束对话框
     */
    private void showGameEndDialog(String message) {
        // 播放胜利动画
        showVictoryAnimation(message);
        
        // 延迟显示对话框，让用户欣赏动画
        Timer dialogTimer = new Timer(3000, e -> {
            int option = JOptionPane.showOptionDialog(
                this,
                message + "\n是否重新开始游戏？",
                "游戏结束",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"重新开始", "退出"},
                "重新开始"
            );
            
            if (option == 0) {
                // 重新开始游戏
                restartGame();
            } else {
                // 退出游戏
                System.exit(0);
            }
        });
        dialogTimer.setRepeats(false);
        dialogTimer.start();
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
            if (message.contains("红方")) {
                winnerColor = Color.RED;
            } else if (message.contains("黑方")) {
                winnerColor = Color.BLACK;
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
     * 重新开始游戏
     */
    public void restartGame() {
        isGamePaused = false; // 重置暂停状态
        board.initializeBoard();
        currentPlayer = PieceColor.RED;
        gameState = GameState.PLAYING;
        clearSelection();
        isAIThinking = false;
        
        // 检查AI是否是红方（先手），如果是则立即开始AI回合
        if (isAIEnabled) {
            PieceColor aiColor = (humanPlayer == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
            if (aiColor == PieceColor.RED && currentPlayer == PieceColor.RED) {
                SwingUtilities.invokeLater(this::performAIMove);
            }
        }
        
        updateStatus();
        repaint();
    }
    
    /**
     * 获取当前棋盘对象
     * @return 棋盘对象
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * 翻转棋盘视角
     */
    public void flipBoard() {
        isBoardFlipped = !isBoardFlipped;
        repaint();
    }
    
    /**
     * 获取棋盘翻转状态
     */
    public boolean isBoardFlipped() {
        return isBoardFlipped;
    }
    
    /**
     * 坐标转换：将逻辑坐标转换为显示坐标
     */
    private int getDisplayRow(int logicalRow) {
        return isBoardFlipped ? (9 - logicalRow) : logicalRow;
    }
    
    private int getDisplayCol(int logicalCol) {
        return isBoardFlipped ? (8 - logicalCol) : logicalCol;
    }
    
    /**
     * 坐标转换：将显示坐标转换为逻辑坐标
     */
    private int getLogicalRow(int displayRow) {
        return isBoardFlipped ? (9 - displayRow) : displayRow;
    }
    
    private int getLogicalCol(int displayCol) {
        return isBoardFlipped ? (8 - displayCol) : displayCol;
    }
    
    /**
     * 悔棋功能 - 同时回退红方和黑方各一步
     */

     
    /**
     * 进入残局设置模式
     */
    public void enterEndgameSetupMode() {
        isSettingUpEndgame = true;
        isEndgameMode = false;
        
        // 清空棋盘
        board.clearBoard();
        
        // 重置游戏状态
        currentPlayer = PieceColor.RED;
        gameState = GameState.PLAYING;
        boardHistory.clear();
        lastMoveStart = null;
        lastMoveEnd = null;
        clearSelection();
        
        // 禁用AI
        disableAI();
        
        updateStatus();
        repaint();
        
        showErrorInfo("残局设置模式已开启！\n\n" +
            "操作说明：\n" +
            "• 右键点击空位：放置棋子\n" +
            "• 右键点击棋子：移除棋子\n" +
            "• 设置完成后点击'开始残局'按钮");
    }
    
    /**
     * 退出残局设置模式
     */
    public void exitEndgameSetupMode() {
        isSettingUpEndgame = false;
        
        // 如果正在进行AI对AI残局游戏，则停止对弈
        if (isAIvsAIMode) {
            isAIvsAIMode = false;
            isEndgameMode = false;
            
            // 清理AI实例
            if (redAI != null) {
                if (redAI instanceof DeepSeekPikafishAI) {
                    ((DeepSeekPikafishAI) redAI).shutdown();
                } else if (redAI instanceof PikafishAI) {
                    ((PikafishAI) redAI).cleanup();
                } else if (redAI instanceof FairyStockfishAI) {
                    ((FairyStockfishAI) redAI).cleanup();
                }
                redAI = null;
            }
            if (blackAI != null) {
                if (blackAI instanceof DeepSeekPikafishAI) {
                    ((DeepSeekPikafishAI) blackAI).shutdown();
                } else if (blackAI instanceof PikafishAI) {
                    ((PikafishAI) blackAI).cleanup();
                } else if (blackAI instanceof FairyStockfishAI) {
                    ((FairyStockfishAI) blackAI).cleanup();
                }
                blackAI = null;
            }
            
            // 禁用AI
            disableAI();
            
            System.out.println("🛑 AI对AI残局游戏已结束");
            addAILog("系统", "AI对AI残局游戏已结束");
        }
        
        updateStatus();
        repaint();
    }
    
    /**
     * 开始残局游戏
     */
    public void startEndgameGame(PieceColor aiColor) {
        if (!isSettingUpEndgame) {
            showErrorInfo("请先进入残局设置模式！");
            return;
        }
        
        // 检查棋盘上是否有棋子
        boolean hasRedGeneral = false;
        boolean hasBlackGeneral = false;
        int totalPieces = 0;
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    totalPieces++;
                    if (piece instanceof com.example.chinesechess.core.General) {
                        if (piece.getColor() == PieceColor.RED) {
                            hasRedGeneral = true;
                        } else {
                            hasBlackGeneral = true;
                        }
                    }
                }
            }
        }
        
        if (totalPieces == 0) {
            showErrorInfo("请先在棋盘上放置一些棋子！");
            return;
        }
        
        if (!hasRedGeneral || !hasBlackGeneral) {
            showErrorInfo("棋盘上必须同时有红方和黑方的将/帅！");
            return;
        }
        
        // 检查棋子数量是否合理
        String validationResult = validateEndgameBoardSetup();
        if (validationResult != null) {
            showErrorInfo(validationResult + "\n\n如需继续，请再次点击'开始残局'按钮。");
            // 简化处理：显示警告信息，用户需要再次点击开始按钮
            return;
        }
        
        // 设置残局模式
        isSettingUpEndgame = false;
        isEndgameMode = true;
        endgameAIColor = aiColor;
        
        // 启用AI
        PieceColor humanColor = (aiColor == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
        enableDeepSeekPikafishAI(humanColor, 3, "qwen2.5:7b"); // 默认使用DeepSeek+Pikafish AI
        
        // 如果AI是红方，让AI先走
        if (aiColor == PieceColor.RED && currentPlayer == PieceColor.RED) {
            performAIMove();
        }
        
        updateStatus();
        repaint();
        
        showErrorInfo("残局游戏开始！\n" +
            "AI执" + (aiColor == PieceColor.RED ? "红" : "黑") + "方\n" +
            "您执" + (humanColor == PieceColor.RED ? "红" : "黑") + "方");
    }
    
    /**
     * 开始AI对AI的残局游戏
     */
    public void startAIvsAIEndgameGame() {
        if (!isSettingUpEndgame) {
            showErrorInfo("请先进入残局设置模式！");
            return;
        }
        
        // 检查棋盘上是否有棋子
        boolean hasRedGeneral = false;
        boolean hasBlackGeneral = false;
        int totalPieces = 0;
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    totalPieces++;
                    if (piece instanceof com.example.chinesechess.core.General) {
                        if (piece.getColor() == PieceColor.RED) {
                            hasRedGeneral = true;
                        } else {
                            hasBlackGeneral = true;
                        }
                    }
                }
            }
        }
        
        if (totalPieces == 0) {
            showErrorInfo("请先在棋盘上放置一些棋子！");
            return;
        }
        
        if (!hasRedGeneral || !hasBlackGeneral) {
            showErrorInfo("棋盘上必须同时有红方和黑方的将/帅！");
            return;
        }
        
        // 检查棋子数量是否合理
        String validationResult = validateEndgameBoardSetup();
        if (validationResult != null) {
            showErrorInfo(validationResult + "\n\n如需继续，请再次点击'AI对AI残局'按钮。");
            return;
        }
        
        // 设置AI对AI残局模式
        isSettingUpEndgame = false;
        isEndgameMode = true;
        isAIvsAIMode = true;
        
        // 禁用原有的AI
        disableAI();
        
        // 创建双AI实例
        try {
            redAI = new DeepSeekPikafishAI(PieceColor.RED, 3, "qwen2.5:7b");
            blackAI = new DeepSeekPikafishAI(PieceColor.BLACK, 3, "qwen2.5:7b");
        } catch (Exception e) {
            showErrorInfo("AI初始化失败：" + e.getMessage());
            return;
        }
        
        updateStatus();
        repaint();
        
        showErrorInfo("AI对AI残局游戏开始！\n红方AI vs 黑方AI");
        
        // 如果当前是红方回合，让红方AI先走
        if (currentPlayer == PieceColor.RED) {
            performAIvsAIMove();
        }
    }
    
    /**
     * 执行AI对AI的走棋
     */
    private void performAIvsAIMove() {
        if (isGamePaused) { // 如果游戏暂停，则不执行AI vs AI移动
            isAIThinking = false;
            updateStatus();
            return;
        }

        if (!isAIvsAIMode || isAIThinking) {
            return;
        }
        
        isAIThinking = true;
        
        SwingWorker<Move, Void> worker = new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() throws Exception {
                Object currentAI = (currentPlayer == PieceColor.RED) ? redAI : blackAI;
                String aiName = (currentPlayer == PieceColor.RED) ? "红方AI" : "黑方AI";
                
                addAILog("思考", aiName + "正在思考...");
                
                // 根据AI类型调用相应的getBestMove方法
                if (currentAI instanceof DeepSeekPikafishAI) {
                    return ((DeepSeekPikafishAI) currentAI).getBestMove(board);
                } else if (currentAI instanceof PikafishAI) {
                    return ((PikafishAI) currentAI).getBestMove(board);
                } else if (currentAI instanceof FairyStockfishAI) {
                    return ((FairyStockfishAI) currentAI).getBestMove(board);
                } else {
                    throw new IllegalStateException("不支持的AI类型: " + currentAI.getClass().getSimpleName());
                }
            }
            
            @Override
            protected void done() {
                try {
                    Move aiMove = get();
                    if (aiMove != null) {
                        executeAIvsAIMove(aiMove);
                    } else {
                        handleAIvsAINoMove();
                    }
                } catch (Exception e) {
                    handleAIvsAIError(e);
                } finally {
                    isAIThinking = false;
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 执行AI对AI的走法
     */
    private void executeAIvsAIMove(Move aiMove) {
        String aiName = (currentPlayer == PieceColor.RED) ? "红方AI" : "黑方AI";

        // 执行走法
        Position start = aiMove.getStart();
        Position end = aiMove.getEnd();
        Piece piece = board.getPiece(start.getX(), start.getY());

        // 保存棋盘状态
        saveBoardState();

        // 执行移动
        board.movePiece(start, end);

        // 更新最后一步移动标记
        lastMoveStart = start;
        lastMoveEnd = end;

        // 记录AI日志
        String moveDescription = formatMoveDescription(piece, start, end);
        addAILog("走法", aiName + ": " + moveDescription);

        // 切换玩家
        currentPlayer = (currentPlayer == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;

        // 检查游戏是否结束
        checkGameEnd();

        // 更新状态和重绘
        updateStatus();
        repaint();

        // 如果游戏未结束，继续下一步AI走棋
        if (gameState == GameState.PLAYING && isAIvsAIMode) {
            // 延迟一秒后执行下一步，让用户能看清楚
            Timer timer = new Timer(1000, e -> performAIvsAIMove());
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    /**
     * 处理AI对AI模式下无法走棋的情况
     */
    private void handleAIvsAINoMove() {
        String aiName = (currentPlayer == PieceColor.RED) ? "红方AI" : "黑方AI";
        addAILog("错误", aiName + "无法找到有效走法，游戏结束");
        showErrorInfo(aiName + "无法找到有效走法，游戏结束");
        
        // 设置游戏状态为和棋
        gameState = GameState.DRAW;
        
        // 禁用AI对AI模式，防止继续尝试走棋
        disableAIvsAI();
        
        updateStatus();
    }
    
    /**
     * 处理AI对AI模式下的错误
     */
    private void handleAIvsAIError(Exception e) {
        String aiName = (currentPlayer == PieceColor.RED) ? "红方AI" : "黑方AI";
        addAILog("错误", aiName + "思考出错: " + e.getMessage());
        showErrorInfo(aiName + "思考出错: " + e.getMessage());
    }
    
    /**
     * 检查是否在残局设置模式
     */
    public boolean isInEndgameSetupMode() {
        return isSettingUpEndgame;
    }
    
    /**
     * 检查是否在残局模式
     */
    public boolean isInEndgameMode() {
        return isEndgameMode;
    }
    
    /**
     * 初始化棋子选择菜单
     */
    private void initializePieceSelectionMenu() {
        pieceSelectionMenu = new JPopupMenu();
        pieceSelectionMenu.setFocusable(false);
        
        JLabel titleLabel = new JLabel("选择棋子");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        pieceSelectionMenu.add(titleLabel);
        
        pieceSelectionMenu.addSeparator();
        
        // 添加红方棋子
        JLabel redLabel = new JLabel("红方棋子");
        redLabel.setFont(new Font("微软雅黑", Font.BOLD, 10));
        redLabel.setForeground(Color.RED);
        redLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pieceSelectionMenu.add(redLabel);
        
        for (int i = 0; i < 7; i++) {
            JMenuItem redItem = new JMenuItem(pieceOptions[i]);
            redItem.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            final int index = i;
            redItem.addActionListener(e -> {
                selectedPieceIndex = index;
                confirmPiecePlacement();
            });
            pieceSelectionMenu.add(redItem);
        }
        
        pieceSelectionMenu.addSeparator();
        
        // 添加黑方棋子
        JLabel blackLabel = new JLabel("黑方棋子");
        blackLabel.setFont(new Font("微软雅黑", Font.BOLD, 10));
        blackLabel.setForeground(Color.BLACK);
        blackLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pieceSelectionMenu.add(blackLabel);
        
        for (int i = 7; i < 14; i++) {
            JMenuItem blackItem = new JMenuItem(pieceOptions[i]);
            blackItem.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            final int index = i;
            blackItem.addActionListener(e -> {
                selectedPieceIndex = index;
                confirmPiecePlacement();
            });
            pieceSelectionMenu.add(blackItem);
        }
        
        pieceSelectionMenu.addSeparator();
        
        JMenuItem cancelItem = new JMenuItem("取消");
        cancelItem.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelItem.addActionListener(e -> pieceSelectionMenu.setVisible(false));
        pieceSelectionMenu.add(cancelItem);
    }
    
    // 移除不再使用的滚轮处理方法，因为现在菜单直接显示所有棋子选项
    // private void handlePieceSelectionScroll(int wheelRotation) { ... }
    // private void updatePieceSelectionMenu() { ... }
    
    /**
     * 确认棋子放置
     */
    private void confirmPiecePlacement() {
        if (currentEndgameRow >= 0 && currentEndgameCol >= 0) {
            String selectedPiece = pieceOptions[selectedPieceIndex];
            Piece piece = createPieceFromSelection(selectedPiece);
            if (piece != null) {
                // 首先检查位置是否合法
                String positionValidation = validatePiecePosition(piece, currentEndgameRow, currentEndgameCol);
                if (positionValidation != null) {
                    // 位置错误，不允许放置
                    showErrorInfo("位置错误：" + positionValidation);
                    pieceSelectionMenu.setVisible(false);
                    currentEndgameRow = -1;
                    currentEndgameCol = -1;
                    return;
                }
                
                // 检查棋子数量限制
                String validationMessage = validatePiecePlacement(piece, currentEndgameRow, currentEndgameCol);
                if (validationMessage != null) {
                    // 显示数量警告
                    showErrorInfo("棋子数量警告：" + validationMessage + "\n如需继续放置，请再次点击该位置。");
                    pieceSelectionMenu.setVisible(false);
                    currentEndgameRow = -1;
                    currentEndgameCol = -1;
                    return;
                }
                
                // 放置棋子
                board.setPiece(currentEndgameRow, currentEndgameCol, piece);
                repaint();
                System.out.println("放置棋子: " + piece.getChineseName() + " 在位置 (" + currentEndgameRow + "," + currentEndgameCol + ")");
            }
        }
        pieceSelectionMenu.setVisible(false);
        currentEndgameRow = -1;
        currentEndgameCol = -1;
    }
    
    /**
     * 处理残局设置模式下的右键点击
     */
    private void handleEndgameSetupRightClick(int mouseX, int mouseY) {
        // 将鼠标坐标转换为显示坐标
        int displayCol = (mouseX - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
        int displayRow = (mouseY - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
        
        // 检查显示坐标是否在棋盘范围内
        if (displayRow < 0 || displayRow >= 10 || displayCol < 0 || displayCol >= 9) {
            return;
        }
        
        // 转换为逻辑坐标
        int row = getLogicalRow(displayRow);
        int col = getLogicalCol(displayCol);
        
        Piece currentPiece = board.getPiece(row, col);
        
        if (currentPiece != null) {
            // 如果位置有棋子，移除它
            board.removePiece(row, col);
            System.out.println("移除棋子: " + currentPiece.getChineseName() + " 在位置 (" + row + "," + col + ")");
            repaint();
        } else {
            // 如果位置没有棋子，显示棋子选择菜单
            currentEndgameRow = row;
            currentEndgameCol = col;
            selectedPieceIndex = 0; // 重置选择索引
            pieceSelectionMenu.show(this, mouseX, mouseY);
        }
    }
    
    /**
     * 显示棋子选择对话框
     */
    private void showPieceSelectionDialog(int row, int col) {
        // 创建自定义对话框
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "选择棋子", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 添加说明标签
        JLabel label = new JLabel("选择要放置的棋子:");
        label.setFont(new Font("微软雅黑", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(label, gbc);
        
        // 创建下拉框
        String[] pieceOptions = {
            "红帅", "红仕", "红相", "红马", "红车", "红炮", "红兵",
            "黑将", "黑士", "黑象", "黑马", "黑车", "黑炮", "黑卒"
        };
        
        JComboBox<String> pieceComboBox = new JComboBox<>(pieceOptions);
        pieceComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        pieceComboBox.setPreferredSize(new Dimension(120, 30));
        gbc.gridx = 1; gbc.gridy = 0;
        mainPanel.add(pieceComboBox, gbc);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton confirmButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        
        confirmButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        final boolean[] confirmed = {false};
        
        confirmButton.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // 显示对话框
        dialog.setVisible(true);
        
        // 处理结果
        if (confirmed[0]) {
            String selectedPiece = (String) pieceComboBox.getSelectedItem();
            if (selectedPiece != null) {
                Piece piece = createPieceFromSelection(selectedPiece);
                if (piece != null) {
                    board.setPiece(row, col, piece);
                    repaint();
                    System.out.println("放置棋子: " + piece.getChineseName() + " 在位置 (" + row + "," + col + ")");
                }
            }
        }
    }
    
    /**
     * 根据选择创建棋子对象
     */
    private Piece createPieceFromSelection(String selection) {
        switch (selection) {
            case "红帅": return new General(PieceColor.RED);
            case "红仕": return new Advisor(PieceColor.RED);
            case "红相": return new Elephant(PieceColor.RED);
            case "红马": return new Horse(PieceColor.RED);
            case "红车": return new Chariot(PieceColor.RED);
            case "红炮": return new Cannon(PieceColor.RED);
            case "红兵": return new Soldier(PieceColor.RED);
            case "黑将": return new General(PieceColor.BLACK);
            case "黑士": return new Advisor(PieceColor.BLACK);
            case "黑象": return new Elephant(PieceColor.BLACK);
            case "黑马": return new Horse(PieceColor.BLACK);
            case "黑车": return new Chariot(PieceColor.BLACK);
            case "黑炮": return new Cannon(PieceColor.BLACK);
            case "黑卒": return new Soldier(PieceColor.BLACK);
            default: return null;
        }
    }
    
    /**
     * 验证棋子放置是否合理，返回警告信息（如果有）
     */
    private String validatePiecePlacement(Piece newPiece, int row, int col) {
        // 首先检查位置的合理性
        String positionError = validatePiecePosition(newPiece, row, col);
        if (positionError != null) {
            return positionError;
        }
        
        // 统计当前棋盘上各类棋子的数量
        int[] redCounts = new int[7]; // 帅、仕、相、马、车、炮、兵
        int[] blackCounts = new int[7]; // 将、士、象、马、车、炮、卒
        
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                if (r == row && c == col) continue; // 跳过当前要放置的位置
                Piece piece = board.getPiece(r, c);
                if (piece != null) {
                    int index = getPieceTypeIndex(piece);
                    if (index >= 0) {
                        if (piece.getColor() == PieceColor.RED) {
                            redCounts[index]++;
                        } else {
                            blackCounts[index]++;
                        }
                    }
                }
            }
        }
        
        // 检查新棋子是否超出限制
        int newPieceIndex = getPieceTypeIndex(newPiece);
        if (newPieceIndex >= 0) {
            int[] counts = (newPiece.getColor() == PieceColor.RED) ? redCounts : blackCounts;
            String colorName = (newPiece.getColor() == PieceColor.RED) ? "红方" : "黑方";
            String pieceName = newPiece.getChineseName();
            
            // 定义各类棋子的最大数量
            int[] maxCounts = {1, 2, 2, 2, 2, 2, 5}; // 将/帅、士、象/相、马、车、炮、兵/卒
            
            if (counts[newPieceIndex] >= maxCounts[newPieceIndex]) {
                return String.format("警告：%s的%s数量已达到上限（%d个）！\n" +
                    "继续放置可能导致不合理的棋局。\n" +
                    "建议：移除多余的棋子或选择其他棋子类型。", 
                    colorName, pieceName, maxCounts[newPieceIndex]);
            }
        }
        
        return null; // 无警告
    }
    
    /**
     * 验证棋子位置是否合理
     */
    private String validatePiecePosition(Piece piece, int row, int col) {
        String pieceName = piece.getChineseName();
        PieceColor color = piece.getColor();
        
        // 将/帅只能在九宫格内
        if (piece instanceof General) {
            if (color == PieceColor.RED) {
                // 红帅在下方九宫格 (7-9行, 3-5列)
                if (row < 7 || row > 9 || col < 3 || col > 5) {
                    return String.format("错误：%s只能放置在九宫格内（下方3×3区域）！\n" +
                        "当前位置(%d,%d)不合法。", pieceName, row + 1, col + 1);
                }
            } else {
                // 黑将在上方九宫格 (0-2行, 3-5列)
                if (row < 0 || row > 2 || col < 3 || col > 5) {
                    return String.format("错误：%s只能放置在九宫格内（上方3×3区域）！\n" +
                        "当前位置(%d,%d)不合法。", pieceName, row + 1, col + 1);
                }
            }
        }
        
        // 士只能在九宫格的四个角和正中间
        else if (piece instanceof Advisor) {
            if (color == PieceColor.RED) {
                // 红仕在下方九宫格：四个角(7,3), (7,5), (9,3), (9,5) 和正中间(8,4)
                if (!((row == 7 && (col == 3 || col == 5)) || 
                      (row == 8 && col == 4) || 
                      (row == 9 && (col == 3 || col == 5)))) {
                    return String.format("错误：%s只能放置在九宫格的四个角和正中间！\n" +
                        "合法位置：(8,4), (9,5), (9,4), (10,4), (10,6)\n" +
                        "当前位置(%d,%d)不合法。", pieceName, row + 1, col + 1);
                }
            } else {
                // 黑士在上方九宫格：四个角(0,3), (0,5), (2,3), (2,5) 和正中间(1,4)
                if (!((row == 0 && (col == 3 || col == 5)) || 
                      (row == 1 && col == 4) || 
                      (row == 2 && (col == 3 || col == 5)))) {
                    return String.format("错误：%s只能放置在九宫格的四个角和正中间！\n" +
                        "合法位置：(1,4), (1,6), (2,5), (3,4), (3,6)\n" +
                        "当前位置(%d,%d)不合法。", pieceName, row + 1, col + 1);
                }
            }
        }
        
        // 象/相只能在己方半场的特定位置
        else if (piece instanceof Elephant) {
            if (color == PieceColor.RED) {
                // 红相在己方半场的象位
                if (!((row == 5 && (col == 2 || col == 6)) || 
                      (row == 7 && (col == 0 || col == 4 || col == 8)) || 
                      (row == 9 && (col == 2 || col == 6)))) {
                    return String.format("错误：%s只能放置在己方半场的象位！\n" +
                        "合法位置：(6,2), (6,6), (8,0), (8,4), (8,8), (10,2), (10,6)\n" +
                        "当前位置(%d,%d)不合法。", pieceName, row + 1, col + 1);
                }
            } else {
                // 黑象在己方半场的象位
                if (!((row == 0 && (col == 2 || col == 6)) || 
                      (row == 2 && (col == 0 || col == 4 || col == 8)) || 
                      (row == 4 && (col == 2 || col == 6)))) {
                    return String.format("错误：%s只能放置在己方半场的象位！\n" +
                        "合法位置：(1,2), (1,6), (3,0), (3,4), (3,8), (5,2), (5,6)\n" +
                        "当前位置(%d,%d)不合法。", pieceName, row + 1, col + 1);
                }
            }
        }
        
        // 兵/卒的位置限制
        else if (piece instanceof Soldier) {
            if (color == PieceColor.RED) {
                // 红兵：己方半场只能在兵线上，过河后可以在任意位置
                if (row >= 5) {
                    // 己方半场，只能在兵线上 (第7行，即row=6)
                    if (row != 6 || col % 2 != 0) {
                        return String.format("错误：%s在己方半场只能放置在兵线上！\n" +
                            "兵线位置：(7,1), (7,3), (7,5), (7,7), (7,9)\n" +
                            "当前位置(%d,%d)不合法。", pieceName, row + 1, col + 1);
                    }
                }
            } else {
                // 黑卒：己方半场只能在卒线上，过河后可以在任意位置
                if (row <= 4) {
                    // 己方半场，只能在卒线上 (第4行，即row=3)
                    if (row != 3 || col % 2 != 0) {
                        return String.format("错误：%s在己方半场只能放置在卒线上！\n" +
                            "卒线位置：(4,1), (4,3), (4,5), (4,7), (4,9)\n" +
                            "当前位置(%d,%d)不合法。", pieceName, row + 1, col + 1);
                    }
                }
            }
        }
        
        return null; // 位置合法
    }
    
    /**
      * 获取棋子类型索引
      */
    private int getPieceTypeIndex(Piece piece) {
        if (piece instanceof General) return 0;
        if (piece instanceof Advisor) return 1;
        if (piece instanceof Elephant) return 2;
        if (piece instanceof Horse) return 3;
        if (piece instanceof Chariot) return 4;
        if (piece instanceof Cannon) return 5;
        if (piece instanceof Soldier) return 6;
        return -1;
    }
    
    /**
     * 验证残局棋盘设置是否合理
     */
    private String validateEndgameBoardSetup() {
        // 统计各类棋子数量
        int[] redCounts = new int[7]; // 帅、仕、相、马、车、炮、兵
        int[] blackCounts = new int[7]; // 将、士、象、马、车、炮、卒
        
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece != null) {
                    int index = getPieceTypeIndex(piece);
                    if (index >= 0) {
                        if (piece.getColor() == PieceColor.RED) {
                            redCounts[index]++;
                        } else {
                            blackCounts[index]++;
                        }
                    }
                }
            }
        }
        
        // 检查数量限制
        String[] pieceNames = {"将/帅", "士", "象/相", "马", "车", "炮", "兵/卒"};
        int[] maxCounts = {1, 2, 2, 2, 2, 2, 5};
        StringBuilder warnings = new StringBuilder();
        
        for (int i = 0; i < 7; i++) {
            if (redCounts[i] > maxCounts[i]) {
                warnings.append(String.format("• 红方%s数量超限：%d个（最多%d个）\n", 
                    pieceNames[i], redCounts[i], maxCounts[i]));
            }
            if (blackCounts[i] > maxCounts[i]) {
                warnings.append(String.format("• 黑方%s数量超限：%d个（最多%d个）\n", 
                    pieceNames[i], blackCounts[i], maxCounts[i]));
            }
        }
        
        if (warnings.length() > 0) {
            return "检测到以下棋子数量异常：\n" + warnings.toString() + 
                   "\n这些设置在正常对局中不会出现，可能影响游戏体验。";
        }
        
        return null;
    }


    
    /**
     * 设置聊天面板引用
     */
    public void setStatusUpdateCallback(Consumer<String> callback) {
        this.statusUpdateCallback = callback;
    }

    public void setChatPanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }
    
    /**
     * 设置AI决策日志面板引用
     */
    public void setAILogPanel(AILogPanel aiLogPanel) {
        this.aiLogPanel = aiLogPanel;
    }
    
    /**
     * 通知聊天面板更新棋盘状态
     */
    private void notifyChatPanelBoardUpdate() {
        if (chatPanel != null) {
            chatPanel.updateBoardState(board);
        }
    }
    
    /**
     * 向AI决策日志面板添加日志
     */
    private void addAILog(String type, String message) {
        if (aiLogPanel != null && aiLogPanel.isLogEnabled()) {
            switch (type.toLowerCase()) {
                case "thinking":
                    aiLogPanel.addAIThinking(message);
                    break;
                case "decision":
                    aiLogPanel.addAIDecision(message);
                    break;
                case "error":
                    aiLogPanel.addError(message);
                    break;
                case "system":
                    aiLogPanel.addSystemLog(message);
                    break;
                default:
                    aiLogPanel.addSystemLog(message);
                    break;
            }
        }
    }
    
    /**
     * 请求Pikafish评估当前棋局
     */
    private void requestPikafishEvaluation() {
        if (chatPanel != null) {
            try {
                // 使用反射调用ChatPanel的私有方法requestPikafishEvaluation
                java.lang.reflect.Method method = ChatPanel.class.getDeclaredMethod("requestPikafishEvaluation");
                method.setAccessible(true);
                method.invoke(chatPanel);
            } catch (Exception e) {
                ExceptionHandler.logError("调用Pikafish评估失败: " + e.getMessage(), "BoardPanel");
                if (chatPanel != null) {
                    chatPanel.addChatMessage("❌ Pikafish评估调用失败，请手动点击评估按钮。");
                }
            }
        }
    }
    
    /**
     * 初始化错误信息显示面板
     */
    private void initializeErrorInfoPanel() {
        errorInfoPanel = new JPanel(new BorderLayout());
        errorInfoPanel.setBackground(new Color(255, 245, 245)); // 淡红色背景
        errorInfoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.RED, 1), 
            "提示信息", 
            javax.swing.border.TitledBorder.LEFT, 
            javax.swing.border.TitledBorder.TOP,
            new Font("宋体", Font.BOLD, 12),
            Color.RED
        ));
        errorInfoPanel.setVisible(false);
        
        errorTextArea = new JTextArea(3, 20);
        errorTextArea.setEditable(false);
        errorTextArea.setBackground(new Color(255, 245, 245));
        errorTextArea.setForeground(Color.RED);
        errorTextArea.setFont(new Font("宋体", Font.PLAIN, 12));
        errorTextArea.setLineWrap(true);
        errorTextArea.setWrapStyleWord(true);
        
        errorScrollPane = new JScrollPane(errorTextArea);
        errorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        errorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        errorInfoPanel.add(errorScrollPane, BorderLayout.CENTER);
        
        // 创建清除定时器
        errorClearTimer = new Timer(5000, e -> hideErrorInfo()); // 5秒后自动隐藏
        errorClearTimer.setRepeats(false);
    }
    
    /**
     * 显示错误信息
     */
    public void showErrorInfo(String message) {
        if (errorTextArea != null) {
            errorTextArea.setText(message);
            errorInfoPanel.setVisible(true);
            
            // 重启定时器
            if (errorClearTimer.isRunning()) {
                errorClearTimer.restart();
            } else {
                errorClearTimer.start();
            }
            
            // 通知父容器重新布局
            Container parent = getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
        }
    }
    
    /**
     * 隐藏错误信息
     */
    public void hideErrorInfo() {
        if (errorInfoPanel != null) {
            errorInfoPanel.setVisible(false);
            
            // 通知父容器重新布局
            Container parent = getParent();
            if (parent != null) {
                parent.revalidate();
                parent.repaint();
            }
        }
    }
    
    /**
     * 保存当前棋盘状态到历史记录中
     * 每当有有效移动完成时调用，为悔棋功能提供状态快照
     */
    private void saveBoardState() {
        try {
            // 限制历史记录大小，避免内存溢出
            while (boardHistory.size() >= MAX_HISTORY_SIZE) {
                boardHistory.remove(0);
            }
            
            // 创建当前棋盘状态的完整快照
            com.example.chinesechess.core.BoardState currentState = new com.example.chinesechess.core.BoardState(
                board.getPieces(),
                currentPlayer,
                gameState,
                lastMoveStart,
                lastMoveEnd,
                null, // positionHistory - 暂时为空，可后续扩展
                ++stateCounter
            );
            
            // 添加到历史记录
            boardHistory.add(currentState);
            
            // 调试信息
            System.out.printf("💾 保存棋盘状态[%d]: 当前玩家=%s, 历史总数=%d%n",
                stateCounter, 
                currentPlayer == PieceColor.RED ? "红方" : "黑方",
                boardHistory.size());
                
        } catch (Exception e) {
            System.err.println("⚠️ 保存棋盘状态失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 悔棋功能 - 恢复到上一个有效的棋盘状态
     * 支持在游戏进行中撤销最后一次移动，恢复完整的游戏状态
     */
    public void undoLastMove() {
        // 检查游戏状态限制
        if (isGamePaused) {
            showErrorInfo("游戏已暂停，无法进行悔棋操作！");
            return;
        }
        
        if (isAIThinking) {
            showErrorInfo("AI正在思考中，请稍后再尝试悔棋！");
            return;
        }
        
        // 检查是否有可悔棋的历史记录
        if (boardHistory.isEmpty()) {
            showErrorInfo("当前没有可以撤销的移动记录！");
            return;
        }
        
        try {
            // 获取最后一个保存的状态
            com.example.chinesechess.core.BoardState previousState = boardHistory.get(boardHistory.size() - 1);
            
            // 移除当前状态（即要撤销的状态）
            boardHistory.remove(boardHistory.size() - 1);
            
            // 恢复棋盘状态
            if (previousState != null && previousState.isValid()) {
                // 恢复棋盘布局
                Piece[][] savedBoard = previousState.getPiecesCopy();
                board.setPieces(savedBoard);
                
                // 恢复游戏状态
                currentPlayer = previousState.getCurrentPlayer();
                gameState = previousState.getGameState();
                
                // 恢复移动标记
                Position savedMoveStart = previousState.getLastMoveStart();
                Position savedMoveEnd = previousState.getLastMoveEnd();
                lastMoveStart = (savedMoveStart != null) ? 
                    new Position(savedMoveStart.getX(), savedMoveStart.getY()) : null;
                lastMoveEnd = (savedMoveEnd != null) ? 
                    new Position(savedMoveEnd.getX(), savedMoveEnd.getY()) : null;
                
                // 调试信息
                System.out.printf("🔙 悔棋成功: 恢复到状态[%d], 当前玩家=%s, 剩余历史=%d%n",
                    previousState.getStateIndex(),
                    currentPlayer == PieceColor.RED ? "红方" : "黑方",
                    boardHistory.size());
                    
            } else {
                // 如果没有有效的历史状态，重置为初始状态
                System.out.println("⚠️ 无有效历史状态，重置为游戏开始状态");
                resetToInitialState();
            }
            
            // 清除选择状态和AI建议
            clearSelection();
            clearAISuggestion();
            
            // 更新界面
            updateStatus();
            repaint();
            
            // 通知聊天面板更新棋盘状态
            notifyChatPanelBoardUpdate();
            
            // 播放悔棋音效
            SoundPlayer.getInstance().playSound("undo_move");
            
            addAILog("system", "悔棋操作完成 - 当前轮到" + (currentPlayer == PieceColor.RED ? "红方" : "黑方"));
            
        } catch (Exception e) {
            System.err.println("❌ 悔棋操作失败: " + e.getMessage());
            e.printStackTrace();
            showErrorInfo("悔棋操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置为游戏初始状态
     */
    private void resetToInitialState() {
        board.initializeBoard();
        currentPlayer = PieceColor.RED;
        gameState = GameState.PLAYING;
        lastMoveStart = null;
        lastMoveEnd = null;
        boardHistory.clear();
        stateCounter = 0;
    }
    
    /**
     * 获取悔棋历史数量
     */
    public int getUndoHistorySize() {
        return boardHistory.size();
    }
    
    /**
     * 检查是否可以悔棋
     */
    public boolean canUndo() {
        return !boardHistory.isEmpty() && !isAIThinking && !isGamePaused;
    }

    /**
     * 获取错误信息面板
     */
    public JPanel getErrorInfoPanel() {
        return errorInfoPanel;
    }
    
    /**
     * 检查是否处于AI vs AI模式
     */
    public boolean isAIvsAIMode() {
        return isAIvsAIMode;
    }
    
    /**
     * 启用AI vs AI对弈模式
     */
    public void enableAIvsAI(int difficulty, String modelName) {
        // 禁用原有的AI
        disableAI();
        
        // 设置AI vs AI模式
        isAIvsAIMode = true;
        isAIEnabled = false; // 禁用原有的单AI模式
        
        try {
            // 创建双AI实例，都使用Pikafish引擎
            redAI = new DeepSeekPikafishAI(PieceColor.RED, difficulty, modelName != null ? modelName : "deepseek-r1");
            blackAI = new DeepSeekPikafishAI(PieceColor.BLACK, difficulty, modelName != null ? modelName : "deepseek-r1");
            
            // 设置AI日志面板
            if (aiLogPanel != null) {
                if (redAI instanceof DeepSeekPikafishAI) {
                    ((DeepSeekPikafishAI) redAI).setAILogPanel(aiLogPanel);
                } else if (redAI instanceof PikafishAI) {
                    ((PikafishAI) redAI).setAILogPanel(aiLogPanel);
                } else if (redAI instanceof FairyStockfishAI) {
                    ((FairyStockfishAI) redAI).setAILogPanel(aiLogPanel);
                }
                
                if (blackAI instanceof DeepSeekPikafishAI) {
                    ((DeepSeekPikafishAI) blackAI).setAILogPanel(aiLogPanel);
                } else if (blackAI instanceof PikafishAI) {
                    ((PikafishAI) blackAI).setAILogPanel(aiLogPanel);
                } else if (blackAI instanceof FairyStockfishAI) {
                    ((FairyStockfishAI) blackAI).setAILogPanel(aiLogPanel);
                }
            }
            
            addAILog("system", "AI vs AI对弈模式已启用 - 红方AI vs 黑方AI (Pikafish引擎)");
            System.out.println("🤖 AI vs AI对弈模式已启用");
            
            // 如果当前是红方回合，让红方AI先走
            if (currentPlayer == PieceColor.RED) {
                SwingUtilities.invokeLater(this::performAIvsAIMove);
            }
            
        } catch (Exception e) {
            showErrorInfo("AI初始化失败：" + e.getMessage());
            isAIvsAIMode = false;
            ExceptionHandler.logError("AI vs AI模式初始化失败: " + e.getMessage(), "BoardPanel");
        }
        
        updateStatus();
    }
    
    /**
     * 启用AI vs AI对弈模式（分别配置红方和黑方AI）
     */
    public void enableAIvsAI(int redDifficulty, String redModelName, int blackDifficulty, String blackModelName) {
        // 禁用原有的AI
        disableAI();
        
        // 设置AI vs AI模式
        isAIvsAIMode = true;
        isAIEnabled = false; // 禁用原有的单AI模式
        
        try {
            // 创建双AI实例，分别使用不同的配置
            redAI = new DeepSeekPikafishAI(PieceColor.RED, redDifficulty, redModelName != null ? redModelName : "deepseek-r1");
            blackAI = new DeepSeekPikafishAI(PieceColor.BLACK, blackDifficulty, blackModelName != null ? blackModelName : "deepseek-r1");
            
            // 设置AI日志面板
            if (aiLogPanel != null) {
                if (redAI instanceof DeepSeekPikafishAI) {
                    ((DeepSeekPikafishAI) redAI).setAILogPanel(aiLogPanel);
                } else if (redAI instanceof PikafishAI) {
                    ((PikafishAI) redAI).setAILogPanel(aiLogPanel);
                } else if (redAI instanceof FairyStockfishAI) {
                    ((FairyStockfishAI) redAI).setAILogPanel(aiLogPanel);
                }
                
                if (blackAI instanceof DeepSeekPikafishAI) {
                    ((DeepSeekPikafishAI) blackAI).setAILogPanel(aiLogPanel);
                } else if (blackAI instanceof PikafishAI) {
                    ((PikafishAI) blackAI).setAILogPanel(aiLogPanel);
                } else if (blackAI instanceof FairyStockfishAI) {
                    ((FairyStockfishAI) blackAI).setAILogPanel(aiLogPanel);
                }
            }
            
            String redDifficultyName = getDifficultyName(redDifficulty);
            String blackDifficultyName = getDifficultyName(blackDifficulty);
            addAILog("system", "AI vs AI对弈模式已启用 - 🔴红方AI(" + redDifficultyName + ", " + redModelName + ") vs ⚫黑方AI(" + blackDifficultyName + ", " + blackModelName + ")");
            System.out.println("🤖 AI vs AI对弈模式已启用 - 红方AI(" + redDifficultyName + ", " + redModelName + ") vs 黑方AI(" + blackDifficultyName + ", " + blackModelName + ")");
            
            // 如果当前是红方回合，让红方AI先走
            if (currentPlayer == PieceColor.RED) {
                SwingUtilities.invokeLater(this::performAIvsAIMove);
            }
            
        } catch (Exception e) {
            showErrorInfo("AI初始化失败：" + e.getMessage());
            isAIvsAIMode = false;
            ExceptionHandler.logError("AI vs AI模式初始化失败: " + e.getMessage(), "BoardPanel");
        }
        
        updateStatus();
    }
    
    /**
     * 获取难度名称
     */
    private String getDifficultyName(int difficulty) {
        String[] difficultyNames = {"简单", "普通", "困难", "专家", "大师", "特级", "超级", "顶级", "传奇", "神级"};
        if (difficulty >= 1 && difficulty <= difficultyNames.length) {
            return difficultyNames[difficulty - 1];
        }
        return "未知";
    }
    
    /**
     * 启用AI vs AI对弈模式（支持引擎选择）
     */
    public void enableAIvsAIWithEngines(int redDifficulty, String redModelName, String redEngine,
                                        int blackDifficulty, String blackModelName, String blackEngine) {
        enableAIvsAIWithEnginesAndNN(redDifficulty, redModelName, redEngine, null,
                                     blackDifficulty, blackModelName, blackEngine, null);
    }
    
    /**
     * 启用AI vs AI对弈模式（支持引擎和神经网络选择）
     */
    public void enableAIvsAIWithEnginesAndNN(int redDifficulty, String redModelName, String redEngine, String redNeuralNetwork,
                                             int blackDifficulty, String blackModelName, String blackEngine, String blackNeuralNetwork) {
        // 禁用原有的AI
        disableAI();
        
        // 设置AI vs AI模式
        isAIvsAIMode = true;
        isAIEnabled = false; // 禁用原有的单AI模式
        
        try {
            // 根据选择的引擎创建红方AI
            if ("Pikafish".equals(redEngine)) {
                redAI = new PikafishAI(PieceColor.RED, redDifficulty);
            } else { // FairyStockfish
                redAI = new FairyStockfishAI(PieceColor.RED, redDifficulty, redNeuralNetwork);
            }
            
            // 根据选择的引擎创建黑方AI
            if ("Pikafish".equals(blackEngine)) {
                blackAI = new PikafishAI(PieceColor.BLACK, blackDifficulty);
            } else { // FairyStockfish
                blackAI = new FairyStockfishAI(PieceColor.BLACK, blackDifficulty, blackNeuralNetwork);
            }
            
            // 设置AI日志面板
            if (aiLogPanel != null) {
                if (redAI instanceof DeepSeekPikafishAI) {
                    ((DeepSeekPikafishAI) redAI).setAILogPanel(aiLogPanel);
                } else if (redAI instanceof PikafishAI) {
                    ((PikafishAI) redAI).setAILogPanel(aiLogPanel);
                } else if (redAI instanceof FairyStockfishAI) {
                    ((FairyStockfishAI) redAI).setAILogPanel(aiLogPanel);
                }
                
                if (blackAI instanceof DeepSeekPikafishAI) {
                    ((DeepSeekPikafishAI) blackAI).setAILogPanel(aiLogPanel);
                } else if (blackAI instanceof PikafishAI) {
                    ((PikafishAI) blackAI).setAILogPanel(aiLogPanel);
                } else if (blackAI instanceof FairyStockfishAI) {
                    ((FairyStockfishAI) blackAI).setAILogPanel(aiLogPanel);
                }
            }
            
            String redDifficultyName = getDifficultyName(redDifficulty);
            String blackDifficultyName = getDifficultyName(blackDifficulty);
            
            String redEngineDisplay = "Pikafish".equals(redEngine) ? redEngine : 
                (redNeuralNetwork != null && !redNeuralNetwork.isEmpty() ? 
                    redEngine + "(" + java.nio.file.Paths.get(redNeuralNetwork).getFileName().toString() + ")" : 
                    redEngine);
            String blackEngineDisplay = "Pikafish".equals(blackEngine) ? blackEngine : 
                (blackNeuralNetwork != null && !blackNeuralNetwork.isEmpty() ? 
                    blackEngine + "(" + java.nio.file.Paths.get(blackNeuralNetwork).getFileName().toString() + ")" : 
                    blackEngine);
                    
            addAILog("system", "AI vs AI对弈模式已启用 - 🔴红方AI(" + redEngineDisplay + ", " + redDifficultyName + ") vs ⚫黑方AI(" + blackEngineDisplay + ", " + blackDifficultyName + ")");
            System.out.println("🤖 AI vs AI对弈模式已启用 - 红方AI(" + redEngineDisplay + ", " + redDifficultyName + ") vs 黑方AI(" + blackEngineDisplay + ", " + blackDifficultyName + ")");
            
            // 如果当前是红方回合，让红方AI先走
            if (currentPlayer == PieceColor.RED) {
                SwingUtilities.invokeLater(this::performAIvsAIMove);
            }
            
        } catch (Exception e) {
            showErrorInfo("AI初始化失败：" + e.getMessage());
            isAIvsAIMode = false;
            ExceptionHandler.logError("AI vs AI模式初始化失败: " + e.getMessage(), "BoardPanel");
        }
        
        updateStatus();
    }
    
    /**
     * 禁用AI vs AI对弈模式
     */
    public void disableAIvsAI() {
        isAIvsAIMode = false;
        
        // 清理AI实例
        if (redAI != null) {
            if (redAI instanceof DeepSeekPikafishAI) {
                ((DeepSeekPikafishAI) redAI).shutdown();
            } else if (redAI instanceof PikafishAI) {
                ((PikafishAI) redAI).cleanup();
            } else if (redAI instanceof FairyStockfishAI) {
                ((FairyStockfishAI) redAI).cleanup();
            }
            redAI = null;
        }
        if (blackAI != null) {
            if (blackAI instanceof DeepSeekPikafishAI) {
                ((DeepSeekPikafishAI) blackAI).shutdown();
            } else if (blackAI instanceof PikafishAI) {
                ((PikafishAI) blackAI).cleanup();
            } else if (blackAI instanceof FairyStockfishAI) {
                ((FairyStockfishAI) blackAI).cleanup();
            }
            blackAI = null;
        }
        
        isAIThinking = false;
        addAILog("system", "AI vs AI对弈模式已禁用");
        System.out.println("🔄 AI vs AI对弈模式已禁用");
        updateStatus();
    }
    
    /**
     * 检查是否启用了AI
     */
    public boolean isAIEnabled() {
        return isAIEnabled;
    }
    
    /**
     * 获取当前玩家
     */
    public PieceColor getCurrentPlayer() {
        return currentPlayer;
    }
    
    /**
     * 设置当前玩家
     */
    public void setCurrentPlayer(PieceColor player) {
        this.currentPlayer = player;
        updateStatus();
    }
    
    /**
     * 从 Pikafish 获取当前局面的最佳走法
     */
    public String getBestMoveFromPikafish() {
        try {
            // 如果已有 DeepSeekPikafish AI，使用它
            if (deepSeekPikafishAI != null) {
                Move bestMove = deepSeekPikafishAI.getBestMove(board);
                if (bestMove != null) {
                    return formatMoveForDisplay(bestMove);
                }
            } else {
                // 创建临时的 Pikafish AI 实例进行分析
                DeepSeekPikafishAI tempAI = new DeepSeekPikafishAI(currentPlayer, 5, "deepseek-r1:7b");
                if (aiLogPanel != null) {
                    tempAI.setAILogPanel(aiLogPanel);
                }
                Move bestMove = tempAI.getBestMove(board);
                tempAI.shutdown(); // 清理临时实例
                if (bestMove != null) {
                    return formatMoveForDisplay(bestMove);
                }
            }
        } catch (Exception e) {
            System.err.println("Pikafish 分析出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Pikafish 分析失败", e);
        }
        return null;
    }
    
    /**
     * 从 Fairy-Stockfish 获取当前局面的最佳走法
     */
    public String getBestMoveFromFairyStockfish() {
        try {
            // 如果已有 Fairy-Stockfish AI，使用它
            if (fairyStockfishAI != null) {
                Move bestMove = fairyStockfishAI.getBestMove(board);
                if (bestMove != null) {
                    return formatMoveForDisplay(bestMove);
                }
            } else {
                // 创建临时的 Fairy-Stockfish AI 实例进行分析
                FairyStockfishAI tempAI = new FairyStockfishAI(currentPlayer, 5);
                if (aiLogPanel != null) {
                    tempAI.setAILogPanel(aiLogPanel);
                }
                Move bestMove = tempAI.getBestMove(board);
                tempAI.cleanup(); // 清理临时实例
                if (bestMove != null) {
                    return formatMoveForDisplay(bestMove);
                }
            }
        } catch (Exception e) {
            System.err.println("Fairy-Stockfish 分析出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Fairy-Stockfish 分析失败", e);
        }
        return null;
    }
    
    /**
     * 格式化走法以供显示
     */
    private String formatMoveForDisplay(Move move) {
        if (move == null) return null;
        
        Position from = move.getStart();
        Position to = move.getEnd();
        
        // 获取棋子信息
        Piece piece = board.getPiece(from.getX(), from.getY());
        String pieceName = "";
        if (piece != null) {
            pieceName = getPieceDisplayName(piece);
        }
        
        // 格式化位置信息
        String fromPos = formatPosition(from);
        String toPos = formatPosition(to);
        
        return String.format("%s %s → %s", pieceName, fromPos, toPos);
    }
    
    /**
     * 获取棋子显示名称
     */
    private String getPieceDisplayName(Piece piece) {
        if (piece == null) return "";
        
        String colorPrefix = (piece.getColor() == PieceColor.RED) ? "红" : "黑";
        
        if (piece instanceof General) {
            return colorPrefix + (piece.getColor() == PieceColor.RED ? "帅" : "将");
        } else if (piece instanceof Advisor) {
            return colorPrefix + (piece.getColor() == PieceColor.RED ? "仕" : "士");
        } else if (piece instanceof Elephant) {
            return colorPrefix + (piece.getColor() == PieceColor.RED ? "相" : "象");
        } else if (piece instanceof Horse) {
            return colorPrefix + "马";
        } else if (piece instanceof Chariot) {
            return colorPrefix + "车";
        } else if (piece instanceof Cannon) {
            return colorPrefix + "炮";
        } else if (piece instanceof Soldier) {
            return colorPrefix + (piece.getColor() == PieceColor.RED ? "兵" : "卒");
        } else {
            return colorPrefix + "？";
        }
    }
    
    /**
     * 格式化位置信息
     */
    private String formatPosition(Position pos) {
        if (pos == null) return "？？";
        
        // 转换为中国象棋标准表示法
        char file = (char)('a' + pos.getY());
        int rank = 10 - pos.getX();
        
        return String.format("%c%d", file, rank);
    }
    
    /**
     * 获取游戏状态
     */
    public com.example.chinesechess.core.GameState getGameState() {
        return gameState;
    }

    /**
     * 暂停游戏
     */
    public void pauseGame() {
        isGamePaused = true;
        addAILog("system", "游戏已暂停");
        System.out.println("⏸️ 游戏已暂停");
        updateStatus();
    }

    /**
     * 恢复游戏
     */
    public void resumeGame() {
        isGamePaused = false;
        addAILog("system", "游戏已恢复");
        System.out.println("▶️ 游戏已恢复");
        updateStatus();

        // 如果是AI的回合，恢复后自动执行AI移动
        if (isAIEnabled && currentPlayer == humanPlayer.getOppositeColor()) {
            SwingUtilities.invokeLater(this::performAIMove);
        } else if (isAIvsAIMode) {
            SwingUtilities.invokeLater(this::performAIvsAIMove);
        }
    }

    /**
     * 检查游戏是否暂停
     */
    public boolean isGamePaused() {
        return isGamePaused;
    }
    
    /**
     * 开始玩家对玩家残局游戏
     */
    public void startPlayerVsPlayerEndgame() {
        if (!isSettingUpEndgame) {
            showErrorInfo("请先进入残局设置模式！");
            return;
        }
        
        // 检查棋盘上是否有棋子
        boolean hasRedGeneral = false;
        boolean hasBlackGeneral = false;
        int totalPieces = 0;
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    totalPieces++;
                    if (piece instanceof com.example.chinesechess.core.General) {
                        if (piece.getColor() == PieceColor.RED) {
                            hasRedGeneral = true;
                        } else {
                            hasBlackGeneral = true;
                        }
                    }
                }
            }
        }
        
        if (totalPieces == 0) {
            showErrorInfo("请先在棋盘上放置一些棋子！");
            return;
        }
        
        if (!hasRedGeneral || !hasBlackGeneral) {
            showErrorInfo("棋盘上必须同时有红方和黑方的将/帅！");
            return;
        }
        
        // 检查棋子数量是否合理
        String validationResult = validateEndgameBoardSetup();
        if (validationResult != null) {
            showErrorInfo(validationResult + "\n\n如需继续，请再次点击'开始残局'按钮。");
            return;
        }
        
        // 设置玩家对玩家残局模式
        isSettingUpEndgame = false;
        isEndgameMode = true;
        
        // 禁用AI
        disableAI();
        
        updateStatus();
        repaint();
        
        showErrorInfo("玩家对玩家残局游戏开始！\n红方玩家 vs 黑方玩家");
    }
    
    /**
     * 计算棋盘面板的合理大小
     * 确保棋盘有足够的空间显示完整的棋盘和坐标
     */
    public Dimension calculateBoardSize() {
        // 计算棋盘本身的大小：9列 × 10行 的格子
        // 中国象棋棋盘是9条纵线，10条横线，形成8×9的格子
        // 但为了显示棋盘线条，我们需要9×10的空间
        int boardWidth = 8 * CELL_SIZE;   // 8个格子宽度
        int boardHeight = 9 * CELL_SIZE;  // 9个格子高度
        
        // 加上边距：左右各MARGIN，上下各MARGIN
        // 还要加上坐标显示的额外空间
        int totalWidth = boardWidth + 2 * MARGIN;
        int totalHeight = boardHeight + 2 * MARGIN;
        
        // 确保最小尺寸
        int minWidth = Math.max(totalWidth, 600);
        int minHeight = Math.max(totalHeight, 700);
        
        System.out.println("📐 计算棋盘尺寸: 格子大小=" + CELL_SIZE + ", 边距=" + MARGIN + ", 总尺寸=" + minWidth + "x" + minHeight);
        
        return new Dimension(minWidth, minHeight);
    }
    
    @Override
    public Dimension getPreferredSize() {
        // 重写getPreferredSize方法，确保布局管理器能正确计算大小
        return calculateBoardSize();
    }
    
    @Override
    public Dimension getMinimumSize() {
        // 重写getMinimumSize方法
        return calculateBoardSize();
    }
}
