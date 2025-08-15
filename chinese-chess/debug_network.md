# Network Debugging Guide

## Issue
Black player's moves are not appearing on the red player's client in network mode.

## Analysis Completed

### ✅ Client-Side Code Analysis
1. **Move Sending** (`NetworkClient.sendMove`): ✅ Correctly implemented
   - Creates `MoveMessage` with proper coordinates
   - Calls `sendMessage()` to serialize and send via socket

2. **Move Reception** (`NetworkClient.handleMove`): ✅ Correctly implemented  
   - Receives move messages and calls `eventListener.onMoveReceived()`
   - Properly handles coordinate data

3. **Move Execution** (`BoardPanel.executeOpponentMove`): ✅ Correctly implemented
   - Uses server coordinates directly (no incorrect transformations)
   - Validates and executes moves on the board
   - Switches players and updates game state

4. **AI Turn Logic Fix**: ✅ Already Fixed
   - `isAITurn()` now returns `false` in network mode
   - Prevents blocking of user clicks during network play

### ✅ Server-Side Code Analysis  
1. **Move Handling** (`ClientHandler.handleMoveMessage`): ✅ Correctly implemented
   - Calls `server.forwardMove()` to relay moves

2. **Move Forwarding** (`ChessGameServer.forwardMove`): ✅ Correctly implemented
   - Finds opponent in the same room
   - Forwards move message to opponent's client

## Next Debugging Steps

### 1. Enable Debug Logging
Run both clients with console output visible to see:
- When moves are sent: `"📤 发送消息: MOVE"`  
- When moves are received: `"📨 收到消息: MOVE"`
- Actual coordinate values in the logs

### 2. Test Sequence
1. Start server: `java -jar target/chinese-chess-1.0-SNAPSHOT.jar server`
2. Start red player client, create room
3. Start black player client, join room  
4. Make a red player move - confirm it appears on black client
5. Make a black player move - check if it appears on red client

### 3. Check for Specific Issues

#### A. Coordinate System Consistency
Both clients should use the same coordinate system. Verify:
- Server coordinates: (0,0) = top-left, red pieces at bottom (rows 7-9)
- Client coordinates should match server exactly

#### B. Network Connection Issues
- Check if black client is actually connected to server
- Verify both clients are in the same room
- Confirm game state is "PLAYING"

#### C. Message Serialization
- Verify JSON serialization/deserialization works correctly
- Check that `MoveMessage` contains all required fields

### 4. Quick Test Commands
```bash
# Start server with verbose logging
cd /Users/rremixwang/workspace/chinese/chinese-chess
mvn exec:java -Dexec.mainClass="com.example.chinesechess.network.ChessGameServer"

# Monitor server logs
tail -f server.log

# Check network connections
netstat -an | grep 8080
```

### 5. Potential Root Causes
Given that the code looks correct, the issue might be:

1. **Network connectivity**: Black client not properly connected
2. **Room state**: Clients not in same room or game not started properly  
3. **Message timing**: Messages sent before game is fully initialized
4. **Server-side filtering**: Server incorrectly filtering/dropping messages
5. **JSON serialization**: Message corruption during network transmission

### 6. Immediate Action Items
1. Test with console logging enabled for both clients
2. Verify server logs show move forwarding
3. Check that both clients receive `GameStartMessage` 
4. Confirm `localPlayerColor` is set correctly on both clients

The network architecture is sound, but there may be a subtle timing or connectivity issue causing the problem.
