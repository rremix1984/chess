package network;

import java.io.Serializable;

/**
 * 网络消息基础接口
 * 所有网络传输的消息都必须实现此接口
 */
public interface NetworkMessage extends Serializable {
    
    /**
     * 获取消息类型
     * @return 消息类型枚举
     */
    MessageType getType();
    
    /**
     * 消息类型枚举
     */
    enum MessageType {
        // 连接相关
        CONNECT_REQUEST,        // 连接请求
        CONNECT_RESPONSE,       // 连接响应
        DISCONNECT,             // 断开连接
        
        // 房间相关
        CREATE_ROOM_REQUEST,    // 创建房间请求
        CREATE_ROOM_RESPONSE,   // 创建房间响应
        JOIN_ROOM_REQUEST,      // 加入房间请求
        JOIN_ROOM_RESPONSE,     // 加入房间响应
        LEAVE_ROOM,             // 离开房间
        
        // 游戏相关
        GAME_START,             // 游戏开始
        GAME_END,               // 游戏结束
        MOVE,                   // 棋子移动
        GAME_STATE_SYNC,        // 游戏状态同步
        
        // 通用
        HEARTBEAT,              // 心跳包
        ERROR,                  // 错误消息
        TEXT_MESSAGE            // 文本消息
    }
}
