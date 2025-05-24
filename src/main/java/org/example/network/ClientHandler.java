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

    @Override
    public void run() {
        try {
            // Bước đầu tiên: Client gửi thông tin PlayerModel để "đăng nhập" vào server
            Message connectMessage = (Message) ois.readObject();
            if (connectMessage.getType() == MessageType.C2S_CONNECT_REQUEST && connectMessage.getPayload() instanceof PlayerModel) {
                this.player = (PlayerModel) connectMessage.getPayload();
                // TODO: Có thể kiểm tra xem player này đã kết nối từ client khác chưa nếu cần
                server.addConnectedClient(this); // Thêm vào danh sách client đang kết nối của server
                sendMessage(new Message(MessageType.S2C_CONNECTION_ACKNOWLEDGED, this.player)); // Gửi lại player model (có thể đã được server cập nhật)
                logger.info("Người chơi " + player.getUsername() + " (" + clientSocket.getInetAddress() + ") đã kết nối.");
                server.broadcastRoomList(); // Cập nhật danh sách phòng cho tất cả client
            } else {
                logger.warning("Yêu cầu kết nối không hợp lệ từ " + clientSocket.getInetAddress());
                sendMessage(new Message(MessageType.S2C_ERROR, "Yêu cầu kết nối không hợp lệ."));
                closeConnection();
                return;
            }

            // Vòng lặp xử lý tin nhắn từ client
            Message clientMessage;
            while ((clientMessage = (Message) ois.readObject()) != null) {
                logger.fine("Nhận tin nhắn từ " + player.getUsername() + ": " + clientMessage.getType() + " - Payload: " + clientMessage.getPayload());
                handleClientMessage(clientMessage);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (player != null) {
                logger.log(Level.INFO, "Người chơi " + player.getUsername() + " (" + clientSocket.getInetAddress() + ") đã ngắt kết nối.", e.getMessage());
            } else {
                logger.log(Level.INFO, "Client (" + clientSocket.getInetAddress() + ") đã ngắt kết nối trước khi xác thực.", e.getMessage());
            }
        } finally {
            handleDisconnect();
            closeConnection();
        }
    }

    private void handleClientMessage(Message message) {
        if (this.player == null) {
            logger.warning("Client chưa xác thực gửi tin nhắn: " + message.getType());
            return;
        }
        // Ưu tiên xử lý tin nhắn trong game nếu client đang ở trong phòng và phòng đang chơi
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
                case C2S_LOBBY_CHAT: // Cho phép chat cả khi đang chơi game
                    if (message.getPayload() instanceof String) {
                        String chatMsg = (String) message.getPayload();
                        logger.info("[CHAT GAME " + currentRoom.getRoomId() + "] " + player.getUsername() + ": " + chatMsg);
                        Message chatRelayMsg = new Message(MessageType.S2C_LOBBY_CHAT, "[Game] " + player.getUsername() + ": " + chatMsg);
                        if(currentRoom.getHandler1() != null) currentRoom.getHandler1().sendMessage(chatRelayMsg);
                        if(currentRoom.getHandler2() != null) currentRoom.getHandler2().sendMessage(chatRelayMsg);
                    }
                    break;
                // Các tin nhắn khác không liên quan đến game có thể bị bỏ qua hoặc log lại
                default:
                    logger.warning("Client " + player.getUsername() + " gửi tin nhắn " + message.getType() + " không hợp lệ khi đang trong game tại phòng " + currentRoom.getRoomId());
                    // Có thể gửi S2C_ERROR cho client này
                    // sendMessage(new Message(MessageType.S2C_ERROR, "Hành động không hợp lệ khi đang trong trận đấu."));
                    break;
            }
        }
        // Nếu không trong game hoặc game chưa bắt đầu, xử lý tin nhắn sảnh chờ
        else if (currentRoom != null && ("WAITING_READY".equals(currentRoom.getStatus()) || "READY_TO_START".equals(currentRoom.getStatus())) ) {
            // Xử lý tin nhắn cho phòng chờ sẵn sàng
            switch (message.getType()) {
                case C2S_LEAVE_ROOM:
                    server.leaveRoom(currentRoom.getRoomId(), this);
                    // ClientHandler sẽ không còn currentRoom nữa sau khi server xử lý
                    // Thông báo cho đối thủ (nếu có) và cập nhật danh sách phòng đã được xử lý trong server.leaveRoom và ClientHandler.handleDisconnect
                    sendMessage(new Message(MessageType.S2C_ROOM_LEFT, null));
                    // Cập nhật currentRoom thành null sau khi server đã xử lý xong
                    // server.leaveRoom đã gọi currentRoom.removePlayer(this)
                    // Nếu phòng trống, server sẽ xóa phòng. ClientHandler cần được cập nhật là nó không còn trong phòng nào.
                    this.currentRoom = null; // Đánh dấu client này không còn ở trong phòng
                    server.broadcastRoomList();
                    break;
                case C2S_LOBBY_CHAT:
                    if (message.getPayload() instanceof String) {
                        String chatMsg = (String) message.getPayload();
                        logger.info("[CHAT PHÒNG " + currentRoom.getRoomId() + "] " + player.getUsername() + ": " + chatMsg);
                        Message chatRelayMsg = new Message(MessageType.S2C_LOBBY_CHAT, player.getUsername() + ": " + chatMsg);
                        if(currentRoom.getHandler1()!=null) currentRoom.getHandler1().sendMessage(chatRelayMsg);
                        if(currentRoom.getHandler2()!=null) currentRoom.getHandler2().sendMessage(chatRelayMsg);
                    }
                    break;
                case C2S_PLAYER_READY:
                    if (message.getPayload() instanceof Boolean) {
                        boolean isReady = (Boolean) message.getPayload();
                        currentRoom.setPlayerReady(this, isReady); // Truyền handler thay vì player model
                        logger.info(player.getUsername() + (isReady ? " đã sẵn sàng." : " hủy sẵn sàng.") + " trong phòng " + currentRoom.getRoomId());

                        ClientHandler opponentHandler = currentRoom.getOpponentHandler(this);
                        if (opponentHandler != null) {
                            opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_READY_STATUS, new Object[]{this.player.getUsername(), isReady}));
                        }

                        if ("READY_TO_START".equals(currentRoom.getStatus())) {
                            logger.info("Cả hai người chơi trong phòng " + currentRoom.getRoomId() + " đã sẵn sàng. Bắt đầu game!");
                            server.startGameForRoom(currentRoom);
                        }
                        server.broadcastRoomList();
                    }
                    break;
                default:
                    logger.warning("Client " + player.getUsername() + " gửi tin nhắn " + message.getType() + " không hợp lệ khi đang trong phòng chờ " + currentRoom.getRoomId());
                    break;
            }
        }
        else { // Client ở sảnh chờ chung (currentRoom == null)
            switch (message.getType()) {
                case C2S_GET_ROOM_LIST:
                    server.sendRoomListToClient(this);
                    logger.info(player.getUsername() + " yêu cầu danh sách phòng.");
                    break;
                case C2S_CREATE_ROOM:
                    if (message.getPayload() instanceof Integer) {
                        int betAmount = (Integer) message.getPayload();
                        if (player.getRankScore() < betAmount) {
                            sendMessage(new Message(MessageType.S2C_ERROR, "Bạn không đủ " + betAmount + " xu để tạo phòng này."));
                            logger.warning(player.getUsername() + " không đủ tiền ("+player.getRankScore()+") để tạo phòng cược " + betAmount);
                            return;
                        }
                        Room createdRoom = server.createRoom(this.player, this, betAmount); // Gán phòng mới cho client handler này
                        if (createdRoom != null) {
                            this.currentRoom = createdRoom; // Cập nhật currentRoom
                            sendMessage(new Message(MessageType.S2C_ROOM_JOINED, this.currentRoom.getRoomInfo()));
                            logger.info(player.getUsername() + " đã tạo phòng " + this.currentRoom.getRoomId() + " với mức cược " + betAmount);
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
                                opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_JOINED_ROOM, this.player));
                                opponentHandler.sendMessage(new Message(MessageType.S2C_ROOM_JOINED, this.currentRoom.getRoomInfo()));
                            }
                            server.broadcastRoomList();
                        }
                        // Không cần else ở đây vì server.joinRoom đã gửi lỗi nếu thất bại
                    }
                    break;
                case C2S_LOBBY_CHAT: // Chat ở sảnh chung (nếu không ở trong phòng nào)
                    if (message.getPayload() instanceof String) {
                        logger.info("[CHAT SẢNH] " + player.getUsername() + ": " + message.getPayload());
                        server.broadcastMessageToAll(new Message(MessageType.S2C_LOBBY_CHAT, "[Sảnh] " + player.getUsername() + ": " + message.getPayload()), this);
                    }
                    break;
                default:
                    logger.warning("Loại tin nhắn không xác định từ " + player.getUsername() + " ở sảnh: " + message.getType());
                    break;
            }
        }
    }

    private void handleDisconnect() { // Được gọi khi client ngắt kết nối hoặc có lỗi stream
        if (player != null) { // Chỉ xử lý nếu client đã được xác thực
            logger.info(player.getUsername() + " (" + clientSocket.getInetAddress() + ") đang ngắt kết nối...");
            if (currentRoom != null) {
                // Nếu đang trong game, xử thua
                if ("PLAYING".equals(currentRoom.getStatus())) {
                    logger.info(player.getUsername() + " ngắt kết nối khi đang chơi trong phòng " + currentRoom.getRoomId() + ". Xử thua.");
                    server.endGameForRoom(currentRoom, player.getUsername() + " đã ngắt kết nối.", this);
                } else { // Nếu đang ở phòng chờ
                    logger.info(player.getUsername() + " ngắt kết nối khi đang ở phòng chờ " + currentRoom.getRoomId() + ".");
                    ClientHandler opponentHandler = currentRoom.getOpponentHandler(this); // Lấy đối thủ trước khi rời
                    server.leaveRoom(currentRoom.getRoomId(), this); // Gọi server để xử lý rời phòng
                    if (opponentHandler != null && currentRoom != null && !currentRoom.isEmpty()) { // Nếu phòng còn và có đối thủ
                        Room roomAfterLeave = server.getRoomById(currentRoom.getRoomId()); // Lấy trạng thái phòng mới nhất
                        if (roomAfterLeave != null) {
                            opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_LEFT_ROOM, this.player.getUsername()));
                            opponentHandler.sendMessage(new Message(MessageType.S2C_ROOM_JOINED, roomAfterLeave.getRoomInfo()));
                        } else { // Phòng đã bị xóa
                            opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_LEFT_ROOM, this.player.getUsername()));
                        }
                    }
                }
                currentRoom = null; // Client này không còn ở trong phòng nào nữa
            }
            server.removeConnectedClient(this);
            server.broadcastRoomList(); // Cập nhật sảnh cho các client khác
            logger.info(player.getUsername() + " đã chính thức ngắt kết nối khỏi server.");
        } else {
            logger.info("Client (" + clientSocket.getInetAddress() + ") chưa xác thực đã ngắt kết nối.");
            server.removeConnectedClient(this); // Vẫn xóa khỏi danh sách để tránh rò rỉ
        }
    }

    public void sendMessage(Message message) {
        try {
            if (oos != null && !clientSocket.isClosed()) {
                oos.writeObject(message);
                oos.flush();
                logger.finer("Đã gửi tin nhắn tới " + (player != null ? player.getUsername() : clientSocket.getInetAddress()) + ": " + message.getType());
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Lỗi gửi tin nhắn tới " + (player != null ? player.getUsername() : clientSocket.getInetAddress()), e);
            closeConnection();
        }
    }

    public void closeConnection() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Lỗi đóng kết nối client.", e);
        }
    }

    public PlayerModel getPlayer() {
        return player;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }
}