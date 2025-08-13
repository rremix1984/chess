package com.example.internationalchess.core;

/**
 * 位置类
 * 表示棋盘上的一个位置
 */
public class Position {
    
    private int row;
    private int col;
    
    /**
     * 构造函数
     * @param row 行
     * @param col 列
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    /**
     * 获取行
     * @return 行
     */
    public int getRow() {
        return row;
    }
    
    /**
     * 设置行
     * @param row 行
     */
    public void setRow(int row) {
        this.row = row;
    }
    
    /**
     * 获取列
     * @return 列
     */
    public int getCol() {
        return col;
    }
    
    /**
     * 设置列
     * @param col 列
     */
    public void setCol(int col) {
        this.col = col;
    }
    
    /**
     * 获取X坐标（列的别名）
     * @return X坐标
     */
    public int getX() {
        return col;
    }
    
    /**
     * 获取Y坐标（行的别名）
     * @return Y坐标
     */
    public int getY() {
        return row;
    }
    
    /**
     * 检查位置是否有效
     * @return 是否有效
     */
    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    
    /**
     * 检查两个位置是否相等
     * @param obj 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return row == position.row && col == position.col;
    }
    
    /**
     * 获取哈希码
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return row * 8 + col;
    }
    
    /**
     * 转换为字符串
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
    
    /**
     * 转换为国际象棋标准记号
     * @return 标准记号（如a1, e4等）
     */
    public String toChessNotation() {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }
    
    /**
     * 从国际象棋标准记号创建位置
     * @param notation 标准记号（如a1, e4等）
     * @return 位置对象
     */
    public static Position fromChessNotation(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Invalid chess notation: " + notation);
        }
        
        char file = notation.charAt(0);
        char rank = notation.charAt(1);
        
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid chess notation: " + notation);
        }
        
        int col = file - 'a';
        int row = 8 - (rank - '0');
        
        return new Position(row, col);
    }
    
    /**
     * 计算两个位置之间的距离
     * @param other 另一个位置
     * @return 距离
     */
    public double distanceTo(Position other) {
        int deltaRow = this.row - other.row;
        int deltaCol = this.col - other.col;
        return Math.sqrt(deltaRow * deltaRow + deltaCol * deltaCol);
    }
    
    /**
     * 计算两个位置之间的曼哈顿距离
     * @param other 另一个位置
     * @return 曼哈顿距离
     */
    public int manhattanDistanceTo(Position other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
    }
    
    /**
     * 获取相邻的位置
     * @return 相邻位置列表
     */
    public java.util.List<Position> getAdjacentPositions() {
        java.util.List<Position> adjacent = new java.util.ArrayList<>();
        
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                
                Position pos = new Position(row + dr, col + dc);
                if (pos.isValid()) {
                    adjacent.add(pos);
                }
            }
        }
        
        return adjacent;
    }
    
    /**
     * 创建位置的副本
     * @return 位置副本
     */
    public Position copy() {
        return new Position(row, col);
    }
}