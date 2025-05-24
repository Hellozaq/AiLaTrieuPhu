package org.example.view;

import org.example.model.PlayAudioURL;
import org.example.model.PlayerModel;
import org.example.network.GameClient;
// import org.example.service.PlayerService; // Bỏ import nếu không dùng trực tiếp ở đây

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ModeSelectionFrame2 extends JFrame {

    private PlayerModel player;

    private JButton offlineButton;
    private JButton onlineButton;
    private JLabel titleLabel;
    private JLabel backgroundLabel;

    private JLabel avatarLabel;
    private JLabel usernameLabel;
    private JLabel moneyDisplayPrefixLabel; // Đổi tên để rõ nghĩa hơn "Tiền:"
    private JLabel moneyValueLabel;
    private JLabel userInfoBackgroundLabel;
    private GameClient gameClient;

    public ModeSelectionFrame2(PlayerModel player) {
        this.player = player;

        setTitle("Ai Là Triệu Phú - Trang Chủ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
        loadPlayerData();
        addListeners();

//        PlayAudioURL.playAudio(getClass().getResource("/audio/nhạc-bắt-đầu-chương-trình-ALTP-_2008-2020_.wav"), -10);
    }

    private void initComponents() {
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(700, 500));
        getContentPane().add(layeredPane);

        backgroundLabel = new JLabel();
        try {
            ImageIcon bgIcon = new ImageIcon(getClass().getResource("/background/login-bg.png"));
            Image image = bgIcon.getImage().getScaledInstance(700, 500, Image.SCALE_SMOOTH);
            backgroundLabel.setIcon(new ImageIcon(image));
            backgroundLabel.setBounds(0, 0, 700, 500);
            layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh nền: " + e.getMessage());
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBounds(0, 150, 700, 250);

        titleLabel = new JLabel("CHỌN CHẾ ĐỘ CHƠI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);

        offlineButton = new JButton("CHƠI OFFLINE");
        offlineButton.setFont(new Font("Arial", Font.BOLD, 18));
        offlineButton.setPreferredSize(new Dimension(220, 60));
        offlineButton.setBackground(new Color(0, 122, 204));
        offlineButton.setForeground(Color.WHITE);
        offlineButton.setFocusPainted(false);

        onlineButton = new JButton("CHƠI ONLINE (1 VS 1)");
        onlineButton.setFont(new Font("Arial", Font.BOLD, 18));
        onlineButton.setPreferredSize(new Dimension(280, 60));
        onlineButton.setBackground(new Color(255, 102, 0));
        onlineButton.setForeground(Color.WHITE);
        onlineButton.setFocusPainted(false);

        buttonPanel.add(offlineButton);
        buttonPanel.add(onlineButton);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(buttonPanel);

        layeredPane.add(mainPanel, JLayeredPane.PALETTE_LAYER);

        userInfoBackgroundLabel = new JLabel();
        try {
            userInfoBackgroundLabel.setIcon(new ImageIcon(getClass().getResource("/elements/uname and avt.png")));
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh nền cho user info: " + e.getMessage());
        }
        userInfoBackgroundLabel.setBounds(450, 20, 230, 60);

        avatarLabel = new JLabel();
        avatarLabel.setBounds(453, 23, 30, 30);

        usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Ebrima", Font.BOLD, 14));
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setBounds(490, 25, 100, 20);

        moneyDisplayPrefixLabel = new JLabel("Tiền:"); // Hoặc "Xu:", "Rank:" tùy bạn
        moneyDisplayPrefixLabel.setFont(new Font("Arial", Font.BOLD, 12));
        moneyDisplayPrefixLabel.setForeground(Color.ORANGE);
        moneyDisplayPrefixLabel.setBounds(490, 42, 40, 20); // Điều chỉnh width nếu chữ dài hơn

        moneyValueLabel = new JLabel("0");
        moneyValueLabel.setFont(new Font("Calibri", Font.BOLD, 14));
        moneyValueLabel.setForeground(Color.WHITE);
        moneyValueLabel.setBounds(530, 42, 100, 20); // Điều chỉnh X nếu moneyDisplayPrefixLabel thay đổi


        layeredPane.add(avatarLabel, Integer.valueOf(JLayeredPane.MODAL_LAYER));
        layeredPane.add(usernameLabel, Integer.valueOf(JLayeredPane.MODAL_LAYER));
        layeredPane.add(moneyDisplayPrefixLabel, Integer.valueOf(JLayeredPane.MODAL_LAYER));
        layeredPane.add(moneyValueLabel, Integer.valueOf(JLayeredPane.MODAL_LAYER));
        if (userInfoBackgroundLabel.getIcon() != null) {
            layeredPane.add(userInfoBackgroundLabel, Integer.valueOf(JLayeredPane.PALETTE_LAYER + 1));
        }
    }

    private void loadPlayerData() {
        if (player != null) {
            usernameLabel.setText(player.getUsername());

            // ---- ĐIỀU CHỈNH Ở ĐÂY ----
            // Chọn hiển thị rankScore hay score
            // Giả sử bạn muốn hiển thị rankScore làm "Tiền"
            moneyValueLabel.setText(String.valueOf(player.getRankScore()));
            // Nếu muốn hiển thị score, dùng:
            // moneyValueLabel.setText(String.valueOf(player.getScore()));

            String avatarPath = player.getAvatarPath();
            if (avatarPath != null && !avatarPath.isEmpty()) {
                try {
                    avatarLabel.setIcon(new ImageIcon(getClass().getResource("/avatar/" + avatarPath + ".png")));
                } catch (Exception e) {
                    System.err.println("Không tìm thấy avatar: " + avatarPath + ". " + e.getMessage());
                    avatarLabel.setIcon(new ImageIcon(getClass().getResource("/avatar/default.png")));
                }
            } else {
                avatarLabel.setIcon(new ImageIcon(getClass().getResource("/avatar/default.png")));
            }
        }
    }

    private void addListeners() {
        offlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlayAudioURL.playClickAudio();
//                PlayAudioURL.stopAllAudio();
                if (player.getAvatarPath() == null || player.getAvatarPath().isEmpty()) {
                    JOptionPane.showMessageDialog(ModeSelectionFrame2.this,
                            "Vui lòng chọn avatar trước khi bắt đầu chơi!",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    SelectAvatarFrame.display(player, 1, avatarLabel);
                } else {
                    GameFrame.display(player);
                    dispose();
                }
            }
        });

        onlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlayAudioURL.playClickAudio();
                if (player.getAvatarPath() == null || player.getAvatarPath().isEmpty()) {
                    JOptionPane.showMessageDialog(ModeSelectionFrame2.this,
                            "Vui lòng chọn avatar trước khi bắt đầu chơi online!",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    SelectAvatarFrame.display(player, 1, avatarLabel); // Chế độ 1 là chọn lần đầu
                    return;
                }

                // Khởi tạo và kết nối GameClient
                // Lấy host và port từ một file config hoặc hardcode tạm thời
                String serverHost = "localhost"; // Hoặc địa chỉ IP của server
                int serverPort = 12345;       // Phải khớp với PORT của Server

                gameClient = new GameClient(serverHost, serverPort);
                boolean connected = gameClient.connect(player); // Truyền PlayerModel hiện tại

                if (connected) {
//                    PlayAudioURL.stopAllAudio(); // Dừng nhạc nền của ModeSelectionFrame
                    LobbyFrame.display(player, gameClient); // Mở LobbyFrame và truyền player, gameClient
                    dispose(); // Đóng ModeSelectionFrame
                } else {
                    // Xử lý lỗi kết nối, ví dụ hiển thị JOptionPane
                    JOptionPane.showMessageDialog(ModeSelectionFrame2.this,
                            "Không thể kết nối đến máy chủ game online.\nVui lòng thử lại sau hoặc kiểm tra kết nối mạng.",
                            "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
                    gameClient = null; // Reset gameClient nếu kết nối thất bại
                }
            }
        });

        avatarLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                PlayAudioURL.playPopOnAudio();
                SelectAvatarFrame.display(player, 2, avatarLabel);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                avatarLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                avatarLabel.setCursor(Cursor.getDefaultCursor());
            }
        });

        offlineButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                offlineButton.setBackground(new Color(0, 153, 255));
                PlayAudioURL.playAudio(getClass().getResource("/audio/pop-3-269281.wav"));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                offlineButton.setBackground(new Color(0, 122, 204));
            }
        });

        onlineButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                onlineButton.setBackground(new Color(255, 128, 51));
                PlayAudioURL.playAudio(getClass().getResource("/audio/pop-3-269281.wav"));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                onlineButton.setBackground(new Color(255, 102, 0));
            }
        });
    }

    public static void display(PlayerModel player) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new ModeSelectionFrame2(player).setVisible(true);
            }
        });
    }
}