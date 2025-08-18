package com.example.go.lad;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores progress under ~/.yourapp/go/ld_progress.json.
 */
public class FileProgressStore implements ProgressStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, ProblemProgress> map = new HashMap<>();
    private final Path file;

    public FileProgressStore() {
        file = Path.of(System.getProperty("user.home"), ".yourapp", "go", "ld_progress.json");
        load();
    }

    @Override
    public ProblemProgress get(String id) {
        return map.computeIfAbsent(id, ProblemProgress::new);
    }

    @Override
    public void markPassed(String id, int moves, long timeMs, int usedHints) {
        ProblemProgress p = get(id);
        p.passed = true;
        p.bestMoves = moves;
        p.bestTimeMs = timeMs;
        p.usedHints = usedHints;
        save();
    }

    private void load() {
        try {
            if (Files.exists(file)) {
                try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    Type type = new TypeToken<Map<String, ProblemProgress>>(){}.getType();
                    Map<String, ProblemProgress> data = GSON.fromJson(r, type);
                    if (data != null) {
                        map.putAll(data);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(map, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
