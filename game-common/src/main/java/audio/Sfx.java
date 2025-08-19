package audio;

import javax.sound.sampled.*;
import java.io.File;
import java.util.*;

/**
 * Stone-on-wood sound effect manager. It prefers loading external WAV samples
 * (sfx/go_tick_XX.wav and sfx/go_knock_XX.wav) and falls back to a procedural
 * synthesiser when samples are unavailable. Each call plays a layered tick and
 * knock with subtle randomisation to avoid the "machine gun" effect.
 */
public final class Sfx {
    private static final List<Clip> TICKS = new ArrayList<>();
    private static final List<Clip> KNOCKS = new ArrayList<>();
    private static float masterGainDb = 0f;

    private Sfx() {}

    /** Preload sample banks. Call during application startup. */
    public static void init() {
        if (!TICKS.isEmpty() || !KNOCKS.isEmpty()) return;
        preloadBatch(TICKS, "sfx/go_tick_", 6);
        preloadBatch(KNOCKS, "sfx/go_knock_", 6);
    }

    private static void preloadBatch(List<Clip> bank, String prefix, int n) {
        for (int i = 1; i <= n; i++) {
            String path = prefix + String.format("%02d.wav", i);
            Clip c = loadClip(path);
            if (c != null) bank.add(c);
        }
    }

    private static Clip loadClip(String path) {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(new File(path))) {
            DataLine.Info info = new DataLine.Info(Clip.class, in.getFormat());
            Clip c = (Clip) AudioSystem.getLine(info);
            c.open(in);
            return c;
        } catch (Exception e) {
            System.err.println("[SFX] Load fail: " + path + " -> " + e.getMessage());
            return null; // allow fallback
        }
    }

    public static void setMasterGainDb(float db) { masterGainDb = db; }

    /**
     * Play a layered stone drop sound. Power should be between 0 and 1 and
     * roughly correspond to the perceived strength of the move.
     */
    public static void playStoneOnWood(float power01) {
        if (TICKS.isEmpty() && KNOCKS.isEmpty()) init();
        if (!TICKS.isEmpty() && !KNOCKS.isEmpty()) {
            playFromSamples(power01);
        } else {
            SynthStoneSfx.playSynthetics(power01);
        }
    }

    private static void playFromSamples(float power01) {
        Clip tick = pick(TICKS);
        Clip knock = pick(KNOCKS);

        float volRnd = (float)(Math.random()*3f - 1.5f); // ±1.5 dB
        setGain(tick, masterGainDb + volRnd + lerp(-1.0f, 1.5f, power01));
        setPan(tick, (float)(Math.random()*0.10 - 0.05));
        restart(tick);

        int delayMs = 25 + (int)(Math.random()*15); // 25–40ms later play knock layer
        new javax.swing.Timer(delayMs, e -> {
            float volRnd2 = (float)(Math.random()*3f - 1.5f);
            setGain(knock, masterGainDb - 1.0f + volRnd2 + lerp(-0.5f, 1.0f, power01));
            setPan(knock, (float)(Math.random()*0.10 - 0.05));
            restart(knock);
            ((javax.swing.Timer)e.getSource()).stop();
        }).start();
    }

    private static <T> T pick(List<T> list) {
        return list.get((int)(Math.random()*list.size()));
    }

    private static void restart(Clip c) {
        if (c.isRunning()) c.stop();
        c.setFramePosition(0);
        c.start();
    }

    private static void setGain(Clip c, float gainDb) {
        try {
            if (c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl g = (FloatControl)c.getControl(FloatControl.Type.MASTER_GAIN);
                float clamped = Math.max(g.getMinimum(), Math.min(g.getMaximum(), gainDb));
                g.setValue(clamped);
            }
        } catch (Exception ignore) {}
    }

    private static void setPan(Clip c, float pan) {
        try {
            if (c.isControlSupported(FloatControl.Type.PAN)) {
                FloatControl p = (FloatControl)c.getControl(FloatControl.Type.PAN);
                p.setValue(Math.max(-1f, Math.min(1f, pan)));
            }
        } catch (Exception ignore) {}
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
}

