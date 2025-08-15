package com.example.chinesechess.network;

/**
 * 简单的网络测试客户端
 * 用于调试网络连接和消息传输
 */
public class TestNetworkClient {
    
    public static void main(String[] args) {
        System.out.println("🧪 启动网络测试客户端...");
        
        try {
            // 创建网络客户端
            NetworkClient client = new NetworkClient();
            
            // 设置事件监听器
            client.setEventListener(new NetworkClient.ClientEventListener() {
                @Override
                public void onConnected() {
                    System.out.println("✅ 连接成功");
                }
                
                @Override
                public void onDisconnected(String reason) {
                    System.out.println("❌ 连接断开: " + reason);
                }
                
                @Override
                public void onConnectionError(String error) {
                    System.out.println("🔥 连接错误: " + error);
                }
                
                @Override
                public void onMessageReceived(NetworkMessage message) {
                    System.out.println("📨 收到消息: " + message.getType());
                }
                
                @Override
                public void onRoomCreated(String roomId) {
                    System.out.println("🏠 房间创建成功: " + roomId);
                }
                
                @Override
                public void onRoomJoined(String roomId, String opponentName) {
                    System.out.println("🚪 加入房间成功: " + roomId + ", 对手: " + opponentName);
                }
                
                @Override
                public void onRoomListReceived(java.util.List<RoomInfo> rooms) {
                    System.out.println("📋 收到房间列表, 共 " + rooms.size() + " 个房间:");
                    for (RoomInfo room : rooms) {
                        System.out.println("  - " + room.getRoomId() + ": " + room.getRoomName() + 
                                          " (" + room.getCurrentPlayers() + "/" + room.getMaxPlayers() + ", " + room.getGameStatus() + ")");
                    }
                }
                
                @Override
                public void onGameStarted(String redPlayer, String blackPlayer, String yourColor) {
                    System.out.println("🎮 游戏开始: 红=" + redPlayer + ", 黑=" + blackPlayer + ", 我的颜色=" + yourColor);
                }
                
                @Override
                public void onMoveReceived(int fromRow, int fromCol, int toRow, int toCol) {
                    System.out.println("♟️ 对手移动: (" + fromRow + "," + fromCol + ") -> (" + toRow + "," + toCol + ")");
                }
                
                @Override
                public void onGameEnded(String winner, String reason) {
                    System.out.println("🏁 游戏结束: " + winner + " 获胜, 原因: " + reason);
                }
                
                @Override
                public void onGameStateUpdate(String gameState, String currentPlayer, boolean isGameOver, String winner) {
                    System.out.println("🔄 游戏状态更新: " + gameState + ", 当前玩家: " + currentPlayer);
                }
                
                @Override
                public void onError(String error) {
                    System.out.println("⚠️ 错误: " + error);
                }
            });
            
            // 连接到服务器
            client.connect("localhost", 8080, "TestPlayer");
            
            // 等待一段时间让连接建立
            Thread.sleep(1000);
            
            // 测试创建房间
            if (client.isConnected()) {
                System.out.println("🏠 尝试创建房间...");
                client.createRoom("测试房间", "");
                Thread.sleep(500); // 等待响应
            } else {
                System.out.println("❌ 服务器连接失败，请确认服务器已启动");
            }
            
            client.disconnect();
            System.out.println("🧪 测试完成");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
