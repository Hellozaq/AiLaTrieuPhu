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
        if (this.player == null) { // Chưa xác thực mà gửi tin nhắn khác connect
            logger.warning("Client chưa xác thực gửi tin nhắn: " + message.getType());
            return;
        }
        switch (message.getType()) {
            case C2S_GET_ROOM_LIST:
                server.sendRoomListToClient(this);
                logger.info(player.getUsername() + " yêu cầu danh sách phòng.");
                break;
            case C2S_CREATE_ROOM:
                if (message.getPayload() instanceof Integer) {
                    int betAmount = (Integer) message.getPayload();
                    // Kiểm tra xem người chơi có đủ tiền không (tạm thời bỏ qua, sẽ thêm sau)
                    if (player.getRankScore() < betAmount) {
                        sendMessage(new Message(MessageType.S2C_ERROR, "Bạn không đủ tiền để tạo phòng với mức cược này."));
                        logger.warning(player.getUsername() + " không đủ tiền ("+player.getRankScore()+") để tạo phòng cược " + betAmount);
                        return;
                    }
                    currentRoom = server.createRoom(this.player, this, betAmount);
                    if (currentRoom != null) {
                        sendMessage(new Message(MessageType.S2C_ROOM_JOINED, currentRoom.getRoomInfo()));
                        logger.info(player.getUsername() + " đã tạo phòng " + currentRoom.getRoomId() + " với mức cược " + betAmount);
                        server.broadcastRoomList(); // Cập nhật cho các client khác
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
                        currentRoom = joinedRoom;
                        // Gửi thông tin phòng cho người vừa join
                        sendMessage(new Message(MessageType.S2C_ROOM_JOINED, currentRoom.getRoomInfo()));
                        logger.info(player.getUsername() + " đã tham gia phòng " + roomIdToJoin);

                        // Thông báo cho người chơi còn lại trong phòng VÀ gửi lại RoomInfo cập nhật cho họ
                        ClientHandler opponentHandler = currentRoom.getOpponentHandler(this);
                        if (opponentHandler != null) {
                            // Tin nhắn này báo cho client của đối thủ biết CÓ người mới vào, và đó là ai
                            opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_JOINED_ROOM, this.player));
                            // GỬI LẠI RoomInfo cập nhật cho đối thủ, giờ đã có đủ 2 người chơi
                            opponentHandler.sendMessage(new Message(MessageType.S2C_ROOM_JOINED, currentRoom.getRoomInfo()));
                        }
                        server.broadcastRoomList(); // Cập nhật danh sách phòng cho sảnh chờ chung
                    } else {
                        sendMessage(new Message(MessageType.S2C_ERROR, "Không thể tham gia phòng. Phòng không tồn tại, đã đầy hoặc bạn không đủ tiền cược."));
                        logger.warning(player.getUsername() + " tham gia phòng " + roomIdToJoin + " thất bại.");
                    }
                }
                break;
            case C2S_LEAVE_ROOM:
                if (currentRoom != null) {
                    String leftRoomId = currentRoom.getRoomId(); // Lưu lại ID trước khi currentRoom có thể bị set thành null
                    server.leaveRoom(leftRoomId, this.player);
                    logger.info(player.getUsername() + " đã rời phòng " + leftRoomId);
                    sendMessage(new Message(MessageType.S2C_ROOM_LEFT, null)); // Xác nhận rời phòng cho người gửi

                    ClientHandler opponentHandler = currentRoom.getOpponentHandler(this); // Lấy đối thủ TRƯỚC KHI currentRoom bị thay đổi bởi server.leaveRoom

                    // Cần lấy lại đối tượng Room từ server sau khi leaveRoom đã xử lý xong
                    // để gửi thông tin phòng cập nhật (chỉ còn 1 người) cho đối thủ còn lại.
                    Room roomAfterLeave = server.getRoomById(leftRoomId); // Cần thêm phương thức này vào Server.java

                    if (opponentHandler != null && roomAfterLeave != null && !roomAfterLeave.isEmpty()) {
                        opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_LEFT_ROOM, this.player.getUsername()));
                        // Gửi lại thông tin phòng cập nhật cho đối thủ (giờ phòng chỉ còn 1 người)
                        opponentHandler.sendMessage(new Message(MessageType.S2C_ROOM_JOINED, roomAfterLeave.getRoomInfo()));
                    } else if (opponentHandler != null && (roomAfterLeave == null || roomAfterLeave.isEmpty())) {
                        // Nếu phòng bị xóa (vì không còn ai) thì không cần gửi S2C_ROOM_JOINED nữa
                        // mà có thể client của đối thủ sẽ tự xử lý khi không nhận được thông tin phòng nữa hoặc server gửi 1 tin nhắn khác
                        opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_LEFT_ROOM, this.player.getUsername()));
                        // Có thể client của đối thủ sẽ tự động quay về sảnh khi không còn phòng hoặc nhận được S2C_ROOM_LIST_UPDATE
                    }

                    currentRoom = null; // Client này không còn ở trong phòng nào nữa
                    server.broadcastRoomList();
                }
                break;
            case C2S_LOBBY_CHAT:
                if (currentRoom != null && message.getPayload() instanceof String) {
                    String chatMsg = (String) message.getPayload();
                    logger.info("[CHAT PHÒNG " + currentRoom.getRoomId() + "] " + player.getUsername() + ": " + chatMsg);
                    // Gửi tin nhắn cho cả 2 người trong phòng
                    Message chatRelayMsg = new Message(MessageType.S2C_LOBBY_CHAT, player.getUsername() + ": " + chatMsg);
                    currentRoom.getHandler1().sendMessage(chatRelayMsg);
                    if (currentRoom.getHandler2() != null) {
                        currentRoom.getHandler2().sendMessage(chatRelayMsg);
                    }
                } else if (currentRoom == null && message.getPayload() instanceof String) {
                    // Chat ở sảnh chờ chung (nếu có logic này) - tạm thời bỏ qua
                    logger.info("[CHAT SẢNH] " + player.getUsername() + ": " + message.getPayload());
                    server.broadcastMessageToAll(new Message(MessageType.S2C_LOBBY_CHAT, "[Sảnh] " + player.getUsername() + ": " + message.getPayload()), this);

                }
                break;
            case C2S_PLAYER_READY:
                if (currentRoom != null && message.getPayload() instanceof Boolean) {
                    boolean isReady = (Boolean) message.getPayload();
                    currentRoom.setPlayerReady(this.player, isReady);
                    logger.info(player.getUsername() + (isReady ? " đã sẵn sàng." : " hủy sẵn sàng.") + " trong phòng " + currentRoom.getRoomId());

                    // Thông báo cho đối thủ về trạng thái sẵn sàng
                    ClientHandler opponentHandler = currentRoom.getOpponentHandler(this);
                    if (opponentHandler != null) {
                        opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_READY_STATUS, new Object[]{this.player.getUsername(), isReady}));
                    }

                    // Kiểm tra xem cả hai đã sẵn sàng để bắt đầu game chưa
                    if (currentRoom.getStatus().equals("READY_TO_START")) {
                        logger.info("Cả hai người chơi trong phòng " + currentRoom.getRoomId() + " đã sẵn sàng. Bắt đầu game!");
                        server.startGameForRoom(currentRoom);
                    }
                    server.broadcastRoomList(); // Cập nhật trạng thái phòng
                }
                break;
            default:
                logger.warning("Loại tin nhắn không xác định từ " + player.getUsername() + ": " + message.getType());
                break;
        }
    }

    private void handleDisconnect() {
        if (currentRoom != null) {
            server.leaveRoom(currentRoom.getRoomId(), this.player);
            ClientHandler opponentHandler = currentRoom.getOpponentHandler(this);
            if (opponentHandler != null) {
                opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_LEFT_ROOM, this.player.getUsername()));
                opponentHandler.sendMessage(new Message(MessageType.S2C_ROOM_JOINED, currentRoom.getRoomInfo())); // Cập nhật phòng cho đối thủ
            }
            currentRoom = null;
        }
        server.removeConnectedClient(this);
        server.broadcastRoomList();
        if (player != null) {
            logger.info(player.getUsername() + " đã chính thức ngắt kết nối khỏi server.");
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