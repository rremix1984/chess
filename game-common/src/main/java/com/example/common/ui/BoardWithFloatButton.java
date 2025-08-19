package com.example.common.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * A layered pane that holds a board component and a floating wooden button
 * for toggling fullscreen. The button stays docked to the board's right edge
 * and switches its icon based on the fullscreen state.
 */
public class BoardWithFloatButton extends JLayeredPane {
    public enum Position { RIGHT_MIDDLE, BOTTOM_RIGHT }

    private final JComponent board;
    private final WoodButton toggleButton = new WoodButton();
    private final Position position;
    private boolean fullscreen;

    public BoardWithFloatButton(JComponent board) {
        this(board, Position.RIGHT_MIDDLE);
    }

    public BoardWithFloatButton(JComponent board, Position position) {
        this.board = board;
        this.position = position;
        setLayout(null);
        add(board, JLayeredPane.DEFAULT_LAYER);
        add(toggleButton, JLayeredPane.PALETTE_LAYER);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutChildren();
            }
        });
        setFullscreen(false);
    }
    @Override
    public Dimension getPreferredSize() {
        return board.getPreferredSize();
    }

    private void layoutChildren() {
        int W = getWidth();
        int H = getHeight();
        board.setBounds(0, 0, W, H);
        int bw = toggleButton.getPreferredSize().width;
        int bh = toggleButton.getPreferredSize().height;
        int margin = 14;
        int x = W - bw - margin;
        int y;
        if (position == Position.BOTTOM_RIGHT) {
            y = H - bh - margin;
        } else { // RIGHT_MIDDLE
            y = H / 2 - bh / 2;
        }
        toggleButton.setBounds(x, y, bw, bh);
    }

    public JComponent getBoard() {
        return board;
    }

    public WoodButton getFullscreenButton() { return toggleButton; }

    public void setFullscreen(boolean fs) {
        fullscreen = fs;
        if (fs) {
            toggleButton.setTextLabel("┘└\n┐┌");
        } else {
            toggleButton.setTextLabel("┌ ┐\n└ ┘");
        }
    }

}
