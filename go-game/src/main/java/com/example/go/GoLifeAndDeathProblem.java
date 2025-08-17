package com.example.go;

/**
 * 表示一个死活棋题，包括初始棋盘和先手方
 */
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * 表示一个死活棋题，包括初始棋盘和先手方以及基本元数据。
 * <p>
 * 该类提供了一个静态的 {@link #fromJson(Reader)} 方法，
 * 可以从 JSON 描述中构造出一个题目实例。JSON 格式示例见
 * <code>resources/problems/*.json</code>。
 * </p>
 */
public class GoLifeAndDeathProblem {
    private final int[][] board;
    private final int startingPlayer;

    private final String id;
    private final int size;
    private final String goal;
    private final String level;
    private final List<String> hints;
    private final List<String> answer;

    /**
     * 仅包含棋盘和先手信息的简化构造函数，供旧代码兼容使用。
     */
    public GoLifeAndDeathProblem(int[][] board, int startingPlayer) {
        this("", GoGame.BOARD_SIZE, null, null, board, startingPlayer,
                new ArrayList<>(), new ArrayList<>());
    }

    public GoLifeAndDeathProblem(
            String id,
            int size,
            String goal,
            String level,
            int[][] board,
            int startingPlayer,
            List<String> hints,
            List<String> answer) {
        this.id = id;
        this.size = size;
        this.goal = goal;
        this.level = level;
        this.board = board;
        this.startingPlayer = startingPlayer;
        this.hints = hints;
        this.answer = answer;
    }

    /**
     * 根据 JSON 数据创建死活棋题。
     *
     * @param reader JSON 输入流
     * @return 解析得到的题目
     * @throws IOException 如果读取失败
     */
    public static GoLifeAndDeathProblem fromJson(Reader reader) throws IOException {
        JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

        String id = obj.has("id") ? obj.get("id").getAsString() : "";
        int size = obj.has("size") ? obj.get("size").getAsInt() : GoGame.BOARD_SIZE;
        String goal = obj.has("goal") ? obj.get("goal").getAsString() : null;
        String level = obj.has("level") ? obj.get("level").getAsString() : null;
        String toPlay = obj.has("toPlay") ? obj.get("toPlay").getAsString() : "B";

        int startingPlayer = "W".equalsIgnoreCase(toPlay) ? GoGame.WHITE : GoGame.BLACK;

        int[][] board = new int[GoGame.BOARD_SIZE][GoGame.BOARD_SIZE];
        JsonArray stones = obj.getAsJsonArray("initialStones");
        if (stones != null) {
            for (int i = 0; i < stones.size(); i++) {
                JsonObject s = stones.get(i).getAsJsonObject();
                int x = s.get("x").getAsInt() - 1; // JSON 中坐标从1开始
                int y = s.get("y").getAsInt() - 1;
                String c = s.get("c").getAsString();
                board[y][x] = "W".equalsIgnoreCase(c) ? GoGame.WHITE : GoGame.BLACK;
            }
        }

        List<String> hints = new ArrayList<>();
        JsonArray hintArray = obj.getAsJsonArray("hints");
        if (hintArray != null) {
            hintArray.forEach(h -> hints.add(h.getAsString()));
        }

        List<String> answer = new ArrayList<>();
        JsonArray ansArray = obj.getAsJsonArray("answer");
        if (ansArray != null) {
            ansArray.forEach(a -> answer.add(a.getAsString()));
        }

        return new GoLifeAndDeathProblem(id, size, goal, level, board, startingPlayer, hints, answer);
    }

    public int[][] getBoard() {
        return board;
    }

    public int getStartingPlayer() {
        return startingPlayer;
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public String getGoal() {
        return goal;
    }

    public String getLevel() {
        return level;
    }

    public List<String> getHints() {
        return hints;
    }

    public List<String> getAnswer() {
        return answer;
    }
}
