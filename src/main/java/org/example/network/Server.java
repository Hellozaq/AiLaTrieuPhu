package org.example.network;

import org.example.model.PlayerModel; // Import PlayerModel

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class Server {
    private static final int PORT = 12345; // Cổng server
    private ServerSocket serverSocket;
    private ExecutorService clientExecutorService; // Dùng thread pool để quản lý client
    private final List<ClientHandler> connectedClients = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Room> activeRooms = new ConcurrentHashMap<>();

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public Server() {
        setupLogger();
        clientExecutorService = Executors.newCachedThreadPool();
    }

    private void setupLogger() {
        try {
            // Tắt logger mặc định của root để tránh in 2 lần nếu console handler của root được cấu hình
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            if (handlers.length > 0 && handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }

            logger.setLevel(Level.INFO); // Mức log chung (có thể đổi thành FINE, FINER để debug)

            // Console Handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO); // Log ra console từ INFO trở lên
            consoleHandler.setFormatter(new SimpleFormatter() {
                private static final String FORMAT = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(FORMAT,
                            new java.util.Date(lr.getMillis()),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage()
                    );
                }
            });
            logger.addHandler(consoleHandler);

            // File Handler
            FileHandler fileHandler = new FileHandler("server_log.%u.%g.txt", 1024 * 1024, 5, true); // Log xoay vòng, 1MB/file, 5 files
            fileHandler.setLevel(Level.FINE); // Ghi vào file từ FINE trở lên (chi tiết hơn)
            fileHandler.setFormatter(new SimpleFormatter()); // Có thể tùy chỉnh formatter cho file nếu muốn
            logger.addHandler(fileHandler);

            logger.setUseParentHandlers(false); // Không sử dụng handler của logger cha (root)

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Lỗi cấu hình file logger.", e);
        }
    }


    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            logger.info("Server Ai Là Triệu Phú Online đã khởi động trên cổng " + PORT);

            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Client mới kết nối từ: " + clientSocket.getInetAddress());
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clientExecutorService.submit(clientHandler);
                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        logger.info("Server socket đã đóng.");
                    } else {
                        logger.log(Level.SEVERE, "Lỗi chấp nhận kết nối client.", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Không thể khởi động server trên cổng " + PORT, e);
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        try {
            clientExecutorService.shutdownNow(); // Cố gắng dừng tất cả các luồng client
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler client : connectedClients) {
                client.closeConnection();
            }
            connectedClients.clear();
            activeRooms.clear();
            logger.info("Server đã dừng.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Lỗi khi dừng server.", e);
        }
    }

    // Quản lý Client
    public synchronized void addConnectedClient(ClientHandler clientHandler) {
        connectedClients.add(clientHandler);
    }

    public synchronized void removeConnectedClient(ClientHandler clientHandler) {
        connectedClients.remove(clientHandler);
    }

    // Quản lý Phòng
    public synchronized Room createRoom(PlayerModel player, ClientHandler handler, int betAmount) {
        // TODO: Trừ tiền cược của người chơi player.getRankScore() - betAmount
        // player.setRankScore(player.getRankScore() - betAmount); // Cần cập nhật vào DB sau này
        // server.getPlayerService().updatePlayer(player); // Ví dụ
        Room newRoom = new Room(player, handler, betAmount);
        activeRooms.put(newRoom.getRoomId(), newRoom);
        logger.info("Phòng " + newRoom.getRoomId() + " được tạo bởi " + player.getUsername() + " với mức cược " + betAmount);
        return newRoom;
    }

    public synchronized Room joinRoom(String roomId, PlayerModel player, ClientHandler handler) {
        Room room = activeRooms.get(roomId);
        if (room != null && !room.isFull()) {
            if (player.getRankScore() < room.getBetAmount()) {
                logger.warning(player.getUsername() + " không đủ tiền ("+ player.getRankScore() +") để vào phòng " + roomId + " cược " + room.getBetAmount());
                return null; // Không đủ tiền cược
            }
            // TODO: Trừ tiền cược của người chơi
            if (room.addPlayer2(player, handler)) {
                logger.info(player.getUsername() + " đã tham gia phòng " + roomId);
                return room;
            }
        }
        logger.warning(player.getUsername() + " không thể tham gia phòng " + roomId + ". Lý do: " + (room == null ? "Không tồn tại" : "Phòng đầy hoặc lỗi khác"));
        return null;
    }

    public synchronized void leaveRoom(String roomId, PlayerModel player) {
        Room room = activeRooms.get(roomId);
        if (room != null) {
            logger.info(player.getUsername() + " rời phòng " + roomId);
            room.removePlayer(player);
            if (room.isEmpty()) {
                activeRooms.remove(roomId);
                logger.info("Phòng " + roomId + " trống và đã bị xóa.");
            }
        }
    }

    public synchronized List<RoomInfo> getRoomInfoList() {
        List<RoomInfo> roomInfos = new ArrayList<>();
        for (Room room : activeRooms.values()) {
            roomInfos.add(room.getRoomInfo());
        }
        return roomInfos;
    }

    public synchronized void broadcastRoomList() {
        List<RoomInfo> roomInfos = getRoomInfoList();
        Message roomListMessage = new Message(MessageType.S2C_ROOM_LIST_UPDATE, roomInfos);
        for (ClientHandler client : connectedClients) {
            // Chỉ gửi danh sách phòng cho những client chưa ở trong phòng nào hoặc LobbyFrame đang active
            // Hoặc đơn giản là gửi cho tất cả, client tự quyết định có hiển thị hay không
            client.sendMessage(roomListMessage);
        }
        logger.fine("Đã gửi cập nhật danh sách phòng cho " + connectedClients.size() + " client(s).");
    }

    public synchronized void broadcastMessageToAll(Message message, ClientHandler excludeClient) {
        for (ClientHandler client : connectedClients) {
            if (client != excludeClient) { // Không gửi lại cho người gửi
                client.sendMessage(message);
            }
        }
    }


    public void sendRoomListToClient(ClientHandler clientHandler) {
        clientHandler.sendMessage(new Message(MessageType.S2C_ROOM_LIST_UPDATE, getRoomInfoList()));
    }

    public void startGameForRoom(Room room) {
        if (room.getStatus().equals("READY_TO_START")) {
            room.setStatus("PLAYING"); // Chuyển trạng thái phòng
            // TODO: Logic trừ tiền cược của cả 2 người chơi (nếu chưa trừ lúc tạo/join)

            // Thông báo cho cả 2 client trong phòng là game bắt đầu
            Message gameStartMsg = new Message(MessageType.S2C_GAME_STARTING, room.getRoomId());
            if (room.getHandler1() != null) room.getHandler1().sendMessage(gameStartMsg);
            if (room.getHandler2() != null) room.getHandler2().sendMessage(gameStartMsg);

            logger.info("Game bắt đầu cho phòng " + room.getRoomId() + " giữa " + room.getPlayer1().getUsername() + " và " + (room.getPlayer2() != null ? room.getPlayer2().getUsername() : "UNKNOWN"));
            broadcastRoomList(); // Cập nhật trạng thái phòng cho mọi người

            // TODO: Bắt đầu gửi câu hỏi đầu tiên cho phòng này
            // sendNextQuestionToRoom(room);
        }
    }

    public synchronized Room getRoomById(String roomId) {
        return activeRooms.get(roomId);
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
        // Để server chạy, có thể thêm shutdown hook để dừng server một cách an toàn
        Runtime.getRuntime().addShutdownHook(new Thread(server::stopServer));
    }
}