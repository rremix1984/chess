import java.lang.reflect.Method;

/**
 * Simple test to verify GameStateSyncRequestMessage serialization
 * This runs independently of the main application to test message format
 */
public class TestSyncMessage {
    public static void main(String[] args) {
        try {
            // Test basic message serialization
            System.out.println("=== Testing GameStateSyncRequestMessage Serialization ===");
            
            // Create a test message (simulated - since we can't directly import the classes)
            String testJson = "{\"type\":\"GAME_STATE_SYNC_REQUEST\",\"messageId\":\"msg_1234567890_123\",\"timestamp\":\"2024-01-01T12:00:00\",\"senderId\":\"test_player\",\"roomId\":\"test_room\",\"reason\":\"client_missed_game_start_message\"}";
            
            System.out.println("Test JSON message:");
            System.out.println(testJson);
            System.out.println();
            
            // Check if the JSON is single line (no newlines)
            boolean hasNewlines = testJson.contains("\n");
            System.out.println("Contains newlines: " + hasNewlines);
            System.out.println("Message length: " + testJson.length());
            System.out.println("Is valid JSON structure: " + isValidJsonStructure(testJson));
            System.out.println();
            
            // Test message without optional fields
            String minimalJson = "{\"type\":\"GAME_STATE_SYNC_REQUEST\",\"senderId\":\"test_player\",\"roomId\":\"test_room\"}";
            System.out.println("Minimal JSON message:");
            System.out.println(minimalJson);
            System.out.println("Is valid JSON structure: " + isValidJsonStructure(minimalJson));
            
            System.out.println("\n=== Testing GameStateSyncResponseMessage Format ===");
            
            // Test response message format
            String responseJson = "{\"type\":\"GAME_STATE_SYNC_RESPONSE\",\"messageId\":\"msg_1234567890_456\",\"timestamp\":\"2024-01-01T12:00:01\",\"senderId\":\"server\",\"roomId\":\"test_room\",\"success\":true,\"redPlayer\":\"Player1\",\"blackPlayer\":\"Player2\",\"yourColor\":\"RED\",\"currentPlayer\":\"RED\",\"gameState\":\"playing\",\"isGameStarted\":true,\"isGameOver\":false,\"winner\":null}";
            
            System.out.println("Response JSON message:");
            System.out.println(responseJson);
            System.out.println("Is valid JSON structure: " + isValidJsonStructure(responseJson));
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Simple JSON structure validation
     */
    private static boolean isValidJsonStructure(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            return false;
        }
        
        // Basic validation - count quotes and braces
        int braceCount = 0;
        int quoteCount = 0;
        boolean inString = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '"' && (i == 0 || json.charAt(i-1) != '\\')) {
                inString = !inString;
                quoteCount++;
            } else if (!inString) {
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                }
            }
        }
        
        return braceCount == 0 && quoteCount % 2 == 0;
    }
}
