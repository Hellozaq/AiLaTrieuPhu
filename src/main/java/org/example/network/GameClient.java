package org.example.network;

import org.example.model.PlayerModel;
import org.example.view.LobbyFrame; // Sẽ cần để cập nhật UI
// import org.example.view.GameFrame; // Sau này cho game online

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameClient {
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    protected PlayerModel player;
    private volatile boolean running = false; // Dùng để kiểm soát luồng lắng nghe

    protected LobbyFrame lobbyFrame; // Tham chiếu đến LobbyFrame để cập nhật UI

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
            } catch (SocketException e){
                if (running) { // Chỉ log lỗi nếu client vẫn đang chạy (không phải do chủ động ngắt kết nối)
                    logger.log(Level.WARNING, "Mất kết nối tới server (SocketException): " + e.getMessage());
                }
            }
            catch (IOException | ClassNotFoundException e) {
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

    private void processServerMessage(Message message) {
        // Xử lý các loại tin nhắn từ server và cập nhật UI tương ứng
        // Sẽ gọi các phương thức của lobbyFrame (hoặc gameFrame sau này)
        if (lobbyFrame == null && message.getType() != MessageType.S2C_GAME_STARTING /*và các loại msg ko cần lobby*/) {
            logger.warning("LobbyFrame chưa được thiết lập để xử lý tin nhắn: " + message.getType());
            // return; // Tạm thời cho phép xử lý để debug, nhưng cần lobbyFrame để tương tác
        }

        switch (message.getType()) {
            case S2C_ROOM_LIST_UPDATE:
                if (message.getPayload() instanceof List) {
                    List<RoomInfo> roomInfos = (List<RoomInfo>) message.getPayload();
                    if (lobbyFrame != null) {
                        lobbyFrame.updateRoomList(roomInfos);
                    }
                    logger.info("Đã nhận danh sách phòng: " + roomInfos.size() + " phòng.");
                }
                break;
            case S2C_ROOM_JOINED:
                if (message.getPayload() instanceof RoomInfo) {
                    RoomInfo joinedRoomInfo = (RoomInfo) message.getPayload();
                    if (lobbyFrame != null) {
                        lobbyFrame.handleRoomJoined(joinedRoomInfo);
                    }
                    logger.info("Đã tham gia/cập nhật phòng: " + joinedRoomInfo.getRoomId());
                }
                break;
            case S2C_OPPONENT_JOINED_ROOM:
                if (message.getPayload() instanceof PlayerModel) {
                    PlayerModel opponent = (PlayerModel) message.getPayload();
                    if (lobbyFrame != null) {
                        lobbyFrame.cacheOpponentPlayerModel(opponent);
                    }
                    logger.info("Đối thủ " + opponent.getUsername() + " đã tham gia phòng.");
                }
                break;
            case S2C_OPPONENT_LEFT_ROOM:
                if (message.getPayload() instanceof String) { // Server gửi username của người rời đi
                    String opponentUsername = (String) message.getPayload();
                    if (lobbyFrame != null) {
                        lobbyFrame.handleOpponentLeft(opponentUsername);
                    }
                    logger.info("Đối thủ " + opponentUsername + " đã rời phòng.");
                }
                break;
            case S2C_OPPONENT_READY_STATUS:
                // Payload là Object[] {String opponentUsername, Boolean isReady}
                if (message.getPayload() instanceof Object[]) {
                    Object[] data = (Object[]) message.getPayload();
                    String opponentUsername = (String) data[0];
                    boolean isReady = (Boolean) data[1];
                    if (lobbyFrame != null) {
                        lobbyFrame.updateOpponentReadyStatus(opponentUsername, isReady);
                    }
                    logger.info("Đối thủ " + opponentUsername + " trạng thái sẵn sàng: " + isReady);
                }
                break;
            case S2C_LOBBY_CHAT:
                if (message.getPayload() instanceof String) {
                    String chatMessage = (String) message.getPayload();
                    if (lobbyFrame != null) {
                        lobbyFrame.appendChatMessage(chatMessage);
                    }
                    logger.info("Tin nhắn chat nhận được: " + chatMessage);
                }
                break;
            case S2C_GAME_STARTING:
                // Payload có thể là RoomID hoặc thông tin game
                logger.info("Server thông báo game bắt đầu! Payload: " + message.getPayload());
                if (lobbyFrame != null) {
                    lobbyFrame.handleGameStarting(message.getPayload()); // payload có thể là roomID
                }
                break;
            case S2C_ERROR:
                if (message.getPayload() instanceof String) {
                    String errorMessage = (String) message.getPayload();
                    logger.warning("Lỗi từ Server: " + errorMessage);
                    if (lobbyFrame != null) {
                        lobbyFrame.showErrorMessage(errorMessage);
                    }
                }
                break;
            case S2C_UPDATE_PLAYER_INFO: // Ví dụ, cập nhật tiền sau khi cược
                if (message.getPayload() instanceof PlayerModel) {
                    this.player = (PlayerModel) message.getPayload();
                    logger.info("Thông tin người chơi được cập nhật: " + this.player.getUsername() + ", Tiền: " + this.player.getRankScore());
                    if (lobbyFrame != null) {
                        lobbyFrame.updateCurrentPlayerInfo(this.player);
                    }
                }
                break;
            // Thêm các case khác cho các tin nhắn trong game sau này
            default:
                logger.info("Nhận tin nhắn chưa xử lý từ server: " + message.getType());
                break;
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