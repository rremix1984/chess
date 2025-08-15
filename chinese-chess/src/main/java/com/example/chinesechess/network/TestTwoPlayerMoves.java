package com.example.chinesechess.network;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 测试两个客户端之间的移动消息转发
 * 专门用于调试黑方移动不显示在红方客户端的问题
 */
public class TestTwoPlayerMoves {
    
    private static CountDownLatch gameStartLatch = new CountDownLatch(2);
    private static CountDownLatch moveLatch = new CountDownLatch(1);
    private static String roomId = null;
    
    public static void main(String[] args) {
        System.out.println("🧪🧪 启动双客户端移动测试...");
        
        try {
            // 创建红方客户端
            NetworkClient redClient = createClient("RedPlayer", new RedPlayerListener());
            
            // 创建黑方客户端  
            NetworkClient blackClient = createClient("BlackPlayer", new BlackPlayerListener());
            
            // 连接到服务器
            System.out.println("🔴 红方客户端连接...");
            redClient.connect("localhost", 8080, "RedPlayer");
            Thread.sleep(1000);
            
            System.out.println("⚫ 黑方客户端连接...");
            blackClient.connect("localhost", 8080, "BlackPlayer");
            Thread.sleep(1000);
            
            // 红方创建房间
            System.out.println("🏠 红方创建房间...");
            redClient.createRoom("移动测试房间", "");
            Thread.sleep(1000);
            
            // 等待房间创建成功
            int attempts = 0;
            while (roomId == null && attempts < 10) {
                Thread.sleep(100);
                attempts++;
            }
            
            if (roomId == null) {
                System.out.println("❌ 获取房间ID失败！");
                return;
            }
            
            // 黑方加入房间  
            System.out.println("🚪 黑方加入房间: " + roomId);
            blackClient.joinRoom(roomId, "");
            Thread.sleep(2000);
            
            // 等待游戏开始
            System.out.println("⏳ 等待游戏开始...");
            boolean gameStarted = gameStartLatch.await(5, TimeUnit.SECONDS);
            if (!gameStarted) {
                System.out.println("❌ 游戏开始超时！");
                return;
            }
            
            System.out.println("🎮 游戏已开始，开始测试移动...");
            Thread.sleep(1000);
            
            // 红方先走（兵前进）
            System.out.println("🔴 红方移动: 兵 (6,0) -> (5,0)");
            redClient.sendMove(6, 0, 5, 0);
            Thread.sleep(2000);
            
            // 黑方响应（兵前进）
            System.out.println("⚫ 黑方移动: 兵 (3,0) -> (4,0)");
            blackClient.sendMove(3, 0, 4, 0);
            
            // 等待移动完成
            boolean moveReceived = moveLatch.await(3, TimeUnit.SECONDS);
            if (moveReceived) {
                System.out.println("✅ 移动测试完成！");
            } else {
                System.out.println("❌ 移动接收超时！");
            }
            
            Thread.sleep(1000);
            
            // 清理
            redClient.disconnect();
            blackClient.disconnect();
            
            System.out.println("🧪🧪 测试完成");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.exit(0);
    }
    
    private static NetworkClient createClient(String playerName, NetworkClient.ClientEventListener listener) {
        NetworkClient client = new NetworkClient();
        client.setEventListener(listener);
        return client;
    }
    
    static class RedPlayerListener implements NetworkClient.ClientEventListener {
        @Override
        public void onConnected() {
            System.out.println("🔴✅ 红方连接成功");
        }
        
        @Override
        public void onDisconnected(String reason) {
            System.out.println("🔴❌ 红方断开: " + reason);
        }
        
        @Override
        public void onConnectionError(String error) {
            System.out.println("🔴🔥 红方连接错误: " + error);
        }
        
        @Override
        public void onRoomCreated(String roomId) {
            System.out.println("🔴🏠 红方房间创建成功: " + roomId);
            TestTwoPlayerMoves.roomId = roomId;
        }
        
        @Override
        public void onGameStarted(String redPlayer, String blackPlayer, String yourColor) {
            System.out.println("🔴🎮 红方游戏开始: 我是" + yourColor + "方");
            gameStartLatch.countDown();
        }
        
        @Override
        public void onMoveReceived(int fromRow, int fromCol, int toRow, int toCol) {
            System.out.println("🔴♟️ 红方收到对手移动: (" + fromRow + "," + fromCol + ") -> (" + toRow + "," + toCol + ")");
            moveLatch.countDown();
        }
        
        @Override
        public void onRoomJoined(String roomId, String opponentName) {}
        @Override
        public void onRoomListReceived(java.util.List<RoomInfo> rooms) {}
        @Override
        public void onGameEnded(String winner, String reason) {}
        @Override
        public void onGameStateUpdate(String gameState, String currentPlayer, boolean isGameOver, String winner) {}
        @Override
        public void onMessageReceived(NetworkMessage message) {}
        @Override
        public void onError(String error) {
            System.out.println("🔴⚠️ 红方错误: " + error);
        }
    }
    
    static class BlackPlayerListener implements NetworkClient.ClientEventListener {
        @Override
        public void onConnected() {
            System.out.println("⚫✅ 黑方连接成功");
        }
        
        @Override
        public void onDisconnected(String reason) {
            System.out.println("⚫❌ 黑方断开: " + reason);
        }
        
        @Override
        public void onConnectionError(String error) {
            System.out.println("⚫🔥 黑方连接错误: " + error);
        }
        
        @Override
        public void onRoomJoined(String roomId, String opponentName) {
            System.out.println("⚫🚪 黑方加入房间成功: " + roomId + ", 对手: " + opponentName);
        }
        
        @Override
        public void onGameStarted(String redPlayer, String blackPlayer, String yourColor) {
            System.out.println("⚫🎮 黑方游戏开始: 我是" + yourColor + "方");
            gameStartLatch.countDown();
        }
        
        @Override
        public void onMoveReceived(int fromRow, int fromCol, int toRow, int toCol) {
            System.out.println("⚫♟️ 黑方收到对手移动: (" + fromRow + "," + fromCol + ") -> (" + toRow + "," + toCol + ")");
        }
        
        @Override
        public void onRoomCreated(String roomId) {}
        @Override
        public void onRoomListReceived(java.util.List<RoomInfo> rooms) {}
        @Override
        public void onGameEnded(String winner, String reason) {}
        @Override
        public void onGameStateUpdate(String gameState, String currentPlayer, boolean isGameOver, String winner) {}
        @Override
        public void onMessageReceived(NetworkMessage message) {}
        @Override
        public void onError(String error) {
            System.out.println("⚫⚠️ 黑方错误: " + error);
        }
    }
}
