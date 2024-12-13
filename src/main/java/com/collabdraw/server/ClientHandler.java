package com.collabdraw.server;

import java.io.*;
import java.net.*;
import java.util.*;
import com.collabdraw.common.Message;
import com.collabdraw.common.DrawCommand;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DrawServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private boolean running;
    
    public ClientHandler(Socket socket, DrawServer server) {
        this.socket = socket;
        this.server = server;
        this.running = true;
    }
    
    @Override
    public void run() {
        try {
            setupStreams();
            handleLogin();
            processCommands();
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void setupStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }
    
    private void handleLogin() throws IOException {
        try {
            Message loginMessage = (Message) in.readObject();
            if (loginMessage.getType() == Message.MessageType.USER_JOIN) {
                this.username = loginMessage.getSender();
                server.addClient(username, this);
                // Send current canvas state to new client
                server.sendCanvasState(this);
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Invalid login message");
        }
    }
    
    private void processCommands() {
        try {
            while (running) {
                Object command = in.readObject();
                if (command instanceof Message) {
                    server.broadcast((Message) command);
                } else if (command instanceof DrawCommand) {
                    server.broadcastDrawCommand((DrawCommand) command);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            running = false;
        }
    }
    
    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to " + username + ": " + e.getMessage());
            running = false;
        }
    }
    
    public void sendDrawCommand(DrawCommand command) {
        try {
            out.writeObject(command);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending draw command to " + username + ": " + e.getMessage());
            running = false;
        }
    }
    
    public String getUsername() {
        return username;
    }
    
    private void cleanup() {
        running = false;
        server.removeClient(username);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error cleaning up client handler: " + e.getMessage());
        }
    }
}