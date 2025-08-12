package com.example.flightchess;

import java.util.List;
import java.util.Random;

/**
 * 飞行棋AI类
 */
public class FlightChessAI {
    private final Random random;
    private final int difficulty; // 1-5难度等级
    
    public FlightChessAI(int difficulty) {
        this.difficulty = Math.max(1, Math.min(5, difficulty));
        this.random = new Random();
    }
    
    /**
     * 获取AI的最佳移动
     */
    public int getBestMove(FlightChessGame game, int player, int diceValue) {
        List<Integer> validPlanes = game.getMovablePlanes(player, diceValue);
        
        if (validPlanes.isEmpty()) {
            return -1; // 无法移动
        }
        
        switch (difficulty) {
            case 1:
                return getRandomMove(validPlanes);
            case 2:
                return getBasicMove(game, player, diceValue, validPlanes);
            case 3:
                return getIntermediateMove(game, player, diceValue, validPlanes);
            case 4:
                return getAdvancedMove(game, player, diceValue, validPlanes);
            case 5:
                return getExpertMove(game, player, diceValue, validPlanes);
            default:
                return getRandomMove(validPlanes);
        }
    }
    
    /**
     * 随机移动（难度1）
     */
    private int getRandomMove(List<Integer> validPlanes) {
        return validPlanes.get(random.nextInt(validPlanes.size()));
    }
    
    /**
     * 基础移动（难度2）- 优先起飞和到达终点
     */
    private int getBasicMove(FlightChessGame game, int player, int diceValue, List<Integer> validPlanes) {
        FlightChessGame.Plane[] planes = game.getPlayerPlanes(player);
        
        // 优先级1：如果有飞机可以到达终点
        for (int planeIndex : validPlanes) {
            FlightChessGame.Plane plane = planes[planeIndex];
            if (plane.position >= 0 && plane.position + diceValue >= game.MAIN_TRACK_SIZE) {
                return planeIndex;
            }
        }
        
        // 优先级2：如果投掷6点且有飞机在家园，优先起飞
        if (diceValue == 6) {
            for (int planeIndex : validPlanes) {
                FlightChessGame.Plane plane = planes[planeIndex];
                if (plane.position == -1) {
                    return planeIndex;
                }
            }
        }
        
        // 优先级3：移动已在跑道上的飞机
        for (int planeIndex : validPlanes) {
            FlightChessGame.Plane plane = planes[planeIndex];
            if (plane.position >= 0) {
                return planeIndex;
            }
        }
        
        return getRandomMove(validPlanes);
    }
    
    /**
     * 中级移动（难度3）- 考虑击落对手
     */
    private int getIntermediateMove(FlightChessGame game, int player, int diceValue, List<Integer> validPlanes) {
        FlightChessGame.Plane[] planes = game.getPlayerPlanes(player);
        
        // 优先级1：如果可以击落对手飞机
        for (int planeIndex : validPlanes) {
            FlightChessGame.Plane plane = planes[planeIndex];
            if (plane.position >= 0) {
                int newPosition = plane.position + diceValue;
                if (newPosition < game.MAIN_TRACK_SIZE) {
                    // 检查是否会击落对手
                    if (wouldCaptureOpponent(game, player, newPosition)) {
                        return planeIndex;
                    }
                }
            }
        }
        
        return getBasicMove(game, player, diceValue, validPlanes);
    }
    
    /**
     * 高级移动（难度4）- 考虑安全性和战略位置
     */
    private int getAdvancedMove(FlightChessGame game, int player, int diceValue, List<Integer> validPlanes) {
        FlightChessGame.Plane[] planes = game.getPlayerPlanes(player);
        
        // 优先级1：避免被击落的危险移动
        List<Integer> safeMoves = validPlanes.stream()
            .filter(planeIndex -> {
                FlightChessGame.Plane plane = planes[planeIndex];
                if (plane.position >= 0) {
                    int newPosition = plane.position + diceValue;
                    return !wouldBeInDanger(game, player, newPosition);
                }
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
        
        if (!safeMoves.isEmpty()) {
            return getIntermediateMove(game, player, diceValue, safeMoves);
        }
        
        return getIntermediateMove(game, player, diceValue, validPlanes);
    }
    
    /**
     * 专家移动（难度5）- 综合考虑所有因素
     */
    private int getExpertMove(FlightChessGame game, int player, int diceValue, List<Integer> validPlanes) {
        FlightChessGame.Plane[] planes = game.getPlayerPlanes(player);
        int bestMove = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (int planeIndex : validPlanes) {
            double score = evaluateMove(game, player, planeIndex, diceValue);
            if (score > bestScore) {
                bestScore = score;
                bestMove = planeIndex;
            }
        }
        
        return bestMove != -1 ? bestMove : getRandomMove(validPlanes);
    }
    
    /**
     * 评估移动的分数
     */
    private double evaluateMove(FlightChessGame game, int player, int planeIndex, int diceValue) {
        FlightChessGame.Plane[] planes = game.getPlayerPlanes(player);
        FlightChessGame.Plane plane = planes[planeIndex];
        double score = 0;
        
        if (plane.position == -1) {
            // 起飞
            if (diceValue == 6) {
                score += 50; // 起飞奖励
            }
        } else {
            int newPosition = plane.position + diceValue;
            
            // 到达终点奖励
            if (newPosition >= game.MAIN_TRACK_SIZE) {
                score += 100;
            } else {
                // 前进奖励
                score += diceValue * 2;
                
                // 击落对手奖励
                if (wouldCaptureOpponent(game, player, newPosition)) {
                    score += 30;
                }
                
                // 安全性考虑
                if (wouldBeInDanger(game, player, newPosition)) {
                    score -= 20;
                }
                
                // 接近终点奖励
                double progressRatio = (double) newPosition / game.MAIN_TRACK_SIZE;
                score += progressRatio * 10;
            }
        }
        
        return score;
    }
    
    /**
     * 检查是否会击落对手飞机
     */
    private boolean wouldCaptureOpponent(FlightChessGame game, int player, int position) {
        for (int otherPlayer = 0; otherPlayer < game.getPlayerCount(); otherPlayer++) {
            if (otherPlayer != player) {
                FlightChessGame.Plane[] otherPlanes = game.getPlayerPlanes(otherPlayer);
                for (FlightChessGame.Plane otherPlane : otherPlanes) {
                    if (otherPlane.position == position && !game.isSafePosition(position)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 检查移动后是否会处于危险位置
     */
    private boolean wouldBeInDanger(FlightChessGame game, int player, int position) {
        if (game.isSafePosition(position)) {
            return false;
        }
        
        // 简化的危险检测：检查其他玩家是否可能在下一轮击落此位置
        for (int otherPlayer = 0; otherPlayer < game.getPlayerCount(); otherPlayer++) {
            if (otherPlayer != player) {
                FlightChessGame.Plane[] otherPlanes = game.getPlayerPlanes(otherPlayer);
                for (FlightChessGame.Plane otherPlane : otherPlanes) {
                    if (otherPlane.position >= 0) {
                        // 检查对手是否可能在1-6步内到达此位置
                        for (int dice = 1; dice <= 6; dice++) {
                            if (otherPlane.position + dice == position) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
}