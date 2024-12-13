package com.collabdraw.client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ToolPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JButton colorButton;
    private JComboBox<Integer> strokeSize;
    private JButton clearButton;
    private JButton saveButton;
    private Color currentColor = Color.BLACK;
    private ToolListener toolListener;
    
    public interface ToolListener {
        void onColorChanged(Color color);
        void onStrokeSizeChanged(int size);
        void onClearCanvas();
        void onSaveDrawing();
    }
    
    public ToolPanel(ToolListener listener) {
        this.toolListener = listener;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Tools"));
        
        // Color button
        colorButton = new JButton("Color");
        colorButton.setBackground(currentColor);
        colorButton.setForeground(Color.WHITE);
        
        // Stroke size combo box
        Integer[] sizes = {1, 2, 3, 4, 5, 6, 8, 10, 12, 14, 16, 20};
        strokeSize = new JComboBox<>(sizes);
        strokeSize.setSelectedItem(2);
        
        // Clear and save buttons
        clearButton = new JButton("Clear");
        saveButton = new JButton("Save");
        
        // Add components
        add(colorButton);
        add(new JLabel("Stroke:"));
        add(strokeSize);
        add(clearButton);
        add(saveButton);
        
        // Add listeners
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(
                this, 
                "Choose Color",
                currentColor
            );
            if (newColor != null) {
                currentColor = newColor;
                colorButton.setBackground(currentColor);
                colorButton.setForeground(getBestForegroundFor(currentColor));
                toolListener.onColorChanged(currentColor);
            }
        });
        
        strokeSize.addActionListener(e -> 
            toolListener.onStrokeSizeChanged((Integer)strokeSize.getSelectedItem())
        );
        
        clearButton.addActionListener(e -> toolListener.onClearCanvas());
        saveButton.addActionListener(e -> toolListener.onSaveDrawing());
    }
    
    private Color getBestForegroundFor(Color background) {
        // Calculate relative luminance
        double luminance = (0.299 * background.getRed() +
                          0.587 * background.getGreen() +
                          0.114 * background.getBlue()) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }
    
    public Color getCurrentColor() {
        return currentColor;
    }
    
    public int getCurrentStrokeSize() {
        return (Integer)strokeSize.getSelectedItem();
    }
}