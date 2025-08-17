package audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Runtime sound synthesis and playback utility.
 * Generates short percussive sounds representing different materials
 * so the game does not rely on external binary assets.
 */
public final class SoundManager {

    /** Material profile for different board games. */
    public enum SoundProfile { WOOD, STONE }

    /** Events that can trigger sounds. */
    public enum Event { PIECE_DROP, PIECE_CAPTURE, CHECK, WIN }

    private static final AudioFormat FMT = new AudioFormat(44100, 16, 1, true, false);

    // cache for synthesized PCM for each profile/event pair
    private static final Map<SoundProfile, Map<Event, byte[]>> CACHE = new EnumMap<>(SoundProfile.class);

    static {
        for (SoundProfile p : SoundProfile.values()) {
            CACHE.put(p, new EnumMap<>(Event.class));
        }
    }

    private SoundManager() {}

    /**
     * Play a sound for given profile and event. Waveforms are generated on
     * demand and cached for reuse to minimise latency.
     */
    public static void play(SoundProfile profile, Event event) {
        byte[] pcm = CACHE.get(profile).get(event);
        if (pcm == null) {
            pcm = synth(profile, event);
            CACHE.get(profile).put(event, pcm);
        }
        playPcm(pcm);
    }

    private static void playPcm(byte[] pcm) {
        try (AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(pcm), FMT, pcm.length / FMT.getFrameSize())) {
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            System.err.println("Audio playback failed: " + e.getMessage());
        }
    }

    // === synthesis ===
    private static byte[] synth(SoundProfile profile, Event event) {
        int ms;
        switch (event) {
            case PIECE_DROP:
                ms = 120;
                break;
            case PIECE_CAPTURE:
                ms = 170;
                break;
            case CHECK:
                ms = 220;
                break;
            case WIN:
                ms = 300;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + event);
        }
        float baseHz = (profile == SoundProfile.WOOD) ? 420f : 1500f;
        float hardness = (profile == SoundProfile.WOOD) ? 0.35f : 0.75f;
        float body = (profile == SoundProfile.WOOD) ? 0.55f : 0.25f;
        return PercSynth.pluckHit(FMT, ms, baseHz, hardness, body);
    }
}
