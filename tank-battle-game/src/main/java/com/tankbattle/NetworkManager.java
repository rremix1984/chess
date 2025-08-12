package com.tankbattle;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 网络管理器，处理多人连机功能
 */
public class NetworkManager {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private List<ClientHandler> clients;
    private boolean isServer;
    private GameMessageListener messageListener;
    
    public NetworkManager() {
        clients = new CopyOnWriteArrayList<>();
    }
    
    /**
     * 启动服务器
     */
    public boolean startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isServer = true;
            
            // 在新线程中监听客户端连接
            new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                        clients.add(clientHandler);
                        new Thread(clientHandler).start();
                        System.out.println("客户端连接: " + clientSocket.getInetAddress());
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 连接到服务器
     */
    public boolean connectToServer(String host, int port) {
        try {
            clientSocket = new Socket(host, port);
            isServer = false;
            
            // 在新线程中监听服务器消息
            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String message;
                    while ((message = reader.readLine()) != null) {
                        if (messageListener != null) {
                            messageListener.onMessageReceived(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 广播消息给所有客户端
     */
    public void broadcastMessage(String message) {
        if (isServer) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }
    
    /**
     * 发送消息到服务器
     */
    public void sendToServer(String message) {
        if (!isServer && clientSocket != null) {
            try {
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                writer.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 设置消息监听器
     */
    public void setMessageListener(GameMessageListener listener) {
        this.messageListener = listener;
    }
    
    /**
     * 关闭网络连接
     */
    public void close() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            clients.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 处理客户端连接的内部类
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private NetworkManager networkManager;
        private PrintWriter writer;
        private BufferedReader reader;
        
        public ClientHandler(Socket socket, NetworkManager networkManager) {
            this.socket = socket;
            this.networkManager = networkManager;
            try {
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    // 转发消息给其他客户端
                    for (ClientHandler client : networkManager.clients) {
                        if (client != this) {
                            client.sendMessage(message);
                        }
                    }
                    
                    // 通知游戏逻辑
                    if (networkManager.messageListener != null) {
                        networkManager.messageListener.onMessageReceived(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    networkManager.clients.remove(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        public void sendMessage(String message) {
            if (writer != null) {
                writer.println(message);
            }
        }
    }
    
    /**
     * 游戏消息监听接口
     */
    public interface GameMessageListener {
        void onMessageReceived(String message);
    }
}
