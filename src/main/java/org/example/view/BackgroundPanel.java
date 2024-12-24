package org.example.view;

import javax.swing.*;
import java.awt.*;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    public BackgroundPanel(String backgroundImagePath) {
        this.backgroundImage = new ImageIcon(backgroundImagePath).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(),getHeight() ,this);
    }
}
