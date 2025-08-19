package com.example.common.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * A layered pane that holds a board component and two floating wooden buttons
 * for entering and exiting fullscreen. Buttons stay docked to the board's
 * right edge.
 */
public class BoardWithFloatButton extends JLayeredPane {
    public enum Position { RIGHT_MIDDLE, BOTTOM_RIGHT }

    private final JComponent board;
    private final WoodButton enterButton = new WoodButton();
    private final WoodButton exitButton  = new WoodButton();
    private final Position position;

    public BoardWithFloatButton(JComponent board) {
        this(board, Position.RIGHT_MIDDLE);
    }

    public BoardWithFloatButton(JComponent board, Position position) {
        this.board = board;
        this.position = position;
        setLayout(null);
        add(board, JLayeredPane.DEFAULT_LAYER);
        enterButton.setTextLabel("┌ ┐\n└ ┘");
        exitButton.setTextLabel("┘└\n┐┌");
        add(enterButton, JLayeredPane.PALETTE_LAYER);
        add(exitButton, JLayeredPane.PALETTE_LAYER);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutChildren();
            }
        });
    }
    @Override
    public Dimension getPreferredSize() {
        return board.getPreferredSize();
    }

    private void layoutChildren() {
        int W = getWidth();
        int H = getHeight();
        board.setBounds(0, 0, W, H);
        int bw = enterButton.getPreferredSize().width;
        int bh = enterButton.getPreferredSize().height;
        int margin = 14;
        int spacing = 10;
        int x = W - bw - margin;
        int totalH = bh * 2 + spacing;
        int y;
        if (position == Position.BOTTOM_RIGHT) {
            y = H - totalH - margin;
        } else { // RIGHT_MIDDLE
            y = H / 2 - totalH / 2;
        }
        enterButton.setBounds(x, y, bw, bh);
        exitButton.setBounds(x, y + bh + spacing, bw, bh);
    }

    public JComponent getBoard() {
        return board;
    }

    public WoodButton getFullscreenButton() { return enterButton; }
    public WoodButton getExitButton() { return exitButton; }

}
