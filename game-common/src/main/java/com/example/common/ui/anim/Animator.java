package com.example.common.ui.anim;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * 简单的基于Timer的60FPS动画器，提供t∈[0,1]的插值回调。
 */
public class Animator {
    private final Timer timer;
    private final long start;
    private final int duration;

    public Animator(int durationMs, Consumer<Float> updater, Runnable onComplete) {
        this.duration = durationMs;
        this.start = System.currentTimeMillis();
        int delay = 1000 / 60;
        this.timer = new Timer(delay, null);
        this.timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float t = (System.currentTimeMillis() - start) / (float) duration;
                if (t >= 1f) {
                    t = 1f;
                }
                updater.accept(t);
                if (t >= 1f) {
                    timer.stop();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        });
    }

    public void start() {
        timer.start();
    }
}
