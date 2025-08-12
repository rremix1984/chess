import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * 纯 Swing 游戏启动器
 * 完全避免 JavaFX，解决 macOS 崩溃问题
 */
public class SwingGameLauncher extends JFrame {
    
    private static final String PROJECT_PATH = "/Users/wangxiaozhe/workspace/chinese-chess-game";
    
    public SwingGameLauncher() {
        initializeUI();
        setupMacOSIntegration();
    }
    
    private void initializeUI() {
        setTitle("多游戏平台 - 纯 Swing 版 (macOS 兼容)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        
        // 设置 macOS 风格的外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("无法设置系统外观，使用默认外观");
        }
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 220)); // 米色背景
        
        // 标题
        JLabel titleLabel = new JLabel("🎮 游戏中心 - macOS 稳定版", SwingConstants.CENTER);
        titleLabel.setFont(new Font("PingFang SC", Font.BOLD, 24));
        titleLabel.setForeground(new Color(139, 69, 19));
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        // 游戏按钮面板
        JPanel gamePanel = createGamePanel();
        
        // 状态栏
        JPanel statusPanel = createStatusPanel();
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new GridLayout(3, 3, 20, 20));
        panel.setBackground(new Color(245, 245, 220));
        panel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // 街头霸王按钮（主要焦点）
        JButton sfButton = createGameButton(
            "👊 街头霸王", 
            "激烈的格斗游戏",
            new Color(220, 20, 60),
            this::launchStreetFighter
        );
        
        // 街头霸王增强版
        JButton sfNewButton = createGameButton(
            "🥊 街头霸王增强版", 
            "全新的街头霸王体验",
            new Color(255, 69, 0),
            this::launchStreetFighterNew
        );
        
        // 其他游戏按钮
        JButton chessButton = createGameButton(
            "🏮 中国象棋", 
            "传统的中国象棋游戏",
            new Color(139, 69, 19),
            this::launchChineseChess
        );
        
        JButton goButton = createGameButton(
            "⚫⚪ 围棋", 
            "古老的策略棋盘游戏",
            new Color(47, 79, 79),
            this::launchGo
        );
        
        JButton flightButton = createGameButton(
            "✈️ 飞行棋", 
            "有趣的家庭友好型桌面游戏",
            new Color(30, 144, 255),
            this::launchFlightChess
        );
        
        JButton tankButton = createGameButton(
            "🚗 坦克大战", 
            "经典的坦克对战游戏",
            new Color(34, 139, 34),
            this::launchTankBattle
        );
        
        JButton systemButton = createGameButton(
            "🔧 系统检查", 
            "检查系统状态和设置",
            new Color(75, 0, 130),
            this::runSystemCheck
        );
        
        // 空白占位符
        JPanel placeholder1 = new JPanel();
        placeholder1.setBackground(new Color(245, 245, 220));
        JPanel placeholder2 = new JPanel();
        placeholder2.setBackground(new Color(245, 245, 220));
        
        // 按行添加按钮
        panel.add(sfButton);      // 第1行
        panel.add(sfNewButton);
        panel.add(chessButton);
        
        panel.add(goButton);      // 第2行
        panel.add(flightButton);
        panel.add(tankButton);
        
        panel.add(systemButton);  // 第3行
        panel.add(placeholder1);
        panel.add(placeholder2);
        
        return panel;
    }
    
    private JButton createGameButton(String title, String description, Color bgColor, Runnable action) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // 标题标签
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("PingFang SC", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        // 描述标签
        JLabel descLabel = new JLabel(description, SwingConstants.CENTER);
        descLabel.setFont(new Font("PingFang SC", Font.PLAIN, 12));
        descLabel.setForeground(Color.WHITE);
        
        button.add(titleLabel, BorderLayout.CENTER);
        button.add(descLabel, BorderLayout.SOUTH);
        
        // 鼠标悬停效果
        Color originalColor = bgColor;
        Color hoverColor = bgColor.brighter();
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
        
        button.addActionListener(e -> {
            button.setEnabled(false);
            button.setText("启动中...");
            SwingUtilities.invokeLater(() -> {
                try {
                    action.run();
                } finally {
                    button.setEnabled(true);
                    button.add(titleLabel, BorderLayout.CENTER);
                    button.add(descLabel, BorderLayout.SOUTH);
                    button.revalidate();
                }
            });
        });
        
        return button;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 220));
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JLabel statusLabel = new JLabel("✅ 纯 Swing 界面 - macOS JavaFX 崩溃问题已解决", SwingConstants.CENTER);
        statusLabel.setFont(new Font("PingFang SC", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0, 128, 0));
        
        JLabel javaLabel = new JLabel("Java 版本: " + System.getProperty("java.version"), SwingConstants.CENTER);
        javaLabel.setFont(new Font("PingFang SC", Font.PLAIN, 10));
        javaLabel.setForeground(Color.GRAY);
        
        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(javaLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupMacOSIntegration() {
        // 针对 macOS 的特殊设置
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.awt.application.name", "GameCenter");
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
    }
    
    // 游戏启动方法
    private void launchStreetFighter() {
        showMessage("正在启动街头霸王...");
        executeCommand(new String[]{
            "bash", "-c", 
            "cd " + PROJECT_PATH + "/StreetFighter && mvn javafx:run"
        });
    }
    
    private void launchStreetFighterNew() {
        showMessage("正在启动街头霸王增强版...");
        executeCommand(new String[]{
            "bash", "-c", 
            "cd " + PROJECT_PATH + "/StreetFighterNew && mvn javafx:run"
        });
    }
    
    private void launchChineseChess() {
        showMessage("正在启动中国象棋...");
        executeCommand(new String[]{
            "java", "-cp", PROJECT_PATH + "/game-launcher/target/game-launcher-1.0-SNAPSHOT.jar",
            "com.example.chinesechess.ChineseChessMain"
        });
    }
    
    private void launchGo() {
        showMessage("正在启动围棋...");
        executeCommand(new String[]{
            "java", "-cp", PROJECT_PATH + "/game-launcher/target/game-launcher-1.0-SNAPSHOT.jar",
            "com.example.go.GoFrame"
        });
    }
    
    private void launchFlightChess() {
        showMessage("正在启动飞行棋...");
        executeCommand(new String[]{
            "java", "-cp", PROJECT_PATH + "/game-launcher/target/game-launcher-1.0-SNAPSHOT.jar",
            "com.example.flightchess.FlightChessFrame"
        });
    }
    
    private void launchTankBattle() {
        showMessage("正在启动坦克大战...");
        executeCommand(new String[]{
            "java", "-cp", PROJECT_PATH + "/game-launcher/target/game-launcher-1.0-SNAPSHOT.jar",
            "com.tankbattle.TankBattleGame"
        });
    }
    
    private void runSystemCheck() {
        showMessage("正在运行系统检查...");
        executeCommand(new String[]{
            "bash", PROJECT_PATH + "/check_system_status.sh"
        });
    }
    
    private void executeCommand(String[] command) {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(new File(PROJECT_PATH));
                Process process = pb.start();
                
                SwingUtilities.invokeLater(() -> 
                    showMessage("游戏启动成功！(进程 ID: " + process.pid() + ")"));
                    
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> 
                    showError("启动失败: " + e.getMessage()));
            }
        }).start();
    }
    
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "信息", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        // 设置 macOS 系统属性
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.awt.application.name", "GameCenter");
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                new SwingGameLauncher().setVisible(true);
            } catch (Exception e) {
                System.err.println("启动器启动失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
