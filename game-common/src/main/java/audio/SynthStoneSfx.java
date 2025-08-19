package audio;

import javax.sound.sampled.*;

/**
 * Procedural fallback used when external stone drop samples are not available.
 * Generates a short bright "tick" followed by a lower "knock" using simple
 * oscillators and plays them with a small delay so that applications can still
 * provide audio feedback without any assets.
 */
final class SynthStoneSfx {
    private static final float SR = 44_100f;

    private SynthStoneSfx() {}

    static void playSynthetics(float power01) {
        byte[] tick = synthTick(0.030f + (float)Math.random()*0.010f);
        byte[] knock = synthKnock(0.160f + (float)Math.random()*0.060f);

        float tickGain = dbToLin(-6f + 6f*power01);
        float knockGain = dbToLin(-8f + 6f*power01);
        scalePcm(tick, tickGain);
        scalePcm(knock, knockGain);

        new Thread(() -> playPcm(tick)).start();
        int delayMs = 25 + (int)(Math.random()*15);
        new javax.swing.Timer(delayMs, e -> {
            new Thread(() -> playPcm(knock)).start();
            ((javax.swing.Timer)e.getSource()).stop();
        }).start();
    }

    private static byte[] synthTick(float sec) {
        int n = Math.round(SR * sec);
        double f1 = 5200 + Math.random()*1400;
        double f2 = 3200 + Math.random()*900;
        double a = 1.0, a2 = 0.7;
        double tau = sec * 0.35;
        return oscMix(n, new double[]{f1,f2}, new double[]{a,a2}, tau, 0.0008);
    }

    private static byte[] synthKnock(float sec) {
        int n = Math.round(SR * sec);
        double f1 = 320 + Math.random()*120;
        double f2 = 520 + Math.random()*160;
        double a = 1.0, a2 = 0.6;
        double tau = sec * 0.55;
        return oscMix(n, new double[]{f1,f2}, new double[]{a,a2}, tau, 0.0015);
    }

    private static byte[] oscMix(int n,double[] f,double[] amp,double tau,double noise){
        byte[] pcm = new byte[n*2];
        double twoPi = Math.PI*2.0, tStep = 1.0/SR;
        double[] phase = new double[f.length];
        java.util.Random rnd = new java.util.Random();

        for (int i=0;i<n;i++) {
            double t = i*tStep;
            double env = Math.exp(-t / tau);
            double s = 0.0;
            for (int k=0;k<f.length;k++) {
                phase[k] += twoPi*f[k]*tStep;
                s += amp[k]*Math.sin(phase[k]);
            }
            s /= f.length;
            s += (rnd.nextDouble()*2-1)*noise;
            s *= env;

            short val = (short)Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, s*32760));
            pcm[i*2] = (byte)(val & 0xff);
            pcm[i*2+1] = (byte)((val >> 8) & 0xff);
        }
        return pcm;
    }

    private static void scalePcm(byte[] pcm, float gainLin) {
        for (int i=0;i<pcm.length;i+=2) {
            short v = (short)((pcm[i] & 0xff) | (pcm[i+1] << 8));
            int s = Math.round(v * gainLin);
            s = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, s));
            pcm[i] = (byte)(s & 0xff);
            pcm[i+1] = (byte)((s >> 8) & 0xff);
        }
    }

    private static float dbToLin(float db){ return (float)Math.pow(10.0, db/20.0); }

    private static void playPcm(byte[] pcm) {
        try {
            AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SR, 16, 1, 2, SR, false);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, fmt));
            line.open(fmt); line.start();
            line.write(pcm, 0, pcm.length);
            line.drain(); line.stop(); line.close();
        } catch (Exception e) {
            System.err.println("[SFX] synth playback fail: " + e.getMessage());
        }
    }
}

