package org.example.view;

import org.example.model.PlayAudioURL;
import org.example.model.PlayerModel;
import org.example.network.GameClient;
import org.example.network.Message;
import org.example.network.MessageType;
import org.example.network.RoomInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class LobbyFrame extends JFrame {

    private PlayerModel currentPlayer;
    private GameClient gameClient;

    // UI Components
    // Khu vực danh sách phòng
    private JList<RoomInfo> roomList;
    private DefaultListModel<RoomInfo> roomListModel;
    private JButton refreshRoomListButton;
    private JButton createRoomButton;
    private JButton joinRoomButton;

    // Khu vực thông tin phòng hiện tại
    private JPanel currentRoomPanel;
    private JLabel roomNameLabel;
    private JLabel betAmountLabel;

    private JLabel player1AvatarLabel;
    private JLabel player1NameLabel;
    private JCheckBox player1ReadyCheckBox; // Hoặc JLabel hiển thị trạng thái

    private JLabel player2AvatarLabel;
    private JLabel player2NameLabel;
    private JCheckBox player2ReadyCheckBox; // Hoặc JLabel

    private JButton readyButton;
    private JButton leaveRoomButton;

    // Khu vực chat
    private JTextArea chatArea;
    private JTextField chatInputField;
    private JButton sendChatButton;

    // Thông tin người chơi hiện tại
    private JLabel currentPlayerNameLabel;
    private JLabel currentPlayerMoneyLabel;
    private JLabel currentPlayerAvatarLabel; // Avatar của người chơi hiện tại

    private RoomInfo currentJoinedRoomInfo; // Thông tin phòng đang tham gia
    private boolean isPlayerReady = false; // Trạng thái sẵn sàng của người chơi hiện tại

    private Map<String, PlayerModel> knownPlayers = new HashMap<>(); // Lưu PlayerModel của bản thân và đối thủ


    private static final Logger logger = Logger.getLogger(LobbyFrame.class.getName()); // Client-side logging

    public LobbyFrame(PlayerModel player, GameClient client) {
        this.currentPlayer = player;
        this.gameClient = client;
        this.gameClient.setLobbyFrame(this); // Rất quan trọng: để GameClient gọi lại LobbyFrame
        this.knownPlayers.put(player.getUsername(), player); // Thêm chính mình vào


        setTitle("Sảnh Chờ Game - Ai Là Triệu Phú Online");
        setSize(900, 700); // Kích thước lớn hơn để chứa các thành phần
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Xử lý đóng cửa sổ riêng
        setLayout(new BorderLayout(10, 10)); // Layout chính

        initComponents();
        addListeners();

        // Yêu cầu danh sách phòng khi mở lobby
        gameClient.sendMessage(new Message(MessageType.C2S_GET_ROOM_LIST, null));
        updateCurrentPlayerDisplay(); // Hiển thị thông tin người chơi ban đầu
//        PlayAudioURL.playAudio(getClass().getResource("/audio/lobby_music.wav"), -10); // Ví dụ nhạc nền lobby
    }

    private void initComponents() {
        // Panel chính cho người chơi và phòng
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        // Panel thông tin người chơi hiện tại (góc trên trái)
        JPanel playerInfoPanel = createPlayerInfoPanel();
        topPanel.add(playerInfoPanel, BorderLayout.WEST);

        // Panel danh sách phòng (ở giữa phía trên)
        JPanel roomListPanel = createRoomListPanel();
        topPanel.add(roomListPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Panel cho phòng hiện tại và chat (chia làm 2 phần)
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setResizeWeight(0.6); // Phòng hiện tại chiếm 60%

        currentRoomPanel = createCurrentRoomPanel();
        centerSplitPane.setLeftComponent(currentRoomPanel);

        JPanel chatPanel = createChatPanel();
        centerSplitPane.setRightComponent(chatPanel);

        add(centerSplitPane, BorderLayout.CENTER);

        // Khởi tạo ẩn panel phòng hiện tại
        currentRoomPanel.setVisible(false);
    }

    private JPanel createPlayerInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Thông Tin Cá Nhân"));
        panel.setPreferredSize(new Dimension(200, 120)); // Kích thước cố định

        currentPlayerAvatarLabel = new JLabel();
        currentPlayerAvatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Đặt kích thước cho avatar
        currentPlayerAvatarLabel.setPreferredSize(new Dimension(50, 50));
        currentPlayerAvatarLabel.setMinimumSize(new Dimension(50, 50));
        currentPlayerAvatarLabel.setMaximumSize(new Dimension(50, 50));
        currentPlayerAvatarLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));


        currentPlayerNameLabel = new JLabel("Tên: Loading...");
        currentPlayerNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        currentPlayerMoneyLabel = new JLabel("Tiền: Loading...");
        currentPlayerMoneyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(5));
        panel.add(currentPlayerAvatarLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(currentPlayerNameLabel);
        panel.add(currentPlayerMoneyLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }


    private JPanel createRoomListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Danh Sách Phòng Chơi"));

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setCellRenderer(new RoomListRenderer()); // Custom renderer để hiển thị đẹp hơn
        JScrollPane scrollPane = new JScrollPane(roomList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        refreshRoomListButton = new JButton("Làm Mới");
        createRoomButton = new JButton("Tạo Phòng Mới");
        joinRoomButton = new JButton("Vào Phòng Đã Chọn");
        joinRoomButton.setEnabled(false); // Ban đầu không có phòng nào được chọn

        buttonPanel.add(refreshRoomListButton);
        buttonPanel.add(createRoomButton);
        buttonPanel.add(joinRoomButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCurrentRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Phòng Chờ"));
        panel.setPreferredSize(new Dimension(450, 0));


        // Panel thông tin chung của phòng
        JPanel roomDetailsPanel = new JPanel();
        roomDetailsPanel.setLayout(new BoxLayout(roomDetailsPanel, BoxLayout.Y_AXIS));
        roomNameLabel = new JLabel("ID Phòng: ");
        roomNameLabel.setFont(roomNameLabel.getFont().deriveFont(Font.BOLD, 16f));
        roomNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        betAmountLabel = new JLabel("Mức cược: ");
        betAmountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomDetailsPanel.add(roomNameLabel);
        roomDetailsPanel.add(betAmountLabel);
        panel.add(roomDetailsPanel, BorderLayout.NORTH);


        // Panel chứa thông tin 2 người chơi
        JPanel playersPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // 1 hàng, 2 cột

        // Player 1 (chủ phòng / người vào trước)
        JPanel p1Panel = new JPanel();
        p1Panel.setLayout(new BoxLayout(p1Panel, BoxLayout.Y_AXIS));
        p1Panel.setBorder(BorderFactory.createTitledBorder("Người chơi 1"));
        player1AvatarLabel = new JLabel(); // Đặt kích thước cố định
        player1AvatarLabel.setPreferredSize(new Dimension(80, 80));
        player1AvatarLabel.setMinimumSize(new Dimension(80, 80));
        player1AvatarLabel.setMaximumSize(new Dimension(80, 80));
        player1AvatarLabel.setBorder(BorderFactory.createEtchedBorder());
        player1AvatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        player1NameLabel = new JLabel("Đang chờ...");
        player1ReadyCheckBox = new JCheckBox("Sẵn sàng");
        player1ReadyCheckBox.setEnabled(false); // Chỉ chủ nhân của slot này mới được tick

        p1Panel.add(player1AvatarLabel);
        p1Panel.add(Box.createRigidArea(new Dimension(0, 5)));
        p1Panel.add(player1NameLabel);
        p1Panel.add(player1ReadyCheckBox);
        alignComponentsCenter(p1Panel);


        // Player 2
        JPanel p2Panel = new JPanel();
        p2Panel.setLayout(new BoxLayout(p2Panel, BoxLayout.Y_AXIS));
        p2Panel.setBorder(BorderFactory.createTitledBorder("Người chơi 2"));
        player2AvatarLabel = new JLabel();
        player2AvatarLabel.setPreferredSize(new Dimension(80, 80));
        player2AvatarLabel.setMinimumSize(new Dimension(80, 80));
        player2AvatarLabel.setMaximumSize(new Dimension(80, 80));
        player2AvatarLabel.setBorder(BorderFactory.createEtchedBorder());
        player2AvatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        player2NameLabel = new JLabel("Đang chờ...");
        player2ReadyCheckBox = new JCheckBox("Sẵn sàng");
        player2ReadyCheckBox.setEnabled(false);

        p2Panel.add(player2AvatarLabel);
        p2Panel.add(Box.createRigidArea(new Dimension(0, 5)));
        p2Panel.add(player2NameLabel);
        p2Panel.add(player2ReadyCheckBox);
        alignComponentsCenter(p2Panel);

        playersPanel.add(p1Panel);
        playersPanel.add(p2Panel);
        panel.add(playersPanel, BorderLayout.CENTER);

        // Panel nút điều khiển phòng
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        readyButton = new JButton("Sẵn Sàng");
        leaveRoomButton = new JButton("Rời Phòng");
        controlPanel.add(readyButton);
        controlPanel.add(leaveRoomButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }
    private void alignComponentsCenter(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel || comp instanceof JCheckBox) {
                ((JComponent) comp).setAlignmentX(Component.CENTER_ALIGNMENT);
            }
        }
    }


    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Chat Trong Phòng"));
        panel.setPreferredSize(new Dimension(300,0));


        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret)chatArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // Tự động cuộn xuống
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        chatInputField = new JTextField();
        sendChatButton = new JButton("Gửi");
        inputPanel.add(chatInputField, BorderLayout.CENTER);
        inputPanel.add(sendChatButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addListeners() {
        // Xử lý đóng cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });

        refreshRoomListButton.addActionListener(e -> {
            PlayAudioURL.playClickAudio();
            gameClient.sendMessage(new Message(MessageType.C2S_GET_ROOM_LIST, null));
            logger.info("Yêu cầu làm mới danh sách phòng.");
        });

        createRoomButton.addActionListener(e -> {
            PlayAudioURL.playClickAudio();
            handleCreateRoom();
        });

        roomList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                joinRoomButton.setEnabled(roomList.getSelectedValue() != null);
            }
        });

        joinRoomButton.addActionListener(e -> {
            PlayAudioURL.playClickAudio();
            RoomInfo selectedRoom = roomList.getSelectedValue();
            if (selectedRoom != null) {
                // Kiểm tra xem phòng có đang chờ không
                if ("WAITING".equalsIgnoreCase(selectedRoom.getStatus()) || "WAITING_READY".equalsIgnoreCase(selectedRoom.getStatus()) && selectedRoom.getPlayerCount() < 2) {
                    if (currentPlayer.getRankScore() < selectedRoom.getBetAmount()) {
                        JOptionPane.showMessageDialog(this, "Bạn không đủ " + selectedRoom.getBetAmount() + " xu để vào phòng này.", "Không Đủ Tiền", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    logger.info("Yêu cầu tham gia phòng: " + selectedRoom.getRoomId());
                    gameClient.sendMessage(new Message(MessageType.C2S_JOIN_ROOM, selectedRoom.getRoomId()));
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể tham gia phòng này. Phòng có thể đã đầy hoặc đang chơi.", "Không Thể Tham Gia", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        readyButton.addActionListener(e -> {
            PlayAudioURL.playClickAudio();
            isPlayerReady = !isPlayerReady; // Đảo trạng thái
            readyButton.setText(isPlayerReady ? "Hủy Sẵn Sàng" : "Sẵn Sàng");
            // Cập nhật checkbox của chính mình
            if (currentJoinedRoomInfo != null) {
                if (currentPlayer.getUsername().equals(currentJoinedRoomInfo.getPlayer1Name())) {
                    player1ReadyCheckBox.setSelected(isPlayerReady);
                } else if (currentPlayer.getUsername().equals(currentJoinedRoomInfo.getPlayer2Name())) {
                    player2ReadyCheckBox.setSelected(isPlayerReady);
                }
            }
            gameClient.sendMessage(new Message(MessageType.C2S_PLAYER_READY, isPlayerReady));
            logger.info("Gửi trạng thái sẵn sàng: " + isPlayerReady);
        });

        leaveRoomButton.addActionListener(e -> {
            PlayAudioURL.playClickAudio();
            if (currentJoinedRoomInfo != null) {
                logger.info("Yêu cầu rời phòng: " + currentJoinedRoomInfo.getRoomId());
                gameClient.sendMessage(new Message(MessageType.C2S_LEAVE_ROOM, null));
            }
        });

        ActionListener sendChatAction = e -> {
            String messageText = chatInputField.getText().trim();
            if (!messageText.isEmpty()) {
                PlayAudioURL.playClickAudio();
                gameClient.sendMessage(new Message(MessageType.C2S_LOBBY_CHAT, messageText));
                chatInputField.setText("");
                logger.info("Gửi tin nhắn chat: " + messageText);
            }
        };
        sendChatButton.addActionListener(sendChatAction);
        chatInputField.addActionListener(sendChatAction); // Enter để gửi
    }

    private void handleWindowClose() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn thoát sảnh chờ không?",
                "Xác Nhận Thoát",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (currentJoinedRoomInfo != null) { // Nếu đang trong phòng thì gửi yêu cầu rời phòng
                gameClient.sendMessage(new Message(MessageType.C2S_LEAVE_ROOM, null));
            }
            gameClient.disconnect(); // Ngắt kết nối client
//            PlayAudioURL.stopAllAudio();
            // Quay lại ModeSelectionFrame hoặc WelcomeFrame tùy logic
            ModeSelectionFrame.display(currentPlayer); // Ví dụ: quay lại chọn mode
            dispose();
        }
    }

    private void handleCreateRoom() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nhập số tiền cược cho phòng:"));
        JTextField betField = new JTextField("1000"); // Giá trị mặc định
        panel.add(betField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tạo Phòng Mới",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int betAmount = Integer.parseInt(betField.getText());
                if (betAmount <= 0) {
                    JOptionPane.showMessageDialog(this, "Số tiền cược phải lớn hơn 0.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (currentPlayer.getRankScore() < betAmount) {
                    JOptionPane.showMessageDialog(this, "Bạn không đủ " + betAmount + " xu để tạo phòng này.", "Không Đủ Tiền", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                logger.info("Yêu cầu tạo phòng với mức cược: " + betAmount);
                gameClient.sendMessage(new Message(MessageType.C2S_CREATE_ROOM, betAmount));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Số tiền cược không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ----- Phương thức được gọi bởi GameClient để cập nhật UI -----

    public void updateRoomList(List<RoomInfo> roomInfos) {
        SwingUtilities.invokeLater(() -> {
            roomListModel.clear();
            if (roomInfos != null) {
                for (RoomInfo info : roomInfos) {
                    roomListModel.addElement(info);
                }
            }
            logger.fine("Danh sách phòng trên UI đã được cập nhật.");
        });
    }

    public void handleRoomJoined(RoomInfo joinedRoomInfoFromServer) {
        SwingUtilities.invokeLater(() -> {
            currentJoinedRoomInfo = joinedRoomInfoFromServer; // Luôn lấy thông tin mới nhất từ server
            currentRoomPanel.setVisible(true);
            roomNameLabel.setText("ID Phòng: " + currentJoinedRoomInfo.getRoomId());
            betAmountLabel.setText("Mức cược: " + currentJoinedRoomInfo.getBetAmount() + " xu");

            clearPlayerSlot(player1AvatarLabel, player1NameLabel, player1ReadyCheckBox);
            clearPlayerSlot(player2AvatarLabel, player2NameLabel, player2ReadyCheckBox);

            if (currentJoinedRoomInfo.getPlayer1Name() != null) {
                // Lấy avatar path cho player1 nếu có (ví dụ server gửi PlayerModel đầy đủ trong 1 message khác, hoặc client tự query)
                // Tạm thời vẫn dùng default
                setPlayerSlot(player1AvatarLabel, player1NameLabel, player1ReadyCheckBox,
                        currentJoinedRoomInfo.getPlayer1Name(), false); // Trạng thái ready ban đầu là false
                if(currentPlayer.getUsername().equals(currentJoinedRoomInfo.getPlayer1Name())) {
                    // Kiểm tra trạng thái ready của chính mình từ server nếu có, hoặc mặc định
                    player1ReadyCheckBox.setSelected(isPlayerReady); // isPlayerReady là trạng thái của client hiện tại
                }
            }

            if (currentJoinedRoomInfo.getPlayer2Name() != null) {
                setPlayerSlot(player2AvatarLabel, player2NameLabel, player2ReadyCheckBox,
                        currentJoinedRoomInfo.getPlayer2Name(), false);
                if(currentPlayer.getUsername().equals(currentJoinedRoomInfo.getPlayer2Name())) {
                    player2ReadyCheckBox.setSelected(isPlayerReady);
                }
            } else {
                player2NameLabel.setText("Đang chờ đối thủ...");
            }

            // Không cần isPlayerReady = false; ở đây nữa vì nó nên được quản lý độc lập
            // readyButton.setText(isPlayerReady ? "Hủy Sẵn Sàng" : "Sẵn Sàng");

            createRoomButton.setEnabled(false);
            joinRoomButton.setEnabled(false);
            roomList.setEnabled(false);
            refreshRoomListButton.setEnabled(false);
            logger.info("Đã vào/cập nhật phòng: " + currentJoinedRoomInfo.getRoomId() + ". UI được cập nhật.");
        });
    }

    private void clearPlayerSlot(JLabel avatar, JLabel name, JCheckBox ready) {
        avatar.setIcon(null); // Xóa avatar
        avatar.setText("Trống");
        name.setText("Đang chờ...");
        ready.setSelected(false);
        ready.setEnabled(false);
    }

    private void setPlayerSlot(JLabel avatarLabel, JLabel nameLabel, JCheckBox readyCheckBox, String playerName, boolean isReady) {
        PlayerModel playerToShow = knownPlayers.get(playerName);
        String avatarPath = "default"; // Mặc định
        if (playerToShow != null && playerToShow.getAvatarPath() != null && !playerToShow.getAvatarPath().isEmpty()) {
            avatarPath = playerToShow.getAvatarPath();
        }

        try {
            avatarLabel.setText("");
            avatarLabel.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/avatar/" + avatarPath + ".png")).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            avatarLabel.setIcon(null);
            avatarLabel.setText("N/A");
            logger.warning("Không thể tải avatar cho: " + playerName + " với path: " + avatarPath);
        }

        nameLabel.setText(playerName);
        readyCheckBox.setSelected(isReady);
        readyCheckBox.setEnabled(false); // Luôn disable checkbox, trạng thái được điều khiển bởi server và nút "Ready"
    }

    public void cacheOpponentPlayerModel(PlayerModel opponent) {
        if (opponent != null) {
            this.knownPlayers.put(opponent.getUsername(), opponent);
        }
    }

//    public void handleOpponentJoined(PlayerModel opponent) {
//        SwingUtilities.invokeLater(() -> {
//            if (currentJoinedRoomInfo != null) {
//                // Giả sử người chơi hiện tại là player1, đối thủ sẽ là player2
//                // Hoặc ngược lại. Cần xác định đúng slot.
//                if (currentPlayer.getUsername().equals(currentJoinedRoomInfo.getPlayer1Name())) {
//                    currentJoinedRoomInfo.setPlayer2Name(opponent.getUsername()); // Cập nhật tạm thời RoomInfo ở client
//                    setPlayerSlot(player2AvatarLabel, player2NameLabel, player2ReadyCheckBox, opponent.getUsername(), false);
//                } else { // Người chơi hiện tại là player2 (khó xảy ra nếu join là người thứ 2)
//                    currentJoinedRoomInfo.setPlayer1Name(opponent.getUsername());
//                    setPlayerSlot(player1AvatarLabel, player1NameLabel, player1ReadyCheckBox, opponent.getUsername(), false);
//                }
//                logger.info("Đối thủ " + opponent.getUsername() + " đã tham gia. UI được cập nhật.");
//            }
//        });
//    }

    public void handleOpponentLeft(String opponentUsername) {
        SwingUtilities.invokeLater(() -> {
//            if (currentJoinedRoomInfo != null) {
//                // Kiểm tra xem ai là đối thủ và xóa thông tin của họ
//                if (opponentUsername.equals(currentJoinedRoomInfo.getPlayer1Name())) {
//                    clearPlayerSlot(player1AvatarLabel, player1NameLabel, player1ReadyCheckBox);
//                    currentJoinedRoomInfo.setPlayer1Name(null);
//                } else if (opponentUsername.equals(currentJoinedRoomInfo.getPlayer2Name())) {
//                    clearPlayerSlot(player2AvatarLabel, player2NameLabel, player2ReadyCheckBox);
//                    currentJoinedRoomInfo.setPlayer2Name(null);
//                }
                // Người chơi hiện tại có thể trở thành người chơi 1 nếu người chơi 1 rời đi
                // Logic này cần server xử lý và gửi lại thông tin phòng chính xác
                appendChatMessage("Hệ thống: " + opponentUsername + " đã rời phòng.");
                logger.info("Đối thủ " + opponentUsername + " đã rời đi. UI được cập nhật.");
//            }
        });
    }

    public void updateOpponentReadyStatus(String opponentUsername, boolean isReady) {
        SwingUtilities.invokeLater(() -> {
            if (currentJoinedRoomInfo != null) {
                if (opponentUsername.equals(currentJoinedRoomInfo.getPlayer1Name())) {
                    player1ReadyCheckBox.setSelected(isReady);
                } else if (opponentUsername.equals(currentJoinedRoomInfo.getPlayer2Name())) {
                    player2ReadyCheckBox.setSelected(isReady);
                }
                logger.info("Trạng thái sẵn sàng của " + opponentUsername + " là " + isReady + ". UI được cập nhật.");
            }
        });
    }

    public void appendChatMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            logger.fine("Tin nhắn chat được hiển thị: " + message);
        });
    }

    public void handleGameStarting(Object payload) { // payload có thể là roomID
        SwingUtilities.invokeLater(() -> {
            appendChatMessage("Hệ thống: Trò chơi sắp bắt đầu!");
            JOptionPane.showMessageDialog(this, "Trò chơi sắp bắt đầu!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
//            PlayAudioURL.stopAllAudio(); // Dừng nhạc lobby
            // TODO: Mở OnlineGameFrame và truyền gameClient, currentPlayer, và thông tin phòng (payload)
            // OnlineGameFrame.display(currentPlayer, gameClient, payload);
            dispose();
            logger.info("Game bắt đầu, đóng LobbyFrame.");
        });
    }

    public void showErrorMessage(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, errorMessage, "Lỗi từ Server", JOptionPane.ERROR_MESSAGE);
            logger.warning("Hiển thị lỗi cho người dùng: " + errorMessage);
        });
    }

    public void updateCurrentPlayerInfo(PlayerModel updatedPlayer) {
        this.currentPlayer = updatedPlayer;
        SwingUtilities.invokeLater(this::updateCurrentPlayerDisplay);
    }

    public void updateCurrentPlayerDisplay() {
        if (currentPlayer != null) {
            currentPlayerNameLabel.setText("Tên: " + currentPlayer.getUsername());
            currentPlayerMoneyLabel.setText("Tiền: " + currentPlayer.getRankScore() + " xu");
            try {
                if (currentPlayer.getAvatarPath() != null && !currentPlayer.getAvatarPath().isEmpty()) {
                    currentPlayerAvatarLabel.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/avatar/" + currentPlayer.getAvatarPath() + ".png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
                } else {
                    currentPlayerAvatarLabel.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/avatar/default.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
                }
            } catch (Exception e) {
                logger.warning("Không thể tải avatar cho người chơi hiện tại: " + e.getMessage());
                currentPlayerAvatarLabel.setIcon(null); // Hoặc ảnh mặc định lỗi
                currentPlayerAvatarLabel.setText("N/A");
            }
        }
    }


    public void handleRoomLeft() { // Được gọi khi server xác nhận rời phòng thành công
        SwingUtilities.invokeLater(() -> {
            currentJoinedRoomInfo = null;
            currentRoomPanel.setVisible(false);
            isPlayerReady = false;
            readyButton.setText("Sẵn Sàng");

            // Kích hoạt lại các nút của danh sách phòng
            createRoomButton.setEnabled(true);
            joinRoomButton.setEnabled(roomList.getSelectedValue() != null);
            roomList.setEnabled(true);
            refreshRoomListButton.setEnabled(true);

            appendChatMessage("Hệ thống: Bạn đã rời phòng.");
            logger.info("Đã rời phòng, UI trở lại trạng thái sảnh chờ.");
        });
    }

    public void showConnectionLostDialog() {
        SwingUtilities.invokeLater(() -> {
            if (isVisible()) { // Chỉ hiển thị nếu lobby còn đang mở
                JOptionPane.showMessageDialog(this,
                        "Mất kết nối tới máy chủ. Vui lòng thử lại sau.",
                        "Mất Kết Nối",
                        JOptionPane.ERROR_MESSAGE);
                // Có thể đóng LobbyFrame và quay lại ModeSelectionFrame hoặc WelcomeFrame
//                PlayAudioURL.stopAllAudio();
                // Không truyền currentPlayer nếu không chắc nó còn valid
                // WelcomeFrame.display(); // Hoặc ModeSelectionFrame nếu muốn giữ phiên đăng nhập
                ModeSelectionFrame.display(this.currentPlayer); // Cố gắng quay lại với player hiện tại
                dispose();
            }
        });
    }


    // Custom renderer cho JList hiển thị RoomInfo
    private static class RoomListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof RoomInfo) {
                RoomInfo info = (RoomInfo) value;
                setText(String.format("ID: %s | Cược: %d xu | (%d/2) | %s",
                        info.getRoomId(), info.getBetAmount(), info.getPlayerCount(), info.getStatus()));
                // Thêm icon tùy theo trạng thái phòng nếu muốn
                // Ví dụ: nếu info.getStatus().equals("PLAYING"), setIcon(...)
            }
            return this;
        }
    }

    // Phương thức display để gọi từ ModeSelectionFrame
    public static void display(PlayerModel player, GameClient client) {
        SwingUtilities.invokeLater(() -> {
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
            new LobbyFrame(player, client).setVisible(true);
        });
    }

    // Để test riêng LobbyFrame (cần mock GameClient và PlayerModel)
    public static void main(String[] args) {
        PlayerModel testPlayer = new PlayerModel();
        testPlayer.setUsername("TestUserLobby");
        testPlayer.setRankScore(50000);
        testPlayer.setAvatarPath("1"); // Tên file avatar không có ".png"

        // Mock GameClient (không kết nối thực sự cho mục đích test UI)
        GameClient mockClient = new GameClient("localhost", 12345) {
            @Override
            public boolean connect(PlayerModel player) {
                this.player = player;
                logger.info("Mock connect successful for " + player.getUsername());
                // Không start listener thật
                return true;
            }
            @Override
            public void sendMessage(Message message) {
                logger.info("Mock sending message: " + message);
                // Không gửi đi đâu cả
                // Có thể giả lập phản hồi từ server ở đây để test UI
                if (message.getType() == MessageType.C2S_GET_ROOM_LIST && this.getLobbyFrame() != null) {
                    List<RoomInfo> fakeRooms = new java.util.ArrayList<>();
                    fakeRooms.add(new RoomInfo("R001", "PlayerA", null, 1, "WAITING", 1000));
                    fakeRooms.add(new RoomInfo("R002", "PlayerB", "PlayerC", 2, "WAITING_READY", 2000));
                    fakeRooms.add(new RoomInfo("R003", "PlayerD", "PlayerE", 2, "PLAYING", 500));
                    this.getLobbyFrame().updateRoomList(fakeRooms);
                }
            }
            @Override
            public void setLobbyFrame(LobbyFrame lobbyFrame) {
                super.setLobbyFrame(lobbyFrame);
            }
            public LobbyFrame getLobbyFrame(){ //Thêm getter để mock sendMessage có thể gọi
                return super.lobbyFrame;
            }

        };
        // Quan trọng: Phải gọi connect (dù là mock) để player trong client được thiết lập
        mockClient.connect(testPlayer);

        LobbyFrame.display(testPlayer, mockClient);
    }
}