package com.example.flightchess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 飞行棋棋盘面板
 */
public class FlightChessBoardPanel extends JPanel {
    private static final int BOARD_SIZE = 600;
    private static final int CELL_SIZE = 30;
    private static final int PLANE_SIZE = 20;
    private static final int HOME_AREA_SIZE = 120;
    
    private FlightChessGame game;
    private FlightChessAI[] aiPlayers;
    private boolean[] isAIPlayer;
    private int diceValue;
    private boolean waitingForMove;
    private List<Integer> movablePlanes;
    
    // 状态更新回调
    private Runnable onStateUpdate;
    
    public FlightChessBoardPanel() {
        this.game = new FlightChessGame();
        this.aiPlayers = new FlightChessAI[4];
        this.isAIPlayer = new boolean[4];
        this.diceValue = 0;
        this.waitingForMove = false;
        
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }
    
    public void setOnStateUpdate(Runnable callback) {
        this.onStateUpdate = callback;
    }
    
    public void setAIPlayer(int player, boolean isAI, int difficulty) {
        isAIPlayer[player] = isAI;
        if (isAI) {
            aiPlayers[player] = new FlightChessAI(difficulty);
        } else {
            aiPlayers[player] = null;
        }
    }
    
    public void rollDice() {
        if (game.isGameOver()) {
            return;
        }
        
        diceValue = game.rollDice();
        movablePlanes = game.getMovablePlanes(game.getCurrentPlayer(), diceValue);
        
        if (movablePlanes.isEmpty()) {
            // 无法移动，切换到下一个玩家
            game.nextPlayer();
            waitingForMove = false;
            
            // 如果下一个玩家是AI，自动进行回合
            SwingUtilities.invokeLater(this::checkAITurn);
        } else {
            waitingForMove = true;
            
            // 如果当前玩家是AI，自动选择移动
            if (isAIPlayer[game.getCurrentPlayer()]) {
                SwingUtilities.invokeLater(this::performAIMove);
            }
        }
        
        updateState();
        repaint();
    }
    
    private void performAIMove() {
        if (!waitingForMove || game.isGameOver()) {
            return;
        }
        
        int currentPlayer = game.getCurrentPlayer();
        if (isAIPlayer[currentPlayer] && aiPlayers[currentPlayer] != null) {
            int planeIndex = aiPlayers[currentPlayer].getBestMove(game, currentPlayer, diceValue);
            if (planeIndex >= 0) {
                makeMove(planeIndex);
            }
        }
    }
    
    private void makeMove(int planeIndex) {
        if (!waitingForMove || game.isGameOver()) {
            return;
        }
        
        boolean moved = game.movePlane(game.getCurrentPlayer(), planeIndex, diceValue);
        if (moved) {
            waitingForMove = false;
            
            // 检查是否获胜
            if (game.isGameOver()) {
                updateState();
                repaint();
                return;
            }
            
            // 如果投掷6点，可以再次投掷
            if (diceValue != 6) {
                game.nextPlayer();
            }
            
            // 如果下一个玩家是AI，自动进行回合
            SwingUtilities.invokeLater(this::checkAITurn);
        }
        
        updateState();
        repaint();
    }
    
