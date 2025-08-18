package com.example.flightchess.core;

import java.util.*;

import static com.example.flightchess.core.Board.*;

public class MoveGenerator {

    public static List<Move> legalMoves(Game game, Player player, int dice, Rules rules) {
        List<Move> res = new ArrayList<>();
        if (dice == 6) {
            for (Piece pc : player.pieces) {
                if (pc.inHangar) {
                    Move mv = tryTakeoff(game, player, pc, rules);
                    if (mv != null) res.add(mv);
                }
            }
        }
        for (Piece pc : player.pieces) {
            if (!pc.inHangar) {
                Move mv = tryAdvance(game, player, pc, dice, rules);
                if (mv != null) res.add(mv);
            }
        }
        return res;
    }

    private static Move tryTakeoff(Game game, Player p, Piece pc, Rules rules) {
        int start = game.board.startIndex(p.color);
        Move mv = new Move();
        mv.piece = pc;
        mv.dice = 6;
        mv.path.add(new CellOnRing(start));
        fillKicks(game, p, start, mv, rules);
        return mv;
    }

    private static Move tryAdvance(Game game, Player p, Piece pc, int dice, Rules rules) {
        Move mv = new Move();
        mv.piece = pc;
        mv.dice = dice;
        if (pc.inHomeLane) {
            int target = pc.lanePos + dice;
            if (target > 5) return null;
            for (int i = pc.lanePos + 1; i <= target; i++) {
                mv.path.add(new CellOnLane(p.color, i));
            }
            return mv;
        } else {
            int at = pc.ringIndex;
            int steps = dice;
            while (steps > 0) {
                int next = game.board.stepOnRing(at, 1);
                at = next;
                steps--;
                if (at == game.board.entryIndex(p.color) && steps > 0) {
                    for (int i = 0; i < steps; i++) {
                        mv.path.add(new CellOnLane(p.color, i));
                    }
                    steps = 0;
                    break;
                } else {
                    mv.path.add(new CellOnRing(at));
                }
            }
            if (!mv.path.isEmpty() && !(mv.path.get(mv.path.size()-1) instanceof CellOnLane)) {
                CellOnRing last = (CellOnRing) mv.path.get(mv.path.size()-1);
                Integer jmp = game.board.getJumpTarget(p.color, last.ringIndex);
                int guard = 0;
                while (jmp != null && rules.chainJump && guard++ < 8) {
                    mv.path.add(new CellOnRing(jmp));
                    jmp = game.board.getJumpTarget(p.color, jmp);
                }
            }
            if (!mv.path.isEmpty() && !(mv.path.get(mv.path.size()-1) instanceof CellOnLane)) {
                int endRing = ((CellOnRing) mv.path.get(mv.path.size()-1)).ringIndex;
                fillKicks(game, p, endRing, mv, rules);
            }
            return mv;
        }
    }

    private static void fillKicks(Game game, Player self, int ringEnd, Move mv, Rules rules) {
        if (rules.enableSafeCells && rules.safeCells.contains(ringEnd)) return;
        for (Player other : game.players) if (other != self) {
            for (Piece op : other.pieces) {
                if (!op.inHomeLane && !op.inHangar && op.ringIndex == ringEnd) {
                    mv.kicked.add(op);
                }
            }
        }
    }
}
