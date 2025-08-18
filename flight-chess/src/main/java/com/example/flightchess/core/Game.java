package com.example.flightchess.core;

import java.util.ArrayList;
import java.util.List;

public class Game {
    public final Rules rules = new Rules();
    public final Board board = new Board();
    public final List<Player> players = new ArrayList<>();
    public int currentIdx = 0;
    public int extraRollChain = 0;
    public Rng rng = new Rng();

    public Player current() { return players.get(currentIdx); }
    public void nextTurn() { currentIdx = (currentIdx + 1) % players.size(); extraRollChain = 0; }
    public int rollDice() { return rng.next1to6(); }

    public void applyMove(Move mv) {
        Piece pc = mv.piece;
        // handle kicked enemies
        for (Piece k : mv.kicked) {
            k.inHangar = true;
            k.inHomeLane = false;
            k.ringIndex = -1;
            k.lanePos = -1;
        }
        if (pc.inHangar) {
            // takeoff
            CellOnRing dest = (CellOnRing) mv.path.get(mv.path.size()-1);
            pc.inHangar = false;
            pc.inHomeLane = false;
            pc.ringIndex = dest.ringIndex;
        } else {
            Cell last = mv.path.get(mv.path.size()-1);
            if (last instanceof CellOnLane) {
                CellOnLane cl = (CellOnLane) last;
                pc.inHomeLane = true;
                pc.lanePos = cl.lanePos;
                pc.ringIndex = -1;
                if (cl.lanePos == 5) {
                    pc.finished = true;
                }
            } else if (last instanceof CellOnRing) {
                CellOnRing cr = (CellOnRing) last;
                pc.ringIndex = cr.ringIndex;
            }
        }
        // handle extra roll
        if (mv.dice == 6 && rules.extraRollOnSix) {
            extraRollChain++;
            if (extraRollChain > rules.maxExtraRollChain) {
                nextTurn();
            }
        } else {
            nextTurn();
        }
    }

    public boolean isWin(Player p) {
        for (Piece pc : p.pieces) {
            if (!pc.finished) return false;
        }
        return true;
    }
}
