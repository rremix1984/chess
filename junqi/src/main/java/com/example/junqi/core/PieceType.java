package com.example.junqi.core;

/**
 * 军棋棋子类型枚举
 * 按照战斗力从低到高排序，数值越大战斗力越强
 */
public enum PieceType {
    // 特殊棋子
    FLAG(0, "军旗", "旗"),
    MINE(0, "地雷", "雷"),
    
    // 工兵（可以挖雷）
    ENGINEER(1, "工兵", "工"),
    
    // 军官棋子（按等级排序）
    PLATOON_LEADER(2, "排长", "排"),
    COMPANY_COMMANDER(3, "连长", "连"),
    BATTALION_COMMANDER(4, "营长", "营"),
    REGIMENT_COMMANDER(5, "团长", "团"),
    BRIGADE_COMMANDER(6, "旅长", "旅"),
    DIVISION_COMMANDER(7, "师长", "师"),
    CORPS_COMMANDER(8, "军长", "军"),
    
    // 最高级别
    COMMANDER(9, "司令", "司");
    
    private final int power;      // 战斗力
    private final String name;    // 完整名称
    private final String symbol;  // 显示符号
    
    PieceType(int power, String name, String symbol) {
        this.power = power;
        this.name = name;
        this.symbol = symbol;
    }
    
    public int getPower() {
        return power;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    /**
     * 判断当前棋子是否能吃掉对方棋子
     */
    public boolean canCapture(PieceType opponent) {
        // 特殊规则
        if (this == ENGINEER && opponent == MINE) {
            return true; // 工兵可以挖雷
        }
        
        if (this == MINE || opponent == MINE) {
            return false; // 地雷不能主动攻击，其他棋子碰到地雷会被炸死（除工兵）
        }
        
        if (opponent == FLAG) {
            return true; // 任何棋子都可以吃军旗
        }
        
        if (this == FLAG) {
            return false; // 军旗不能主动攻击
        }
        
        // 一般规则：战斗力高的吃战斗力低的
        return this.power > opponent.power;
    }
    
    /**
     * 判断是否可以移动
     */
    public boolean canMove() {
        return this != FLAG && this != MINE; // 军旗和地雷不能移动
    }
    
    /**
     * 获取棋子数量配置
     */
    public int getCount() {
        switch (this) {
            case FLAG:
            case COMMANDER:
                return 1;
            case MINE:
                return 3;
            case ENGINEER:
                return 3;
            case PLATOON_LEADER:
            case COMPANY_COMMANDER:
            case BATTALION_COMMANDER:
                return 2;
            default:
                return 1;
        }
    }
}
