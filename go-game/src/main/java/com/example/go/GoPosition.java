package com.example.go;

import java.util.Objects;

/**
 * 围棋位置类
 */
public class GoPosition {
    public final int row;
    public final int col;
    
    public GoPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GoPosition that = (GoPosition) obj;
        return row == that.row && col == that.col;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
    
    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}