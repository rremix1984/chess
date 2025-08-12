package com.example.internationalchess.ui;

import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.core.GameState;
import com.example.internationalchess.core.Position;
import com.example.internationalchess.core.Move;
import com.example.internationalchess.ai.InternationalChessAI;
import com.example.internationalchess.ai.InternationalLLMChessAI;
import com.example.internationalchess.ai.InternationalHybridChessAI;
import com.example.internationalchess.ai.DeepSeekPikafishAI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.MultipleGradientPaint;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import javax.swing.Timer;
import com.example.sound.SoundPlayer;

/**
 * 国际象棋棋盘面板
 */
public class InternationalBoardPanel extends JPanel {

    // 棋盘常量
    private static final int CELL_SIZE = 75; // 每个格子的大小
    private static final int MARGIN = 40; // 边距
    private static final int BOARD_SIZE = 8; // 国际象棋是8x8的棋盘
    
    // 棋子图片
    private Image whitePawnImage, whiteRookImage, whiteKnightImage, whiteBishopImage, whiteQueenImage, whiteKingImage;
    private Image blackPawnImage, blackRookImage, blackKnightImage, blackBishopImage, blackQueenImage, blackKingImage;
    
    // 棋盘状态
    private InternationalChessBoard chessBoard; // 国际象棋棋盘
    private boolean isAIEnabled = false; // 是否启用AI
    private boolean isAIThinking = false; // AI是否正在思考
    
    // AI相关
    private InternationalChessAI traditionalAI;
    private InternationalLLMChessAI llmAI;
    private InternationalHybridChessAI hybridAI;
    private DeepSeekPikafishAI deepSeekPikafishAI;
    private boolean useLLM = false;
    private boolean useHybrid = false;
    private boolean useDeepSeekPikafish = false;
    private char humanPlayer = InternationalChessBoard.WHITE; // 默认人类执白棋
    
    // 选中状态
    private int selectedRow = -1;
    private int selectedCol = -1;
    
    // 聊天面板引用
    private ChatPanel chatPanel;
    
    // AI日志面板引用
    private AILogPanel aiLogPanel;
    
    // 状态更新回调
    private Consumer<String> statusUpdateCallback;
    
    // 移动历史记录（用于悔棋功能）
    private java.util.List<String> moveHistory = new java.util.ArrayList<>();
    
    public InternationalBoardPanel() {
        setPreferredSize(new Dimension(
                MARGIN * 2 + CELL_SIZE * BOARD_SIZE,
                MARGIN * 2 + CELL_SIZE * BOARD_SIZE));
        
        // 初始化棋盘
        chessBoard = new InternationalChessBoard();
        
        // 加载棋子图片
        loadPieceImages();
        
        // 初始化移动历史
        moveHistory = new java.util.ArrayList<>();
        
        // 添加鼠标事件监听
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }
    
    /**
     * 重置棋盘
     */
    public void resetBoard() {
        chessBoard = new InternationalChessBoard();
        selectedRow = -1;
        selectedCol = -1;
        isAIThinking = false;
        moveHistory.clear(); // 清空移动历史
        
        if (statusUpdateCallback != null) {
            updateStatus();
        }
        
        repaint();
        
        // 如果启用了AI且当前是AI回合，触发AI移动
        if (isAIEnabled && isAITurn()) {
            SwingUtilities.invokeLater(this::performAIMove);
        }
    }
    
    /**
     * 加载棋子图片
     */
    private void loadPieceImages() {
        // 这里应该加载实际的棋子图片
        // 由于没有实际的图片资源，我们将在绘制时使用简单的形状和文字代替
    }
    
    /**
     * 处理鼠标点击事件
     */
    private void handleMouseClick(int x, int y) {
        // 如果AI正在思考或游戏已结束，不处理点击事件
        if (isAIThinking || chessBoard.getGameState() != GameState.PLAYING) {
            return;
        }
        
        // 如果启用了AI且当前是AI回合，不处理点击事件
        if (isAIEnabled && isAITurn()) {
            return;
        }
        
        // 将像素坐标转换为棋盘坐标
        int col = (x - MARGIN) / CELL_SIZE;
        int row = (y - MARGIN) / CELL_SIZE;
        
        // 检查坐标是否在棋盘范围内
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return;
        }
        
