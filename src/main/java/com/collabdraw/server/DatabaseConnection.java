package com.collabdraw.server;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.util.Properties;
import com.collabdraw.common.Message;
import com.collabdraw.common.DrawCommand;

public class DatabaseConnection {
    private Connection conn;
    private Properties props;
    
    public DatabaseConnection() {
        loadProperties();
        connect();
    }
    
    private void loadProperties() {
        props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            props.load(input);
        } catch (IOException e) {
            System.err.println("Error loading database properties: " + e.getMessage());
            // Use defaults
            props.setProperty("db.url", "jdbc:mysql://localhost:3306/collabdraw");
            props.setProperty("db.user", "root");
            props.setProperty("db.password", "password");
        }
    }
    
    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
            );
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
    
    public void saveUser(String username) {
        String sql = "INSERT IGNORE INTO users (username) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }
    
    public void saveChatMessage(Message message) {
        String sql = "INSERT INTO chat_history (username, message) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message.getSender());
            pstmt.setString(2, message.getContent());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving chat message: " + e.getMessage());
        }
    }
    
    public void saveDrawing(String username, List<DrawCommand> commands) {
        String sql = "INSERT INTO drawings (username, data) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(commands);
            
            pstmt.setString(1, username);
            pstmt.setBytes(2, bos.toByteArray());
            pstmt.executeUpdate();
        } catch (SQLException | IOException e) {
            System.err.println("Error saving drawing: " + e.getMessage());
        }
    }
    
    public List<Message> getRecentChatMessages(int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT username, message, created_at FROM chat_history " +
                    "ORDER BY created_at DESC LIMIT ?";
                    
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message msg = new Message(
                    Message.MessageType.CHAT,
                    rs.getString("username"),
                    rs.getString("message")
                );
                messages.add(msg);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving chat messages: " + e.getMessage());
        }
        
        Collections.reverse(messages);
        return messages;
    }
    
    public List<DrawCommand> getLatestDrawing() {
        String sql = "SELECT data FROM drawings ORDER BY created_at DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                byte[] data = rs.getBytes("data");
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
                return (List<DrawCommand>) in.readObject();
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            System.err.println("Error retrieving latest drawing: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}