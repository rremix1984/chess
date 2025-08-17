package com.example.go;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试死活棋模块基础功能
 */
public class GoLifeAndDeathGameTest {
    @Test
    public void testStartProblemLoadsBoardAndPlayer() {
        GoLifeAndDeathGame game = new GoLifeAndDeathGame();
        assertTrue(game.getProblemCount() > 0);
        game.startProblem(0);
        int[][] board = game.getBoard();
        assertEquals(GoGame.BLACK, board[0][0]);
        assertEquals(GoGame.WHITE, board[1][0]);
        assertEquals(GoGame.BLACK, game.getCurrentPlayer());
    }
}
