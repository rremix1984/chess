package com.example.monopoly;

import com.example.common.game.GameContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 大富翁游戏主界面
 */
public class MonopolyFrame extends JFrame {
    
    private MonopolyBoard board;
    private JPanel gamePanel;
    private JPanel controlPanel;
    private JLabel statusLabel;
    private JButton rollDiceButton;
    private JLabel currentPlayerLabel;
    private JLabel cashLabel;
    private JTextArea logArea;
    private List<Player> players;
    private int currentPlayerIndex;
    private Random random;
    private boolean gameStarted;
    
    public MonopolyFrame() {
        setTitle("大富翁游戏");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        
        random = new Random();
        gameStarted = false;
        
        initializeGame();
        createUI();
        
        // 窗口关闭时返回游戏选择界面
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                returnToGameSelection();
            }
        });
    }
    
    private void initializeGame() {
        // 初始化玩家
        players = new ArrayList<>();
        players.add(new Player("玩家1", Color.RED, 1500));
        players.add(new Player("玩家2", Color.BLUE, 1500));
        players.add(new Player("玩家3", Color.GREEN, 1500));
        players.add(new Player("玩家4", Color.ORANGE, 1500));
        
        currentPlayerIndex = 0;
        
        // 初始化棋盘
        board = new MonopolyBoard();
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        // 创建游戏面板（棋盘）
        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(new Color(245, 245, 220));
        gamePanel.add(board, BorderLayout.CENTER);
        
        // 创建控制面板
        controlPanel = createControlPanel();
        
        // 创建信息面板
        JPanel infoPanel = createInfoPanel();
        
        add(gamePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.EAST);
        
        updateUI();
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(new Color(245, 245, 220));
        panel.setBorder(BorderFactory.createTitledBorder("游戏控制"));
        
        rollDiceButton = new JButton("掷骰子");
        rollDiceButton.setFont(new Font("宋体", Font.BOLD, 16));
        rollDiceButton.setBackground(new Color(34, 139, 34));
        rollDiceButton.setForeground(Color.WHITE);
        rollDiceButton.setPreferredSize(new Dimension(100, 40));
        
        rollDiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rollDice();
            }
        });
        
        JButton startButton = new JButton("开始游戏");
        startButton.setFont(new Font("宋体", Font.BOLD, 16));
        startButton.setBackground(new Color(30, 144, 255));
        startButton.setForeground(Color.WHITE);
        startButton.setPreferredSize(new Dimension(100, 40));
        
        startButton.addActionListener(e -> startGame());
        
        JButton backButton = new JButton("返回主菜单");
        backButton.setFont(new Font("宋体", Font.BOLD, 16));
        backButton.setBackground(new Color(220, 20, 60));
        backButton.setForeground(Color.WHITE);
        backButton.setPreferredSize(new Dimension(120, 40));
        
        backButton.addActionListener(e -> returnToGameSelection());
        
        panel.add(startButton);
        panel.add(rollDiceButton);
        panel.add(backButton);
        
        rollDiceButton.setEnabled(false); // 初始不可用
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setBorder(BorderFactory.createTitledBorder("游戏信息"));
        panel.setBackground(new Color(245, 245, 220));
        
        // 当前玩家信息
        JPanel playerInfoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        playerInfoPanel.setBackground(new Color(245, 245, 220));
        playerInfoPanel.setBorder(BorderFactory.createTitledBorder("当前玩家"));
        
        currentPlayerLabel = new JLabel("当前玩家: 玩家1");
        currentPlayerLabel.setFont(new Font("宋体", Font.BOLD, 14));
        
        cashLabel = new JLabel("现金: $1500");
        cashLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        
        statusLabel = new JLabel("点击开始游戏");
        statusLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        statusLabel.setForeground(Color.BLUE);
        
        playerInfoPanel.add(currentPlayerLabel);
        playerInfoPanel.add(cashLabel);
        playerInfoPanel.add(statusLabel);
        
        // 游戏日志
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("游戏日志"));
        logPanel.setBackground(new Color(245, 245, 220));
        
        logArea = new JTextArea(20, 20);
        logArea.setEditable(false);
        logArea.setFont(new Font("宋体", Font.PLAIN, 12));
        logArea.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(playerInfoPanel, BorderLayout.NORTH);
        panel.add(logPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void startGame() {
        gameStarted = true;
        rollDiceButton.setEnabled(true);
        statusLabel.setText("游戏开始！轮到 " + getCurrentPlayer().getName());
        addLog("游戏开始！所有玩家从起点出发。");
        
        // 将所有玩家放置在起点
        for (Player player : players) {
            player.setPosition(0);
            board.updatePlayerPosition(player);
        }
        
        board.repaint();
    }
    
    private void rollDice() {
        if (!gameStarted) return;
        
        Player currentPlayer = getCurrentPlayer();
        int dice1 = random.nextInt(6) + 1;
        int dice2 = random.nextInt(6) + 1;
        int totalMove = dice1 + dice2;
        
        addLog(currentPlayer.getName() + " 掷出骰子: " + dice1 + " + " + dice2 + " = " + totalMove);
        
        // 移动玩家
        int newPosition = (currentPlayer.getPosition() + totalMove) % 40;
        
        // 检查是否经过起点
        if (newPosition < currentPlayer.getPosition()) {
            currentPlayer.addMoney(200);
            addLog(currentPlayer.getName() + " 经过起点，获得$200！");
        }
        
        currentPlayer.setPosition(newPosition);
        board.updatePlayerPosition(currentPlayer);
        
        // 处理土地事件
        handleLandEvent(currentPlayer, newPosition);
        
        // 切换到下一个玩家
        nextPlayer();
        
        updateUI();
        board.repaint();
    }
    
    private void handleLandEvent(Player player, int position) {
        String landName = board.getLandName(position);
        
        if (position == 0) {
            addLog(player.getName() + " 到达起点！");
        } else if (position == 10) {
            addLog(player.getName() + " 到达监狱，只是参观。");
        } else if (position == 20) {
            addLog(player.getName() + " 到达免费停车场。");
        } else if (position == 30) {
            addLog(player.getName() + " 被送入监狱！");
            player.setPosition(10); // 移动到监狱位置
        } else {
            // 普通土地
            int landValue = board.getLandValue(position);
            if (landValue > 0) {
                if (player.getMoney() >= landValue) {
                    int choice = JOptionPane.showConfirmDialog(
                        this,
                        "是否购买 " + landName + "？\n价格: $" + landValue,
                        "购买土地",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        player.subtractMoney(landValue);
                        addLog(player.getName() + " 购买了 " + landName + "，花费$" + landValue);
                    } else {
                        addLog(player.getName() + " 选择不购买 " + landName);
                    }
                } else {
                    addLog(player.getName() + " 资金不足，无法购买 " + landName);
                }
            } else {
                addLog(player.getName() + " 到达 " + landName);
            }
        }
    }
    
    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        statusLabel.setText("轮到 " + getCurrentPlayer().getName());
    }
    
    private Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    private void updateUI() {
        Player currentPlayer = getCurrentPlayer();
        currentPlayerLabel.setText("当前玩家: " + currentPlayer.getName());
        currentPlayerLabel.setForeground(currentPlayer.getColor());
        cashLabel.setText("现金: $" + currentPlayer.getMoney());
    }
    
    private void addLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void returnToGameSelection() {
        dispose();
        if (!GameContext.isSinglePlayer()) {
            System.exit(0);
        }
    }
    
    /**
     * 主方法，用于独立测试
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new MonopolyFrame().setVisible(true);
        });
    }
}
