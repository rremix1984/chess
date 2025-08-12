package com.example.chinesechess.terminal;

import com.example.chinesechess.core.Board;
import com.example.chinesechess.core.PieceColor;
import com.example.chinesechess.core.Move;
import com.example.chinesechess.core.Position;
import com.example.chinesechess.core.Piece;
import com.example.chinesechess.core.GameState;
import com.example.chinesechess.core.General;
import com.example.chinesechess.core.Chariot;
import com.example.chinesechess.core.Cannon;
import com.example.chinesechess.core.Horse;
import com.example.chinesechess.core.Advisor;
import com.example.chinesechess.core.Elephant;
import com.example.chinesechess.core.Soldier;
import com.example.chinesechess.ai.ChessAI;
import com.example.chinesechess.ai.DeepSeekPikafishAI;
import com.example.chinesechess.ai.EnhancedChessAI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 终端版本的中国象棋游戏
 * 支持文字描述下盲棋，玩家可以通过自然语言与AI对弈
 */
public class TerminalChessGame {
    private Board board;
    private PieceColor playerColor;
    private PieceColor aiColor;
    private Scanner scanner;
    private EnhancedChessAI enhancedAI;
    private ChessAI chessAI;
    private DeepSeekPikafishAI deepSeekPikafishAI;
    private String currentAIType;
    private int difficulty;
    private String modelName;
    private boolean gameRunning;
    private List<String> moveHistory;
    private MoveParser moveParser;
    
    // 棋子名称映射
    private static final Map<String, String> PIECE_NAMES = new HashMap<>();
    static {
        PIECE_NAMES.put("将", "General");
        PIECE_NAMES.put("帅", "General");
        PIECE_NAMES.put("士", "Advisor");
        PIECE_NAMES.put("仕", "Advisor");
        PIECE_NAMES.put("象", "Elephant");
        PIECE_NAMES.put("相", "Elephant");
        PIECE_NAMES.put("马", "Horse");
        PIECE_NAMES.put("車", "Chariot");
        PIECE_NAMES.put("车", "Chariot");
        PIECE_NAMES.put("炮", "Cannon");
        PIECE_NAMES.put("砲", "Cannon");
        PIECE_NAMES.put("兵", "Soldier");
        PIECE_NAMES.put("卒", "Soldier");
    }
    
    // 位置映射（中文数字到阿拉伯数字）
    private static final Map<String, Integer> CHINESE_NUMBERS = new HashMap<>();
    static {
        CHINESE_NUMBERS.put("一", 1);
        CHINESE_NUMBERS.put("二", 2);
        CHINESE_NUMBERS.put("三", 3);
        CHINESE_NUMBERS.put("四", 4);
        CHINESE_NUMBERS.put("五", 5);
        CHINESE_NUMBERS.put("六", 6);
        CHINESE_NUMBERS.put("七", 7);
        CHINESE_NUMBERS.put("八", 8);
        CHINESE_NUMBERS.put("九", 9);
        CHINESE_NUMBERS.put("十", 10);
        // 也支持阿拉伯数字
        for (int i = 1; i <= 10; i++) {
            CHINESE_NUMBERS.put(String.valueOf(i), i);
        }
    }
    
    public TerminalChessGame() {
        this.board = new Board();
        this.scanner = new Scanner(System.in);
        this.moveHistory = new ArrayList<>();
        this.gameRunning = true;
        this.moveParser = new MoveParser(board);
    }
    
    /**
     * 启动游戏
     */
    public void start() {
        printWelcome();
        setupGame();
        gameLoop();
    }
    
