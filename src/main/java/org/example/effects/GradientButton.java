package org.example.effects;

import javax.swing.*;
import java.awt.*;

class GradientButton extends JButton {
    private Color color1;
    private Color color2;

    public GradientButton(String text, Color color1, Color color2) {
        super(text);
        this.color1 = color1;
        this.color2 = color2;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        GradientPaint gradient = new GradientPaint(0, 0, color1, 0, height, color2);
        g2.setPaint(gradient);
        g2.fillRect(0, 0, width, height);
        super.paintComponent(g);
        g2.dispose();
    }
}

