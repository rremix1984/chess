package com.example.armychess;

/**
 * 军旗游戏玩家类
 */
public class ArmyChessPlayer {

    private String name;

    public ArmyChessPlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void makeMove() {
        System.out.println(name + "正在进行移动...");
        // TODO: 添加玩家移动逻辑
    }
}