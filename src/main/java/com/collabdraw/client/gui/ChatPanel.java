package com.collabdraw.client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.collabdraw.common.Message;

public class ChatPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private ChatMessageListener messageListener;
    
    public interface ChatMessageListener {
        void onMessageSent(String message);
    }
    
    public ChatPanel(ChatMessageListener listener) {
        this.messageListener = listener;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Chat"));
        
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setWrapStyleWord(true);
        chatArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputField = new JTextField();
        sendButton = new JButton("Send");
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        // Add listeners
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }
    
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            messageListener.onMessageSent(message);
            inputField.setText("");
        }
        inputField.requestFocus();
    }
    
    public void addMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message.toString() + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    public void clear() {
        chatArea.setText("");
    }
}