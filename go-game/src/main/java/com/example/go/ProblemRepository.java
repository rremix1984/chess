package com.example.go;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 从资源目录加载死活棋题的仓库。
 */
public class ProblemRepository {
    private final List<GoLifeAndDeathProblem> problems = new ArrayList<>();

    public ProblemRepository() {
        loadProblems();
    }

    private void loadProblems() {
        try {
            URL dirURL = getClass().getResource("/problems");
            if (dirURL == null) {
                return;
            }
            Path path = Paths.get(dirURL.toURI());
            try (Stream<Path> files = Files.list(path)) {
                files.filter(p -> p.toString().endsWith(".json"))
                        .forEach(p -> {
                            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8)) {
                                GoLifeAndDeathProblem prob = GoLifeAndDeathProblem.fromJson(reader);
                                problems.add(prob);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<GoLifeAndDeathProblem> getProblems() {
        return Collections.unmodifiableList(problems);
    }
}