    /**
     * 打印欢迎信息
     */
    private void printWelcome() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║          🏮 终端象棋对弈 🏮          ║");
        System.out.println("║                                      ║");
        System.out.println("║    欢迎来到智能象棋终端对弈系统！    ║");
        System.out.println("║                                      ║");
        System.out.println("║  🤖 支持多种AI引擎                  ║");
        System.out.println("║  💬 支持自然语言走棋                ║");
        System.out.println("║  🎯 支持盲棋对弈                    ║");
        System.out.println("║                                      ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * 游戏设置
     */
    private void setupGame() {
        // 提供快捷选项
        System.out.println("🚀 游戏设置选项：");
        System.out.println("0. 快速开始（默认：红方 + GPT-4 + 大师难度）");
        System.out.println("1. 自定义设置");
        System.out.print("请输入选择 (0-1): ");
        
        int setupChoice = getIntInput(0, 1);
        
        if (setupChoice == 0) {
            // 使用默认设置
            playerColor = PieceColor.RED;
            aiColor = PieceColor.BLACK;
            difficulty = 5; // 大师难度
            modelName = "gpt-4";
            
            // 暂时使用增强AI代替大模型AI（避免编译错误）
            enhancedAI = new EnhancedChessAI(aiColor, difficulty);
            currentAIType = "增强AI";
            
            System.out.println("\n🎮 快速设置完成！");
            System.out.println("玩家：红方（先手）");
            System.out.println("AI：增强AI（难度：大师）");
            System.out.println("模型：GPT-4（暂时使用增强AI）");
            System.out.println();
            return;
        }
        
        // 自定义设置流程
        // 选择玩家颜色
        System.out.println("\n请选择您的棋子颜色：");
        System.out.println("1. 红方（先手）");
        System.out.println("2. 黑方（后手）");
        System.out.print("请输入选择 (1-2): ");
        
        int colorChoice = getIntInput(1, 2);
        playerColor = (colorChoice == 1) ? PieceColor.RED : PieceColor.BLACK;
        aiColor = (playerColor == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
        
        System.out.println("您选择了：" + (playerColor == PieceColor.RED ? "红方（先手）" : "黑方（后手）"));
        
        // 选择AI类型
        System.out.println("\n请选择AI类型：");
        System.out.println("1. 传统AI（快速响应）");
        System.out.println("2. 增强AI（更强算法）");
        System.out.println("3. 大模型AI（智能分析）");
        System.out.println("4. 混合AI（推荐，最强）");
        System.out.println("5. DeepSeek-Pikafish AI（专业引擎+AI融合，顶级棋力）");
        System.out.print("请输入选择 (1-5): ");
        
        int aiChoice = getIntInput(1, 5);
        
        // 选择难度
        System.out.println("\n请选择难度级别：");
        System.out.println("1. 简单");
        System.out.println("2. 普通");
        System.out.println("3. 困难");
        System.out.println("4. 专家");
        System.out.println("5. 大师");
        System.out.print("请输入选择 (1-5): ");
        
        difficulty = getIntInput(1, 5);
        
        // 如果选择了大模型AI、混合AI或DeepSeek-Pikafish AI，需要选择模型
        if (aiChoice == 3 || aiChoice == 4 || aiChoice == 5) {
            System.out.println("\n请选择大模型：");
            System.out.println("1. GPT-4");
            System.out.println("2. Claude-3");
            System.out.println("3. Gemini-Pro");
            if (aiChoice == 5) {
                System.out.println("4. DeepSeek-R1（推荐，专为象棋优化）");
            }
            System.out.print("请输入选择 (1-" + (aiChoice == 5 ? "4" : "3") + "): ");
            
            int modelChoice = getIntInput(1, aiChoice == 5 ? 4 : 3);
            switch (modelChoice) {
                case 1: modelName = "gpt-4"; break;
                case 2: modelName = "claude-3"; break;
                case 3: modelName = "gemini-pro"; break;
                case 4: 
                    if (aiChoice == 5) {
                        modelName = "deepseek-r1";
                    }
                    break;
            }
        }
        
        // 初始化AI
        initializeAI(aiChoice);
        
        System.out.println("\n🎮 游戏设置完成！");
        System.out.println("玩家：" + (playerColor == PieceColor.RED ? "红方" : "黑方"));
        System.out.println("AI：" + currentAIType + "（难度：" + getDifficultyName(difficulty) + "）");
        if (modelName != null) {
            System.out.println("模型：" + modelName);
        }
        System.out.println();
    }
    
    /**
     * 初始化AI
     */
    private void initializeAI(int aiChoice) {
        switch (aiChoice) {
            case 1:
                chessAI = new ChessAI(aiColor, difficulty);
                currentAIType = "传统AI";
                break;
            case 2:
                enhancedAI = new EnhancedChessAI(aiColor, difficulty);
                currentAIType = "增强AI";
                break;
            case 3:
                // 暂时使用增强AI代替大模型AI
                enhancedAI = new EnhancedChessAI(aiColor, difficulty);
                currentAIType = "增强AI（代替大模型AI）";
                break;
            case 4:
                // 暂时使用增强AI代替混合AI
                enhancedAI = new EnhancedChessAI(aiColor, difficulty);
                currentAIType = "增强AI（代替混合AI）";
                break;
            case 5:
                try {
                    deepSeekPikafishAI = new DeepSeekPikafishAI(aiColor, difficulty, modelName != null ? modelName : "deepseek-r1");
                    currentAIType = "DeepSeek-Pikafish AI";
                    System.out.println("✅ DeepSeek-Pikafish AI 初始化成功");
                    
                    // 检查Pikafish引擎状态
                    if (deepSeekPikafishAI.isPikafishAvailable()) {
                        System.out.println("🚀 Pikafish引擎已就绪，提供专业级棋力");
                    } else {
                        System.out.println("⚠️  Pikafish引擎不可用，将使用纯DeepSeek模式");
                    }
                } catch (Exception e) {
                    System.out.println("❌ DeepSeek-Pikafish AI 初始化失败: " + e.getMessage());
                    System.out.println("🔄 自动切换到增强AI");
                    enhancedAI = new EnhancedChessAI(aiColor, difficulty);
                    currentAIType = "增强AI（DeepSeek-Pikafish AI备用）";
                }
                break;
        }
    }
    
    /**
     * 主游戏循环
     */
    private void gameLoop() {
        PieceColor currentPlayer = PieceColor.RED; // 红方先手
        
        while (gameRunning) {
            // 显示当前棋盘
            displayBoard();
            
            // 检查游戏状态
            GameState gameState = board.checkGameState(currentPlayer);
            if (gameState != GameState.PLAYING && gameState != GameState.IN_CHECK) {
                handleGameEnd(gameState);
                break;
            }
            
            if (gameState == GameState.IN_CHECK) {
                System.out.println("⚠️  " + (currentPlayer == PieceColor.RED ? "红方" : "黑方") + "被将军！");
            }
            
            System.out.println("\n当前回合：" + (currentPlayer == PieceColor.RED ? "红方" : "黑方"));
            
            if (currentPlayer == playerColor) {
                // 玩家回合
                handlePlayerMove();
            } else {
                // AI回合
                handleAIMove();
            }
            
            // 切换玩家
            currentPlayer = (currentPlayer == PieceColor.RED) ? PieceColor.BLACK : PieceColor.RED;
        }
    }
    
    /**
     * 显示棋盘
     */
    private void displayBoard() {
        System.out.println("\n📋 当前棋盘：");
        
        // 准备历史记录显示
        List<String> historyLines = prepareHistoryDisplay();
        
        // 显示列标题
        System.out.print("    一  二  三  四  五  六  七  八  九");
        if (!historyLines.isEmpty()) {
            System.out.print("      📜 操作记录（盲棋术语）");
        }
        System.out.println();
        
        // 显示棋盘和历史记录
        for (int row = 0; row < 10; row++) {
            // 显示行号
            System.out.printf("%2d ", 10 - row);
            
            // 显示棋盘这一行
            for (int col = 0; col < 9; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) {
                    System.out.print(" ·  ");
                } else {
                    String name = piece.getChineseName();
                    if (piece.getColor() == PieceColor.RED) {
                        System.out.print("[" + name.charAt(0) + "] ");
                    } else {
                        System.out.print("(" + name.charAt(0) + ") ");
                    }
                }
            }
            
            // 显示对应的历史记录行
            if (row < historyLines.size()) {
                System.out.print("     " + historyLines.get(row));
            }
            
            System.out.println();
        }
        
        // 显示方位标识
        System.out.println("    红方：[棋] 黑方：(棋)");
        
        // 如果历史记录比棋盘行数多，继续显示剩余的历史记录
        for (int i = 10; i < historyLines.size(); i++) {
            System.out.println("                                         " + historyLines.get(i));
        }
        
        System.out.println();
    }
    
    /**
     * 准备历史记录显示
     */
    private List<String> prepareHistoryDisplay() {
        List<String> historyLines = new ArrayList<>();
        
        if (moveHistory.isEmpty()) {
            historyLines.add("暂无走棋记录");
            return historyLines;
        }
        
        // 显示最近的步骤（最多显示15步，适合棋盘高度）
        int startIndex = Math.max(0, moveHistory.size() - 15);
        
        for (int i = startIndex; i < moveHistory.size(); i++) {
            String move = moveHistory.get(i);
            String blindNotation = convertToBlindNotation(move, i + 1);
            historyLines.add(String.format("%2d. %s", i + 1, blindNotation));
        }
        
        return historyLines;
    }
    
    /**
     * 转换为盲棋术语
     */
    private String convertToBlindNotation(String moveDescription, int moveNumber) {
        try {
            // 解析移动描述，提取关键信息
            // 格式：红方: 马从(10,2)到(8,3) 或 黑方: 车从(1,1)到(1,5)
            
            String[] parts = moveDescription.split(": ");
            if (parts.length != 2) {
                return moveDescription; // 如果格式不符合预期，返回原始描述
            }
            
            String player = parts[0]; // 红方 或 黑方
            String moveInfo = parts[1]; // 马从(10,2)到(8,3)
            
            // 提取棋子名称和坐标
            if (moveInfo.contains("从") && moveInfo.contains("到")) {
                String pieceName = moveInfo.substring(0, moveInfo.indexOf("从"));
                
                // 提取起始和结束坐标
                String coordPart = moveInfo.substring(moveInfo.indexOf("从") + 1);
                String[] coords = coordPart.split("到");
                
                if (coords.length == 2) {
                    String startCoord = coords[0].trim();
                    String endCoord = coords[1].trim();
                    
                    // 解析坐标 (10,2) -> row=10, col=2
                    Position start = parseCoordinate(startCoord);
                    Position end = parseCoordinate(endCoord);
                    
                    if (start != null && end != null) {
                        return formatBlindNotation(pieceName, start, end, player.equals("红方"));
                    }
                }
            }
            
            return moveDescription; // 如果解析失败，返回原始描述
            
        } catch (Exception e) {
            return moveDescription; // 如果出现异常，返回原始描述
        }
    }
    
    /**
     * 解析坐标字符串
     */
    private Position parseCoordinate(String coord) {
        try {
            // 移除括号并分割
            coord = coord.replace("(", "").replace(")", "");
            String[] parts = coord.split(",");
            if (parts.length == 2) {
                int row = Integer.parseInt(parts[0].trim());
                int col = Integer.parseInt(parts[1].trim());
                return new Position(10 - row, col - 1); // 转换为内部坐标系
            }
        } catch (Exception e) {
            // 解析失败
        }
        return null;
    }
    
    /**
     * 格式化为盲棋术语
     */
    private String formatBlindNotation(String pieceName, Position start, Position end, boolean isRed) {
        // 将内部坐标转换为显示坐标
        int startRow = 10 - start.getX(); // 1-10
        int startCol = start.getY() + 1;  // 1-9
        int endRow = 10 - end.getX();     // 1-10
        int endCol = end.getY() + 1;      // 1-9
        
        // 中文数字映射
        String[] chineseNumbers = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        
        // 确定移动方向和类型
        String direction;
        String target;
        
        if (startCol == endCol) {
            // 纵向移动
            if (isRed) {
                if (endRow < startRow) {
                    direction = "进";
                    target = String.valueOf(startRow - endRow);
                } else {
                    direction = "退";
                    target = String.valueOf(endRow - startRow);
                }
            } else {
                // 黑方的进退方向相反
                if (endRow > startRow) {
                    direction = "进";
                    target = String.valueOf(endRow - startRow);
                } else {
                    direction = "退";
                    target = String.valueOf(startRow - endRow);
                }
            }
        } else {
            // 横向移动
            direction = "平";
            target = endCol <= 9 ? chineseNumbers[endCol] : String.valueOf(endCol);
        }
        
        // 构建盲棋术语
        String startColStr = startCol <= 9 ? chineseNumbers[startCol] : String.valueOf(startCol);
        
        // 处理特殊棋子名称
        String shortPieceName = pieceName;
        if (pieceName.equals("将") || pieceName.equals("帅")) {
            shortPieceName = isRed ? "帅" : "将";
        } else if (pieceName.equals("士") || pieceName.equals("仕")) {
            shortPieceName = isRed ? "仕" : "士";
        } else if (pieceName.equals("象") || pieceName.equals("相")) {
            shortPieceName = isRed ? "相" : "象";
        } else if (pieceName.equals("兵") || pieceName.equals("卒")) {
            shortPieceName = isRed ? "兵" : "卒";
        }
        
        return shortPieceName + startColStr + direction + target;
    }
    
    /**
     * 可视化展示错误原因
     */
    private void displayErrorVisualization(Move move, String errorMessage) {
        System.out.println("\n🎯 错误可视化展示：");
        System.out.println("    一  二  三  四  五  六  七  八  九");
        
        Position start = move.getStart();
        Position end = move.getEnd();
        
        for (int row = 0; row < 10; row++) {
            System.out.printf("%2d ", 10 - row);
            for (int col = 0; col < 9; col++) {
                boolean isStart = (row == start.getX() && col == start.getY());
                boolean isEnd = (row == end.getX() && col == end.getY());
                
                if (isStart && isEnd) {
                    // 起始和目标是同一位置
                    System.out.print(" ○  ");
                } else if (isStart) {
                    // 起始位置
                    Piece piece = board.getPiece(row, col);
                    if (piece == null) {
                        System.out.print(" ✗  "); // 起始位置没有棋子
                    } else if (piece.getColor() != playerColor) {
                        System.out.print(" ✖  "); // 不是己方棋子
                    } else {
                        System.out.print(" ✓  "); // 正确的起始位置
                    }
                } else if (isEnd) {
                    // 目标位置
                    if (!isValidPosition(row, col)) {
                        System.out.print(" ✗  "); // 超出棋盘范围
                    } else {
                        Piece targetPiece = board.getPiece(row, col);
                        if (targetPiece != null && targetPiece.getColor() == playerColor) {
                            System.out.print(" !  "); // 目标位置有己方棋子
                        } else {
                            System.out.print(" ✗  "); // 无效目标位置
                        }
                    }
                } else {
                    // 普通位置
                    Piece piece = board.getPiece(row, col);
                    if (piece == null) {
                        System.out.print(" ·  ");
                    } else {
                        String name = piece.getChineseName();
                        if (piece.getColor() == PieceColor.RED) {
                            System.out.print("[" + name.charAt(0) + "] ");
                        } else {
                            System.out.print("(" + name.charAt(0) + ") ");
                        }
                    }
                }
            }
            System.out.println();
        }
        
        // 显示图例
        System.out.println("\n📖 图例说明：");
        System.out.println("✓ 起始位置（正确）    ✗ 目标位置（错误）");
        System.out.println("✗ 起始位置（无棋子）  ! 目标位置（己方棋子）");
        System.out.println("✖ 起始位置（敌方棋子） ○ 起始=目标位置");
        
        // 显示移动路径（如果是直线移动）
        if (isLinearMove(start, end)) {
            displayMovePath(start, end);
        }
        
        // 显示该棋子的合法移动范围
        displayValidMoves(start);
        
        System.out.println();
    }
    
    /**
     * 检查是否为直线移动
     */
    private boolean isLinearMove(Position start, Position end) {
        return start.getX() == end.getX() || start.getY() == end.getY() || 
               Math.abs(start.getX() - end.getX()) == Math.abs(start.getY() - end.getY());
    }
    
    /**
     * 显示移动路径
     */
    private void displayMovePath(Position start, Position end) {
        System.out.println("🛤️  移动路径分析：");
        
        int startX = start.getX(), startY = start.getY();
        int endX = end.getX(), endY = end.getY();
        
        // 计算移动方向
        int deltaX = Integer.compare(endX, startX);
        int deltaY = Integer.compare(endY, startY);
        
        // 显示路径上的障碍物
        int currentX = startX + deltaX;
        int currentY = startY + deltaY;
        
        boolean hasObstacle = false;
        while (currentX != endX || currentY != endY) {
            if (isValidPosition(currentX, currentY)) {
                Piece obstacle = board.getPiece(currentX, currentY);
                if (obstacle != null) {
                    System.out.println("   ⚠️  路径上有障碍：位置(" + (10 - currentX) + "," + (currentY + 1) + 
                                     ") - " + obstacle.getChineseName());
                    hasObstacle = true;
                }
            }
            currentX += deltaX;
            currentY += deltaY;
        }
        
        if (!hasObstacle) {
            System.out.println("   ✅ 路径畅通无阻");
        }
    }
    
    /**
     * 显示棋子的合法移动范围
     */
    private void displayValidMoves(Position piecePosition) {
        if (!isValidPosition(piecePosition.getX(), piecePosition.getY())) {
            return;
        }
        
        Piece piece = board.getPiece(piecePosition.getX(), piecePosition.getY());
        if (piece == null || piece.getColor() != playerColor) {
            return;
        }
        
        System.out.println("💡 " + piece.getChineseName() + "的合法移动范围：");
        System.out.println("   一 二 三 四 五 六 七 八 九");
        
        // 计算所有合法移动
        boolean[][] validMoves = new boolean[10][9];
        int validMoveCount = 0;
        
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                Position targetPos = new Position(row, col);
                if (piece.isValidMove(board, piecePosition, targetPos) && 
                    board.isMoveSafe(piecePosition, targetPos, playerColor)) {
                    validMoves[row][col] = true;
                    validMoveCount++;
                }
            }
        }
        
