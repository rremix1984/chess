package audio;

import javax.sound.sampled.*;
import java.util.*;

/**
 * Simple sound effect manager for Go stone hits. It procedurally generates
 * short "tick" and "knock" clips so that no external binary assets are
 * required. Each call plays a random variation with subtle gain and pan
 * differences to avoid repetition.
 */
public final class Sfx {
    private static final int SAMPLE_RATE = 44_100;
    private static final List<Clip> TICKS = new ArrayList<>();
    private static final List<Clip> KNOCKS = new ArrayList<>();
    private static float masterGainDb = 0f;
    private static boolean initialised = false;

    private Sfx() {}

    /** Prepare procedural clips. */
    public static void init() {
        if (initialised) return;
        generateBatch(TICKS, true, 6);
        generateBatch(KNOCKS, false, 6);
        initialised = true;
    }

    private static void generateBatch(List<Clip> bank, boolean tick, int n) {
        for (int i = 0; i < n; i++) {
            bank.add(generateClip(tick));
        }
    }

    /**
     * Create a short audio clip. Tick clips are bright, high frequency and
     * very short; knock clips are lower and decay more slowly.
     */
    private static Clip generateClip(boolean tick) {
        try {
            int samples = (int)(SAMPLE_RATE * (tick ? 0.025 : 0.18));
            byte[] data = new byte[samples * 2];
            double freq = tick ? 8_000 + Math.random() * 4_000
                               : 220 + Math.random() * 120;
            for (int i = 0; i < samples; i++) {
                double t = i / (double) SAMPLE_RATE;
                double env = Math.exp(-t * (tick ? 70 : 8));
                double sample;
                if (tick) {
                    // brighter click: add harmonic and reduce noise
                    sample = (Math.sin(2 * Math.PI * freq * t)
                             + Math.sin(2 * Math.PI * freq * 1.7 * t) * 0.3
                             + (Math.random() * 2 - 1) * 0.05) * env;
                } else {
                    // subtle second harmonic for wood resonance
                    sample = (Math.sin(2 * Math.PI * freq * t)
                             + Math.sin(2 * Math.PI * freq * 2 * t) * 0.25) * env;
                }
                int val = (int)(sample * Short.MAX_VALUE);
                data[i * 2] = (byte)(val & 0xff);
                data[i * 2 + 1] = (byte)((val >>> 8) & 0xff);
            }
            AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            Clip c = AudioSystem.getClip();
            c.open(fmt, data, 0, data.length);
            return c;
        } catch (LineUnavailableException e) {
            throw new RuntimeException("Generate SFX fail", e);
        }
    }

    public static void setMasterGainDb(float db) { masterGainDb = db; }

    /** Play layered stone-on-wood sound. */
    public static void playStoneOnWood(float power01) {
        if (!initialised) init();
        Clip tick = pick(TICKS);
        Clip knock = pick(KNOCKS);

        float rndVol = (float) (Math.random() * 3f - 1.5f); // ±1.5dB
        setGain(tick, masterGainDb + rndVol + lerp(-1.0f, 1.5f, power01));
        setPan(tick, (float)(Math.random()*0.10 - 0.05));
        restart(tick);

        int delayMs = 25 + (int)(Math.random()*15);
        new javax.swing.Timer(delayMs, e -> {
            float rndVol2 = (float)(Math.random()*3f - 1.5f);
            setGain(knock, masterGainDb - 1.0f + rndVol2 + lerp(-0.5f, 1.0f, power01));
            setPan(knock, (float)(Math.random()*0.10 - 0.05));
            restart(knock);
            ((javax.swing.Timer)e.getSource()).stop();
        }).start();
    }

    private static void restart(Clip c) {
        if (c.isRunning()) c.stop();
        c.setFramePosition(0);
        c.start();
    }

    private static void setGain(Clip c, float gainDb) {
        if (c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl g = (FloatControl)c.getControl(FloatControl.Type.MASTER_GAIN);
            gainDb = clamp(gainDb, g.getMinimum(), g.getMaximum());
            g.setValue(gainDb);
        }
    }

    private static void setPan(Clip c, float pan) {
        if (c.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl p = (FloatControl)c.getControl(FloatControl.Type.PAN);
            p.setValue(clamp(pan, -1f, 1f));
        }
    }

    private static float clamp(float v, float a, float b) {
        return Math.max(a, Math.min(b, v));
    }

    private static <T> T pick(List<T> list) {
        return list.get((int)(Math.random() * list.size()));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
