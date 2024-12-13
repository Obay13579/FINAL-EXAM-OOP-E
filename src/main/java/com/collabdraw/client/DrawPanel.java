package com.collabdraw.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import com.collabdraw.common.DrawCommand;

public class DrawPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private BufferedImage canvas;
    private Graphics2D g2d;
    private List<DrawCommand> commands;
    private Color currentColor = Color.BLACK;
    private int strokeSize = 2;
    private Point lastPoint;
    private DrawCommandListener commandListener;
    
    public interface DrawCommandListener {
        void onDrawCommand(DrawCommand command);
    }
    
    public DrawPanel(DrawCommandListener listener) {
        this.commandListener = listener;
        this.commands = new ArrayList<>();
        setupPanel();
    }
    
    private void setupPanel() {
        setPreferredSize(new DefaultSize(800, 600));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // Initialize canvas
        createNewCanvas();
        
        // Mouse listeners
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
                DrawCommand command = DrawCommand.startLine(null, lastPoint);
                commandListener.onDrawCommand(command);
                commands.add(command);
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                Point newPoint = e.getPoint();
                drawLine(lastPoint, newPoint);
                DrawCommand command = DrawCommand.drawLine(null, newPoint);
                commandListener.onDrawCommand(command);
                commands.add(command);
                lastPoint = newPoint;
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                DrawCommand command = DrawCommand.endLine(null);
                commandListener.onDrawCommand(command);
                commands.add(command);
            }
        };
        
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }
    
    private void createNewCanvas() {
        canvas = new BufferedImage(getWidth() || 1, getHeight() || 1, 
                                 BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(currentColor);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (canvas != null) {
            g.drawImage(canvas, 0, 0, null);
        }
    }
    
    private void drawLine(Point start, Point end) {
        g2d.setColor(currentColor);
        g2d.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, 
                                    BasicStroke.JOIN_ROUND));
        g2d.drawLine(start.x, start.y, end.x, end.y);
        repaint();
    }
    
    public void processCommand(DrawCommand command) {
        switch (command.getType()) {
            case START_LINE:
                lastPoint = command.getPoint();
                break;
            case DRAW_LINE:
                Point newPoint = command.getPoint();
                drawLine(lastPoint, newPoint);
                lastPoint = newPoint;
                break;
            case CLEAR:
                clear();
                break;
            case SET_COLOR:
                setColor(command.getColor());
                break;
            case SET_STROKE:
                setStrokeSize(command.getStrokeSize());
                break;
        }
        commands.add(command);
    }
    
    public void setColor(Color color) {
        this.currentColor = color;
        if (g2d != null) {
            g2d.setColor(color);
        }
    }
    
    public void setStrokeSize(int size) {
        this.strokeSize = size;
    }
    
    public void clear() {
        createNewCanvas();
        repaint();
    }
    
    public BufferedImage getImage() {
        return canvas;
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        BufferedImage newCanvas = new BufferedImage(width, height, 
                                                  BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newCanvas.createGraphics();
        g.drawImage(canvas, 0, 0, null);
        g.dispose();
        canvas = newCanvas;
        g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
    }
    
    public List<DrawCommand> getCommands() {
        return new ArrayList<>(commands);
    }
}