        // 显示棋盘和合法移动
        for (int row = 0; row < 10; row++) {
            System.out.printf("%2d ", 10 - row);
            for (int col = 0; col < 9; col++) {
                boolean isPiecePosition = (row == piecePosition.getX() && col == piecePosition.getY());
                boolean isValidMove = validMoves[row][col];
                
                if (isPiecePosition) {
                    System.out.print("◎ "); // 当前棋子位置
                } else if (isValidMove) {
                    System.out.print("○ "); // 合法移动位置
                } else {
                    Piece boardPiece = board.getPiece(row, col);
                    if (boardPiece == null) {
                        System.out.print(" · ");
                    } else {
                        String name = boardPiece.getChineseName();
                        if (boardPiece.getColor() == PieceColor.RED) {
                            System.out.print("[" + name.charAt(0) + "]");
                        } else {
                            System.out.print("(" + name.charAt(0) + ")");
                        }
                    }
                }
            }
            System.out.println();
        }
        
        System.out.println("📊 统计：该" + piece.getChineseName() + "共有 " + validMoveCount + " 个合法移动位置");
        System.out.println("◎ 当前棋子位置    ○ 可移动位置");
    }
    
    /**
     * 处理玩家移动
     */
    private void handlePlayerMove() {
        System.out.println("💭 请输入您的走法（支持多种格式）：");
        System.out.println("   📝 示例：");
        System.out.println("   - 马二进三 / 车1进1 / 炮八平五");
        System.out.println("   - 从(10,1)到(8,2) / (10,1)->(8,2)");
        System.out.println("   - 马走日字到(8,3) / 前马进三");
        System.out.println("   - 马23 / 车15");
        System.out.println();
        System.out.println("   🎮 命令：help | board | history | quit | hint");
        
        while (true) {
            System.out.print("请输入走法: ");
            
            String input;
            try {
                // 检查是否有可用输入
                if (!scanner.hasNextLine()) {
                    System.err.println("错误：无法读取输入。请确保在交互式终端中运行程序。");
                    System.exit(1);
                }
                
                input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    continue;
                }
            } catch (Exception e) {
                System.err.println("输入错误: " + e.getMessage());
                System.err.println("请在交互式终端中运行程序。");
                System.exit(1);
                return; // 这行不会执行，但为了编译通过
            }
            
            // 处理特殊命令
            if (handleSpecialCommands(input)) {
                continue;
            }
            
            // 解析走法
            Move move = parseMove(input, playerColor);
            if (move == null) {
                System.out.println("❌ 无法理解您的走法：\"" + input + "\"");
                System.out.println("💡 可能的原因：");
                System.out.println("   - 输入格式不正确");
                System.out.println("   - 棋子名称错误（应为：帅/将、车、马、炮、相/象、仕/士、兵/卒）");
                System.out.println("   - 坐标超出范围（行：1-10，列：1-9）");
                System.out.println("   - 马不能使用进退平记谱法，请使用坐标格式");
                System.out.println("💭 请参考help命令查看支持的格式");
                continue;
            }
            
            // 验证走法
            String validationResult = validateMove(move, playerColor);
            if (validationResult == null) {
                // 检查是否有吃子，如果有则播放动画
                Piece capturedPiece = board.getPiece(move.getEnd().getX(), move.getEnd().getY());
                if (capturedPiece != null) {
                    playEarthquakeAnimation(move.getEnd());
                }
                
                // 走法有效，执行移动
                board.movePiece(move.getStart(), move.getEnd());
                String moveDescription = formatMove(move);
                moveHistory.add((playerColor == PieceColor.RED ? "红方" : "黑方") + ": " + moveDescription);
                System.out.println("✅ 走法执行成功：" + moveDescription);
                break;
            } else {
                System.out.println("❌ 无效的走法：\"" + input + "\"");
                System.out.println("💡 错误原因：" + validationResult);
                // 可视化展示错误
                displayErrorVisualization(move, validationResult);
            }
        }
    }
    
    /**
     * 处理AI移动
     */
    private void handleAIMove() {
        System.out.println("🤖 " + currentAIType + "正在思考中...");
        long startTime = System.currentTimeMillis();
        
        Move aiMove = null;
        try {
            if (deepSeekPikafishAI != null) {
                aiMove = deepSeekPikafishAI.getBestMove(board);
                
                // 显示引擎状态信息
                String engineStatus = deepSeekPikafishAI.getEngineStatus();
                if (!engineStatus.isEmpty()) {
                    System.out.println("🔧 引擎状态: " + engineStatus);
                }
            } else if (enhancedAI != null) {
                aiMove = enhancedAI.getBestMove(board);
            } else if (chessAI != null) {
                aiMove = chessAI.getBestMove(board);
            }
        } catch (Exception e) {
            System.out.println("⚠️  AI思考出现异常，使用备用逻辑...");
            System.out.println("错误详情: " + e.getMessage());
            aiMove = getRandomValidMove(aiColor);
        }
        
        long endTime = System.currentTimeMillis();
        
        if (aiMove != null) {
            // 检查AI是否有吃子，如果有则播放动画
            Piece capturedPiece = board.getPiece(aiMove.getEnd().getX(), aiMove.getEnd().getY());
            if (capturedPiece != null) {
                playEarthquakeAnimation(aiMove.getEnd());
            }
            
            board.movePiece(aiMove.getStart(), aiMove.getEnd());
            String moveDescription = formatMove(aiMove);
            moveHistory.add((aiColor == PieceColor.RED ? "红方" : "黑方") + ": " + moveDescription);
            System.out.println("🎯 " + currentAIType + "走法：" + moveDescription);
            System.out.println("⏱️  思考时间：" + (endTime - startTime) + "ms");
        } else {
            System.out.println("❌ AI无法找到合法走法，游戏可能已结束。");
            gameRunning = false;
        }
    }
    
    /**
     * 处理特殊命令
     */
    private boolean handleSpecialCommands(String input) {
        switch (input.toLowerCase()) {
            case "help":
                showHelp();
                return true;
            case "board":
                displayBoard();
                return true;
            case "history":
                showHistory();
                return true;
            case "hint":
                showAIHint();
                return true;
            case "quit":
                System.out.println("👋 感谢游戏，再见！");
                gameRunning = false;
                return true;
            default:
                return false;
        }
    }
    
    /**
     * 显示帮助信息
     */
    private void showHelp() {
        System.out.println("\n📖 走法输入帮助：");
        System.out.println("支持以下格式：");
        System.out.println();
        System.out.println("1. 🎯 中国象棋标准记谱法：");
        System.out.println("   - 马二进三（马从二路进到三路）");
        System.out.println("   - 车1进1（车从1路进1格）");
        System.out.println("   - 炮八平五（炮从八路平移到五路）");
        System.out.println("   - 兵五进一（兵从五路进1格）");
        System.out.println();
        System.out.println("2. 📍 坐标格式：");
        System.out.println("   - 从(10,1)到(8,2)");
        System.out.println("   - (10,1)->(8,2)");
        System.out.println("   - 10,1 to 8,2");
        System.out.println("   - 10 1 8 2");
        System.out.println();
        System.out.println("3. 💬 自然语言描述：");
        System.out.println("   - 马走日字到(8,3)");
        System.out.println("   - 车直走到5,1");
        System.out.println("   - 前马进三");
        System.out.println("   - 左车平移");
        System.out.println();
        System.out.println("4. ⚡ 简化格式：");
        System.out.println("   - 马23（马从2路到3路）");
        System.out.println("   - 车15（车从1路到5路）");
        System.out.println();
        System.out.println("5. 🎮 特殊命令：");
        System.out.println("   - help: 显示此帮助");
        System.out.println("   - board: 重新显示棋盘");
        System.out.println("   - history: 查看走棋历史");
        System.out.println("   - hint: 🤖 AI助手建议（不会下棋时的好帮手！）");
        System.out.println("   - quit: 退出游戏");
        System.out.println();
        System.out.println("💡 提示：支持中文数字（一二三...）和阿拉伯数字（123...）");
        System.out.println("💡 提示：棋盘坐标从左下角(10,1)到右上角(1,9)");
        System.out.println();
    }
    
    /**
     * 显示走棋历史
     */
    private void showHistory() {
        System.out.println("\n📜 走棋历史：");
        if (moveHistory.isEmpty()) {
            System.out.println("暂无走棋记录");
        } else {
            for (int i = 0; i < moveHistory.size(); i++) {
                System.out.println((i + 1) + ". " + moveHistory.get(i));
            }
        }
        System.out.println();
    }
    
    /**
     * 显示AI助手建议
     */
    private void showAIHint() {
        System.out.println("\n🤖 AI助手正在分析当前局面...");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 使用AI引擎获取最佳走法
            Move bestMove = null;
            if (deepSeekPikafishAI != null) {
                bestMove = deepSeekPikafishAI.getBestMove(board);
            } else if (enhancedAI != null) {
                bestMove = enhancedAI.getBestMove(board);
            } else if (chessAI != null) {
                bestMove = chessAI.getBestMove(board);
            }
            
            long endTime = System.currentTimeMillis();
            
            if (bestMove != null) {
                 // 获取走法描述
                 String moveDescription = formatMove(bestMove);
                 String blindNotation = convertToBlindNotation(moveDescription, 1);
                 
                 System.out.println("💡 AI建议走法：");
                 System.out.println("   📝 标准描述：" + moveDescription);
                 System.out.println("   🎯 盲棋术语：" + blindNotation);
                 System.out.println("   ⏱️  分析时间：" + (endTime - startTime) + "ms");
                
                // 分析走法原因
                analyzeHintReason(bestMove);
                
                // 显示可视化提示
                displayHintVisualization(bestMove);
                
            } else {
                System.out.println("❌ AI无法找到合适的走法建议");
                System.out.println("💭 可能原因：");
                System.out.println("   - 当前局面已无合法走法");
                System.out.println("   - AI引擎出现异常");
            }
            
        } catch (Exception e) {
            System.out.println("⚠️  AI助手分析出现异常：" + e.getMessage());
            System.out.println("💭 您可以尝试：");
            System.out.println("   - 输入 'board' 重新查看棋盘");
            System.out.println("   - 输入 'help' 查看走法格式");
            System.out.println("   - 分析当前局面，寻找攻击或防守机会");
        }
        
        System.out.println();
    }
    
    /**
     * 分析AI建议的走法原因
     */
    private void analyzeHintReason(Move move) {
        Position start = move.getStart();
        Position end = move.getEnd();
        Piece piece = board.getPiece(start.getX(), start.getY());
        Piece targetPiece = board.getPiece(end.getX(), end.getY());
        
        System.out.println("🧠 走法分析：");
        
        if (targetPiece != null) {
            System.out.println("   ⚔️  攻击走法：吃掉对方的" + targetPiece.getChineseName());
            
            // 分析被吃棋子的价值
            String value = getPieceValue(targetPiece);
            System.out.println("   💰 棋子价值：" + value);
        } else {
            System.out.println("   🎯 位置走法：改善棋子位置");
        }
        
        // 分析战术意图
        String tacticalIntent = analyzeTacticalIntent(move, piece);
        if (!tacticalIntent.isEmpty()) {
            System.out.println("   🎪 战术意图：" + tacticalIntent);
        }
        
        // 检查是否形成威胁
        String threats = analyzeThreats(move);
        if (!threats.isEmpty()) {
            System.out.println("   ⚡ 威胁分析：" + threats);
        }
    }
    
    /**
      * 获取棋子价值描述
      */
     private String getPieceValue(Piece piece) {
         if (piece instanceof General) {
             return "无价（将军）";
         } else if (piece instanceof Chariot) {
             return "高价值（车）";
         } else if (piece instanceof Cannon) {
             return "高价值（炮）";
         } else if (piece instanceof Horse) {
             return "中等价值（马）";
         } else if (piece instanceof Advisor) {
             return "低价值（士）";
         } else if (piece instanceof Elephant) {
             return "低价值（象）";
         } else if (piece instanceof Soldier) {
              return "基础价值（兵）";
         } else {
             return "未知价值";
         }
     }
    
    /**
     * 分析战术意图
     */
    private String analyzeTacticalIntent(Move move, Piece piece) {
        Position start = move.getStart();
        Position end = move.getEnd();
        
        // 检查是否向前推进
        if (piece.getColor() == PieceColor.RED && end.getX() < start.getX()) {
            return "向前推进，增加攻击性";
        } else if (piece.getColor() == PieceColor.BLACK && end.getX() > start.getX()) {
            return "向前推进，增加攻击性";
        }
        
        // 检查是否保护重要棋子
        if (isProtectingImportantPiece(end)) {
            return "保护重要棋子";
        }
        
        // 检查是否控制关键位置
        if (isControllingKeyPosition(end)) {
            return "控制关键位置";
        }
        
        return "";
    }
    
    /**
     * 分析威胁
     */
    private String analyzeThreats(Move move) {
        // 简化的威胁分析
        Position end = move.getEnd();
        
        // 检查是否威胁对方将军
        if (isThreateningKing(end)) {
            return "威胁对方将军";
        }
        
        // 检查是否威胁重要棋子
        if (isThreateningImportantPieces(end)) {
            return "威胁对方重要棋子";
        }
        
        return "";
    }
    
    /**
     * 显示走法提示的可视化
     */
    private void displayHintVisualization(Move move) {
        System.out.println("🎯 走法可视化：");
        System.out.println("   起始位置 ◎    目标位置 ★");
        System.out.println("   一 二 三 四 五 六 七 八 九");
        
        Position start = move.getStart();
        Position end = move.getEnd();
        
        for (int row = 0; row < 10; row++) {
            System.out.printf("%2d ", 10 - row);
            for (int col = 0; col < 9; col++) {
                boolean isStartPosition = (row == start.getX() && col == start.getY());
                boolean isEndPosition = (row == end.getX() && col == end.getY());
                
                if (isStartPosition) {
                    System.out.print("◎ ");
                } else if (isEndPosition) {
                    System.out.print("★ ");
                } else {
                    Piece piece = board.getPiece(row, col);
                    if (piece == null) {
                        System.out.print(" · ");
                    } else {
                        String name = piece.getChineseName();
                        if (piece.getColor() == PieceColor.RED) {
                            System.out.print("[" + name.charAt(0) + "]");
                        } else {
                            System.out.print("(" + name.charAt(0) + ")");
                        }
                    }
                }
            }
            System.out.println();
        }
        System.out.println();
    }
    
    // 辅助方法（简化实现）
    private boolean isProtectingImportantPiece(Position pos) {
        // 简化实现：检查周围是否有重要棋子
        return false;
    }
    
    private boolean isControllingKeyPosition(Position pos) {
        // 简化实现：检查是否在关键位置（如中心、河界等）
        return pos.getX() >= 4 && pos.getX() <= 5;
    }
    
    private boolean isThreateningKing(Position pos) {
        // 简化实现：检查是否威胁对方将军
        return false;
    }
    
    private boolean isThreateningImportantPieces(Position pos) {
        // 简化实现：检查是否威胁重要棋子
        return false;
    }
    
    /**
     * 解析走法
     */
    private Move parseMove(String input, PieceColor playerColor) {
        return moveParser.parseMove(input, playerColor);
    }
    

    
    /**
     * 验证走法（返回错误信息，null表示有效）
     */
    private String validateMove(Move move, PieceColor playerColor) {
        Position start = move.getStart();
        Position end = move.getEnd();
        
        // 检查坐标是否在棋盘范围内
        if (!isValidPosition(start.getX(), start.getY())) {
            return "起始位置(" + (10 - start.getX()) + "," + (start.getY() + 1) + ")超出棋盘范围";
        }
        if (!isValidPosition(end.getX(), end.getY())) {
            return "目标位置(" + (10 - end.getX()) + "," + (end.getY() + 1) + ")超出棋盘范围";
        }
        
        // 检查起始位置是否有棋子
        Piece piece = board.getPiece(start.getX(), start.getY());
        if (piece == null) {
            return "起始位置(" + (10 - start.getX()) + "," + (start.getY() + 1) + ")没有棋子";
        }
        
        // 检查是否是己方棋子
        if (piece.getColor() != playerColor) {
            String colorName = piece.getColor() == PieceColor.RED ? "红方" : "黑方";
            return "起始位置的棋子属于" + colorName + "，不是您的棋子";
        }
        
        // 检查目标位置是否有己方棋子
        Piece targetPiece = board.getPiece(end.getX(), end.getY());
        if (targetPiece != null && targetPiece.getColor() == playerColor) {
            return "目标位置(" + (10 - end.getX()) + "," + (end.getY() + 1) + ")有您自己的棋子，不能移动到此位置";
        }
        
        // 检查走法是否符合棋子的移动规则
        if (!piece.isValidMove(board, start, end)) {
            String pieceName = piece.getChineseName();
            return pieceName + "不能从(" + (10 - start.getX()) + "," + (start.getY() + 1) + 
                   ")移动到(" + (10 - end.getX()) + "," + (end.getY() + 1) + ")，违反了" + pieceName + "的移动规则";
        }
        
        // 检查走法是否安全（不会导致己方将军被将军）
        if (!board.isMoveSafe(start, end, playerColor)) {
            return "此走法会导致己方将军被将军，属于无效走法";
        }
        
        return null; // 走法有效
    }
    
    /**
     * 验证并执行走法（保留原方法供AI使用）
     */
    private boolean validateAndExecuteMove(Move move, PieceColor playerColor) {
        Position start = move.getStart();
        Position end = move.getEnd();
        
        // 检查起始位置是否有己方棋子
        Piece piece = board.getPiece(start.getX(), start.getY());
        if (piece == null || piece.getColor() != playerColor) {
            return false;
        }
        
        // 检查走法是否合法
        if (!piece.isValidMove(board, start, end)) {
            return false;
        }
        
        // 检查走法是否安全（不会导致己方将军被将军）
        if (!board.isMoveSafe(start, end, playerColor)) {
            return false;
        }
        
        // 执行走法
        board.movePiece(start, end);
        return true;
    }
    
    /**
     * 格式化走法为可读字符串
     */
    private String formatMove(Move move) {
        Position start = move.getStart();
        Position end = move.getEnd();
        
        Piece piece = board.getPiece(end.getX(), end.getY());
        String pieceName = (piece != null) ? piece.getChineseName() : "棋子";
        
        return String.format("%s从(%d,%d)到(%d,%d)", 
            pieceName, 
            10 - start.getX(), start.getY() + 1,
            10 - end.getX(), end.getY() + 1);
    }
    
    /**
     * 获取随机合法走法（备用逻辑）
     */
    private Move getRandomValidMove(PieceColor color) {
        List<Move> validMoves = new ArrayList<>();
        
        for (int startRow = 0; startRow < 10; startRow++) {
            for (int startCol = 0; startCol < 9; startCol++) {
                Piece piece = board.getPiece(startRow, startCol);
                if (piece != null && piece.getColor() == color) {
                    Position start = new Position(startRow, startCol);
                    
                    for (int endRow = 0; endRow < 10; endRow++) {
                        for (int endCol = 0; endCol < 9; endCol++) {
                            Position end = new Position(endRow, endCol);
                            if (piece.isValidMove(board, start, end) && 
                                board.isMoveSafe(start, end, color)) {
                                validMoves.add(new Move(start, end));
                            }
                        }
                    }
                }
            }
        }
        
        if (!validMoves.isEmpty()) {
            Random random = new Random();
            return validMoves.get(random.nextInt(validMoves.size()));
        }
        
        return null;
    }
    
    /**
     * 处理游戏结束
     */
    private void handleGameEnd(GameState gameState) {
        System.out.println("\n🎊 游戏结束！");
        
        switch (gameState) {
            case RED_WINS:
                System.out.println("🔴 红方获胜！");
                break;
            case BLACK_WINS:
                System.out.println("⚫ 黑方获胜！");
                break;
            case DRAW:
                System.out.println("🤝 和棋！");
                break;
        }
        
        // 显示最终棋盘
        displayBoard();
        
        // 显示游戏统计
        System.out.println("📊 游戏统计：");
        System.out.println("总步数：" + moveHistory.size());
        System.out.println("玩家：" + (playerColor == PieceColor.RED ? "红方" : "黑方"));
        System.out.println("AI：" + currentAIType);
        
        // 询问是否再来一局
        System.out.print("\n是否再来一局？(y/n): ");
        try {
            if (scanner.hasNextLine()) {
                String response = scanner.nextLine().trim().toLowerCase();
                if (response.equals("y") || response.equals("yes")) {
                    // 清理当前AI资源
                    cleanupAI();
                    
                    // 重新开始游戏
                    board = new Board();
                    moveHistory.clear();
                    gameRunning = true;
                    setupGame();
                    gameLoop();
                } else {
                    // 用户选择不再玩，清理资源
                    cleanupAI();
                    System.out.println("👋 感谢游戏，再见！");
                }
            }
        } catch (Exception e) {
            System.out.println("输入处理异常，游戏结束。");
            cleanupAI();
        }
    }
    
    /**
     * 清理AI资源
     */
    void cleanupAI() {
        try {
            if (deepSeekPikafishAI != null) {
                System.out.println("🔧 正在关闭DeepSeek-Pikafish AI引擎...");
                deepSeekPikafishAI.shutdown();
                deepSeekPikafishAI = null;
                System.out.println("✅ DeepSeek-Pikafish AI引擎已关闭");
            }
        } catch (Exception e) {
            System.err.println("⚠️ 关闭AI引擎时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取整数输入
     */
    private int getIntInput(int min, int max) {
        while (true) {
            try {
                // 检查是否有可用输入
                if (!scanner.hasNextLine()) {
                    System.err.println("错误：无法读取输入。请确保在交互式终端中运行程序。");
                    System.exit(1);
                }
                
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.print("请输入 " + min + " 到 " + max + " 之间的数字: ");
                    continue;
                }
                
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.print("请输入 " + min + " 到 " + max + " 之间的数字: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("请输入有效的数字: ");
            } catch (Exception e) {
                System.err.println("输入错误: " + e.getMessage());
                System.err.println("请在交互式终端中运行程序。");
                System.exit(1);
            }
        }
    }
    
    /**
     * 获取难度名称
     */
    private String getDifficultyName(int difficulty) {
        switch (difficulty) {
            case 1: return "简单";
            case 2: return "普通";
            case 3: return "困难";
            case 4: return "专家";
            case 5: return "大师";
            case 6: return "特级";
            case 7: return "超级";
            case 8: return "顶级";
            case 9: return "传奇";
            case 10: return "神级";
            default: return "未知";
        }
    }
    
    /**
     * 检查位置是否有效
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 10 && col >= 0 && col < 9;
    }
    
    /**
     * 播放地震动画效果
     * 当发生吃子时，让被吃的棋子和周围的棋子都"跳起来"
     */
    private void playEarthquakeAnimation(Position capturePosition) {
        try {
            System.out.println("\n🌊🌊🌊 地震动画开始！🌊🌊🌊");
            System.out.println("💥 位置: (" + (capturePosition.getX() + 1) + "," + (capturePosition.getY() + 1) + ")");
            
            // 获取受影响的位置（被吃棋子位置及其周围一圈）
            List<Position> affectedPositions = getAffectedPositions(capturePosition);
            System.out.println("🎯 影响范围: " + affectedPositions.size() + " 个位置");
            
            // 播放3帧动画，每帧持续更长时间
            for (int frame = 0; frame < 3; frame++) {
                System.out.println("\n═══════════════════════════════════════");
                System.out.println("🎬 第 " + (frame + 1) + " 帧动画");
                System.out.println("═══════════════════════════════════════");
                
                // 显示动画帧
                displayBoardWithAnimation(affectedPositions, frame);
                
                // 等待更长时间，让用户能看清动画
                Thread.sleep(800);
            }
            
            System.out.println("\n✨✨✨ 地震动画结束！✨✨✨");
            System.out.println("按回车键继续...");
            
            // 等待用户按键
            try {
                System.in.read();
            } catch (Exception e) {
                // 忽略读取异常
            }
            
            // 最后显示正常棋盘
            System.out.println("\n" + repeatString("=", 50));
            displayBoard();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("❌ 动画被中断");
        }
    }
    
    /**
     * 重复字符串指定次数
     */
    private String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * 获取受地震影响的位置（被吃棋子位置及其周围一圈）
     */
    private List<Position> getAffectedPositions(Position center) {
        List<Position> positions = new ArrayList<>();
        
        // 添加中心位置
        positions.add(center);
        
        // 添加周围8个方向的位置
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        
        for (int i = 0; i < 8; i++) {
            int newX = center.getX() + dx[i];
            int newY = center.getY() + dy[i];
            
            if (isValidPosition(newX, newY)) {
                positions.add(new Position(newX, newY));
            }
        }
        
        return positions;
    }
    
    /**
     * 显示带动画效果的棋盘
     */
    private void displayBoardWithAnimation(List<Position> affectedPositions, int frame) {
        // 动画效果标题
        String[] frameTitles = {
            "🚀 第1帧：棋子向上跳跃！",
            "💥 第2帧：棋子向下震动！", 
            "🌊 第3帧：棋子左右摇摆！"
        };
        
        System.out.println(frameTitles[frame]);
        System.out.println("  1 2 3 4 5 6 7 8 9");
        
        for (int row = 0; row < 10; row++) {
            System.out.print((10 - row) + " ");
            
            for (int col = 0; col < 9; col++) {
                Position currentPos = new Position(row, col);
                Piece piece = board.getPiece(row, col);
                
                // 检查当前位置是否受地震影响
                boolean isAffected = affectedPositions.contains(currentPos);
                
                if (piece == null) {
                    // 空位置
                    if (isAffected) {
                        // 地震效果：空位置也有动画
                        switch (frame) {
                            case 0:
                                System.out.print("⬆"); // 向上箭头
                                break;
                            case 1:
                                System.out.print("⬇"); // 向下箭头
                                break;
                            case 2:
                                System.out.print("〰"); // 波浪线
                                break;
                            default:
                                System.out.print("十");
                        }
                    } else {
                        System.out.print("十");
                    }
                } else {
                    // 有棋子的位置
                    String pieceSymbol = getPieceSymbol(piece);
                    
                    if (isAffected) {
                        // 地震效果：棋子跳跃，使用更明显的符号
                        switch (frame) {
                            case 0:
                                System.out.print("🔺" + pieceSymbol); // 向上跳 - 红色三角
                                break;
                            case 1:
                                System.out.print("🔻" + pieceSymbol); // 向下落 - 蓝色三角
                                break;
                            case 2:
                                System.out.print("💫" + pieceSymbol); // 摇摆 - 星星效果
                                break;
                            default:
                                System.out.print(pieceSymbol);
                        }
                    } else {
                        System.out.print("  " + pieceSymbol); // 正常棋子，添加空格对齐
                    }
                }
                
                if (col < 8) {
                    System.out.print(" ");
                }
            }
            
            System.out.println();
            
            // 在第5行后添加楚河汉界
            if (row == 4) {
                if (frame == 2) {
                    System.out.println("  〰〰〰楚河汉界〰〰〰"); // 摇摆效果
                } else {
                    System.out.println("  ～～～楚河汉界～～～");
                }
            }
        }
        
        // 显示动画提示和效果说明
        String[] animationTexts = {
            "💥 地震开始！所有棋子都在向上跳跃！🚀",
            "🌊 震动继续！棋子们正在剧烈震动！💥", 
            "✨ 余震摇摆！棋子们在左右摇摆！🌊"
        };
        
        if (frame < animationTexts.length) {
            System.out.println("\n" + repeatString("🎭", 20));
            System.out.println(animationTexts[frame]);
            System.out.println(repeatString("🎭", 20));
        }
        
        // 显示受影响的位置信息
        System.out.println("\n🎯 受地震影响的位置：");
        for (Position pos : affectedPositions) {
            Piece p = board.getPiece(pos.getX(), pos.getY());
            String desc = p != null ? getPieceSymbol(p) : "空位";
            System.out.print("(" + (pos.getX() + 1) + "," + (pos.getY() + 1) + ":" + desc + ") ");
        }
        System.out.println();
    }
    
    /**
     * 清屏方法
     */
    private void clearScreen() {
        try {
            // 尝试使用ANSI转义序列清屏
            System.out.print("\033[2J\033[H");
            System.out.flush();
        } catch (Exception e) {
            // 如果清屏失败，打印一些空行
            for (int i = 0; i < 20; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * 获取棋子符号
     */
    private String getPieceSymbol(Piece piece) {
        String name = piece.getChineseName();
        boolean isRed = piece.getColor() == PieceColor.RED;
        
        // 根据棋子类型和颜色返回符号
        switch (name) {
            case "帅": return isRed ? "帅" : "将";
            case "将": return isRed ? "帅" : "将";
            case "车": return isRed ? "車" : "车";
            case "马": return isRed ? "馬" : "马";
            case "炮": return isRed ? "炮" : "砲";
            case "砲": return isRed ? "炮" : "砲";
            case "相": return isRed ? "相" : "象";
            case "象": return isRed ? "相" : "象";
            case "仕": return isRed ? "仕" : "士";
            case "士": return isRed ? "仕" : "士";
            case "兵": return isRed ? "兵" : "卒";
            case "卒": return isRed ? "兵" : "卒";
            default: return name;
        }
    }
    
    /**
     * 主函数
     */
    public static void main(String[] args) {
        TerminalChessGame game = new TerminalChessGame();
        
        // 添加shutdown hook，确保程序异常退出时也能清理资源
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🔧 程序正在退出，清理资源中...");
            game.cleanupAI();
            System.out.println("✅ 资源清理完成，再见！");
        }));
        
        try {
            game.start();
        } catch (Exception e) {
            System.err.println("❌ 游戏运行时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 确保资源被清理
            game.cleanupAI();
        }
    }
}