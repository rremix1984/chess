package audio;

import javax.sound.sampled.AudioFormat;
import java.util.Random;

/**
 * Utility class generating short percussive hits using basic DSP.
 * The synthesis is intentionally simple but produces distinct
 * wood or stone like sounds depending on parameters.
 */
public final class PercSynth {
    private static final Random RND = new Random();

    private PercSynth() {}

    public static byte[] pluckHit(AudioFormat fmt, int durationMs, float baseHz,
                                  float hardness, float body) {
        int sr = (int) fmt.getSampleRate();
        int total = durationMs * sr / 1000;
        double[] buf = new double[total];

        // initial impulse and noise
        for (int i = 0; i < Math.min(total, sr / 400); i++) {
            buf[i] += Math.exp(-i * 40.0 / sr);
        }
        for (int i = 0; i < total; i++) {
            buf[i] += (RND.nextDouble() * 2 - 1) * 0.2 * hardness;
        }

        // resonant peak to emulate material body
        double q = body * 15 + 5;
        iirPeak(buf, sr, baseHz, q, 0.9);

        // extra brightness for harder material
        if (hardness > 0.6f) {
            hipass(buf, sr, 1200, 0.2);
        }

        // attack-decay envelope
        double atk = 0.002;
        double dec = 0.10 + body * 0.12;
        applyAD(buf, sr, atk, dec);

        return toPcm16(buf);
    }

    // ==== DSP helpers ====
    private static void iirPeak(double[] x, int sr, double f0, double q, double gain) {
        double w0 = 2 * Math.PI * f0 / sr;
        double alpha = Math.sin(w0) / (2 * q);
        double A = Math.sqrt(gain);
        double b0 = 1 + alpha * A;
        double b1 = -2 * Math.cos(w0);
        double b2 = 1 - alpha * A;
        double a0 = 1 + alpha / A;
        double a1 = -2 * Math.cos(w0);
        double a2 = 1 - alpha / A;

        double z1 = 0, z2 = 0;
        for (int i = 0; i < x.length; i++) {
            double in = x[i];
            double out = (b0 / a0) * in + (b1 / a0) * z1 + (b2 / a0) * z2 - (a1 / a0) * z1 - (a2 / a0) * z2;
            z2 = z1;
            z1 = out;
            x[i] = out;
        }
    }

    private static void hipass(double[] x, int sr, double fc, double mix) {
        double c = Math.exp(-2 * Math.PI * fc / sr);
        double hp = 0, prev = 0;
        for (int i = 0; i < x.length; i++) {
            double h = c * (hp + x[i] - prev);
            prev = x[i];
            hp = h;
            x[i] = (1 - mix) * x[i] + mix * h;
        }
    }

    private static void applyAD(double[] x, int sr, double atkSec, double decSec) {
        int atk = Math.max(1, (int) (atkSec * sr));
        int dec = Math.max(1, (int) (decSec * sr));
        for (int i = 0; i < x.length; i++) {
            double env = (i < atk) ? (i / (double) atk) : Math.exp(-(i - atk) / (double) dec);
            x[i] *= env;
        }
    }

    private static byte[] toPcm16(double[] x) {
        double max = 1e-9;
        for (double v : x) {
            max = Math.max(max, Math.abs(v));
        }
        double gain = 0.9 / max;
        byte[] out = new byte[x.length * 2];
        int j = 0;
        for (double v : x) {
            int s = (int) Math.round(Math.max(-1, Math.min(1, v * gain)) * 32767.0);
            out[j++] = (byte) (s & 0xFF);
            out[j++] = (byte) ((s >> 8) & 0xFF);
        }
        return out;
    }
}
