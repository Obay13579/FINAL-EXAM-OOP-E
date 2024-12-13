package com.collabdraw.server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import com.collabdraw.common.Message;
import com.collabdraw.common.DrawCommand;

public class DrawServer {
    private static final int DEFAULT_PORT = 12345;
    
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private Map<String, ClientHandler> clients;
    private DatabaseConnection db;
    private List<DrawCommand> currentDrawing;
    private boolean running;
    private int port;
    
    public DrawServer(int port) {
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
        this.currentDrawing = new ArrayList<>();
        this.pool = Executors.newCachedThreadPool();
        this.db = new DatabaseConnection();
    }
    
    public DrawServer() {
        this(DEFAULT_PORT);
    }
    
    public void start() {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                pool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }
    
    public void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
        db.saveUser(username);
        
        // Notify others
        broadcast(new Message(Message.MessageType.USER_JOIN, username, 
                            "has joined the room"));
    }
    
    public void removeClient(String username) {
        clients.remove(username);
        broadcast(new Message(Message.MessageType.USER_LEAVE, username, 
                            "has left the room"));
    }
    
    public void broadcast(Message message) {
        if (message.getType() == Message.MessageType.CHAT) {
            db.saveChatMessage(message);
        }
        
        for (ClientHandler client : clients.values()) {
            if (!client.getUsername().equals(message.getSender())) {
                client.sendMessage(message);
            }
        }
    }
    
    public void broadcastDrawCommand(DrawCommand command) {
        currentDrawing.add(command);
        
        for (ClientHandler client : clients.values()) {
            if (!client.getUsername().equals(command.getUsername())) {
                client.sendDrawCommand(command);
            }
        }
    }
    
    public void sendCanvasState(ClientHandler client) {
        // Send recent chat history
        List<Message> recentMessages = db.getRecentChatMessages(50);
        for (Message message : recentMessages) {
            client.sendMessage(message);
        }
        
        // Send current drawing state
        for (DrawCommand command : currentDrawing) {
            client.sendDrawCommand(command);
        }
    }
    
    public void saveCurrentDrawing(String username) {
        db.saveDrawing(username, currentDrawing);
    }
    
    public void shutdown() {
        running = false;
        for (ClientHandler client : clients.values()) {
            client.sendMessage(new Message(Message.MessageType.ERROR, "Server", 
                                         "Server is shutting down"));
        }
        
        try {
            if (serverSocket != null) serverSocket.close();
            if (pool != null) {
                pool.shutdown();
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            }
            if (db != null) db.close();
        } catch (Exception e) {
            System.err.println("Error during server shutdown: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        DrawServer server = new DrawServer(port);
        server.start();
    }
}