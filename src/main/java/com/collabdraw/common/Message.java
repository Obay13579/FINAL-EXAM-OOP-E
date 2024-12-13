package com.collabdraw.common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        CHAT,
        DRAW,
        USER_JOIN,
        USER_LEAVE,
        ERROR
    }
    
    private MessageType type;
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    
    public Message(MessageType type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public MessageType getType() { return type; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", timestamp, sender, content);
    }
}