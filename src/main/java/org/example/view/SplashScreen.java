package org.example.view;

import org.example.conf.HibernateUtil;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JFrame {
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public SplashScreen() {
        setTitle("Đang khởi động...");
        setSize(350, 120);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Đang khởi động máy chủ...", SwingConstants.CENTER);
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        add(statusLabel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
    }

    public void startApp() {
        setVisible(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                HibernateUtil.getSessionFactory();
                return null;
            }

            @Override
            protected void done() {
                setVisible(false);
                dispose();
                WelcomeFrame.display();
            }
        };
        worker.execute();
    }
}