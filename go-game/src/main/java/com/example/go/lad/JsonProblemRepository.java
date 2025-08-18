package com.example.go.lad;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Loads problems from resources/problems/*.json.
 */
public class JsonProblemRepository implements ProblemRepository {
    private final Map<String, GoLifeAndDeathProblem> cache = new LinkedHashMap<>();

    public JsonProblemRepository() {
        load();
    }

    private void load() {
        try {
            URL dirURL = getClass().getResource("/problems");
            if (dirURL == null) {
                return;
            }
            var uri = dirURL.toURI();
            if ("jar".equals(uri.getScheme())) {
                try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    Path dir = fs.getPath("/problems");
                    try (Stream<Path> paths = Files.list(dir)) {
                        paths.filter(p -> p.toString().endsWith(".json"))
                                .forEach(this::loadFile);
                    }
                }
            } else {
                Path dir = Paths.get(uri);
                try (Stream<Path> paths = Files.list(dir)) {
                    paths.filter(p -> p.toString().endsWith(".json"))
                            .forEach(this::loadFile);
                }
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFile(Path p) {
        try (Reader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            GoLifeAndDeathProblem prob = new GoLifeAndDeathProblem();
            prob.id = obj.get("id").getAsString();
            prob.size = obj.get("size").getAsInt();
            prob.toPlay = "W".equalsIgnoreCase(obj.get("toPlay").getAsString()) ? GoColor.WHITE : GoColor.BLACK;
            prob.goal = obj.get("goal").getAsString();

            JsonArray init = obj.getAsJsonArray("initialStones");
            if (init != null) {
                for (JsonElement el : init) {
                    JsonObject s = el.getAsJsonObject();
                    int x = s.get("x").getAsInt();
                    int y = s.get("y").getAsInt();
                    GoColor c = "W".equalsIgnoreCase(s.get("c").getAsString()) ? GoColor.WHITE : GoColor.BLACK;
                    prob.initial.add(new GoStone(x, y, c));
                }
            }

            if (obj.has("hints")) {
                List<String> hs = new ArrayList<>();
                JsonArray arr = obj.getAsJsonArray("hints");
                for (JsonElement el : arr) hs.add(el.getAsString());
                prob.hints = hs;
            }

            if (obj.has("answer")) {
                List<GoPoint> ans = new ArrayList<>();
                JsonArray arr = obj.getAsJsonArray("answer");
                for (JsonElement el : arr) {
                    String s = el.getAsString();
                    int x = s.charAt(0) - 'A' + 1;
                    int y = Integer.parseInt(s.substring(1));
                    ans.add(new GoPoint(x, y));
                }
                prob.answer = ans;
            }

            cache.put(prob.id, prob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<GoLifeAndDeathProblem> list(ProblemFilter f) {
        return new ArrayList<>(cache.values());
    }

    @Override
    public GoLifeAndDeathProblem get(String id) {
        return cache.get(id);
    }
}
