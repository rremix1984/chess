package com.example.common.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Utility that toggles a frame between windowed and fullscreen modes. Certain
 * components can be hidden while in fullscreen.
 */
public class FullscreenToggler {
    private final JFrame frame;
    private final JComponent[] toHide;
    private boolean fullscreen;

    private Rectangle windowedBounds;
    private int windowedState;
    private boolean windowedDecorated;

    private JSplitPane split;
    private int dividerBackup = -1;

    public FullscreenToggler(JFrame frame, JComponent... hideWhenFullscreen) {
        this.frame = frame;
        this.toHide = hideWhenFullscreen;

        // Register shortcuts
        JRootPane root = frame.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F11"), "toggleFS");
        root.getActionMap().put("toggleFS", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { toggle(); }
        });
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitFS");
        root.getActionMap().put("exitFS", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (fullscreen) toggle(); }
        });
    }

    public FullscreenToggler withSplitPane(JSplitPane sp) {
        this.split = sp;
        return this;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void toggle() {
        if (fullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    private void enterFullscreen() {
        fullscreen = true;

        windowedBounds = frame.getBounds();
        windowedState = frame.getExtendedState();
        windowedDecorated = !frame.isUndecorated();

        for (JComponent c : toHide) {
            if (c != null) c.setVisible(false);
        }

        if (split != null) {
            dividerBackup = split.getDividerLocation();
            int max = split.getMaximumDividerLocation();
            split.setDividerLocation(max);
        }

        frame.dispose();
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        bringToFront();
        frame.revalidate();
        frame.repaint();
    }

    private void exitFullscreen() {
        fullscreen = false;

        for (JComponent c : toHide) {
            if (c != null) c.setVisible(true);
        }

        if (split != null && dividerBackup >= 0) {
            split.setDividerLocation(dividerBackup);
        }

        frame.dispose();
        frame.setUndecorated(!windowedDecorated ? true : false);
        frame.setExtendedState(windowedState);
        if (windowedBounds != null) frame.setBounds(windowedBounds);
        frame.setVisible(true);
        bringToFront();
        frame.revalidate();
        frame.repaint();
    }

    /** Ensure the game window stays above any previously opened frames. */
    private void bringToFront() {
        // Temporarily toggling always-on-top is a cross-platform trick to raise the window.
        boolean aot = frame.isAlwaysOnTop();
        try {
            frame.setAlwaysOnTop(true);
            frame.toFront();
            frame.requestFocus();
        } finally {
            frame.setAlwaysOnTop(aot);
        }
    }
}
