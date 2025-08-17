package com.example.launcher.util;

import java.util.Map;

/**
 * Utility for mapping game type to human readable Chinese name.
 */
public final class GameDisplay {
    private static final Map<String, String> TYPE_TO_NAME = Map.of(
            "chinese-chess", "中国象棋",
            "international-chess", "国际象棋",
            "gomoku", "五子棋",
            "go-game", "围棋",
            "tank-battle-game", "坦克大战",
            "monopoly", "大富翁"
    );

    private GameDisplay() {
    }

    /**
     * Returns Chinese display name for game type code.
     */
    public static String name(String gameType) {
        return TYPE_TO_NAME.getOrDefault(gameType, gameType);
    }
}
