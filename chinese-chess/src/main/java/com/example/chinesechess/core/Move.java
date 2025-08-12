package com.example.chinesechess.core;

import com.example.chinesechess.core.Position;

/**
 * 表示一次棋子移动
 */
public class Move {
    private final Position start;
    private final Position end;
    
    public Move(Position start, Position end) {
        this.start = start;
        this.end = end;
    }
    
    public Position getStart() {
        return start;
    }
    
    public Position getEnd() {
        return end;
    }
    
    @Override
    public String toString() {
        return "Move{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Move move = (Move) o;
        
        if (!start.equals(move.start)) return false;
        return end.equals(move.end);
    }
    
    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }
}