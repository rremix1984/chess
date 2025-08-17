package com.example.go;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的进度存储，保存在用户目录下的 JSON 文件中。
 */
public class ProgressStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path storeFile;
    private Map<String, Record> records = new HashMap<>();

    public ProgressStore() {
        this.storeFile = Path.of(System.getProperty("user.home"), ".yourapp", "go", "ld_progress.json");
        load();
    }

    public Record getRecord(String id) {
        return records.get(id);
    }

    public void updateRecord(String id, Record record) {
        records.put(id, record);
        save();
    }

    private void load() {
        try {
            if (Files.exists(storeFile)) {
                Type type = new TypeToken<Map<String, Record>>(){}.getType();
                String json = Files.readString(storeFile, StandardCharsets.UTF_8);
                Map<String, Record> data = GSON.fromJson(json, type);
                if (data != null) {
                    records = data;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            Files.createDirectories(storeFile.getParent());
            String json = GSON.toJson(records);
            Files.writeString(storeFile, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 题目进度记录。
     */
    public static class Record {
        public boolean passed;
        public int bestMoves;
        public long bestTimeMs;
        public int usedHints;
        public long lastPlayed;
    }
}