    private void checkAITurn() {
        if (!game.isGameOver() && !waitingForMove && isAIPlayer[game.getCurrentPlayer()]) {
            // AI玩家自动投掷骰子
            Timer timer = new Timer(1000, e -> rollDice());
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    private void handleMouseClick(int x, int y) {
        if (!waitingForMove || game.isGameOver() || isAIPlayer[game.getCurrentPlayer()]) {
            return;
        }
        
        // 检查点击的是否是可移动的飞机
        int currentPlayer = game.getCurrentPlayer();
        FlightChessGame.Plane[] planes = game.getPlayerPlanes(currentPlayer);
        
        for (int i = 0; i < planes.length; i++) {
            if (movablePlanes.contains(i)) {
                Point planePos = getPlaneScreenPosition(currentPlayer, i);
                if (Math.abs(x - planePos.x) <= PLANE_SIZE/2 && Math.abs(y - planePos.y) <= PLANE_SIZE/2) {
                    makeMove(i);
                    break;
                }
            }
        }
    }
    
    private Point getPlaneScreenPosition(int player, int planeIndex) {
        FlightChessGame.Plane plane = game.getPlayerPlanes(player)[planeIndex];
        
        if (plane.position == -1) {
            // 在家园
            return getHomePosition(player, planeIndex);
        } else if (plane.position >= FlightChessGame.MAIN_TRACK_SIZE) {
            // 在终点跑道
            return getFinishPosition(player, plane.position - FlightChessGame.MAIN_TRACK_SIZE);
        } else {
            // 在主跑道
            return getMainTrackPosition(plane.position);
        }
    }
    
    private Point getHomePosition(int player, int planeIndex) {
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        int homeOffset = HOME_AREA_SIZE / 2;
        
        int homeX, homeY;
        switch (player) {
            case 0: // 红色，左上
                homeX = centerX - homeOffset;
                homeY = centerY - homeOffset;
                break;
            case 1: // 蓝色，右上
                homeX = centerX + homeOffset;
                homeY = centerY - homeOffset;
                break;
            case 2: // 黄色，右下
                homeX = centerX + homeOffset;
                homeY = centerY + homeOffset;
                break;
            case 3: // 绿色，左下
                homeX = centerX - homeOffset;
                homeY = centerY + homeOffset;
                break;
            default:
                homeX = centerX;
                homeY = centerY;
        }
        
        // 在家园区域内排列飞机
        int offsetX = (planeIndex % 2) * 30 - 15;
        int offsetY = (planeIndex / 2) * 30 - 15;
        
        return new Point(homeX + offsetX, homeY + offsetY);
    }
    
    private Point getMainTrackPosition(int position) {
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        int trackRadius = 200;
        
        // 计算角度（顺时针）
        double angle = (position * 360.0 / FlightChessGame.MAIN_TRACK_SIZE) * Math.PI / 180;
        
        int x = centerX + (int)(trackRadius * Math.cos(angle));
        int y = centerY + (int)(trackRadius * Math.sin(angle));
        
        return new Point(x, y);
    }
    
    private Point getFinishPosition(int player, int finishPosition) {
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        int finishOffset = 50 + finishPosition * 20;
        
        switch (player) {
            case 0: // 红色，向中心
                return new Point(centerX - finishOffset, centerY);
            case 1: // 蓝色，向中心
                return new Point(centerX, centerY - finishOffset);
            case 2: // 黄色，向中心
                return new Point(centerX + finishOffset, centerY);
            case 3: // 绿色，向中心
                return new Point(centerX, centerY + finishOffset);
            default:
                return new Point(centerX, centerY);
        }
    }
    
    public void restartGame() {
        game.restart();
        diceValue = 0;
        waitingForMove = false;
        movablePlanes = null;
        updateState();
        repaint();
        
        // 如果第一个玩家是AI，开始AI回合
        checkAITurn();
    }
    
    public FlightChessGame getGame() {
        return game;
    }
    
    public int getDiceValue() {
        return diceValue;
    }
    
    public boolean isWaitingForMove() {
        return waitingForMove;
    }
    
    private void updateState() {
        if (onStateUpdate != null) {
            onStateUpdate.run();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawBoard(g2d);
        drawPlanes(g2d);
        
        g2d.dispose();
    }
    
    private void drawBoard(Graphics2D g2d) {
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_SIZE / 2;
        
        // 绘制背景
        g2d.setColor(new Color(245, 245, 220));
        g2d.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);
        
        // 绘制主跑道
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(3));
        int trackRadius = 200;
        g2d.drawOval(centerX - trackRadius, centerY - trackRadius, trackRadius * 2, trackRadius * 2);
        
        // 绘制跑道格子
        for (int i = 0; i < FlightChessGame.MAIN_TRACK_SIZE; i++) {
            Point pos = getMainTrackPosition(i);
            
            // 安全位置用不同颜色
            if (game.isSafePosition(i)) {
                g2d.setColor(Color.GREEN);
            } else {
                g2d.setColor(Color.WHITE);
            }
            
            g2d.fillOval(pos.x - CELL_SIZE/2, pos.y - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(pos.x - CELL_SIZE/2, pos.y - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);
        }
        
        // 绘制家园区域
        Color[] playerColors = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};
        for (int player = 0; player < 4; player++) {
            Point homeCenter = getHomePosition(player, 0);
            g2d.setColor(playerColors[player]);
            g2d.fillRect(homeCenter.x - HOME_AREA_SIZE/2, homeCenter.y - HOME_AREA_SIZE/2, 
                        HOME_AREA_SIZE, HOME_AREA_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(homeCenter.x - HOME_AREA_SIZE/2, homeCenter.y - HOME_AREA_SIZE/2, 
                        HOME_AREA_SIZE, HOME_AREA_SIZE);
        }
        
        // 绘制终点跑道
        g2d.setStroke(new BasicStroke(2));
        for (int player = 0; player < 4; player++) {
            g2d.setColor(playerColors[player]);
            for (int i = 0; i < FlightChessGame.FINISH_CELLS; i++) {
                Point pos = getFinishPosition(player, i);
                g2d.fillOval(pos.x - CELL_SIZE/3, pos.y - CELL_SIZE/3, CELL_SIZE*2/3, CELL_SIZE*2/3);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(pos.x - CELL_SIZE/3, pos.y - CELL_SIZE/3, CELL_SIZE*2/3, CELL_SIZE*2/3);
                g2d.setColor(playerColors[player]);
            }
        }
        
        // 绘制中心区域
        g2d.setColor(new Color(255, 215, 0)); // 金色
        g2d.fillOval(centerX - 30, centerY - 30, 60, 60);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(centerX - 30, centerY - 30, 60, 60);
    }
    
    private void drawPlanes(Graphics2D g2d) {
        Color[] playerColors = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};
        
        for (int player = 0; player < 4; player++) {
            FlightChessGame.Plane[] planes = game.getPlayerPlanes(player);
            g2d.setColor(playerColors[player]);
            
            for (int i = 0; i < planes.length; i++) {
                Point pos = getPlaneScreenPosition(player, i);
                
                // 高亮可移动的飞机
                if (waitingForMove && player == game.getCurrentPlayer() && 
                    movablePlanes != null && movablePlanes.contains(i)) {
                    g2d.setStroke(new BasicStroke(3));
                    g2d.setColor(Color.ORANGE);
                    g2d.drawOval(pos.x - PLANE_SIZE/2 - 2, pos.y - PLANE_SIZE/2 - 2, 
                                PLANE_SIZE + 4, PLANE_SIZE + 4);
                    g2d.setColor(playerColors[player]);
                }
                
                // 绘制飞机
                g2d.fillOval(pos.x - PLANE_SIZE/2, pos.y - PLANE_SIZE/2, PLANE_SIZE, PLANE_SIZE);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(pos.x - PLANE_SIZE/2, pos.y - PLANE_SIZE/2, PLANE_SIZE, PLANE_SIZE);
                
                // 绘制飞机编号
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                FontMetrics fm = g2d.getFontMetrics();
                String text = String.valueOf(i + 1);
                int textX = pos.x - fm.stringWidth(text) / 2;
                int textY = pos.y + fm.getAscent() / 2;
                g2d.drawString(text, textX, textY);
            }
        }
    }
}