package org.example.view;

import javax.swing.*;
import java.awt.*;

public class QuizShowGUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Quiz Show");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Display area
        JLabel displayArea = new JLabel("Câu hỏi hiển thị ở đây", SwingConstants.CENTER);
        displayArea.setFont(new Font("Arial", Font.BOLD, 20));
        displayArea.setPreferredSize(new Dimension(800, 100));
        mainPanel.add(displayArea, BorderLayout.NORTH);

        // Answer buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 10, 10));

        JButton answerButton1 = new JButton("Đáp án A");
        JButton answerButton2 = new JButton("Đáp án B");
        JButton answerButton3 = new JButton("Đáp án C");
        JButton answerButton4 = new JButton("Đáp án D");

        // Make buttons transparent
        makeButtonTransparent(answerButton1);
        makeButtonTransparent(answerButton2);
        makeButtonTransparent(answerButton3);
        makeButtonTransparent(answerButton4);

        buttonPanel.add(answerButton1);
        buttonPanel.add(answerButton2);
        buttonPanel.add(answerButton3);
        buttonPanel.add(answerButton4);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Prize ladder
        JPanel prizePanel = new JPanel();
        prizePanel.setLayout(new GridLayout(15, 1));
        String[] prizes = {"100 TRIỆU", "56,000,000", "40,000,000", "32,000,000", "16,000,000",
                "12 TRIỆU", "8,000,000", "6,000,000", "4,000,000", "2,000,000",
                "1 TRIỆU", "500,000", "300,000", "200,000", "100,000"};
        for (int i = 0; i < prizes.length; i++) {
            JLabel prizeLabel = new JLabel((15 - i) + ": " + prizes[i], SwingConstants.CENTER);
            prizeLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            prizePanel.add(prizeLabel);
        }

        mainPanel.add(prizePanel, BorderLayout.EAST);

        // Add main panel to frame
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    static void makeButtonTransparent(JButton button) {
        button.setContentAreaFilled(false);
        button.setOpaque(false);
    }
}
