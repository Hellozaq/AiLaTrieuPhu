package org.example.network;

import org.example.model.PlayerModel;
import org.example.model.QuestionModel;
import org.example.view.LobbyFrame; // Sẽ cần để cập nhật UI
import org.example.view.OnlineGameFrame;
// import org.example.view.GameFrame; // Sau này cho game online

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class GameClient {
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    protected PlayerModel player;
    private volatile boolean running = false; // Dùng để kiểm soát luồng lắng nghe

    protected LobbyFrame lobbyFrame;
    private OnlineGameFrame onlineGameFrame; // Thêm biến thành viên này


    private static final Logger logger = Logger.getLogger(GameClient.class.getName());

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
        // Không cần setup logger ở client nếu không muốn ghi file log riêng cho client
        // Logger ở client chủ yếu để debug trên console
        logger.setLevel(Level.INFO);
    }

    public boolean connect(PlayerModel player) {
        this.player = player;
        try {
            socket = new Socket(host, port);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Gửi thông điệp kết nối đầu tiên chứa thông tin người chơi
            sendMessage(new Message(MessageType.C2S_CONNECT_REQUEST, this.player));

            // Chờ xác nhận từ server
            Message serverResponse = (Message) ois.readObject();
            if (serverResponse.getType() == MessageType.S2C_CONNECTION_ACKNOWLEDGED) {
                // Cập nhật player model nếu server có gửi lại (ví dụ, server gán ID mới hoặc cập nhật gì đó)
                if (serverResponse.getPayload() instanceof PlayerModel) {
                    this.player = (PlayerModel) serverResponse.getPayload();
                }
                logger.info("Kết nối tới server thành công. Người chơi: " + this.player.getUsername());
                running = true;
                startServerListener(); // Bắt đầu luồng lắng nghe tin nhắn từ server
                return true;
            } else if (serverResponse.getType() == MessageType.S2C_ERROR) {
                logger.severe("Lỗi kết nối từ server: " + serverResponse.getPayload());
                closeConnection();
                return false;
            } else {
                logger.severe("Phản hồi không mong muốn từ server khi kết nối: " + serverResponse.getType());
                closeConnection();
                return false;
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Không thể kết nối tới server " + host + ":" + port, e);
            return false;
        }
    }

    private void startServerListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (running && socket != null && !socket.isClosed()) {
                    Message serverMessage = (Message) ois.readObject();
                    if (serverMessage != null) {
                        logger.fine("Client nhận: " + serverMessage);
                        processServerMessage(serverMessage);
                    }
                }
            } catch (SocketException e) {
                if (running) { // Chỉ log lỗi nếu client vẫn đang chạy (không phải do chủ động ngắt kết nối)
                    logger.log(Level.WARNING, "Mất kết nối tới server (SocketException): " + e.getMessage());
                }
            } catch (IOException | ClassNotFoundException e) {
                if (running) { // Chỉ log lỗi nếu client vẫn đang chạy
                    logger.log(Level.SEVERE, "Lỗi khi đọc tin nhắn từ server hoặc mất kết nối.", e);
                }
            } finally {
                logger.info("Luồng lắng nghe server đã dừng.");
                if (running) { // Nếu vẫn đang running mà luồng dừng (do lỗi) thì thử đóng kết nối
                    // Có thể thông báo cho người dùng ở đây
                    if (lobbyFrame != null) {
                        lobbyFrame.showConnectionLostDialog();
                    }
                    closeConnection();
                }
            }
        });
        listenerThread.setName("Client-ServerListener-" + player.getUsername());
        listenerThread.setDaemon(true); // Để luồng tự kết thúc khi chương trình chính kết thúc
        listenerThread.start();
    }

    // Trong GameClient.java
    private void processServerMessage(Message message) {
        if (message == null) {
            logger.warning("processServerMessage nhận được tin nhắn null.");
            return;
        }

        // 1. Ưu tiên xử lý nếu OnlineGameFrame đang hoạt động
        if (onlineGameFrame != null && onlineGameFrame.isDisplayable()) {
            // Người chơi đang trong trận đấu
            switch (message.getType()) {
                case S2C_GAME_QUESTION:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        if (data.length >= 2 && data[0] instanceof QuestionModel) {
                            QuestionModel question = (QuestionModel) data[0];
                            int timeLimit = (Integer) data[1];
                            logger.info("Nhận câu hỏi mới: ID=" + question.getId() + ", thời gian=" + timeLimit);
                            SwingUtilities.invokeLater(() -> {
                                onlineGameFrame.displayQuestion(question, timeLimit);
                            });
                        } else {
                            logger.warning("Dữ liệu câu hỏi không hợp lệ: " + message.getPayload());
                        }
                    }
                    break;
                case S2C_ANSWER_RESULT:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        // (int questionId, int myChoice, boolean myResult, int opponentChoice, boolean opponentResult, int correctAnswerIndex)
                        onlineGameFrame.showAnswerResult((int) data[0], (int) data[1], (boolean) data[2], (int) data[3], (boolean) data[4], (int) data[5]);
                    }
                    break;
                case S2C_UPDATE_GAME_SCORE:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        // (int myOnlineScore, int opponentOnlineScore)
                        onlineGameFrame.updateScores((int) data[0], (int) data[1]);
                    }
                    break;
                case S2C_GAME_OVER:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        // (String winnerUsername, int prize, boolean iAmWinner)
                        onlineGameFrame.showGameOver((String) data[0], (int) data[1], (boolean) data[2]);
                    }
                    break;
                case S2C_ERROR: // Lỗi có thể xảy ra trong game
                    if (message.getPayload() instanceof String) {
                        onlineGameFrame.showErrorMessage((String) message.getPayload());
                    }
                    break;

                case S2C_HELP_RESULT_5050:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        // questionId, optionToRemove1Index, optionToRemove2Index
                        onlineGameFrame.display5050Result((int) data[0], (int) data[1], (int) data[2]);
                    }
                    break;
                case S2C_HELP_RESULT_CALL:
                    // Payload có thể là: new Object[]{questionId}
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        if (data.length > 0 && data[0] instanceof Integer) {
                            int questionIdFromServer = (Integer) data[0];
                            onlineGameFrame.displayCallResult(questionIdFromServer);
                        } else {
                            logger.warning("Payload S2C_HELP_RESULT_CALL không hợp lệ hoặc thiếu questionId.");
                            // Có thể vẫn cho client hiển thị HelpCallFrame dựa trên currentQuestion nếu muốn
                            // onlineGameFrame.displayCallResult(onlineGameFrame.getCurrentQuestion().getId());
                            // Hoặc báo lỗi
                            onlineGameFrame.showErrorMessage("Lỗi nhận kết quả trợ giúp gọi điện.");
                        }
                    } else { // Hoặc nếu server chỉ gửi questionId trực tiếp (không phải Object[])
                        if (message.getPayload() instanceof Integer) {
                            int questionIdFromServer = (Integer) message.getPayload();
                            onlineGameFrame.displayCallResult(questionIdFromServer);
                        } else {
                            logger.warning("Payload S2C_HELP_RESULT_CALL không phải là Integer (questionId).");
                            onlineGameFrame.showErrorMessage("Lỗi nhận kết quả trợ giúp gọi điện (dữ liệu không đúng).");
                        }
                    }
                    break;
                case S2C_HELP_RESULT_AUDIENCE:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        if (data.length >= 3) { // questionId, Map pollResults, int mostVotedOptionIndex
                            onlineGameFrame.displayAudienceResult((int) data[0], (Map<Integer, Double>) data[1], (int) data[2]);
                        } else {
                            logger.warning("Payload S2C_HELP_RESULT_AUDIENCE không đủ phần tử.");
                        }
                    }
                    break;
                case S2C_OPPONENT_USED_HELP:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        // opponentUsername, String helpTypeDescription
                        onlineGameFrame.notifyOpponentUsedHelp((String) data[0], (String) data[1]);
                    }
                    break;
                case S2C_HELP_UNAVAILABLE:
                    if (message.getPayload() instanceof String) {
                        onlineGameFrame.handleHelpUnavailable((String) message.getPayload());
                    }
                    break;

                case S2C_LOBBY_CHAT: // Xử lý tin nhắn chat khi đang trong game
                    if (message.getPayload() instanceof String) {
                        onlineGameFrame.appendChatMessageToArea((String) message.getPayload());
                    }
                    break;

                default:
                    // Nếu OnlineGameFrame đang active nhưng nhận được tin nhắn không dành cho nó,
                    // có thể là tin nhắn toàn cục hoặc lỗi logic.
                    logger.info("GameClient (trong OnlineGameFrame) nhận tin nhắn chưa được xử lý chuyên biệt: " + message.getType());
                    // Bạn có thể thêm xử lý chung ở đây nếu muốn, ví dụ S2C_UPDATE_PLAYER_INFO
                    if (message.getType() == MessageType.S2C_UPDATE_PLAYER_INFO) {
                        if (message.getPayload() instanceof PlayerModel) {
                            this.player = (PlayerModel) message.getPayload(); // Cập nhật model của client
                            // OnlineGameFrame có thể có phương thức cập nhật tiền của người chơi nếu cần
                            // onlineGameFrame.updateCurrentPlayerMoneyDisplay(this.player.getRankScore());
                        }
                    }
                    break;
            }
        }
        // 2. Nếu không ở trong game, xử lý cho LobbyFrame
        else if (lobbyFrame != null && lobbyFrame.isDisplayable()) {
            // Người chơi đang ở sảnh chờ
            switch (message.getType()) {
                case S2C_ROOM_LIST_UPDATE:
                    if (message.getPayload() instanceof List) {
                        lobbyFrame.updateRoomList((List<RoomInfo>) message.getPayload());
                    }
                    break;
                case S2C_ROOM_JOINED:
                    if (message.getPayload() instanceof RoomInfo) {
                        lobbyFrame.handleRoomJoined((RoomInfo) message.getPayload());
                    }
                    break;
                case S2C_ROOM_LEFT:
                    lobbyFrame.handleRoomLeft();
                    break;
                case S2C_OPPONENT_JOINED_ROOM: // Gửi PlayerModel của đối thủ
                    if (message.getPayload() instanceof PlayerModel) {
                        lobbyFrame.cacheOpponentPlayerModel((PlayerModel) message.getPayload());
                    }
                    break;
                case S2C_OPPONENT_LEFT_ROOM: // Gửi username của đối thủ
                    if (message.getPayload() instanceof String) {
                        lobbyFrame.handleOpponentLeft((String) message.getPayload());
                    }
                    break;
                case S2C_OPPONENT_READY_STATUS:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        lobbyFrame.updateOpponentReadyStatus((String) data[0], (Boolean) data[1]);
                    }
                    break;
                case S2C_LOBBY_CHAT:
                    if (message.getPayload() instanceof String) {
                        lobbyFrame.appendChatMessage((String) message.getPayload());
                    }
                    break;
                case S2C_GAME_STARTING: // Tin nhắn này kích hoạt chuyển từ Lobby sang Game
                    lobbyFrame.handleGameStarting(message.getPayload());
                    break;
                case S2C_ERROR: // Lỗi có thể xảy ra ở sảnh
                    if (message.getPayload() instanceof String) {
                        lobbyFrame.showErrorMessage((String) message.getPayload());
                    }
                    break;
                case S2C_UPDATE_PLAYER_INFO: // Cập nhật tiền của người chơi (ví dụ sau khi cược)
                    if (message.getPayload() instanceof PlayerModel) {
                        this.player = (PlayerModel) message.getPayload();
                        lobbyFrame.updateCurrentPlayerInfo(this.player);
                    }
                    break;
                default:
                    logger.info("GameClient (trong LobbyFrame) nhận tin nhắn chưa được xử lý chuyên biệt: " + message.getType());
                    break;
            }
        }
        // 3. Nếu không có frame nào active (trường hợp hiếm hoặc khi mới kết nối chưa có UI)
        else {
            // Có thể có một số tin nhắn ban đầu được xử lý ở đây trước khi UI được hiển thị
            // Ví dụ: S2C_CONNECTION_ACKNOWLEDGED (đã được xử lý trong phương thức connect())
            if (message.getType() != MessageType.S2C_CONNECTION_ACKNOWLEDGED) {
                logger.warning("GameClient nhận tin nhắn nhưng không có frame UI nào active để xử lý: " + message.getType());
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            if (oos != null && socket != null && !socket.isClosed()) {
                oos.writeObject(message);
                oos.flush(); // Đảm bảo tin nhắn được gửi đi ngay
                logger.fine("Client gửi: " + message);
            } else {
                logger.warning("Không thể gửi tin nhắn, kết nối chưa sẵn sàng hoặc đã đóng.");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Lỗi khi gửi tin nhắn tới server.", e);
            // Có thể thử đóng kết nối ở đây nếu lỗi nghiêm trọng
        }
    }

    public void disconnect() {
        running = false; // Dừng luồng lắng nghe
        closeConnection();
        logger.info("Đã ngắt kết nối khỏi server.");
    }

    private void closeConnection() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Lỗi khi đóng kết nối client.", e);
        } finally {
            ois = null;
            oos = null;
            socket = null;
            running = false;
        }
    }

    /**
     * Gán OnlineGameFrame cho client để client có thể cập nhật UI của game.
     * Khi vào game, tham chiếu đến lobbyFrame sẽ được xóa.
     *
     * @param onlineGameFrame The OnlineGameFrame instance.
     */
    public void setOnlineGameFrame(OnlineGameFrame onlineGameFrame) {
        this.onlineGameFrame = onlineGameFrame;
        this.lobbyFrame = null; // Không còn tương tác với Lobby UI khi đã vào game
        logger.info("OnlineGameFrame đã được thiết lập cho GameClient.");
    }


    public PlayerModel getPlayer() {
        return player;
    }

    public void setLobbyFrame(LobbyFrame lobbyFrame) {
        this.lobbyFrame = lobbyFrame;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}