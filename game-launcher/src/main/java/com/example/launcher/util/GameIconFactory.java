package com.example.launcher.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Factory for game icons. It tries to load an icon from assets/icons/{game}.png.
 * If the resource is missing, a simple fallback icon will be drawn using Java2D.
 */
public final class GameIconFactory {
    private GameIconFactory() {
    }

    /**
     * Creates an icon for the given game type.
     *
     * @param gameType type code such as "chinese-chess".
     * @param size icon size in pixels.
     * @return icon instance
     */
    public static Icon icon(String gameType, int size) {
        String path = "/assets/icons/" + gameType + ".png";
        URL url = GameIconFactory.class.getResource(path);
        if (url != null) {
            Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return new FallbackGameIcon(gameType, size);
    }

    /**
     * Simple placeholder icon drawn with Java2D for games without assets.
     */
    private static class FallbackGameIcon implements Icon {
        private final String gameType;
        private final int size;

        FallbackGameIcon(String gameType, int size) {
            this.gameType = gameType;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            switch (gameType) {
                case "chinese-chess":
                    g2.setColor(Color.RED);
                    g2.fillOval(0, 0, size, size);
                    g2.setColor(Color.WHITE);
                    g2.drawString("帅", size / 4, (int) (size * 0.7));
                    break;
                case "international-chess":
                    g2.setColor(Color.BLACK);
                    g2.fill(new RoundRectangle2D.Double(0, size / 4.0, size, size / 2.0, size / 5.0, size / 5.0));
                    g2.fillOval(size / 4, 0, size / 2, size / 2);
                    break;
                case "gomoku":
                case "go-game":
                    g2.setColor(Color.LIGHT_GRAY);
                    for (int i = 0; i < size; i += size / 4) {
                        g2.drawLine(i, 0, i, size);
                        g2.drawLine(0, i, size, i);
                    }
                    g2.setColor(Color.BLACK);
                    g2.fillOval(size / 3, size / 3, size / 3, size / 3);
                    break;
                case "tank-battle-game":
                    g2.setColor(Color.GREEN.darker());
                    g2.fillRect(0, size / 3, size, size / 3);
                    g2.fillRect(size / 2 - size / 8, size / 6, size / 4, size / 3);
                    g2.setColor(Color.BLACK);
                    g2.fillRect(size / 2 - 1, size / 6, size / 2, size / 10);
                    break;
                case "monopoly":
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, size, size);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(0, 0, size - 1, size - 1);
                    g2.drawString("5", size / 3, size / 2);
                    break;
                case "flight-chess":
                    g2.setColor(Color.CYAN.darker());
                    int bodyW = size / 6;
                    int center = size / 2;
                    g2.fillRect(center - bodyW / 2, 0, bodyW, size); // fuselage
                    g2.fillRect(0, center - bodyW / 2, size, bodyW); // wings
                    g2.setColor(Color.CYAN);
                    g2.fillOval(center - bodyW, center - bodyW, bodyW * 2, bodyW * 2); // cockpit
                    break;
                default:
                    g2.setColor(Color.GRAY);
                    g2.fillRect(0, 0, size, size);
                    break;
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
