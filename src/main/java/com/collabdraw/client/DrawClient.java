package com.collabdraw.client;

import com.collabdraw.client.gui.ChatPanel;
import com.collabdraw.client.gui.ToolPanel;
import com.collabdraw.common.Message;
import com.collabdraw.common.DrawCommand;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Properties;

public class DrawClient extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Network components
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean connected;
    
    // GUI components
    private DrawPanel drawPanel;
    private ChatPanel chatPanel;
    private ToolPanel toolPanel;
    private JLabel statusBar;
    
    // Configuration
    private Properties config;
    
    public DrawClient() {
        loadConfiguration();
        setupGUI();
        connectToServer();
    }
    
    private void loadConfiguration() {
        config = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("client.properties")) {
            config.load(input);
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            // Use defaults
            config.setProperty("server.host", "localhost");
            config.setProperty("server.port", "12345");
        }
    }
    
    private void setupGUI() {
        setTitle("CollabDraw");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create components
        drawPanel = new DrawPanel(this::sendDrawCommand);
        chatPanel = new ChatPanel(this::sendChatMessage);
        toolPanel = new ToolPanel(new ToolPanel.ToolListener() {
            @Override
            public void onColorChanged(Color color) {
                drawPanel.setColor(color);
                sendDrawCommand(DrawCommand.setColor(username, color));
            }
            
            @Override
            public void onStrokeSizeChanged(int size) {
                drawPanel.setStrokeSize(size);
                sendDrawCommand(DrawCommand.setStroke(username, size));
            }
            
            @Override
            public void onClearCanvas() {
                drawPanel.clear();
                sendDrawCommand(DrawCommand.clear(username));
            }
            
            @Override
            public void onSaveDrawing() {
                saveDrawing();
            }
        });
        
        statusBar = new JLabel("Disconnected");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Layout
        setLayout(new BorderLayout());
        
        // Main panel with drawing and chat
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(drawPanel));
        splitPane.setRightComponent(chatPanel);
        splitPane.setResizeWeight(0.7);
        
        // Add components
        add(toolPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void connectToServer() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Get username
                    username = getUsernameFromDialog();
                    if (username == null) {
                        System.exit(0);
                    }
                    
                    // Connect to server
                    String host = config.getProperty("server.host");
                    int port = Integer.parseInt(config.getProperty("server.port"));
                    socket = new Socket(host, port);
                    
                    // Setup streams
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                    
                    // Send login message
                    Message loginMessage = new Message(Message.MessageType.USER_JOIN, 
                                                     username, "");
                    out.writeObject(loginMessage);
                    
                    connected = true;
                    updateStatus("Connected as " + username);
                    
                    // Start message receiver
                    startMessageReceiver();
                    
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(DrawClient.this,
                            "Could not connect to server: " + e.getMessage(),
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    });
                }
                return null;
            }
        };
        worker.execute();
    }
    
    private String getUsernameFromDialog() {
        while (true) {
            String input = JOptionPane.showInputDialog(this,
                "Enter your username:",
                "Login",
                JOptionPane.PLAIN_MESSAGE);
            
            if (input == null) {
                return null;
            }
            
            input = input.trim();
            if (!input.isEmpty()) {
                return input;
            }
        }
    }
    
    private void startMessageReceiver() {
        Thread receiverThread = new Thread(() -> {
            try {
                while (connected) {
                    Object received = in.readObject();
                    if (received instanceof Message) {
                        handleMessage((Message) received);
                    } else if (received instanceof DrawCommand) {
                        handleDrawCommand((DrawCommand) received);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    handleDisconnection();
                }
            }
        });
        receiverThread.start();
    }
    
    private void handleMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatPanel.addMessage(message);
        });
    }
    
    private void handleDrawCommand(DrawCommand command) {
        SwingUtilities.invokeLater(() -> {
            drawPanel.processCommand(command);
        });
    }
    
    private void sendChatMessage(String content) {
        if (!connected) return;
        
        try {
            Message message = new Message(Message.MessageType.CHAT, username, content);
            out.writeObject(message);
        } catch (IOException e) {
            handleDisconnection();
        }
    }
    
    private void sendDrawCommand(DrawCommand command) {
        if (!connected) return;
        
        try {
            out.writeObject(command);
        } catch (IOException e) {
            handleDisconnection();
        }
    }
    
    private void saveDrawing() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getParentFile(), file.getName() + ".png");
            }
            
            try {
                javax.imageio.ImageIO.write(drawPanel.getImage(), "png", file);
                JOptionPane.showMessageDialog(this,
                    "Drawing saved successfully!",
                    "Save Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error saving drawing: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleDisconnection() {
        connected = false;
        SwingUtilities.invokeLater(() -> {
            updateStatus("Disconnected from server");
            JOptionPane.showMessageDialog(this,
                "Lost connection to server",
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
        });
    }
    
    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusBar.setText(status);
        });
    }
    
    @Override
    public void dispose() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            new DrawClient().setVisible(true);
        });
    }
}