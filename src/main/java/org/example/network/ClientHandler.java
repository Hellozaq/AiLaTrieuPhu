package org.example.network;

import org.example.model.PlayerModel; // Đảm bảo bạn đã import PlayerModel

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Server server; // Tham chiếu đến server chính
    private PlayerModel player; // Thông tin người chơi của client này
    private Room currentRoom; // Phòng hiện tại mà client đang tham gia

    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        try {
            this.oos = new ObjectOutputStream(clientSocket.getOutputStream());
            this.ois = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Lỗi khởi tạo stream cho client handler: " + clientSocket.getInetAddress(), e);
            closeConnection();
        }
    }

    // Trong org.example.network.ClientHandler.java
    @Override
    public void run() {
        String clientIp = String.valueOf(clientSocket.getInetAddress());
        // player ban đầu sẽ là null cho đến khi đăng nhập/đăng ký thành công

        try {
            logger.info("ClientHandler cho " + clientIp + " đã khởi động và đang chờ tin nhắn (bao gồm Đăng nhập/Đăng ký).");

            Message clientMessage;
            // Đi thẳng vào vòng lặp xử lý tin nhắn.
            // handleClientMessage sẽ kiểm tra xem player đã được xác thực chưa.
            // Vòng lặp sẽ tiếp tục cho đến khi readObject() trả về null (ít khi xảy ra với ObjectInputStream nếu không phải lỗi)
            // hoặc ném ra một Exception (thường là EOFException khi client đóng kết nối, hoặc SocketException).
            while ((clientMessage = (Message) ois.readObject()) != null) {
                logger.fine("Nhận tin nhắn từ " + (player != null ? player.getUsername() : clientIp) +
                        ": " + clientMessage.getType() +
                        " - Payload: " + (clientMessage.getPayload() != null ? clientMessage.getPayload().toString() : "null"));
                handleClientMessage(clientMessage);
            }
            // Nếu vòng lặp kết thúc mà không có exception (ví dụ ois.readObject() trả về null),
            // có thể log thêm ở đây, nhưng thường thì nó sẽ kết thúc bằng exception.
            logger.info("ClientHandler cho " + (player != null ? player.getUsername() : clientIp) + " đã kết thúc vòng lặp đọc tin nhắn một cách bình thường (readObject() trả về null).");

        } catch (java.io.EOFException e) {
            logger.log(Level.INFO, "Client " + (player != null ? player.getUsername() : clientIp) +
                    " đã đóng kết nối (EOFException): " + e.getMessage());
        } catch (java.net.SocketException e) {
            // Thường xảy ra khi client đột ngột đóng kết nối (Connection reset, Broken pipe)
            // hoặc server đóng socket trong khi client đang cố gắng đọc/ghi.
            logger.log(Level.WARNING, "SocketException cho client " + (player != null ? player.getUsername() : clientIp) +
                    ": " + e.getMessage()); // Không cần in full stack trace cho các lỗi reset thông thường
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException trong ClientHandler run() cho " + (player != null ? player.getUsername() : clientIp), e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "ClassNotFoundException trong ClientHandler run() cho " + (player != null ? player.getUsername() : clientIp), e);
        } catch (Exception e) { // Bắt các ngoại lệ không mong muốn khác
            logger.log(Level.SEVERE, "Ngoại lệ không mong muốn trong ClientHandler run() cho " + (player != null ? player.getUsername() : clientIp), e);
        } finally {
            logger.info("ClientHandler cho " + (player != null ? player.getUsername() : clientIp) +
                    " đang thực thi khối finally và gọi handleDisconnect.");
            handleDisconnect(); // Đảm bảo ngắt kết nối và dọn dẹp tài nguyên
        }
    }

    private void handleClientMessage(Message message) {
        if (this.player == null) {
            switch (message.getType()) {
                case C2S_LOGIN_REQUEST:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] loginData = (Object[]) message.getPayload();
                        String username = (String) loginData[0];
                        String password = (String) loginData[1];
                        server.handleLoginRequest(this, username, password);
                    }
                    break;
                case C2S_REGISTER_REQUEST:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] registerData = (Object[]) message.getPayload();
                        String username = (String) registerData[0];
                        String password = (String) registerData[1];
                        server.handleRegisterRequest(this, username, password);
                    }
                    break;
                default:
                    logger.warning("Client chưa xác thực (" + clientSocket.getInetAddress() + ") gửi tin nhắn không hợp lệ: " + message.getType());
                    sendMessage(new Message(MessageType.S2C_ERROR, "Vui lòng đăng nhập hoặc đăng ký trước."));
                    // Có thể cân nhắc đóng kết nối nếu client gửi quá nhiều tin nhắn không hợp lệ
                    // closeConnection();
                    break;
            }
            return;
        }
        // Ưu tiên xử lý tin nhắn trong game nếu client đang ở trong phòng và phòng đang
        // chơi
        if (currentRoom != null && "PLAYING".equals(currentRoom.getStatus())) {
            switch (message.getType()) {
                case C2S_SUBMIT_ANSWER:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        int questionId = (Integer) data[0];
                        int answerIndex = (Integer) data[1];
                        server.handleSubmitAnswer(this, currentRoom, questionId, answerIndex);
                    }
                    break;
                case C2S_LEAVE_GAME: // Client chủ động rời game
                    logger.info(player.getUsername() + " yêu cầu rời trận đấu trong phòng " + currentRoom.getRoomId());
                    server.endGameForRoom(currentRoom, player.getUsername() + " đã rời trận.", this);
                    // ClientHandler này có thể sẽ bị đóng sau đó nếu server quyết định vậy
                    // Hoặc client tự quay về lobby sau khi nhận S2C_GAME_OVER
                    break;

                case C2S_USE_HELP_CALL:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        // String roomIdFromClient = (String) data[0]; // Có thể không cần dùng
                        int questionIdFromClient = (Integer) data[1];
                        server.processPlayerHelpRequest(currentRoom, this, MessageType.C2S_USE_HELP_CALL,
                                questionIdFromClient);
                    }
                    break;
                case C2S_USE_HELP_5050:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        // String roomIdFromClient = (String) data[0]; // Không nhất thiết phải dùng
                        // roomId từ client nếu đã có currentRoom
                        int questionIdFromClient = (Integer) data[1];
                        server.processPlayerHelpRequest(currentRoom, this, MessageType.C2S_USE_HELP_5050,
                                questionIdFromClient);
                    }
                    break;
                case C2S_USE_HELP_AUDIENCE:
                    if (message.getPayload() instanceof Object[]) {
                        Object[] data = (Object[]) message.getPayload();
                        int questionIdFromClient = (Integer) data[1];
                        server.processPlayerHelpRequest(currentRoom, this, MessageType.C2S_USE_HELP_AUDIENCE,
                                questionIdFromClient);
                    }
                    break;

                case C2S_LOBBY_CHAT: // Cho phép chat cả khi đang chơi game
                    if (message.getPayload() instanceof String) {
                        String chatMsg = (String) message.getPayload();
                        logger.info(
                                "[CHAT GAME " + currentRoom.getRoomId() + "] " + player.getUsername() + ": " + chatMsg);
                        // Server thêm tiền tố [Game] để client biết đây là chat trong game
                        Message chatRelayMsg = new Message(MessageType.S2C_LOBBY_CHAT,
                                player.getUsername() + ": " + chatMsg);
                        // Gửi cho cả hai người chơi trong phòng
                        if (currentRoom.getHandler1() != null)
                            currentRoom.getHandler1().sendMessage(chatRelayMsg);
                        if (currentRoom.getHandler2() != null)
                            currentRoom.getHandler2().sendMessage(chatRelayMsg);
                    }
                    break;
                // Các tin nhắn khác không liên quan đến game có thể bị bỏ qua hoặc log lại
                default:
                    logger.warning("Client " + player.getUsername() + " gửi tin nhắn " + message.getType()
                            + " không hợp lệ khi đang trong game tại phòng " + currentRoom.getRoomId());
                    // Có thể gửi S2C_ERROR cho client này
                    // sendMessage(new Message(MessageType.S2C_ERROR, "Hành động không hợp lệ khi
                    // đang trong trận đấu."));
                    break;
            }
        }
        // Nếu không trong game hoặc game chưa bắt đầu, xử lý tin nhắn sảnh chờ
        else if (currentRoom != null && ("WAITING_READY".equals(currentRoom.getStatus())
                || "READY_TO_START".equals(currentRoom.getStatus()))) {
            // Xử lý tin nhắn cho phòng chờ sẵn sàng
            switch (message.getType()) {
                case C2S_LEAVE_ROOM:
                    if (currentRoom != null) {
                        String roomId = currentRoom.getRoomId();
                        logger.info(player.getUsername() + " yêu cầu rời phòng chờ " + roomId);
                        server.leaveRoom(roomId, this);
                    } else {
                        logger.warning(player.getUsername() + " yêu cầu rời phòng không hợp lệ hoặc không có phòng.");
                        sendMessage(new Message(MessageType.S2C_ERROR, "Yêu cầu rời phòng không hợp lệ."));
                    }
                    break;
                case C2S_LOBBY_CHAT:
                    if (message.getPayload() instanceof String) {
                        String chatMsg = (String) message.getPayload();
                        logger.info("[CHAT PHÒNG " + currentRoom.getRoomId() + "] " + player.getUsername() + ": "
                                + chatMsg);
                        Message chatRelayMsg = new Message(MessageType.S2C_LOBBY_CHAT,
                                "[Phòng] " + player.getUsername() + ": " + chatMsg);
                        if (currentRoom.getHandler1() != null)
                            currentRoom.getHandler1().sendMessage(chatRelayMsg);
                        if (currentRoom.getHandler2() != null)
                            currentRoom.getHandler2().sendMessage(chatRelayMsg);
                    }
                    break;
                case C2S_PLAYER_READY:
                    if (message.getPayload() instanceof Boolean) {
                        boolean isReady = (Boolean) message.getPayload();
                        currentRoom.setPlayerReady(this, isReady); // Truyền handler thay vì player model
                        logger.info(player.getUsername() + (isReady ? " đã sẵn sàng." : " hủy sẵn sàng.")
                                + " trong phòng " + currentRoom.getRoomId());

                        ClientHandler opponentHandler = currentRoom.getOpponentHandler(this);
                        if (opponentHandler != null) {
                            opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_READY_STATUS,
                                    new Object[]{this.player.getUsername(), isReady}));
                        }

                        if ("READY_TO_START".equals(currentRoom.getStatus())) {
                            logger.info("Cả hai người chơi trong phòng " + currentRoom.getRoomId()
                                    + " đã sẵn sàng. Bắt đầu game!");
                            server.startGameForRoom(currentRoom);
                        }
                        server.broadcastRoomList();
                    }
                    break;
                default:
                    logger.warning("Client " + player.getUsername() + " gửi tin nhắn " + message.getType()
                            + " không hợp lệ khi đang trong phòng chờ " + currentRoom.getRoomId());
                    break;
            }
        } else { // Client ở sảnh chờ chung (currentRoom == null)
            switch (message.getType()) {
                case C2S_GET_ROOM_LIST:
                    server.sendRoomListToClient(this);
                    logger.info(player.getUsername() + " yêu cầu danh sách phòng.");
                    break;
                case C2S_CREATE_ROOM:
                    if (message.getPayload() instanceof Integer) {
                        int betAmount = (Integer) message.getPayload();
                        if (player.getRankScore() < betAmount) {
                            sendMessage(new Message(MessageType.S2C_ERROR,
                                    "Bạn không đủ " + betAmount + " xu để tạo phòng này."));
                            logger.warning(player.getUsername() + " không đủ tiền (" + player.getRankScore()
                                    + ") để tạo phòng cược " + betAmount);
                            return;
                        }
                        Room createdRoom = server.createRoom(this.player, this, betAmount); // Gán phòng mới cho client
                        // handler này
                        if (createdRoom != null) {
                            this.currentRoom = createdRoom; // Cập nhật currentRoom
                            sendMessage(new Message(MessageType.S2C_ROOM_JOINED, this.currentRoom.getRoomInfo()));
                            logger.info(player.getUsername() + " đã tạo phòng " + this.currentRoom.getRoomId()
                                    + " với mức cược " + betAmount);
                            server.broadcastRoomList();
                        } else {
                            sendMessage(new Message(MessageType.S2C_ERROR, "Không thể tạo phòng."));
                            logger.warning(player.getUsername() + " tạo phòng thất bại.");
                        }
                    }
                    break;
                case C2S_JOIN_ROOM:
                    if (message.getPayload() instanceof String) {
                        String roomIdToJoin = (String) message.getPayload();
                        Room joinedRoom = server.joinRoom(roomIdToJoin, this.player, this);
                        if (joinedRoom != null) {
                            this.currentRoom = joinedRoom; // Cập nhật currentRoom
                            sendMessage(new Message(MessageType.S2C_ROOM_JOINED, this.currentRoom.getRoomInfo()));
                            logger.info(player.getUsername() + " đã tham gia phòng " + roomIdToJoin);

                            ClientHandler opponentHandler = this.currentRoom.getOpponentHandler(this);
                            if (opponentHandler != null) {
                                opponentHandler
                                        .sendMessage(new Message(MessageType.S2C_OPPONENT_JOINED_ROOM, this.player));
                                opponentHandler.sendMessage(
                                        new Message(MessageType.S2C_ROOM_JOINED, this.currentRoom.getRoomInfo()));
                            }
                            server.broadcastRoomList();
                        }
                        // Không cần else ở đây vì server.joinRoom đã gửi lỗi nếu thất bại
                    }
                    break;
                case C2S_LOBBY_CHAT: // Chat ở sảnh chung (nếu không ở trong phòng nào)
                    if (message.getPayload() instanceof String) {
                        logger.info("[CHAT SẢNH] " + player.getUsername() + ": " + message.getPayload());
                        server.broadcastMessageToAll(new Message(MessageType.S2C_LOBBY_CHAT,
                                "[Sảnh] " + player.getUsername() + ": " + message.getPayload()), this);
                    }
                    break;
                default:
                    logger.warning("Loại tin nhắn không xác định từ " + player.getUsername() + " ở sảnh: "
                            + message.getType());
                    break;
            }
        }
    }

    private void handleDisconnect() { // Được gọi khi client ngắt kết nối hoặc có lỗi stream
        {
            logger.info(player.getUsername() + " (" + clientSocket.getInetAddress() + ") đang ngắt kết nối...");
            if (currentRoom != null && player != null) {
                // Nếu đang trong game, xử thua
                if ("PLAYING".equals(currentRoom.getStatus())) {
                    logger.info(player.getUsername() + " ngắt kết nối khi đang chơi trong phòng "
                            + currentRoom.getRoomId() + ". Xử thua.");
                    server.endGameForRoom(currentRoom, player.getUsername() + " đã ngắt kết nối.", this);
                } else { // Nếu đang ở phòng chờ
                    logger.info(player.getUsername() + " ngắt kết nối khi đang ở phòng chờ " + currentRoom.getRoomId()
                            + ".");
                    ClientHandler opponentHandler = currentRoom.getOpponentHandler(this); // Lấy đối thủ trước khi rời
                    server.leaveRoom(currentRoom.getRoomId(), this); // Gọi server để xử lý rời phòng
                    if (opponentHandler != null && currentRoom != null && !currentRoom.isEmpty()) { // Nếu phòng còn và
                        // có đối thủ
                        Room roomAfterLeave = server.getRoomById(currentRoom.getRoomId()); // Lấy trạng thái phòng mới
                        // nhất
                        if (roomAfterLeave != null) {
                            opponentHandler.sendMessage(
                                    new Message(MessageType.S2C_OPPONENT_LEFT_ROOM, this.player.getUsername()));
                            opponentHandler.sendMessage(
                                    new Message(MessageType.S2C_ROOM_JOINED, roomAfterLeave.getRoomInfo()));
                        } else { // Phòng đã bị xóa
                            opponentHandler.sendMessage(
                                    new Message(MessageType.S2C_OPPONENT_LEFT_ROOM, this.player.getUsername()));
                        }
                    }
                }
                currentRoom = null; // Client này không còn ở trong phòng nào nữa
            }
            server.removeConnectedClient(this);
            if (player != null) {
                server.broadcastRoomList(); // Cập nhật sảnh cho các client khác
                logger.info(player.getUsername() + " đã chính thức ngắt kết nối khỏi server.");
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            if (oos != null && !clientSocket.isClosed()) {
                oos.writeObject(message);
                oos.flush();
                logger.finer(
                        "Đã gửi tin nhắn tới " + (player != null ? player.getUsername() : clientSocket.getInetAddress())
                                + ": " + message.getType());
            }
        } catch (IOException e) {
            logger.log(Level.WARNING,
                    "Lỗi gửi tin nhắn tới " + (player != null ? player.getUsername() : clientSocket.getInetAddress()),
                    e);
            closeConnection();
        }
    }

    public void closeConnection() {
        try {
            if (ois != null)
                ois.close();
            if (oos != null)
                oos.close();
            if (clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Lỗi đóng kết nối client.", e);
        }
    }

    public PlayerModel getPlayer() {
        return player;
    }

    public void setPlayer(PlayerModel player) {
        this.player = player;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }
}