package com.example.junqi.core;

/**
 * 军棋棋子类
 */
public class JunQiPiece {
    private final PieceType type;
    private final boolean isRed;    // true为红方，false为黑方
    private boolean isVisible;      // 是否对对手可见
    private boolean isAlive;        // 是否存活
    
    public JunQiPiece(PieceType type, boolean isRed) {
        this.type = type;
        this.isRed = isRed;
        this.isVisible = false;     // 初始状态为暗棋
        this.isAlive = true;
    }
    
    public PieceType getType() {
        return type;
    }
    
    public boolean isRed() {
        return isRed;
    }
    
    public boolean isBlack() {
        return !isRed;
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
    
    public boolean isAlive() {
        return isAlive;
    }
    
    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }
    
    /**
     * 获取显示字符
     */
    public String getDisplaySymbol() {
        if (!isVisible) {
            return isRed ? "红" : "黑"; // 暗棋显示颜色
        }
        return type.getSymbol();
    }
    
    /**
     * 获取完整显示名称
     */
    public String getDisplayName() {
        String color = isRed ? "红" : "黑";
        if (!isVisible) {
            return color + "棋";
        }
        return color + type.getName();
    }
    
    /**
     * 判断是否能攻击指定棋子
     */
    public boolean canAttack(JunQiPiece opponent) {
        if (opponent == null || !opponent.isAlive()) {
            return false;
        }
        
        // 同色棋子不能攻击
        if (this.isRed == opponent.isRed) {
            return false;
        }
        
        // 不能移动的棋子不能主动攻击
        if (!this.type.canMove()) {
            return false;
        }
        
        return this.type.canCapture(opponent.type);
    }
    
    /**
     * 攻击对方棋子，返回攻击结果
     */
    public AttackResult attack(JunQiPiece opponent) {
        if (!canAttack(opponent)) {
            return AttackResult.INVALID;
        }
        
        // 特殊情况：工兵挖雷
        if (this.type == PieceType.ENGINEER && opponent.type == PieceType.MINE) {
            opponent.setAlive(false);
            return AttackResult.WIN;
        }
        
        // 特殊情况：碰到地雷（非工兵）
        if (opponent.type == PieceType.MINE && this.type != PieceType.ENGINEER) {
            this.setAlive(false);
            return AttackResult.LOSE;
        }
        
        // 一般战斗规则
        if (this.type.getPower() > opponent.type.getPower()) {
            opponent.setAlive(false);
            return AttackResult.WIN;
        } else if (this.type.getPower() < opponent.type.getPower()) {
            this.setAlive(false);
            return AttackResult.LOSE;
        } else {
            // 同级别棋子对碰，同归于尽
            this.setAlive(false);
            opponent.setAlive(false);
            return AttackResult.DRAW;
        }
    }
    
    /**
     * 攻击结果枚举
     */
    public enum AttackResult {
        WIN,     // 胜利
        LOSE,    // 失败
        DRAW,    // 平局（同归于尽）
        INVALID  // 无效攻击
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JunQiPiece piece = (JunQiPiece) obj;
        return isRed == piece.isRed && type == piece.type;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() + (isRed ? 1 : 0);
    }
}
