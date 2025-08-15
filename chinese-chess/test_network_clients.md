# Network Client Testing Instructions

## Current Status
✅ Server is running on port 8080  
✅ Server is accepting connections

## Next Steps - Manual Testing Required

### Step 1: Start Red Player Client
1. Open a new terminal window
2. Navigate to the project directory:
   ```bash
   cd /Users/rremixwang/workspace/chinese/chinese-chess
   ```
3. Start the first client:
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.chinesechess.ui.GameFrame"
   ```
4. In the GUI:
   - Click "Network Mode" or go to Game menu
   - Choose "Enable Network Mode"
   - Connect to server: localhost:8080
   - Enter player name: "RedPlayer"
   - Create a room with name "TestRoom" and no password

### Step 2: Start Black Player Client  
1. Open another new terminal window
2. Navigate to the project directory:
   ```bash
   cd /Users/rremixwang/workspace/chinese/chinese-chess
   ```
3. Start the second client:
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.chinesechess.ui.GameFrame"
   ```
4. In the GUI:
   - Click "Network Mode" or go to Game menu
   - Choose "Enable Network Mode"  
   - Connect to server: localhost:8080
   - Enter player name: "BlackPlayer"
   - Join the room "TestRoom"

### Step 3: Watch Server Logs
Monitor the server output in the original terminal for:
- Client connections
- Room creation/joining
- Game start messages
- Move forwarding

### Step 4: Test Moves
1. **Red Player Move**: Make a move (e.g., move a pawn forward)
   - Check: Does it appear on Black Player's client?
2. **Black Player Move**: Make a move (e.g., move a pawn forward)  
   - Check: Does it appear on Red Player's client?

### What to Look For

#### Server Console Output Should Show:
```
🔗 新客户端连接: /127.0.0.1:xxxxx
✅ 玩家连接成功: RedPlayer (ID: client_xxxx)
🏠 房间创建: room_xxxx (TestRoom) by RedPlayer
✅ 玩家连接成功: BlackPlayer (ID: client_xxxx)  
🚪 玩家加入房间: BlackPlayer -> room_xxxx
🎮 游戏开始: RedPlayer(红) vs BlackPlayer(黑)
♟️ 转发移动: RedPlayer (x,y) -> (x,y)
♟️ 转发移动: BlackPlayer (x,y) -> (x,y)
```

#### Client Console Output Should Show:
```
📤 发送消息: MOVE
📨 收到消息: MOVE
🔄 执行对手移动: (x,y) -> (x,y)
```

### If Issues Occur:
1. Check all console outputs for error messages
2. Verify both clients show "Game Started" 
3. Confirm players can make moves (no "Not your turn" errors)
4. Look for missing move messages in server logs

Let me know what happens when you run through these steps!
