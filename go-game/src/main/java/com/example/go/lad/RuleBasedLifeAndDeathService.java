package com.example.go.lad;

import java.util.List;

/**
 * Placeholder implementation of basic life-and-death judging.
 */
public class RuleBasedLifeAndDeathService implements LifeAndDeathService {
    @Override
    public void load(GoLifeAndDeathProblem p, GoGame game) {
        game.resetTo(p);
    }

    @Override
    public JudgeResult judge(GoGame game, GoLifeAndDeathProblem p) {
        JudgeResult r = new JudgeResult();
        r.status = JudgeStatus.UNKNOWN;
        r.reason = "基础判定未实现";
        r.movesUsed = game.getMoveCount();
        return r;
    }

    @Override
    public String nextHint(GoLifeAndDeathProblem p, String currentHintsText) {
        List<String> hs = p.hints == null ? List.of() : p.hints;
        long have = currentHintsText == null ? 0 :
                currentHintsText.lines().filter(l -> l.trim().startsWith("•")).count();
        return have < hs.size() ? hs.get((int) have) : null;
    }
}
