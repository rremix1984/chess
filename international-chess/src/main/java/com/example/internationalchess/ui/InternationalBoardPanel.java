package com.example.internationalchess.ui;

import com.example.internationalchess.core.InternationalChessBoard;
import com.example.internationalchess.core.PieceColor;
import com.example.internationalchess.core.GameState;
import com.example.internationalchess.ai.InternationalChessAI;
import com.example.internationalchess.ai.StockfishAIAdapter;
import com.example.internationalchess.ui.StockfishLogPanel;
import audio.SoundManager;
import static audio.SoundManager.Event.*;
import static audio.SoundManager.SoundProfile.*;

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
    private static final int CELL_SIZE = 80; // 增大格子尺寸
    private static final Color LIGHT_COLOR = new Color(255, 206, 158); // 更加温暖的浅色
    private static final Color DARK_COLOR = new Color(139, 69, 19); // 深棕色
    private static final Color SELECTED_COLOR = new Color(0, 191, 255, 150); // 天蓝色高亮
    private static final Color POSSIBLE_MOVE_COLOR = new Color(50, 205, 50, 100); // 绿色可移动提示
    private static final Color AI_SUGGESTION_FROM_COLOR = new Color(255, 215, 0, 180); // AI建议起始位置（金色）
    private static final Color AI_SUGGESTION_TO_COLOR = new Color(255, 69, 0, 150); // AI建议目标位置（橙红色）
    private static final Color BORDER_COLOR = new Color(101, 67, 33); // 边框颜色
    private static final Color PIECE_SHADOW_COLOR = new Color(0, 0, 0, 80); // 棋子阴影
    
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
    
    // AI建议移动的显示
    private int aiSuggestionFromRow = -1;
    private int aiSuggestionFromCol = -1;
    private int aiSuggestionToRow = -1;
    private int aiSuggestionToCol = -1;
    private Timer aiSuggestionTimer; // 用于定时清除建议高亮

    // 棋子移动动画
    private PieceMoveAnimation moveAnimation;
    
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
        drawAISuggestionHighlight(g2d);
        drawPieces(g2d);
        drawSelection(g2d);
    }
    
    private void drawBoard(Graphics2D g2d) {
        // 启用高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Color cellColor = (row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR;
                
                // 绘制木纹背景
                drawWoodGrainCell(g2d, col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, cellColor);
                
                // 绘制精细边框
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.setColor(BORDER_COLOR);
                g2d.drawRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        
        // 重置画笔
        g2d.setStroke(new BasicStroke(1f));
    }
    
    /**
     * 绘制木纹纹理的格子
     */
    private void drawWoodGrainCell(Graphics2D g2d, int x, int y, int size, Color baseColor) {
        // 绘制基本渐变背景
        GradientPaint baseGradient = new GradientPaint(
            x, y, baseColor.brighter(),
            x + size, y + size, baseColor.darker()
        );
        g2d.setPaint(baseGradient);
        g2d.fillRect(x, y, size, size);
        
        // 添加木纹纹理
        drawWoodGrainTexture(g2d, x, y, size, baseColor);
    }
    
    /**
     * 绘制木纹纹理
     */
    private void drawWoodGrainTexture(Graphics2D g2d, int x, int y, int size, Color baseColor) {
        // 保存原始状态
        Stroke originalStroke = g2d.getStroke();
        
        // 绘制横向木纹线条
        g2d.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        for (int i = 0; i < size; i += 8) {
            // 创建随机木纹颜色
            int alpha = 15 + (i % 25);
            Color grainColor = new Color(
                Math.max(0, Math.min(255, baseColor.getRed() - 20 + (i % 40))),
                Math.max(0, Math.min(255, baseColor.getGreen() - 15 + (i % 30))),
                Math.max(0, Math.min(255, baseColor.getBlue() - 10 + (i % 20))),
                alpha
            );
            g2d.setColor(grainColor);
            
            // 绘制曲线木纹
            int startY = y + i;
            int endY = y + i;
            int startX = x;
            int endX = x + size;
            
            // 添加微妙的波浪效果
            for (int segX = startX; segX < endX - 5; segX += 5) {
                int wave1 = (int)(Math.sin((segX - x) * 0.1) * 1.5);
                int wave2 = (int)(Math.sin((segX - x) * 0.05) * 0.8);
                g2d.drawLine(segX, startY + wave1 + wave2, segX + 5, startY + wave1 + wave2);
            }
        }
        
        // 添加垂直木纹（较淡）
        g2d.setStroke(new BasicStroke(0.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < size; i += 12) {
            Color verticalGrainColor = new Color(
                baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 10
            );
            g2d.setColor(verticalGrainColor);
            g2d.drawLine(x + i, y, x + i, y + size);
        }
        
        // 恢复原始状态
        g2d.setStroke(originalStroke);
    }
    
    private void drawPieces(Graphics2D g2d) {
        // 使用更大的字体和更好的渲染
        g2d.setFont(new Font("Arial Unicode MS", Font.BOLD, 48));
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = g2d.getFontMetrics();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String piece = board.getPiece(row, col);
                if (piece != null && !piece.trim().isEmpty()) {
                    if (moveAnimation != null && moveAnimation.isActive() && row == moveAnimation.toRow && col == moveAnimation.toCol) {
                        continue; // 动画负责绘制该棋子
                    }
                    drawProfessionalPiece(g2d, piece, col, row, fm);
                }
            }
        }

        // 绘制移动中的棋子
        if (moveAnimation != null && moveAnimation.isActive()) {
            moveAnimation.draw(g2d, fm);
        }
    }
    
    /**
     * 绘制专业的棋子（带立体效果）
     */
    private void drawProfessionalPiece(Graphics2D g2d, String piece, int col, int row, FontMetrics fm) {
        int centerX = col * CELL_SIZE + CELL_SIZE / 2;
        int centerY = row * CELL_SIZE + CELL_SIZE / 2;
        drawProfessionalPieceAt(g2d, piece, centerX, centerY, fm);
    }

    // 支持在任意像素位置绘制专业棋子
    private void drawProfessionalPieceAt(Graphics2D g2d, String piece, int centerX, int centerY, FontMetrics fm) {
        String symbol = getPieceSymbol(piece);
        if (symbol.isEmpty()) return;

        int pieceSize = (int)(CELL_SIZE * 0.7); // 棋子占格子70%
        boolean isWhite = piece.charAt(0) == 'W';

        // 绘制远距离阴影（环境光阴影）
        drawEnvironmentShadow(g2d, centerX, centerY, pieceSize);

        // 绘制棋子底座（圆形或方形）
        drawPieceBase(g2d, centerX, centerY, pieceSize, isWhite);

        // 绘制棋子符号
        drawPieceSymbol(g2d, symbol, centerX, centerY, fm, isWhite);

        // 绘制高亮和阴影效果
        drawPieceEffects(g2d, centerX, centerY, pieceSize, isWhite);

        // 绘制表面光照效果
        drawSurfaceLighting(g2d, centerX, centerY, pieceSize, isWhite);
    }
    
    /**
     * 绘制棋子底座（专业版）
     */
    private void drawPieceBase(Graphics2D g2d, int centerX, int centerY, int size, boolean isWhite) {
        int radius = size / 2;
        
        // 绘制深度阴影
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillOval(centerX - radius + 3, centerY - radius + 3, size, size);
        
        // 绘制次级阴影
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval(centerX - radius + 1, centerY - radius + 1, size, size);
        
        // 绘制主体（改进黑棋颜色）
        Color baseColor, highlightColor, midColor;
        if (isWhite) {
            baseColor = new Color(248, 248, 248);
            highlightColor = Color.WHITE;
            midColor = new Color(240, 240, 240);
        } else {
            // 改善黑棋颜色：使用深灰色而非纯黑
            baseColor = new Color(80, 80, 85);
            highlightColor = new Color(130, 130, 135);
            midColor = new Color(100, 100, 105);
        }
        
        // 多层渐变效果
        RadialGradientPaint radialGradient = new RadialGradientPaint(
            centerX - radius/3, centerY - radius/3, radius,
            new float[]{0f, 0.6f, 1f},
            new Color[]{highlightColor, midColor, baseColor}
        );
        g2d.setPaint(radialGradient);
        g2d.fillOval(centerX - radius, centerY - radius, size, size);
        
        // 绘制内部高亮圈
        g2d.setStroke(new BasicStroke(1f));
        g2d.setColor(new Color(255, 255, 255, isWhite ? 120 : 80));
        g2d.drawOval(centerX - radius + 4, centerY - radius + 4, size - 8, size - 8);
        
        // 绘制主边框
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(isWhite ? new Color(160, 160, 160) : new Color(40, 40, 45));
        g2d.drawOval(centerX - radius, centerY - radius, size, size);
        
        // 绘制外边框高亮
        g2d.setStroke(new BasicStroke(1f));
        g2d.setColor(new Color(255, 255, 255, isWhite ? 100 : 60));
        g2d.drawOval(centerX - radius - 1, centerY - radius - 1, size + 2, size + 2);
        
        // 重置画笔
        g2d.setStroke(new BasicStroke(1f));
    }
    
    /**
     * 绘制棋子符号
     */
    private void drawPieceSymbol(Graphics2D g2d, String symbol, int centerX, int centerY, FontMetrics fm, boolean isWhite) {
        int x = centerX - fm.stringWidth(symbol) / 2;
        int y = centerY + fm.getAscent() / 2 - fm.getDescent();
        
        // 绘制符号阴影
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(symbol, x + 1, y + 1);
        
        // 绘制主符号
        if (isWhite) {
            // 白棋：黑色符号带白色边框
            g2d.setColor(Color.WHITE);
            g2d.drawString(symbol, x-1, y-1);
            g2d.drawString(symbol, x-1, y+1);
            g2d.drawString(symbol, x+1, y-1);
            g2d.drawString(symbol, x+1, y+1);
            g2d.setColor(new Color(20, 20, 20));
            g2d.drawString(symbol, x, y);
        } else {
            // 黑棋：纯黑色符号
            g2d.setColor(new Color(10, 10, 10));
            g2d.drawString(symbol, x, y);
        }
    }
    
    /**
     * 绘制环境阴影（远距离软阴影）
     */
    private void drawEnvironmentShadow(Graphics2D g2d, int centerX, int centerY, int size) {
        int radius = size / 2;
        int shadowOffset = 4;
        int shadowSize = size + 8;
        
        // 绘制多层环境阴影，创造柔和的阴影效果
        for (int i = 3; i >= 0; i--) {
            int shadowAlpha = 8 + i * 4; // 递减的透明度
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
    private void drawSurfaceLighting(Graphics2D g2d, int centerX, int centerY, int size, boolean isWhite) {
        int radius = size / 2;
        
        // 光源位置（左上方）
        int lightX = centerX - radius / 2;
        int lightY = centerY - radius / 2;
        
        // 绘制主要高光
        RadialGradientPaint highlight = new RadialGradientPaint(
            lightX, lightY, radius / 3,
            new float[]{0f, 0.7f, 1f},
            new Color[]{
                new Color(255, 255, 255, isWhite ? 180 : 120),
                new Color(255, 255, 255, isWhite ? 80 : 40),
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
        g2d.setColor(new Color(255, 255, 255, isWhite ? 60 : 30));
        g2d.drawOval(centerX - radius + 2, centerY - radius + 2, size - 4, size - 4);
        
        // 绘制反射光（底部右侧）
        int reflectX = centerX + radius / 3;
        int reflectY = centerY + radius / 3;
        g2d.setColor(new Color(255, 255, 255, isWhite ? 40 : 20));
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
     * 绘制棋子特效（高亮和阴影）
     */
    private void drawPieceEffects(Graphics2D g2d, int centerX, int centerY, int size, boolean isWhite) {
        int radius = size / 2;
        
        // 绘制内部阴影（创造深度感）
        g2d.setColor(new Color(0, 0, 0, isWhite ? 30 : 50));
        g2d.fillOval(
            centerX - radius + radius / 4,
            centerY - radius + radius / 4,
            size - radius / 2,
            size - radius / 2
        );
        
        // 绘制边缘高光
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(255, 255, 255, isWhite ? 100 : 50));
        g2d.drawOval(centerX - radius + 3, centerY - radius + 3, size - 6, size - 6);
        
        // 重置画笔
        g2d.setStroke(new BasicStroke(1f));
    }
    
    /**
     * 绘制AI建议移动高亮
     */
    private void drawAISuggestionHighlight(Graphics2D g2d) {
        // 绘制AI建议的起始位置
        if (aiSuggestionFromRow >= 0 && aiSuggestionFromCol >= 0) {
            drawSuggestionCell(g2d, aiSuggestionFromCol, aiSuggestionFromRow, AI_SUGGESTION_FROM_COLOR, "★"); // 星号标记起始位置
        }
        
        // 绘制AI建议的目标位置
        if (aiSuggestionToRow >= 0 && aiSuggestionToCol >= 0) {
            drawSuggestionCell(g2d, aiSuggestionToCol, aiSuggestionToRow, AI_SUGGESTION_TO_COLOR, "▶"); // 箭头标记目标位置
        }
        
        // 绘制连接线
        if (aiSuggestionFromRow >= 0 && aiSuggestionFromCol >= 0 &&
            aiSuggestionToRow >= 0 && aiSuggestionToCol >= 0) {
            drawSuggestionArrow(g2d, 
                aiSuggestionFromCol * CELL_SIZE + CELL_SIZE / 2,
                aiSuggestionFromRow * CELL_SIZE + CELL_SIZE / 2,
                aiSuggestionToCol * CELL_SIZE + CELL_SIZE / 2,
                aiSuggestionToRow * CELL_SIZE + CELL_SIZE / 2
            );
        }
    }
    
    /**
     * 绘制建议单元格
     */
    private void drawSuggestionCell(Graphics2D g2d, int col, int row, Color highlightColor, String symbol) {
        int x = col * CELL_SIZE;
        int y = row * CELL_SIZE;
        
        // 绘制高亮背景
        g2d.setColor(highlightColor);
        g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
        
        // 绘制装饰性边框
        g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(highlightColor.getRed(), highlightColor.getGreen(), highlightColor.getBlue(), 255));
        g2d.drawRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
        
        // 绘制中心标记符号
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int symbolX = x + (CELL_SIZE - fm.stringWidth(symbol)) / 2;
        int symbolY = y + (CELL_SIZE + fm.getAscent() - fm.getDescent()) / 2;
        
        // 添加文本阴影效果
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(symbol, symbolX + 1, symbolY + 1);
        
        // 绘制主文本
        g2d.setColor(Color.WHITE);
        g2d.drawString(symbol, symbolX, symbolY);
        
        // 重置画笔
        g2d.setStroke(new BasicStroke(1f));
    }
    
    /**
     * 绘制建议移动箭头
     */
    private void drawSuggestionArrow(Graphics2D g2d, int fromX, int fromY, int toX, int toY) {
        // 设置箭头样式
        g2d.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(255, 140, 0, 200)); // 橙色箭头
        
        // 绘制主线条
        g2d.drawLine(fromX, fromY, toX, toY);
        
        // 计算箭头角度
        double angle = Math.atan2(toY - fromY, toX - fromX);
        int arrowLength = 20;
        double arrowAngle = Math.PI / 6; // 30度
        
        // 绘制箭头两侧
        int x1 = (int)(toX - arrowLength * Math.cos(angle - arrowAngle));
        int y1 = (int)(toY - arrowLength * Math.sin(angle - arrowAngle));
        int x2 = (int)(toX - arrowLength * Math.cos(angle + arrowAngle));
        int y2 = (int)(toY - arrowLength * Math.sin(angle + arrowAngle));
        
        g2d.drawLine(toX, toY, x1, y1);
        g2d.drawLine(toX, toY, x2, y2);
        
    // 重置画笔
        g2d.setStroke(new BasicStroke(1f));
    }
    
    /**
     * 设置AI建议的移动高亮
     */
    public void setAISuggestion(String move) {
        clearAISuggestion(); // 先清除之前的建议
        
        if (move == null || move.length() < 4) {
            return;
        }
        
        try {
            // 解析移动字符串，例如 "e2e4"
            char fromFile = move.charAt(0);
            int fromRank = Character.getNumericValue(move.charAt(1));
            char toFile = move.charAt(2);
            int toRank = Character.getNumericValue(move.charAt(3));
            
            // 转换为数组坐标（国际象棋坐标系：a1在左下角，a8在左上角）
            aiSuggestionFromCol = fromFile - 'a';
            aiSuggestionFromRow = 8 - fromRank;
            aiSuggestionToCol = toFile - 'a';
            aiSuggestionToRow = 8 - toRank;
            
            // 确保坐标在有效范围内
            if (aiSuggestionFromRow >= 0 && aiSuggestionFromRow < 8 &&
                aiSuggestionFromCol >= 0 && aiSuggestionFromCol < 8 &&
                aiSuggestionToRow >= 0 && aiSuggestionToRow < 8 &&
                aiSuggestionToCol >= 0 && aiSuggestionToCol < 8) {
                
                // 启动定时器，10秒后自动清除建议
                if (aiSuggestionTimer != null) {
                    aiSuggestionTimer.stop();
                }
                aiSuggestionTimer = new Timer(10000, e -> {
                    clearAISuggestion();
                    repaint();
                });
                aiSuggestionTimer.setRepeats(false);
                aiSuggestionTimer.start();
                
                repaint();
                
                // 记录到日志
                if (stockfishLogPanel != null) {
                    String piece = board.getPiece(aiSuggestionFromRow, aiSuggestionFromCol);
                    String pieceNameCh = piece != null ? getPieceNameChinese(piece.charAt(1)) : "棋子";
                    stockfishLogPanel.addAIDecision("💡 建议移动: " + pieceNameCh + " " + 
                        fromFile + fromRank + "→" + toFile + toRank);
                }
            }
        } catch (Exception e) {
            System.err.println("解析AI建议移动时出错: " + e.getMessage());
        }
    }
    
    private void drawSelection(Graphics2D g2d) {
        if (selectedRow >= 0 && selectedCol >= 0) {
            int x = selectedCol * CELL_SIZE;
            int y = selectedRow * CELL_SIZE;
            int centerX = x + CELL_SIZE / 2;
            int centerY = y + CELL_SIZE / 2;

            // 根据棋子尺寸计算环半径（略大于棋子底座）
            int ringRadius = (int) (CELL_SIZE * 0.7 / 2) + 6;

            Stroke oldStroke = g2d.getStroke();
            g2d.setColor(SELECTED_COLOR);
            g2d.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawOval(centerX - ringRadius, centerY - ringRadius, ringRadius * 2, ringRadius * 2);
            g2d.setStroke(oldStroke);
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
                String movingPiece = board.getPiece(selectedRow, selectedCol);
                String targetPiece = board.getPiece(row, col);
                if (board.movePiece(selectedRow, selectedCol, row, col)) {
                    startMoveAnimation(movingPiece, selectedRow, selectedCol, row, col);
                    SoundManager.play(WOOD, targetPiece != null ? PIECE_CAPTURE : PIECE_DROP);
                    updateStatus("移动成功");

                    // 检查游戏状态
                    checkGameState();

                    // 如果AI启用且游戏仍在进行，让AI走棋
                    if (aiEnabled && board.getGameState() == GameState.PLAYING) {
                        SwingUtilities.invokeLater(this::makeAIMove);
                    }
                } else {
                    SoundManager.play(WOOD, PIECE_DROP);
                    updateStatus("移动失败");
                }
            } else {
                SoundManager.play(WOOD, PIECE_DROP);
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
            startMoveAnimation(piece, fromRow, fromCol, toRow, toCol);
            SoundManager.play(WOOD, isCapture ? PIECE_CAPTURE : PIECE_DROP);
            
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
     * 记录AI决策信息（精简版）
     */
    private void logAIDecision(String aiName, int[] move, String reason) {
        if (stockfishLogPanel == null) return;
        
        char fromFile = (char) ('a' + move[1]);
        int fromRank = 8 - move[0];
        char toFile = (char) ('a' + move[3]);
        int toRank = 8 - move[2];
        String moveStr = "" + fromFile + fromRank + "→" + toFile + toRank;
        
        // 简化日志：只记录关键信息
        String piece = board.getPiece(move[0], move[1]);
        String pieceNameCh = getPieceNameChinese(piece.charAt(1));
        stockfishLogPanel.addAIDecision("🤖 " + pieceNameCh + " " + moveStr);
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
                SoundManager.play(WOOD, WIN);
                break;
            case BLACK_WIN:
            case BLACK_CHECKMATE:
                updateStatus("🎉 黑方获胜！");
                SoundManager.play(WOOD, WIN);
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
    
    /**
     * 获取棋盘对象
     */
    public InternationalChessBoard getBoard() {
        return board;
    }
    
    /**
     * 显示AI建议移动
     */
    public void showAISuggestion(int fromRow, int fromCol, int toRow, int toCol) {
        this.aiSuggestionFromRow = fromRow;
        this.aiSuggestionFromCol = fromCol;
        this.aiSuggestionToRow = toRow;
        this.aiSuggestionToCol = toCol;
        
        // 停止之前的计时器
        if (aiSuggestionTimer != null) {
            aiSuggestionTimer.stop();
        }
        
        // 设置5秒后清除建议高亮
        aiSuggestionTimer = new Timer(5000, e -> {
            clearAISuggestion();
        });
        aiSuggestionTimer.setRepeats(false);
        aiSuggestionTimer.start();
        
        // 立即重绘以显示建议
        repaint();
        
        // 生成建议描述
        String piece = board.getPiece(fromRow, fromCol);
        if (piece != null) {
            String pieceNameCh = getPieceNameChinese(piece.charAt(1));
            char fromFile = (char) ('a' + fromCol);
            int fromRank = 8 - fromRow;
            char toFile = (char) ('a' + toCol);
            int toRank = 8 - toRow;
            String moveStr = "" + fromFile + fromRank + "→" + toFile + toRank;
            updateStatus("✨ AI建议: " + pieceNameCh + " " + moveStr);
        }
    }
    
    /**
     * 清除AI建议移动高亮
     */
    public void clearAISuggestion() {
        this.aiSuggestionFromRow = -1;
        this.aiSuggestionFromCol = -1;
        this.aiSuggestionToRow = -1;
        this.aiSuggestionToCol = -1;
        
        if (aiSuggestionTimer != null) {
            aiSuggestionTimer.stop();
            aiSuggestionTimer = null;
        }
        
        repaint();
    }
    
    /**
     * 获取AI建议移动（供外部调用）
     */
    public void requestAISuggestion() {
        if (!aiEnabled || !isHumanTurn() || board.getGameState() != GameState.PLAYING) {
            updateStatus("当前无法获取AI建议");
            return;
        }
        
        updateStatus("💭 正在获取AI建议...");
        
        // 在新线程中计算AI廊议
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                try {
                    if ("Stockfish".equals(aiType) && stockfishAI != null) {
                        return stockfishAI.calculateNextMove(board);
                    } else if (ai != null) {
                        return ai.calculateNextMove(board);
                    }
                } catch (Exception e) {
                    System.err.println("AI计算廚议时出错: " + e.getMessage());
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    int[] move = get();
                    if (move != null && move.length == 4) {
                        showAISuggestion(move[0], move[1], move[2], move[3]);
                    } else {
                        updateStatus("❌ AI无法提供廚议");
                    }
                } catch (Exception e) {
                    updateStatus("❌ 获取AI廚议失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void startMoveAnimation(String piece, int fromRow, int fromCol, int toRow, int toCol) {
        moveAnimation = new PieceMoveAnimation(piece, fromRow, fromCol, toRow, toCol);
        moveAnimation.start();
    }

    /** 棋子移动动画实现 */
    private class PieceMoveAnimation {
        final String piece;
        final int fromRow, fromCol, toRow, toCol;
        final int startX, startY, endX, endY;
        final Rectangle dirtyRect;
        long startTime;
        final int duration = 300; // 动画时长(ms)
        volatile double progress;

        PieceMoveAnimation(String piece, int fromRow, int fromCol, int toRow, int toCol) {
            this.piece = piece;
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.startX = fromCol * CELL_SIZE + CELL_SIZE / 2;
            this.startY = fromRow * CELL_SIZE + CELL_SIZE / 2;
            this.endX = toCol * CELL_SIZE + CELL_SIZE / 2;
            this.endY = toRow * CELL_SIZE + CELL_SIZE / 2;

            int minX = Math.min(startX, endX) - CELL_SIZE / 2 - 20;
            int minY = Math.min(startY, endY) - CELL_SIZE / 2 - 40;
            int width = Math.abs(endX - startX) + CELL_SIZE + 40;
            int height = Math.abs(endY - startY) + CELL_SIZE + 80;
            this.dirtyRect = new Rectangle(minX, minY, width, height);
        }

        void start() {
            startTime = System.currentTimeMillis();
            Thread animator = new Thread(() -> {
                while (progress < 1.0) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    progress = Math.min(1.0, elapsed / (double) duration);
                    SwingUtilities.invokeLater(() -> repaint(dirtyRect));
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    moveAnimation = null;
                    repaint(dirtyRect);
                });
            });
            animator.setDaemon(true);
            animator.start();
        }

        boolean isActive() {
            return progress < 1.0;
        }

        void draw(Graphics2D g2d, FontMetrics fm) {
            double eased = 1 - Math.pow(1 - progress, 3);
            int currentX = (int) (startX + (endX - startX) * eased);
            int currentY = (int) (startY + (endY - startY) * eased - Math.sin(eased * Math.PI) * 20);

            int shake = (int) (Math.sin(eased * Math.PI * 4) * (1 - eased) * 5);
            currentX += shake;
            currentY += shake;

            drawProfessionalPieceAt(g2d, piece, currentX, currentY, fm);
        }
    }
}
