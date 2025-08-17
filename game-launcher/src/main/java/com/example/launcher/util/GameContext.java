package com.example.launcher.util;

/**
 * Global context for runtime options such as single player flag.
 */
public final class GameContext {
    private static volatile boolean singlePlayer = false;

    private GameContext() {}

    public static void setSinglePlayer(boolean sp) {
        singlePlayer = sp;
    }

    public static boolean isSinglePlayer() {
        return singlePlayer;
    }
}
