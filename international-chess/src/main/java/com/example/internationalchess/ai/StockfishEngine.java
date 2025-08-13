package com.example.internationalchess.ai;

import com.example.internationalchess.core.Move;
import com.example.internationalchess.core.Position;
import com.example.internationalchess.core.PieceColor;
import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.ui.StockfishLogPanel;
import com.example.common.config.GameConfig;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Stockfish UCI引擎接口
 */
public class StockfishEngine {
    
    private Process stockfishProcess;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isInitialized = false;
    private int skillLevel;
    private int thinkingTime;
    private StockfishLogPanel logPanel;
    
    public StockfishEngine() {
        this(GameConfig.getInstance().getDefaultSkillLevel(), GameConfig.getInstance().getDefaultThinkingTime(), null);
    }
    
    public StockfishEngine(int skillLevel, int thinkingTimeMs) {
        this(skillLevel, thinkingTimeMs, null);
    }
    
    public StockfishEngine(int skillLevel, int thinkingTimeMs, StockfishLogPanel logPanel) {
        this.skillLevel = Math.max(0, Math.min(20, skillLevel));
        this.thinkingTime = thinkingTimeMs;
        this.logPanel = logPanel;
        initialize();
    }
    
    /**
     * 初始化Stockfish引擎
     */
    private void initialize() {
        try {
            GameConfig config = GameConfig.getInstance();
            
            // 启动Stockfish进程
            ProcessBuilder pb = new ProcessBuilder(config.getStockfishPath());
            pb.redirectErrorStream(true);
            stockfishProcess = pb.start();
            
            reader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(stockfishProcess.getOutputStream()), true);
            
            // 初始化UCI协议
            sendCommand("uci");
            waitForResponse("uciok");
            
            // 设置引擎参数
            sendCommand("setoption name Skill Level value " + skillLevel);
            sendCommand("setoption name Threads value 1");
            
            // 加载NNUE文件
            String nnueFile = config.findNnueFile();
            if (nnueFile != null) {
                sendCommand("setoption name EvalFile value " + nnueFile);
                log("NNUE已加载: " + new File(nnueFile).getName());
                System.out.println("🧠 已加载神经网络文件: " + nnueFile);
            } else {
                System.out.println("⚠️  未找到NNUE文件，使用传统评估");
            }
            
            // 准备引擎
            sendCommand("isready");
            waitForResponse("readyok");
            
            isInitialized = true;
            System.out.println("🚀 Stockfish引擎已启动 (技能等级: " + skillLevel + "/20)");
            
        } catch (IOException e) {
            System.err.println("❌ Stockfish引擎启动失败: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Stockfish", e);
        }
    }
    
    /**
     * 获取Stockfish的最佳移动
     */
    public Move getBestMove(InternationalChessBoard board, PieceColor currentPlayer) {
        if (!isInitialized) {
            System.err.println("Stockfish引擎未初始化");
            return null;
        }
        
        try {
            GameConfig config = GameConfig.getInstance();
            
            // 设置棋盘位置并计算
            String fen = boardToFEN(board, currentPlayer);
            sendCommand("position fen " + fen);
            sendCommand("go movetime " + thinkingTime);
            
            // 查找最佳移动
            String line;
            while ((line = reader.readLine()) != null) {
                if (config.isLogEngineOutput()) {
                    System.out.println("引擎: " + line);
                }
                
                if (logPanel != null) {
                    logPanel.addEngineOutput(line);
                }
                
                if (line.startsWith("bestmove")) {
                    String[] parts = line.split(" ");
                    if (parts.length >= 2) {
                        String bestMoveUci = parts[1];
                        if (!bestMoveUci.equals("(none)")) {
                            return uciMoveToMove(bestMoveUci);
                        }
                    }
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("获取Stockfish移动失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 发送命令到Stockfish
     */
    private void sendCommand(String command) {
        writer.println(command);
        writer.flush();
    }
    
    /**
     * 等待特定响应
     */
    private void waitForResponse(String expectedResponse) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals(expectedResponse)) {
                break;
            }
        }
    }
    
    /**
     * 等待特定响应并记录日志
     */
    private void waitForResponseWithLogging(String expectedResponse) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            // 记录所有响应到日志
            if (logPanel != null) {
                logPanel.addEngineOutput(line);
            }
            
            if (line.equals(expectedResponse)) {
                log("📥 收到预期响应: " + expectedResponse);
                break;
            }
        }
    }
    
    /**
     * 将棋盘状态转换为FEN格式
     */
    private String boardToFEN(InternationalChessBoard board, PieceColor currentPlayer) {
        StringBuilder fen = new StringBuilder();
        
        // 棋盘状态 - FEN格式从第8行开始（黑棋后排）到第1行（白棋后排）
        // 我们的数组：board[0] = 黑棋后排，board[7] = 白棋后排
        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                String piece = board.getPiece(row, col);
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(pieceToFENChar(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row < 7) {
                fen.append("/");
            }
        }
        
        // 当前玩家
        fen.append(" ").append(currentPlayer == PieceColor.WHITE ? "w" : "b");
        
        // 王车易位权限 (简化处理)
        fen.append(" KQkq");
        
        // 吃过路兵目标格
        fen.append(" -");
        
        // 半回合计数和全回合计数 (简化处理)
        fen.append(" 0 1");
        
        return fen.toString();
    }
    
    /**
     * 将棋子转换为FEN字符
     */
    private char pieceToFENChar(String piece) {
        if (piece == null || piece.length() < 2) {
            return ' '; // 错误的棋子表示
        }
        
        char color = piece.charAt(0); // 'W' 或 'B'
        char type = piece.charAt(1);  // 棋子类型
        
        boolean isWhite = (color == 'W');
        
        char fenChar;
        switch (type) {
            case 'K': // King
                fenChar = 'k';
                break;
            case 'Q': // Queen
                fenChar = 'q';
                break;
            case 'R': // Rook
                fenChar = 'r';
                break;
            case 'B': // Bishop
                fenChar = 'b';
                break;
            case 'N': // Knight
                fenChar = 'n';
                break;
            case 'P': // Pawn
                fenChar = 'p';
                break;
            default:
                fenChar = 'p'; // 默认为兵
        }
        
        // 注意：FEN格式中，大写字母表示白棋，小写字母表示黑棋
        return isWhite ? Character.toUpperCase(fenChar) : Character.toLowerCase(fenChar);
    }
    
    /**
     * 将UCI格式的移动转换为Move对象
     */
    private Move uciMoveToMove(String uciMove) {
        if (uciMove.length() < 4) {
            return null;
        }
        
        // UCI格式: "e2e4" 表示从e2移动到e4
        // UCI中：a1在左下角，h8在右上角
        // 我们的棋盘：[0][0]在左上角（黑棋后排），[7][7]在右下角（白棋前排）
        int fromCol = uciMove.charAt(0) - 'a';  // a=0, b=1, ..., h=7
        int fromRankUci = uciMove.charAt(1) - '1';  // 1=0, 2=1, ..., 8=7 (UCI行号)
        int toCol = uciMove.charAt(2) - 'a';
        int toRankUci = uciMove.charAt(3) - '1';
        
        // 转换UCI行号到我们的数组索引：UCI的1对应我们的行7，UCI的8对应我们的行0
        int fromRow = 7 - fromRankUci;
        int toRow = 7 - toRankUci;
        
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);
        
        return new Move(from, to);
    }
    
    /**
     * 设置技能等级 (0-20)
     */
    public void setSkillLevel(int level) {
        this.skillLevel = Math.max(0, Math.min(20, level));
        if (isInitialized) {
            sendCommand("setoption name Skill Level value " + skillLevel);
        }
    }
    
    /**
     * 设置思考时间
     */
    public void setThinkingTime(int milliseconds) {
        this.thinkingTime = milliseconds;
    }
    
    /**
     * 获取引擎状态
     */
    public boolean isReady() {
        return isInitialized && stockfishProcess.isAlive();
    }
    
    /**
     * 关闭引擎
     */
    public void shutdown() {
        if (stockfishProcess != null) {
            try {
                sendCommand("quit");
                stockfishProcess.waitFor(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (stockfishProcess.isAlive()) {
                    stockfishProcess.destroyForcibly();
                }
                isInitialized = false;
                System.out.println("🔌 Stockfish引擎已关闭");
            }
        }
    }
    
    /**
     * 获取引擎信息
     */
    public String getEngineInfo() {
        return String.format("Stockfish Engine (Skill: %d/20, Time: %dms)", 
                           skillLevel, thinkingTime);
    }
    
    /**
     * 设置日志面板
     */
    public void setLogPanel(StockfishLogPanel logPanel) {
        this.logPanel = logPanel;
    }
    
    /**
     * 记录日志
     */
    private void log(String message) {
        if (logPanel != null) {
            logPanel.addStatusLog(message);
        }
    }
    
    /**
     * 记录错误日志
     */
    private void logError(String error) {
        if (logPanel != null) {
            logPanel.addErrorLog(error);
        }
    }
    
    /**
     * 公开的FEN转换方法（用于调试）
     */
    public String debugBoardToFEN(InternationalChessBoard board, PieceColor currentPlayer) {
        return boardToFEN(board, currentPlayer);
    }
}