        // 如果之前没有选中棋子，且点击的位置有己方棋子，则选中该棋子
        if (selectedRow == -1 && selectedCol == -1) {
            String piece = chessBoard.getPiece(row, col);
            if (piece != null) {
                char color = piece.charAt(0);
                boolean isWhiteTurn = chessBoard.isWhiteTurn();
                if ((isWhiteTurn && color == InternationalChessBoard.WHITE) || 
                    (!isWhiteTurn && color == InternationalChessBoard.BLACK)) {
                    selectedRow = row;
                    selectedCol = col;
                    repaint();
                }
            }
        } 
        // 如果已经选中了棋子，则尝试移动
        else {
            // 如果点击的是同一个位置，取消选择
            if (selectedRow == row && selectedCol == col) {
                selectedRow = -1;
                selectedCol = -1;
                repaint();
                return;
            }
            
            // 检查是否是兵的升变移动
            String piece = chessBoard.getPiece(selectedRow, selectedCol);
            boolean isPawnPromotion = false;
            char promotionPiece = InternationalChessBoard.QUEEN; // 默认升变为后
            
            if (piece != null && piece.charAt(1) == InternationalChessBoard.PAWN) {
                char pieceColor = piece.charAt(0);
                int promotionRow = (pieceColor == InternationalChessBoard.WHITE) ? 0 : 7;
                if (row == promotionRow && chessBoard.isValidMove(selectedRow, selectedCol, row, col)) {
                    isPawnPromotion = true;
                    // 显示升变选择对话框
                    promotionPiece = showPromotionDialog();
                    if (promotionPiece == 0) { // 用户取消了选择
                        return;
                    }
                }
            }
            
            // 尝试移动棋子
            boolean moveSuccess;
            if (isPawnPromotion) {
                moveSuccess = chessBoard.movePiece(selectedRow, selectedCol, row, col, promotionPiece);
            } else {
                moveSuccess = chessBoard.movePiece(selectedRow, selectedCol, row, col);
            }
            
            if (moveSuccess) {
                // 记录移动历史
                recordMove(selectedRow, selectedCol, row, col);
                
                // 播放落子音效
                SoundPlayer.getInstance().playSound("piece_drop");
                
                // 移动成功，重置选择
                selectedRow = -1;
                selectedCol = -1;
                
                // 更新状态
                updateStatus();
                
                // 重绘棋盘
                repaint();
                
                // 如果游戏结束，显示结束对话框
                GameState gameState = chessBoard.getGameState();
                if (gameState != GameState.PLAYING) {
                    showGameEndDialog(gameState);
                }
                // 如果游戏未结束且启用了AI且现在是AI回合，触发AI移动
                else if (isAIEnabled && isAITurn()) {
                    SwingUtilities.invokeLater(this::performAIMove);
                }
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制棋盘
        drawBoard(g2d);
        
        // 绘制选中效果
        if (selectedRow != -1 && selectedCol != -1) {
            drawSelection(g2d, selectedRow, selectedCol);
        }
        
        // 绘制棋子
        drawPieces(g2d);
    }
    
    /**
     * 绘制棋盘
     */
    private void drawBoard(Graphics2D g) {
        // 绘制棋盘背景
        g.setColor(new Color(240, 217, 181)); // 浅棕色
        g.fillRect(MARGIN, MARGIN, CELL_SIZE * BOARD_SIZE, CELL_SIZE * BOARD_SIZE);
        
        // 绘制棋盘格子
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // 黑白相间的格子
                if ((row + col) % 2 == 1) {
                    g.setColor(new Color(181, 136, 99)); // 深棕色
                    g.fillRect(
                            MARGIN + col * CELL_SIZE,
                            MARGIN + row * CELL_SIZE,
                            CELL_SIZE,
                            CELL_SIZE);
                }
            }
        }
        
        // 绘制坐标标签
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // 列标签 (a-h)
        for (int col = 0; col < BOARD_SIZE; col++) {
            String label = String.valueOf((char)('a' + col));
            g.drawString(label, MARGIN + col * CELL_SIZE + CELL_SIZE/2 - 5, MARGIN + BOARD_SIZE * CELL_SIZE + 20);
        }
        
