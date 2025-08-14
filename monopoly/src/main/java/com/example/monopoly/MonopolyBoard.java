package com.example.monopoly;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大富翁游戏棋盘
 */
public class MonopolyBoard extends JPanel {
    
    private static final int BOARD_SIZE = 600;
    private static final int CELL_SIZE = 80;
    private static final int INNER_SIZE = BOARD_SIZE - 2 * CELL_SIZE;
    
    // 棋盘格子名称和价格
    private String[] cellNames = {
        "起点", "中山路", "公益金", "延安路", "所得税", "火车站", "静安区", "机会", "黄浦区", "浦东大道",
        "监狱", "南京路", "电力公司", "西藏路", "人民路", "长途汽车", "天津路", "公益金", "北京路", "上海路",
        "免费停车", "淮海路", "机会", "成都路", "重庆路", "地铁", "杭州路", "苏州路", "自来水公司", "南昌路",
        "去监狱", "广州路", "深圳路", "公益金", "北海路", "航空公司", "机会", "香港路", "奢侈税", "台北路"
    };
    
    private int[] cellValues = {
        0, 60, 0, 60, 0, 200, 100, 0, 100, 120,
        0, 140, 150, 140, 160, 200, 180, 0, 180, 200,
        0, 220, 0, 220, 240, 200, 260, 260, 150, 280,
        0, 300, 300, 0, 320, 200, 0, 350, 75, 400
    };
    
    // 颜色组
    private Color[] groupColors = {
        Color.WHITE, new Color(139, 69, 19), Color.WHITE, new Color(139, 69, 19), Color.WHITE, Color.BLACK,
        new Color(173, 216, 230), Color.WHITE, new Color(173, 216, 230), new Color(173, 216, 230),
        Color.WHITE, Color.MAGENTA, Color.WHITE, Color.MAGENTA, Color.MAGENTA, Color.BLACK,
        Color.ORANGE, Color.WHITE, Color.ORANGE, Color.ORANGE,
        Color.WHITE, Color.RED, Color.WHITE, Color.RED, Color.RED, Color.BLACK,
        Color.YELLOW, Color.YELLOW, Color.WHITE, Color.YELLOW,
        Color.WHITE, new Color(34, 139, 34), new Color(34, 139, 34), Color.WHITE, new Color(34, 139, 34), Color.BLACK,
        Color.WHITE, new Color(25, 25, 112), Color.WHITE, new Color(25, 25, 112)
    };
    
    private Map<Player, Point> playerPositions = new HashMap<>();
    private BufferedImage backgroundImage;
    
    public MonopolyBoard() {
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setBackground(new Color(245, 245, 220));
        setBorder(BorderFactory.createRaisedBevelBorder());
        
        // 加载背景图片
        loadBackgroundImage();
    }
    
