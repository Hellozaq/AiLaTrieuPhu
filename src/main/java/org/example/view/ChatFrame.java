package org.example.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatFrame extends JFrame {

    private JTextArea chatArea;
    private JButton startButton;
    private Timer timer;
    private int messageIndex;

    // Các tin nhắn sẽ được hiển thị
    private String[] messages = {
            "A: alo!",
            "B: alo, gì đấy...",
            "A: lâu quá không gặp",
            "B: ừ, thế dạo này thế nào?",
            "A: vẫn ổn, cảm ơn nhé!"
    };

    public ChatFrame() {
        // Thiết lập frame
        setTitle("Chat Simulation");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Tạo và thiết lập chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Tạo và thiết lập nút bắt đầu
        startButton = new JButton("Start Chat");
        add(startButton, BorderLayout.SOUTH);

        // Thiết lập hành động cho nút bắt đầu
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startChat();
            }
        });
    }

    // Bắt đầu chat
    private void startChat() {
        messageIndex = 0;
        chatArea.setText(""); // Xóa nội dung cũ
        startButton.setEnabled(false); // Vô hiệu hóa nút bắt đầu

        // Tạo và thiết lập timer để hiển thị tin nhắn
        timer = new Timer(2000, new ActionListener() { // Thời gian chờ 1 giây giữa các tin nhắn
            @Override
            public void actionPerformed(ActionEvent e) {
                if (messageIndex < messages.length) {
                    chatArea.append(messages[messageIndex] + "\n");
                    messageIndex++;
                } else {
                    timer.stop();
                    dispose();
                    startButton.setEnabled(true); // Kích hoạt lại nút bắt đầu
                }
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatFrame().setVisible(true);
            }
        });
    }
}
