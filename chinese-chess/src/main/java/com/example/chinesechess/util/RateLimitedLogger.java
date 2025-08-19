package com.example.chinesechess.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility logger that rate-limits normal logs to once every five minutes.
 * Error logs should still use System.err.println directly.
 */
public class RateLimitedLogger {
    private static final long INTERVAL_MS = 5 * 60 * 1000; // 5 minutes
    private static final ConcurrentHashMap<String, Long> lastLogTimes = new ConcurrentHashMap<>();

    private RateLimitedLogger() {
        // Utility class
    }

    /**
     * Logs the given message if the specified key hasn't been logged within the
     * interval. Each unique key is tracked separately.
     *
     * @param key     unique identifier for the log type
     * @param message message to print to standard output
     */
    public static void log(String key, String message) {
        long now = System.currentTimeMillis();
        Long last = lastLogTimes.get(key);
        if (last == null || now - last >= INTERVAL_MS) {
            System.out.println(message);
            lastLogTimes.put(key, now);
        }
    }
}