    /**
     * 加载背景图片
     */
    private void loadBackgroundImage() {
        try {
            // 尝试多个可能的路径和格式来查找背景图片
            String[] possiblePaths = {
                "1.png",   // PNG格式（优先）
                "1.jpg",   // JPG格式
                "1.jpeg",  // JPEG格式
                "1.webp",  // WEBP格式
                "../1.png", "../1.jpg", "../1.jpeg", "../1.webp",  // 上级目录
                "../../1.png", "../../1.jpg", "../../1.jpeg", "../../1.webp",  // 上两级目录
                "/Users/wangxiaozhe/workspace/chinese-chess-game/1.png",  // 绝对路径 PNG
                "/Users/wangxiaozhe/workspace/chinese-chess-game/1.jpg",  // 绝对路径 JPG
                "/Users/wangxiaozhe/workspace/chinese-chess-game/1.webp", // 绝对路径 WEBP
                System.getProperty("user.dir") + "/1.png",   // 当前目录 PNG
                System.getProperty("user.dir") + "/1.jpg",   // 当前目录 JPG
                System.getProperty("user.dir") + "/1.webp",  // 当前目录 WEBP
                System.getProperty("user.dir") + "/../1.png", // 上级目录 PNG
                System.getProperty("user.dir") + "/../1.jpg", // 上级目录 JPG
                System.getProperty("user.dir") + "/../1.webp" // 上级目录 WEBP
            };
            
            File imageFile = null;
            for (String path : possiblePaths) {
                File testFile = new File(path);
                System.out.println("正在尝试路径: " + testFile.getAbsolutePath());
                if (testFile.exists()) {
                    imageFile = testFile;
                    break;
                }
            }
            
            if (imageFile != null) {
                backgroundImage = ImageIO.read(imageFile);
                if (backgroundImage != null) {
                    System.out.println("✅ 背景图片加载成功: " + imageFile.getAbsolutePath());
                    System.out.println("图片尺寸: " + backgroundImage.getWidth() + "x" + backgroundImage.getHeight());
                } else {
                    System.err.println("❌ 图片文件存在但无法解析: " + imageFile.getAbsolutePath());
                }
            } else {
                System.out.println("⚠️ 背景图片未找到，尝试了以下路径:");
                for (String path : possiblePaths) {
                    System.out.println("  - " + new File(path).getAbsolutePath());
                }
            }
        } catch (IOException e) {
            System.err.println("❌ 背景图片加载失败: " + e.getMessage());
            backgroundImage = null;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 绘制棋盘
        drawBoard(g2d);
        
        // 绘制中央logo
        drawCenterLogo(g2d);
        
        // 绘制玩家
        drawPlayers(g2d);
        
        g2d.dispose();
    }
    
    private void drawBoard(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int boardSize = Math.min(width, height) - 20;
        int cellSize = boardSize / 11;
        
        int startX = centerX - boardSize / 2;
        int startY = centerY - boardSize / 2;
        
        // 绘制外边框
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawRect(startX, startY, boardSize, boardSize);
        
        // 绘制各个格子
        for (int i = 0; i < 40; i++) {
            Point cellPosition = getCellPosition(i, startX, startY, cellSize, boardSize);
            drawCell(g2d, i, cellPosition.x, cellPosition.y, cellSize);
        }
    }
    
    private void drawCell(Graphics2D g2d, int index, int x, int y, int cellSize) {
        // 设置格子背景色
        g2d.setColor(groupColors[index]);
        g2d.fillRect(x, y, cellSize, cellSize);
        
        // 绘制格子边框
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(x, y, cellSize, cellSize);
        
        // 特殊格子的装饰
        drawSpecialCellDecorations(g2d, index, x, y, cellSize);
        
        // 绘制格子名称
        drawCellName(g2d, index, x, y, cellSize);
        
        // 绘制价格（如果有）
        if (cellValues[index] > 0) {
            drawCellPrice(g2d, cellValues[index], x, y, cellSize);
        }
    }
    
    private void drawSpecialCellDecorations(Graphics2D g2d, int index, int x, int y, int cellSize) {
        g2d.setColor(Color.BLACK);
        
        switch (index) {
            case 0: // 起点
                g2d.setColor(new Color(255, 215, 0)); // 金色
                g2d.fillOval(x + cellSize/4, y + cellSize/4, cellSize/2, cellSize/2);
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("宋体", Font.BOLD, 20));
                FontMetrics fm = g2d.getFontMetrics();
                String goText = "GO";
                int textX = x + (cellSize - fm.stringWidth(goText)) / 2;
                int textY = y + cellSize/2 + fm.getAscent()/2;
                g2d.drawString(goText, textX, textY);
                break;
                
            case 10: // 监狱
                g2d.setColor(new Color(169, 169, 169)); // 灰色
                g2d.fillRect(x + 10, y + 10, cellSize - 20, cellSize - 20);
                g2d.setColor(Color.BLACK);
                // 绘制铁窗效果
                for (int i = 0; i < 4; i++) {
                    g2d.drawLine(x + 15 + i * 10, y + 10, x + 15 + i * 10, y + cellSize - 10);
                }
                break;
                
            case 20: // 免费停车
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("宋体", Font.BOLD, 30));
                g2d.drawString("P", x + cellSize/2 - 10, y + cellSize/2 + 5);
                break;
                
            case 30: // 去监狱
                g2d.setColor(Color.BLUE);
                // 绘制箭头指向监狱
                int[] xPoints = {x + cellSize/4, x + 3*cellSize/4, x + cellSize/2};
                int[] yPoints = {y + cellSize/2, y + cellSize/2, y + 3*cellSize/4};
                g2d.fillPolygon(xPoints, yPoints, 3);
                break;
        }
    }
    
    private void drawCellName(Graphics2D g2d, int index, int x, int y, int cellSize) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("宋体", Font.PLAIN, 10));
        
        String name = cellNames[index];
        FontMetrics fm = g2d.getFontMetrics();
        
        // 根据格子位置调整文字方向
        if (index <= 10) { // 底边，文字水平
            int textX = x + (cellSize - fm.stringWidth(name)) / 2;
            int textY = y + cellSize - 25;
            g2d.drawString(name, textX, textY);
        } else if (index <= 20) { // 左边，文字垂直
            Graphics2D g2dRotated = (Graphics2D) g2d.create();
            g2dRotated.rotate(-Math.PI/2, x + cellSize/2, y + cellSize/2);
            int textX = x + (cellSize - fm.stringWidth(name)) / 2;
            int textY = y + cellSize - 25;
            g2dRotated.drawString(name, textX, textY);
            g2dRotated.dispose();
        } else if (index <= 30) { // 顶边，文字水平但倒置
            Graphics2D g2dRotated = (Graphics2D) g2d.create();
            g2dRotated.rotate(Math.PI, x + cellSize/2, y + cellSize/2);
            int textX = x + (cellSize - fm.stringWidth(name)) / 2;
            int textY = y + cellSize - 25;
            g2dRotated.drawString(name, textX, textY);
            g2dRotated.dispose();
        } else { // 右边，文字垂直
            Graphics2D g2dRotated = (Graphics2D) g2d.create();
            g2dRotated.rotate(Math.PI/2, x + cellSize/2, y + cellSize/2);
            int textX = x + (cellSize - fm.stringWidth(name)) / 2;
            int textY = y + cellSize - 25;
            g2dRotated.drawString(name, textX, textY);
            g2dRotated.dispose();
        }
    }
    
    private void drawCellPrice(Graphics2D g2d, int price, int x, int y, int cellSize) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("宋体", Font.PLAIN, 8));
        String priceText = "$" + price;
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (cellSize - fm.stringWidth(priceText)) / 2;
        int textY = y + 12;
        g2d.drawString(priceText, textX, textY);
    }
    
    private void drawCenterLogo(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int logoSize = Math.min(width, height) / 3;
        
        // 如果有背景图片，绘制背景图片
        if (backgroundImage != null) {
            // 计算图片缩放比例，保持纵横比
            double scaleX = (double) logoSize / backgroundImage.getWidth();
            double scaleY = (double) logoSize / backgroundImage.getHeight();
            double scale = Math.min(scaleX, scaleY); // 保持原始纵横比
            
            int scaledWidth = (int)(backgroundImage.getWidth() * scale);
            int scaledHeight = (int)(backgroundImage.getHeight() * scale);
            
            int imgX = centerX - scaledWidth / 2;
            int imgY = centerY - scaledHeight / 2;
            
            // 绘制缩放后的背景图片
            g2d.drawImage(backgroundImage, imgX, imgY, scaledWidth, scaledHeight, this);
            
            // 在图片上方添加半透明的标题背景
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
            g2d.setColor(new Color(255, 255, 255, 200));
            int titleBgHeight = 60;
            g2d.fillRect(centerX - logoSize/2, centerY - titleBgHeight/2, logoSize, titleBgHeight);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            
            // 绘制边框
            g2d.setColor(new Color(139, 69, 19));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(centerX - logoSize/2, centerY - logoSize/2, logoSize, logoSize);
            
        } else {
            // 如果没有背景图片，使用原来的纯色背景
            g2d.setColor(new Color(255, 250, 240));
            g2d.fillRect(centerX - logoSize/2, centerY - logoSize/2, logoSize, logoSize);
            
            // 绘制边框
            g2d.setColor(new Color(139, 69, 19));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(centerX - logoSize/2, centerY - logoSize/2, logoSize, logoSize);
        }
        
        // 绘制标题（使用深色和阴影增强可读性）
        // 文字阴影效果
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 28));
        String title = "大富翁";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = centerX - fm.stringWidth(title) / 2;
        int textY = centerY - 5;
        g2d.drawString(title, textX + 2, textY + 2); // 阴影
        
        // 正式文字
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawString(title, textX, textY);
        
        // 绘制副标题
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String subtitle = "MONOPOLY";
        fm = g2d.getFontMetrics();
        textX = centerX - fm.stringWidth(subtitle) / 2;
        textY = centerY + 25;
        g2d.drawString(subtitle, textX + 1, textY + 1); // 阴影
        
        // 副标题正式文字
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawString(subtitle, textX, textY);
    }
    
    private void drawPlayers(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int boardSize = Math.min(width, height) - 20;
        int cellSize = boardSize / 11;
        int startX = centerX - boardSize / 2;
        int startY = centerY - boardSize / 2;
        
        for (Map.Entry<Player, Point> entry : playerPositions.entrySet()) {
            Player player = entry.getKey();
            Point cellPosition = getCellPosition(player.getPosition(), startX, startY, cellSize, boardSize);
            
            // 为每个玩家在格子内计算不同的偏移位置，避免重叠
            int playerIndex = getPlayerIndex(player);
            int offsetX = (playerIndex % 2) * 15;
            int offsetY = (playerIndex / 2) * 15;
            
            // 绘制玩家棋子
            g2d.setColor(player.getColor());
            g2d.fillOval(cellPosition.x + 10 + offsetX, cellPosition.y + 10 + offsetY, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(cellPosition.x + 10 + offsetX, cellPosition.y + 10 + offsetY, 15, 15);
            
            // 绘制玩家编号
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("宋体", Font.BOLD, 10));
            String playerNum = String.valueOf(playerIndex + 1);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = cellPosition.x + 17 + offsetX - fm.stringWidth(playerNum) / 2;
            int textY = cellPosition.y + 20 + offsetY;
            g2d.drawString(playerNum, textX, textY);
        }
    }
    
    private Point getCellPosition(int cellIndex, int startX, int startY, int cellSize, int boardSize) {
        if (cellIndex <= 10) {
            // 底边：从右到左
            return new Point(startX + boardSize - cellSize - (cellIndex * cellSize), 
                           startY + boardSize - cellSize);
        } else if (cellIndex <= 20) {
            // 左边：从底到顶
            int leftIndex = cellIndex - 11;
            return new Point(startX, 
                           startY + boardSize - cellSize - cellSize - (leftIndex * cellSize));
        } else if (cellIndex <= 30) {
            // 顶边：从左到右
            int topIndex = cellIndex - 21;
            return new Point(startX + cellSize + (topIndex * cellSize), startY);
        } else {
            // 右边：从顶到底
            int rightIndex = cellIndex - 31;
            return new Point(startX + boardSize - cellSize, 
                           startY + cellSize + (rightIndex * cellSize));
        }
    }
    
    private int getPlayerIndex(Player player) {
        // 简单的方法来获取玩家索引（基于名称）
        String name = player.getName();
        if (name.equals("玩家1")) return 0;
        if (name.equals("玩家2")) return 1;
        if (name.equals("玩家3")) return 2;
        if (name.equals("玩家4")) return 3;
        return 0;
    }
    
    public void updatePlayerPosition(Player player) {
        playerPositions.put(player, new Point(0, 0)); // 位置会在绘制时计算
        repaint();
    }
    
    public String getLandName(int position) {
        if (position >= 0 && position < cellNames.length) {
            return cellNames[position];
        }
        return "未知";
    }
    
    public int getLandValue(int position) {
        if (position >= 0 && position < cellValues.length) {
            return cellValues[position];
        }
        return 0;
    }
}