        // 行标签 (1-8，从下到上)
        for (int row = 0; row < BOARD_SIZE; row++) {
            String label = String.valueOf(BOARD_SIZE - row);
            g.drawString(label, MARGIN - 15, MARGIN + row * CELL_SIZE + CELL_SIZE/2 + 5);
        }
    }
    
    /**
     * 绘制选中效果
     */
    private void drawSelection(Graphics2D g, int row, int col) {
        int centerX = MARGIN + col * CELL_SIZE + CELL_SIZE / 2;
        int centerY = MARGIN + row * CELL_SIZE + CELL_SIZE / 2;
        
        // 绘制3D选中效果
        draw3DSelectionEffect(g, centerX, centerY);
    }
    
    /**
     * 绘制3D选中效果
     */
    private void draw3DSelectionEffect(Graphics2D g, int centerX, int centerY) {
        int baseSize = (int)(CELL_SIZE * 1.2);
        
        // 绘制外层光环
        drawSelectionGlow(g, centerX, centerY, baseSize + 20);
        
        // 绘制中层光环
        drawSelectionGlow(g, centerX, centerY, baseSize + 10);
        
        // 绘制内层边框
        drawSelectionBorder(g, centerX, centerY, baseSize);
    }
    
    /**
     * 绘制选中光环
     */
    private void drawSelectionGlow(Graphics2D g, int centerX, int centerY, int size) {
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
        
        g.setPaint(glowGradient);
        g.fillOval(centerX - size / 2, centerY - size / 2, size, size);
    }
    
    /**
     * 绘制选中边框
     */
    private void drawSelectionBorder(Graphics2D g, int centerX, int centerY, int size) {
        // 外边框
        g.setStroke(new BasicStroke(4));
        g.setColor(new Color(255, 215, 0)); // 金色
        g.drawOval(centerX - size / 2, centerY - size / 2, size, size);
        
        // 内边框
        g.setStroke(new BasicStroke(2));
        g.setColor(new Color(255, 255, 255, 200)); // 白色高光
        g.drawOval(centerX - size / 2 + 3, centerY - size / 2 + 3, size - 6, size - 6);
        
        // 最内层边框
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(255, 215, 0, 150));
        g.drawOval(centerX - size / 2 + 6, centerY - size / 2 + 6, size - 12, size - 12);
    }
    
    /**
     * 绘制棋子
     */
    private void drawPieces(Graphics2D g) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String piece = chessBoard.getPiece(row, col);
                if (piece != null) {
                    drawPiece(g, piece, row, col);
                }
            }
        }
    }
    
    /**
     * 绘制单个棋子
     */
    private void drawPiece(Graphics2D g, String piece, int row, int col) {
        int x = MARGIN + col * CELL_SIZE;
        int y = MARGIN + row * CELL_SIZE;
        int size = (int)(CELL_SIZE * 0.9); // 增大棋子尺寸
        int xCenter = x + CELL_SIZE/2;
        int yCenter = y + CELL_SIZE/2;
        
        char color = piece.charAt(0);
        char type = piece.charAt(1);
        
        // 启用抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制棋子阴影（在棋子下方）
        drawPieceShadow(g, xCenter, yCenter + 3, size);
        
        // 绘制棋子主体
        drawPieceBody(g, color, xCenter, yCenter, size);
        
        // 绘制棋子边框
        drawPieceBorder(g, color, xCenter, yCenter, size);
        
        // 绘制棋子高光
        drawPieceHighlight(g, xCenter, yCenter, size);
        
        // 绘制棋子类型标识
        drawPieceSymbol(g, color, type, xCenter, yCenter, size);
    }
    
    /**
     * 绘制棋子阴影
     */
    private void drawPieceShadow(Graphics2D g, int centerX, int centerY, int size) {
        int shadowOffset = 6; // 增加阴影偏移
        int shadowSize = size + 8; // 增加阴影大小
        
        // 创建更强烈的阴影渐变
        RadialGradientPaint shadowGradient = new RadialGradientPaint(
            centerX + shadowOffset, centerY + shadowOffset, shadowSize,
            new float[]{0.0f, 0.3f, 0.7f, 1.0f},
            new Color[]{
                new Color(0, 0, 0, 130), // 增加阴影不透明度
                new Color(0, 0, 0, 90),
                new Color(0, 0, 0, 40),
                new Color(0, 0, 0, 0)
            }
        );
        
        g.setPaint(shadowGradient);
        g.fillOval(centerX - shadowSize / 2 + shadowOffset, 
                  centerY - shadowSize / 2 + shadowOffset, 
                  shadowSize, shadowSize);
        
        // 添加地面接触阴影
        int contactShadowSize = size + 14; // 增加接触阴影大小
        int contactShadowHeight = contactShadowSize / 3; // 调整高度比例
        
        // 创建椭圆形阴影（模拟地面接触）
        g.setPaint(new Color(0, 0, 0, 50)); // 增加不透明度
        g.fillOval(centerX - contactShadowSize / 2, 
                  centerY + size / 2 - contactShadowHeight / 2, 
                  contactShadowSize, contactShadowHeight);
        
        // 添加额外的柔和阴影层，增强立体感
        int softShadowSize = size + 20;
        g.setPaint(new Color(0, 0, 0, 15));
        g.fillOval(centerX - softShadowSize / 2 + shadowOffset/2, 
                  centerY - softShadowSize / 2 + shadowOffset/2, 
                  softShadowSize, softShadowSize);
    }
    
    /**
     * 绘制棋子主体
     */
    private void drawPieceBody(Graphics2D g, char color, int centerX, int centerY, int size) {
        Color baseColor;
        Color lightColor;
        Color darkColor;
        Color midColor;
        Color edgeColor; // 新增边缘颜色
        
        if (color == 'W') {
            baseColor = new Color(240, 240, 240);
            lightColor = new Color(255, 255, 255);
            midColor = new Color(230, 230, 230);
            darkColor = new Color(180, 180, 180);
            edgeColor = new Color(160, 160, 160); // 白棋边缘颜色
        } else {
            baseColor = new Color(40, 40, 40);
            lightColor = new Color(120, 120, 120);
            midColor = new Color(70, 70, 70);
            darkColor = new Color(0, 0, 0);
            edgeColor = new Color(10, 10, 10); // 黑棋边缘颜色
        }
        
        // 绘制棋子底座（略大于主体，增加立体感）
        int baseSize = (int)(size * 1.05);
        g.setColor(edgeColor);
        g.fillOval(centerX - baseSize / 2, centerY - baseSize / 2 + size/10, baseSize, baseSize/2);
        
        // 创建更复杂的球形渐变效果
        Point2D center = new Point2D.Float(centerX, centerY);
        Point2D focus = new Point2D.Float(centerX - size / 4, centerY - size / 4); // 调整焦点位置
        
        RadialGradientPaint bodyGradient = new RadialGradientPaint(
            focus, size, center,
            new float[]{0.0f, 0.2f, 0.5f, 0.8f, 1.0f}, // 增加渐变点
            new Color[]{lightColor, midColor, baseColor, darkColor, edgeColor},
            MultipleGradientPaint.CycleMethod.NO_CYCLE
        );
        
        g.setPaint(bodyGradient);
        g.fillOval(centerX - size / 2, centerY - size / 2, size, size);
        
        // 添加额外的高光效果
        int smallHighlightSize = size / 5; // 增大高光
        int smallHighlightX = centerX - size / 6;
        int smallHighlightY = centerY - size / 6;
        
        RadialGradientPaint smallHighlight = new RadialGradientPaint(
            smallHighlightX, smallHighlightY, smallHighlightSize,
            new float[]{0.0f, 0.5f, 1.0f}, // 增加渐变点
            new Color[]{
                new Color(255, 255, 255, 230), 
                new Color(255, 255, 255, 150), 
                new Color(255, 255, 255, 0)
            }
        );
        
        g.setPaint(smallHighlight);
        g.fillOval(smallHighlightX - smallHighlightSize / 2, smallHighlightY - smallHighlightSize / 2, 
                  smallHighlightSize, smallHighlightSize);
        
        // 添加底部阴影效果，增强立体感
        int bottomShadowSize = size / 3; // 增大底部阴影
        int bottomShadowX = centerX + size / 8;
        int bottomShadowY = centerY + size / 8;
        
        RadialGradientPaint bottomShadow = new RadialGradientPaint(
            bottomShadowX, bottomShadowY, bottomShadowSize,
            new float[]{0.0f, 0.5f, 1.0f},
            new Color[]{
                new Color(0, 0, 0, color == 'W' ? 70 : 40), 
                new Color(0, 0, 0, color == 'W' ? 40 : 20), 
                new Color(0, 0, 0, 0)
            }
        );
        
        g.setPaint(bottomShadow);
        g.fillOval(bottomShadowX - bottomShadowSize / 2, bottomShadowY - bottomShadowSize / 2, 
                  bottomShadowSize, bottomShadowSize);
        
        // 添加侧面高光，增强立体感
        int sideHighlightSize = size / 6;
        int sideHighlightX = centerX + size / 5;
        int sideHighlightY = centerY - size / 7;
        
        RadialGradientPaint sideHighlight = new RadialGradientPaint(
            sideHighlightX, sideHighlightY, sideHighlightSize,
            new float[]{0.0f, 1.0f},
            new Color[]{
                new Color(255, 255, 255, 100), 
                new Color(255, 255, 255, 0)
            }
        );
        
        g.setPaint(sideHighlight);
        g.fillOval(sideHighlightX - sideHighlightSize / 2, sideHighlightY - sideHighlightSize / 2, 
                  sideHighlightSize, sideHighlightSize);
    }
    
    /**
     * 绘制棋子边框
     */
    private void drawPieceBorder(Graphics2D g, char color, int centerX, int centerY, int size) {
        // 外边框
        g.setStroke(new BasicStroke(3.0f)); // 增加边框粗细
        
        Color borderColor;
        Color innerBorderColor;
        Color outerGlowColor; // 新增外发光颜色
        
        if (color == 'W') {
            borderColor = new Color(80, 80, 80); // 调整边框颜色
            innerBorderColor = new Color(140, 140, 140);
            outerGlowColor = new Color(200, 200, 200);
        } else {
            borderColor = new Color(10, 10, 10);
            innerBorderColor = new Color(60, 60, 60);
            outerGlowColor = new Color(100, 100, 100);
        }
        
        // 绘制外发光效果
        g.setStroke(new BasicStroke(1.0f));
        g.setColor(new Color(outerGlowColor.getRed(), outerGlowColor.getGreen(), outerGlowColor.getBlue(), 40));
        g.drawOval(centerX - size / 2 - 2, centerY - size / 2 - 2, size + 4, size + 4);
        
        // 绘制外边框
        g.setStroke(new BasicStroke(2.5f));
        g.setColor(borderColor);
        g.drawOval(centerX - size / 2, centerY - size / 2, size, size);
        
        // 绘制内边框（增加深度感）
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(innerBorderColor);
        g.drawOval(centerX - size / 2 + 4, centerY - size / 2 + 4, size - 8, size - 8); // 调整内边框位置
        
        // 绘制高光边框（顶部）
        g.setStroke(new BasicStroke(1.2f)); // 增加高光边框粗细
        g.setColor(new Color(255, 255, 255, 150)); // 增加高光不透明度
        
        // 只绘制顶部四分之一圆弧（模拟光照）
        int arcStart = 45; // 开始角度
        int arcExtent = 120; // 增加覆盖角度
        g.drawArc(centerX - size / 2 + 1, centerY - size / 2 + 1, size - 2, size - 2, arcStart, arcExtent);
        
        // 绘制底部阴影边框
        g.setStroke(new BasicStroke(1.0f));
        g.setColor(new Color(0, 0, 0, 80));
        int shadowArcStart = 225; // 底部阴影开始角度
        int shadowArcExtent = 90; // 底部阴影覆盖角度
        g.drawArc(centerX - size / 2 + 1, centerY - size / 2 + 1, size - 2, size - 2, shadowArcStart, shadowArcExtent);
    }
    
    /**
     * 绘制棋子高光
     */
    private void drawPieceHighlight(Graphics2D g, int centerX, int centerY, int size) {
        // 主高光 - 增强主高光效果
        int highlightSize = size / 3;
        int highlightX = centerX - size / 4;
        int highlightY = centerY - size / 4;
        
        // 创建主高光渐变 - 增加亮度和对比度
        RadialGradientPaint highlightGradient = new RadialGradientPaint(
            highlightX, highlightY, highlightSize,
            new float[]{0.0f, 0.3f, 0.6f, 1.0f}, // 增加渐变点
            new Color[]{
                new Color(255, 255, 255, 255), // 完全不透明的中心
                new Color(255, 255, 255, 200),
                new Color(255, 255, 255, 100),
                new Color(255, 255, 255, 0)
            }
        );
        
        g.setPaint(highlightGradient);
        g.fillOval(highlightX - highlightSize / 2, highlightY - highlightSize / 2, 
                  highlightSize, highlightSize);
        
        // 次高光 - 调整位置和大小
        int secondaryHighlightSize = size / 4; // 增大次高光
        int secondaryHighlightX = centerX + size / 5;
        int secondaryHighlightY = centerY + size / 5;
        
        // 创建次高光渐变 - 增加层次感
        RadialGradientPaint secondaryHighlightGradient = new RadialGradientPaint(
            secondaryHighlightX, secondaryHighlightY, secondaryHighlightSize,
            new float[]{0.0f, 0.5f, 1.0f}, // 增加渐变点
            new Color[]{
                new Color(255, 255, 255, 150),
                new Color(255, 255, 255, 80),
                new Color(255, 255, 255, 0)
            }
        );
        
        g.setPaint(secondaryHighlightGradient);
        g.fillOval(secondaryHighlightX - secondaryHighlightSize / 2, secondaryHighlightY - secondaryHighlightSize / 2, 
                  secondaryHighlightSize, secondaryHighlightSize);
        
        // 添加边缘高光 - 增强立体感
        int edgeHighlightSize = size / 2;
        int edgeHighlightX = centerX;
        int edgeHighlightY = centerY - size / 3;
        
        // 创建边缘高光渐变
        RadialGradientPaint edgeHighlightGradient = new RadialGradientPaint(
            edgeHighlightX, edgeHighlightY, edgeHighlightSize,
            new float[]{0.7f, 0.85f, 1.0f}, // 只在边缘附近渐变
            new Color[]{
                new Color(255, 255, 255, 0),
                new Color(255, 255, 255, 60),
                new Color(255, 255, 255, 0)
            }
        );
        
        g.setPaint(edgeHighlightGradient);
        g.fillOval(edgeHighlightX - edgeHighlightSize / 2, edgeHighlightY - edgeHighlightSize / 2, 
                  edgeHighlightSize, edgeHighlightSize);
        
        // 添加微小的焦点高光 - 模拟反射点
        int microHighlightSize = size / 10;
        int microHighlightX = centerX - size / 6;
        int microHighlightY = centerY - size / 5;
        
        g.setColor(new Color(255, 255, 255, 230));
        g.fillOval(microHighlightX - microHighlightSize / 2, microHighlightY - microHighlightSize / 2, 
                  microHighlightSize, microHighlightSize);
    }
    
    /**
     * 绘制棋子符号
     */
    private void drawPieceSymbol(Graphics2D g, char color, char type, int centerX, int centerY, int size) {
        String symbol = getPieceSymbol(color, type);
        
        // 使用更大的字体
        Font font = new Font("Serif", Font.BOLD, (int)(size * 0.7));
        g.setFont(font);
        
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(symbol);
        int textHeight = fm.getAscent();
        
        // 设置文字颜色 - 增强对比度
        Color textColor;
        Color shadowColor;
        
        if (color == 'W') {
            textColor = new Color(40, 40, 40); // 深灰色，更好的对比度
            shadowColor = new Color(255, 255, 255, 120); // 白色阴影
        } else {
            textColor = new Color(240, 240, 240); // 浅灰色，更好的对比度
            shadowColor = new Color(0, 0, 0, 150); // 黑色阴影
        }
        
        // 绘制文字外发光效果
        g.setColor(shadowColor);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(symbol, centerX - textWidth/2 + dx, centerY + textHeight/2 + dy);
                }
            }
        }
        
        // 绘制主文字
        g.setColor(textColor);
        g.drawString(symbol, centerX - textWidth/2, centerY + textHeight/2);
        
        // 添加高光效果
        g.setColor(new Color(255, 255, 255, 80));
        g.drawString(symbol, centerX - textWidth/2 - 1, centerY + textHeight/2 - 1);
    }
    
    /**
     * 获取棋子符号（根据颜色返回不同的Unicode符号）
     */
    private String getPieceSymbol(char color, char type) {
        if (color == 'W') {
            // 白色棋子使用空心符号
            switch (type) {
                case 'K': return "♔"; // 白王
                case 'Q': return "♕"; // 白后
                case 'R': return "♖"; // 白车
                case 'B': return "♗"; // 白象
                case 'N': return "♘"; // 白马
                case 'P': return "♙"; // 白兵
                default: return "?";
            }
        } else {
            // 黑色棋子使用实心符号
            switch (type) {
                case 'K': return "♚"; // 黑王
                case 'Q': return "♛"; // 黑后
                case 'R': return "♜"; // 黑车
                case 'B': return "♝"; // 黑象
                case 'N': return "♞"; // 黑马
                case 'P': return "♟"; // 黑兵
                default: return "?";
            }
        }
    }
    
    /**
     * 设置聊天面板
     */
    public void setChatPanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }
    
    /**
     * 设置状态更新回调
     */
    public void setStatusUpdateCallback(Consumer<String> callback) {
        this.statusUpdateCallback = callback;
        updateStatus(); // 初始化状态显示
    }
    
    /**
     * 更新状态显示
     */
    private void updateStatus() {
        if (statusUpdateCallback != null) {
            boolean isWhiteTurn = chessBoard.isWhiteTurn();
            String playerName = isWhiteTurn ? "⚪ 白方" : "⚫ 黑方";
            String status = "当前玩家: " + playerName;
            
            if (isAIEnabled) {
                if (isAITurn()) {
                    String aiType = "";
                    if (useHybrid) {
                        aiType = "混合AI";
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
    
    /**
     * 检查当前是否是AI回合
     */
    private boolean isAITurn() {
        boolean isWhiteTurn = chessBoard.isWhiteTurn();
        return isAIEnabled && ((isWhiteTurn && humanPlayer == InternationalChessBoard.BLACK) || 
                              (!isWhiteTurn && humanPlayer == InternationalChessBoard.WHITE));
    }
    
    /**
     * 执行AI移动
     */
    private void performAIMove() {
        if (!isAIEnabled || !isAITurn() || isAIThinking) {
            return;
        }
        
        isAIThinking = true;
        updateStatus();
        
        // 根据AI类型执行不同的移动计算
        if (useDeepSeekPikafish && deepSeekPikafishAI != null) {
            // DeepSeekPikafishAI是同步的，在新线程中执行以避免阻塞UI
            new Thread(() -> {
                try {
                    Move move = deepSeekPikafishAI.getBestMove(chessBoard);
                    SwingUtilities.invokeLater(() -> {
                        if (move != null) {
                            // 将Move对象转换为int数组格式
                            Position start = move.getStart();
                            Position end = move.getEnd();
                            int[] moveArray = {start.getX(), start.getY(), end.getX(), end.getY()};
                            applyAIMove(moveArray);
                        } else {
                            isAIThinking = false;
                            updateStatus();
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        System.err.println("DeepSeek+Pikafish AI计算失败: " + e.getMessage());
                        isAIThinking = false;
                        updateStatus();
                    });
                }
            }).start();
        } else if (useHybrid && hybridAI != null) {
            hybridAI.calculateNextMoveAsync(chessBoard).thenAccept(this::applyAIMove);
        } else if (useLLM && llmAI != null) {
            llmAI.calculateNextMoveAsync(chessBoard).thenAccept(this::applyAIMove);
        } else if (traditionalAI != null) {
            // 传统AI是同步的，但我们在新线程中执行以避免阻塞UI
            new Thread(() -> {
                int[] move = traditionalAI.calculateNextMove(chessBoard);
                SwingUtilities.invokeLater(() -> applyAIMove(move));
            }).start();
        }
    }
    
    /**
     * 应用AI的移动
     */
    private void applyAIMove(int[] move) {
        if (move != null && move.length == 4) {
            // 记录AI移动历史
            recordMove(move[0], move[1], move[2], move[3]);
            
            // 执行移动
            chessBoard.movePiece(move[0], move[1], move[2], move[3]);
            
            // 播放落子音效
            SoundPlayer.getInstance().playSound("piece_drop");
            
            // 更新状态
            isAIThinking = false;
            updateStatus();
            
            // 重绘棋盘
            repaint();
            
            // 检查游戏是否结束
            GameState gameState = chessBoard.getGameState();
            if (gameState != GameState.PLAYING) {
                showGameEndDialog(gameState);
            }
        } else {
            // 移动无效
            isAIThinking = false;
            updateStatus();
        }
    }
    
    /**
     * 显示游戏结束对话框
     */
    private void showGameEndDialog(GameState gameState) {
        String message = "";
        if (gameState == GameState.RED_WINS) {
            SoundPlayer.getInstance().playSound("game_win");
            message = "白方获胜！";
        } else if (gameState == GameState.BLACK_WINS) {
            SoundPlayer.getInstance().playSound("game_win");
            message = "黑方获胜！";
        } else if (gameState == GameState.DRAW) {
            message = "和棋！";
        } else {
            return; // 如果游戏未结束，不显示对话框
        }
        
        // 播放胜利动画
        showVictoryAnimation(message);
        
        // 延迟显示对话框，让用户欣赏动画
        final String finalMessage = message;
        Timer dialogTimer = new Timer(3000, e -> {
            JOptionPane.showMessageDialog(this, finalMessage, "游戏结束", JOptionPane.INFORMATION_MESSAGE);
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
            if (message.contains("白方")) {
                winnerColor = Color.WHITE;
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
     * 设置AI启用状态
     */
    public void setAIEnabled(boolean enabled) {
        this.isAIEnabled = enabled;
        updateStatus();
        
        // 如果启用AI且当前是AI回合，触发AI移动
        if (enabled && isAITurn()) {
            SwingUtilities.invokeLater(this::performAIMove);
        }
    }
    
    /**
     * 设置人类玩家颜色
     */
    public void setHumanPlayer(char color) {
        this.humanPlayer = color;
        updateStatus();
        
        // 如果启用AI且当前是AI回合，触发AI移动
        if (isAIEnabled && isAITurn()) {
            SwingUtilities.invokeLater(this::performAIMove);
        }
    }
    
    /**
     * 设置AI类型
     */
    public void setAIType(String type, int difficulty, String model) {
        // 重置AI类型标志
        useLLM = false;
        useHybrid = false;
        useDeepSeekPikafish = false;
        
        // 根据类型初始化相应的AI
        switch (type) {
            case "传统AI":
                traditionalAI = new InternationalChessAI(difficulty, humanPlayer == InternationalChessBoard.WHITE ? 
                                                      InternationalChessBoard.BLACK : InternationalChessBoard.WHITE);
                break;
            case "大模型AI":
                useLLM = true;
                llmAI = new InternationalLLMChessAI(model, humanPlayer == InternationalChessBoard.WHITE ? 
                                                 InternationalChessBoard.BLACK : InternationalChessBoard.WHITE, chatPanel);
                break;
            case "混合AI":
                useHybrid = true;
                hybridAI = new InternationalHybridChessAI(difficulty, humanPlayer == InternationalChessBoard.WHITE ? 
                                                       InternationalChessBoard.BLACK : InternationalChessBoard.WHITE, 
                                                       model, chatPanel);
                break;
            case "DeepSeek+Pikafish":
                useDeepSeekPikafish = true;
                com.example.internationalchess.core.PieceColor aiColor = (humanPlayer == InternationalChessBoard.WHITE) ? 
                    com.example.internationalchess.core.PieceColor.BLACK : com.example.internationalchess.core.PieceColor.RED;
                deepSeekPikafishAI = new DeepSeekPikafishAI(aiColor, difficulty, model != null ? model : "deepseek-r1");
                // 设置AI日志面板
                if (aiLogPanel != null) {
                    deepSeekPikafishAI.setAILogPanel(aiLogPanel);
                }
                break;
        }
        
        updateStatus();
        
        // 如果启用AI且当前是AI回合，触发AI移动
        if (isAIEnabled && isAITurn()) {
            SwingUtilities.invokeLater(this::performAIMove);
        }
    }
    
    /**
     * 获取棋盘
     */
    public InternationalChessBoard getChessBoard() {
        return chessBoard;
    }
    
    /**
     * 设置AI日志面板
     */
    public void setAILogPanel(AILogPanel aiLogPanel) {
        this.aiLogPanel = aiLogPanel;
        
        // 如果已经创建了DeepSeekPikafishAI，设置其日志面板
        if (deepSeekPikafishAI != null) {
            deepSeekPikafishAI.setAILogPanel(aiLogPanel);
        }
    }
    
    /**
     * 记录移动历史
     */
    private void recordMove(int fromRow, int fromCol, int toRow, int toCol) {
        String move = String.format("%d,%d,%d,%d", fromRow, fromCol, toRow, toCol);
        moveHistory.add(move);
    }
    
    /**
     * 悔棋功能
     */
    public boolean undoMove() {
        if (moveHistory.isEmpty() || isAIThinking) {
            return false;
        }
        
        // 如果启用AI，需要悔棋两步（人类和AI各一步）
        int stepsToUndo = isAIEnabled ? 2 : 1;
        
        for (int i = 0; i < stepsToUndo && !moveHistory.isEmpty(); i++) {
            moveHistory.remove(moveHistory.size() - 1);
        }
        
        // 重建棋盘状态
        rebuildBoardFromHistory();
        
        // 重置选择
        selectedRow = -1;
        selectedCol = -1;
        
        // 更新状态
        updateStatus();
        
        // 重绘棋盘
        repaint();
        
        return true;
    }
    
    /**
     * 从历史记录重建棋盘状态
     */
    private void rebuildBoardFromHistory() {
        // 重新初始化棋盘
        chessBoard = new InternationalChessBoard();
        
        // 重新执行所有历史移动
        for (String move : moveHistory) {
            String[] parts = move.split(",");
            if (parts.length == 4) {
                int fromRow = Integer.parseInt(parts[0]);
                int fromCol = Integer.parseInt(parts[1]);
                int toRow = Integer.parseInt(parts[2]);
                int toCol = Integer.parseInt(parts[3]);
                chessBoard.movePiece(fromRow, fromCol, toRow, toCol);
            }
        }
    }
    
    /**
     * 检查是否可以悔棋
     */
    public boolean canUndo() {
        return !moveHistory.isEmpty() && !isAIThinking && chessBoard.getGameState() == GameState.PLAYING;
    }
    
    /**
     * 显示兵升变选择对话框
     * @return 选择的棋子类型，如果取消则返回0
     */
    private char showPromotionDialog() {
        String[] options = {"后 (Queen)", "车 (Rook)", "象 (Bishop)", "马 (Knight)"};
        char[] pieces = {InternationalChessBoard.QUEEN, InternationalChessBoard.ROOK, 
                        InternationalChessBoard.BISHOP, InternationalChessBoard.KNIGHT};
        
        int choice = JOptionPane.showOptionDialog(
            this,
            "请选择兵升变的棋子类型：",
            "兵的升变",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0] // 默认选择后
        );
        
        if (choice >= 0 && choice < pieces.length) {
            return pieces[choice];
        }
        return 0; // 用户取消了选择
    }
}