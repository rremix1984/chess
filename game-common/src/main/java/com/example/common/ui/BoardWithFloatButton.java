package com.example.common.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * A layered pane that holds a board component and a floating wooden button on
 * top-right side. The button can be accessed via {@link #getButton()}.
 */
public class BoardWithFloatButton extends JLayeredPane {
    private final JComponent board;
    private final WoodButton button = new WoodButton();

    public BoardWithFloatButton(JComponent board) {
        this.board = board;
        setLayout(null);
        add(board, JLayeredPane.DEFAULT_LAYER);
        add(button, JLayeredPane.PALETTE_LAYER);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutChildren();
            }
        });
    }

    private void layoutChildren() {
        int W = getWidth();
        int H = getHeight();
        board.setBounds(0, 0, W, H);
        int bw = button.getPreferredSize().width;
        int bh = button.getPreferredSize().height;
        int margin = 14;
        int x = W - bw - margin;
        int y = H / 2 - bh / 2;
        button.setBounds(x, y, bw, bh);
    }

    public JComponent getBoard() {
        return board;
    }

    public WoodButton getButton() {
        return button;
    }
}
