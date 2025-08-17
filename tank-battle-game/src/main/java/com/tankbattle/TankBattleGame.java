package com.tankbattle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 坦克大战游戏主类
 */
public class TankBattleGame {
    private JFrame mainFrame;
    private GamePanel gamePanel;
    private NetworkManager networkManager;
    private Runnable onExit;

    public TankBattleGame() {
        this(null);
    }

    public TankBattleGame(Runnable onExit) {
        this.onExit = onExit;
        initializeUI();
    }
    
    /**
     * 设置按钮样式
     */
    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(Color.BLACK);
        button.setOpaque(true);
        
        // 添加鼠标交互效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = new Color(240, 240, 240);
            Color hoverColor = originalColor.brighter();
            Color pressedColor = originalColor.darker();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLoweredBevelBorder(),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
                ));
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                button.setBackground(pressedColor);
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }
        });
    }
    
    private void initializeUI() {
        mainFrame = new JFrame("坦克大战 - Tank Battle");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (onExit != null) {
                    onExit.run();
                } else {
                    System.exit(0);
                }
            }
        });
        mainFrame.setSize(1000, 800);
        mainFrame.setResizable(false);
        mainFrame.setLocationRelativeTo(null);
        
        // 创建菜单栏
        createMenuBar();
        
        // 显示主菜单
        showMainMenu();
        
        mainFrame.setVisible(true);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu gameMenu = new JMenu("游戏");
        JMenuItem newGameItem = new JMenuItem("新游戏");
        JMenuItem hostGameItem = new JMenuItem("创建房间");
        JMenuItem joinGameItem = new JMenuItem("加入房间");
        JMenuItem exitItem = new JMenuItem("退出");
        
        newGameItem.addActionListener(e -> startSinglePlayerGame());
        hostGameItem.addActionListener(e -> showHostGameDialog());
        joinGameItem.addActionListener(e -> showJoinGameDialog());
        exitItem.addActionListener(e -> mainFrame.dispose());
        
        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(hostGameItem);
        gameMenu.add(joinGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        
        menuBar.add(gameMenu);
        mainFrame.setJMenuBar(menuBar);
    }
    
    private void showMainMenu() {
        JPanel menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(Color.BLACK);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // 游戏标题
        JLabel titleLabel = new JLabel("坦克大战");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 48));
        titleLabel.setForeground(Color.YELLOW);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        menuPanel.add(titleLabel, gbc);
        
        // 按钮
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JButton singlePlayerBtn = new JButton("单人游戏");
        JButton hostGameBtn = new JButton("创建房间");
        JButton joinGameBtn = new JButton("加入房间");
        JButton exitBtn = new JButton("退出游戏");
        
        // 设置按钮样式
        JButton[] buttons = {singlePlayerBtn, hostGameBtn, joinGameBtn, exitBtn};
        for (JButton btn : buttons) {
            btn.setFont(new Font("宋体", Font.BOLD, 20));
            btn.setPreferredSize(new Dimension(200, 50));
            styleButton(btn);
        }
        
        singlePlayerBtn.addActionListener(e -> startSinglePlayerGame());
        hostGameBtn.addActionListener(e -> showHostGameDialog());
        joinGameBtn.addActionListener(e -> showJoinGameDialog());
        exitBtn.addActionListener(e -> mainFrame.dispose());
        
        gbc.gridy = 1;
        menuPanel.add(singlePlayerBtn, gbc);
        gbc.gridy = 2;
        menuPanel.add(hostGameBtn, gbc);
        gbc.gridy = 3;
        menuPanel.add(joinGameBtn, gbc);
        gbc.gridy = 4;
        menuPanel.add(exitBtn, gbc);
        
        mainFrame.setContentPane(menuPanel);
        mainFrame.revalidate();
    }
    
    private void startSinglePlayerGame() {
        gamePanel = new GamePanel(false);
        mainFrame.setContentPane(gamePanel);
        mainFrame.revalidate();
        gamePanel.requestFocus();
        gamePanel.startGame();
    }
    
    private void showHostGameDialog() {
        String port = JOptionPane.showInputDialog(mainFrame, "请输入端口号:", "创建房间", JOptionPane.QUESTION_MESSAGE);
        if (port != null && !port.trim().isEmpty()) {
            try {
                int portNum = Integer.parseInt(port.trim());
                networkManager = new NetworkManager();
                if (networkManager.startServer(portNum)) {
                    startMultiplayerGame(true);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "创建房间失败!", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "端口号格式不正确!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showJoinGameDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("服务器地址:"));
        JTextField hostField = new JTextField("localhost");
        panel.add(hostField);
        panel.add(new JLabel("端口号:"));
        JTextField portField = new JTextField("8080");
        panel.add(portField);
        
        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "加入房间", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String host = hostField.getText().trim();
                int port = Integer.parseInt(portField.getText().trim());
                networkManager = new NetworkManager();
                if (networkManager.connectToServer(host, port)) {
                    startMultiplayerGame(false);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "连接服务器失败!", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "端口号格式不正确!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void startMultiplayerGame(boolean isHost) {
        gamePanel = new GamePanel(true, networkManager, isHost);
        mainFrame.setContentPane(gamePanel);
        mainFrame.revalidate();
        gamePanel.requestFocus();
        gamePanel.startGame();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new TankBattleGame();
        });
    }
}
