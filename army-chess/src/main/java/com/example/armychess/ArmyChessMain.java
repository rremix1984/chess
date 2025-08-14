package com.example.armychess;

/**
 * 军旗游戏主类
 */
public class ArmyChessMain {

    public static void main(String[] args) {
        System.out.println("欢迎来到军旗游戏！");
        // 初始化游戏逻辑
        ArmyChessGame game = new ArmyChessGame();
        game.start();
    }
}