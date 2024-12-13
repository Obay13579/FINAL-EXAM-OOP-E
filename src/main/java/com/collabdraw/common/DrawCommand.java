package com.collabdraw.common;

import java.io.Serializable;
import java.awt.Color;
import java.awt.Point;

public class DrawCommand implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum CommandType {
        START_LINE,
        DRAW_LINE,
        END_LINE,
        CLEAR,
        SET_COLOR,
        SET_STROKE
    }
    
    private CommandType type;
    private Point point;
    private Color color;
    private int strokeSize;
    private String username;
    
    // Constructors for different command types
    public static DrawCommand startLine(String username, Point point) {
        DrawCommand cmd = new DrawCommand();
        cmd.type = CommandType.START_LINE;
        cmd.point = point;
        cmd.username = username;
        return cmd;
    }
    
    public static DrawCommand drawLine(String username, Point point) {
        DrawCommand cmd = new DrawCommand();
        cmd.type = CommandType.DRAW_LINE;
        cmd.point = point;
        cmd.username = username;
        return cmd;
    }
    
    public static DrawCommand endLine(String username) {
        DrawCommand cmd = new DrawCommand();
        cmd.type = CommandType.END_LINE;
        cmd.username = username;
        return cmd;
    }
    
    public static DrawCommand clear(String username) {
        DrawCommand cmd = new DrawCommand();
        cmd.type = CommandType.CLEAR;
        cmd.username = username;
        return cmd;
    }
    
    public static DrawCommand setColor(String username, Color color) {
        DrawCommand cmd = new DrawCommand();
        cmd.type = CommandType.SET_COLOR;
        cmd.color = color;
        cmd.username = username;
        return cmd;
    }
    
    public static DrawCommand setStroke(String username, int size) {
        DrawCommand cmd = new DrawCommand();
        cmd.type = CommandType.SET_STROKE;
        cmd.strokeSize = size;
        cmd.username = username;
        return cmd;
    }
    
    // Getters
    public CommandType getType() { return type; }
    public Point getPoint() { return point; }
    public Color getColor() { return color; }
    public int getStrokeSize() { return strokeSize; }
    public String getUsername() { return username; }
}