package org.example.network;

import org.example.model.PlayerModel; // Import PlayerModel

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import org.example.model.QuestionModel;     // Thêm import
import org.example.controllers.QuestionController; // Thêm import (để lấy câu hỏi)
import org.example.service.AuthService;
import org.example.service.PlayerService;

import java.util.concurrent.*;
import java.util.logging.*;

public class Server {
    private static final int PORT = 12345; // Cổng server
    private ServerSocket serverSocket;
    private ExecutorService clientExecutorService; // Dùng thread pool để quản lý client
    private final List<ClientHandler> connectedClients = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Room> activeRooms = new ConcurrentHashMap<>();
    private QuestionController questionController; // Để lấy danh sách câu hỏi
    private PlayerService playerService;           // Để cập nhật tiền người chơi
    private AuthService authService;

    public static final ScheduledExecutorService timerScheduler = Executors.newSingleThreadScheduledExecutor(); // Dùng chung cho các timer của phòng


    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public Server() {
        setupLogger();
        clientExecutorService = Executors.newCachedThreadPool();

        try {
            questionController = new QuestionController();
            playerService = new PlayerService();
            authService = new AuthService();
            logger.info("QuestionController initialized with " + (questionController.getQuestions() != null ? questionController.getQuestions().size() : "null list") + " questions.");
            if (playerService == null) {
                logger.severe("PLAYER SERVICE IS NULL AFTER INITIALIZATION!");
            } else {
                logger.info("PlayerService initialized successfully.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize QuestionController or PlayerService", e);
            // Critical error, consider stopping server or handling appropriately
        }

    }

    private void setupLogger() {
        try {
            // Tắt logger mặc định của root để tránh in 2 lần nếu console handler của root
            // được cấu hình
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
                            lr.getMessage());
                }
            });
            logger.addHandler(consoleHandler);

            // File Handler
            FileHandler fileHandler = new FileHandler("server_log.%u.%g.txt", 1024 * 1024, 5, true); // Log xoay vòng,
            // 1MB/file, 5
            // files
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

    // Phương thức mới để xử lý yêu cầu đăng nhập
    public synchronized void handleLoginRequest(ClientHandler handler, String username, String password) {
        if (handler == null || username == null || password == null) {
            logger.warning("Yêu cầu đăng nhập không hợp lệ: thiếu thông tin.");
            return;
        }
        if (handler.getPlayer() != null) { // Client này đã đăng nhập rồi
            handler.sendMessage(new Message(MessageType.S2C_LOGIN_FAILURE, "Bạn đã đăng nhập rồi."));
            logger.warning("User " + handler.getPlayer().getUsername() + " cố gắng đăng nhập lại.");
            return;
        }

        PlayerModel player = authService.login(username, password);
        if (player != null) {
            // Kiểm tra xem player này có đang được sử dụng bởi ClientHandler khác không (đăng nhập đa thiết bị?)
            for (ClientHandler ch : connectedClients) {
                if (ch.getPlayer() != null && ch.getPlayer().getId() == player.getId() && ch != handler) {
                    // Player đã đăng nhập ở client khác. Tùy bạn xử lý:
                    // 1. Không cho phép -> gửi lỗi.
                    // 2. Cho phép, có thể ngắt kết nối client cũ. (Phức tạp hơn)
                    handler.sendMessage(new Message(MessageType.S2C_LOGIN_FAILURE, "Tài khoản này đã được đăng nhập ở nơi khác."));
                    logger.warning("Tài khoản " + username + " cố gắng đăng nhập khi đã có phiên khác hoạt động.");
                    return;
                }
            }

            handler.setPlayer(player); // Gán PlayerModel cho ClientHandler
            addConnectedClient(handler); // Thêm vào danh sách client "active" (đã xác thực)
            handler.sendMessage(new Message(MessageType.S2C_LOGIN_SUCCESS, player));
            logger.info("Người chơi " + username + " đăng nhập thành công.");
            broadcastRoomList(); // Cập nhật danh sách phòng cho client vừa đăng nhập
        } else {
            handler.sendMessage(new Message(MessageType.S2C_LOGIN_FAILURE, "Tên đăng nhập hoặc mật khẩu không chính xác."));
            logger.info("Đăng nhập thất bại cho username: " + username);
        }
    }

    // Phương thức mới để xử lý yêu cầu đăng ký
    public synchronized void handleRegisterRequest(ClientHandler handler, String username, String password) {
        if (handler == null || username == null || password == null) {
            logger.warning("Yêu cầu đăng ký không hợp lệ: thiếu thông tin.");
            return;
        }
        if (handler.getPlayer() != null) { // Client này đã đăng nhập/xác thực rồi
            handler.sendMessage(new Message(MessageType.S2C_REGISTER_FAILURE, "Bạn đã đăng nhập, không thể đăng ký."));
            return;
        }

        boolean success = authService.register(username, password);
        if (success) {
            // Có thể lấy lại PlayerModel vừa tạo để gửi về hoặc chỉ gửi thông báo thành công
            // PlayerModel newPlayer = authService.login(username, password); // Để lấy PlayerModel đầy đủ
            handler.sendMessage(new Message(MessageType.S2C_REGISTER_SUCCESS, null)); // Hoặc gửi newPlayer
            logger.info("Người chơi " + username + " đăng ký thành công.");
        } else {
            handler.sendMessage(new Message(MessageType.S2C_REGISTER_FAILURE, "Tên đăng nhập đã tồn tại hoặc có lỗi xảy ra."));
            logger.info("Đăng ký thất bại cho username: " + username);
        }
    }

    // Quản lý Client
    public synchronized void addConnectedClient(ClientHandler clientHandler) {
        connectedClients.add(clientHandler);
    }

    public synchronized void removeConnectedClient(ClientHandler clientHandler) {
        connectedClients.remove(clientHandler);
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
            // Chỉ gửi danh sách phòng cho những client chưa ở trong phòng nào hoặc
            // LobbyFrame đang active
            // Hoặc đơn giản là gửi cho tất cả, client tự quyết định có hiển thị hay không
            client.sendMessage(roomListMessage);
        }
        logger.fine("Đã gửi cập nhật danh sách phòng cho " + connectedClients.size() + " client(s).");
    }

    public synchronized void broadcastMessageToAll(Message message, ClientHandler excludeClient) {
        for (ClientHandler client : connectedClients) {

                client.sendMessage(message);

        }
    }

    public void sendRoomListToClient(ClientHandler clientHandler) {
        clientHandler.sendMessage(new Message(MessageType.S2C_ROOM_LIST_UPDATE, getRoomInfoList()));
    }


    public synchronized Room createRoom(PlayerModel player, ClientHandler handler, int betAmount) {
        // TODO: Trừ tiền cược ban đầu ở đây hoặc khi game bắt đầu
        Room newRoom = new Room(player, handler, betAmount);
        activeRooms.put(newRoom.getRoomId(), newRoom);
        logger.info("Phòng " + newRoom.getRoomId() + " được tạo bởi " + player.getUsername() + " với mức cược " + betAmount);
        return newRoom;
    }

    public synchronized Room joinRoom(String roomId, PlayerModel player, ClientHandler handler) {
        Room room = activeRooms.get(roomId);
        if (room != null && !room.isFull()) {
            if (player.getRankScore() < room.getBetAmount()) {
                logger.warning(player.getUsername() + " không đủ tiền (" + player.getRankScore() + ") để vào phòng " + roomId + " cược " + room.getBetAmount());
                handler.sendMessage(new Message(MessageType.S2C_ERROR, "Không đủ " + room.getBetAmount() + " xu để vào phòng."));
                return null;
            }
            if (room.addPlayer2(player, handler)) {
                logger.info(player.getUsername() + " đã tham gia phòng " + roomId);
                // TODO: Trừ tiền cược ban đầu ở đây hoặc khi game bắt đầu
                return room;
            }
        }
        logger.warning(player.getUsername() + " không thể tham gia phòng " + roomId + ". Lý do: " + (room == null ? "Không tồn tại" : "Phòng đầy hoặc lỗi khác"));
        handler.sendMessage(new Message(MessageType.S2C_ERROR, "Không thể vào phòng. Phòng không tồn tại hoặc đã đầy."));
        return null;
    }

    public synchronized void leaveRoom(String roomId, ClientHandler handler) { // Sửa tham số thành ClientHandler
        Room room = activeRooms.get(roomId);
        if (room != null) {
            PlayerModel leavingPlayer = handler.getPlayer();
            logger.info((leavingPlayer != null ? leavingPlayer.getUsername() : "Một người chơi") + " rời phòng " + roomId);

            // Nếu game đang diễn ra, xử thua cho người rời đi
            if ("PLAYING".equals(room.getStatus())) {
                endGameForRoom(room, (leavingPlayer != null ? leavingPlayer.getUsername() : "Một người chơi") + " đã rời trận.", handler);
                return; // endGameForRoom sẽ xử lý việc xóa phòng nếu cần
            }

            room.removePlayer(handler); // Truyền ClientHandler
            handler.sendMessage(new Message(MessageType.S2C_ROOM_LEFT, roomId)); // Gửi xác nhận cho người vừa rời

            ClientHandler opponentHandler = room.getOpponentHandler(handler); // Lấy đối thủ
            if (room.isEmpty()) {
                activeRooms.remove(roomId);
                logger.info("Phòng " + roomId + " trống và đã bị xóa.");
            } else { // Vẫn còn người
                room.setStatus("WAITING");
                if (opponentHandler != null) { // Thông báo cho người còn lại
                    opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_LEFT_ROOM, handler.getPlayer().getUsername()));
                    opponentHandler.sendMessage(new Message(MessageType.S2C_ROOM_JOINED, room.getRoomInfo())); // Gửi RoomInfo cập nhật
                }
            }
            broadcastRoomList();
            if (room.isEmpty()) {
                activeRooms.remove(roomId);
                logger.info("Phòng " + roomId + " trống và đã bị xóa.");
            } else {
                // Nếu còn người ở lại, cập nhật trạng thái phòng (ví dụ về WAITING)
                room.setStatus("WAITING");
                // Thông báo cho người còn lại (nếu có) đã được xử lý trong ClientHandler -> S2C_OPPONENT_LEFT_ROOM và S2C_ROOM_JOINED
            }
        }
    }

    public synchronized Room getRoomById(String roomId) { // Đã thêm ở lần trước
        return activeRooms.get(roomId);
    }


    // --- Các phương thức mới cho Game Logic ---
    public synchronized void startGameForRoom(Room room) {
        if (room == null || !"READY_TO_START".equals(room.getStatus())) {
            logger.warning("Không thể bắt đầu game cho phòng " + (room != null ? room.getRoomId() : "null") + ". Trạng thái không hợp lệ: " + (room != null ? room.getStatus() : "N/A"));
            return;
        }

        // 1. Trừ tiền cược (ví dụ) - Cần đảm bảo giao dịch an toàn
        try {
            if (room.getPlayer1() != null && room.getPlayer2() != null) {
                int bet = room.getBetAmount();
                if (room.getPlayer1().getRankScore() < bet || room.getPlayer2().getRankScore() < bet) {
                    logger.severe("Lỗi nghiêm trọng: Người chơi không đủ tiền cược khi bắt đầu game trong phòng " + room.getRoomId());
                    // Gửi lỗi cho cả 2, hủy game, hoàn tiền nếu đã trừ
                    Message errMsg = new Message(MessageType.S2C_ERROR, "Lỗi: Một người chơi không đủ tiền cược. Game bị hủy.");
                    if (room.getHandler1() != null) room.getHandler1().sendMessage(errMsg);
                    if (room.getHandler2() != null) room.getHandler2().sendMessage(errMsg);
                    room.setStatus("FINISHED_ERROR"); // Trạng thái lỗi
                    // Không cần xóa phòng ngay, để client nhận lỗi rồi tự rời
                    return;
                }
                // Giả sử đã trừ ở bước join hoặc tạo, hoặc trừ ở đây
                // playerService.deductBet(room.getPlayer1().getId(), bet);
                // playerService.deductBet(room.getPlayer2().getId(), bet);
                // Cập nhật PlayerModel cho client
                // if(room.getHandler1() != null) room.getHandler1().sendMessage(new Message(MessageType.S2C_UPDATE_PLAYER_INFO, playerService.getPlayerById(room.getPlayer1().getId())));
                // if(room.getHandler2() != null) room.getHandler2().sendMessage(new Message(MessageType.S2C_UPDATE_PLAYER_INFO, playerService.getPlayerById(room.getPlayer2().getId())));
                logger.info("Đã xác nhận tiền cược cho phòng " + room.getRoomId());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi xử lý tiền cược cho phòng " + room.getRoomId(), e);
            // Gửi lỗi và dừng game
            return;
        }


        room.setStatus("PLAYING");
        room.resetGameStats(); // Đặt lại điểm số và trạng thái game của phòng

        // Lấy danh sách câu hỏi cho ván đấu (ví dụ 5 câu)
        List<QuestionModel> allQuestions = questionController.getQuestions(); // Lấy tất cả câu hỏi
        List<QuestionModel> gameQuestionsForRoom = new ArrayList<>(allQuestions.subList(0, Math.min(15, allQuestions.size()))); // Chọn 5 câu đầu
        room.setGameQuestions(gameQuestionsForRoom);

        logger.info("Game bắt đầu cho phòng " + room.getRoomId() + " với " + gameQuestionsForRoom.size() + " câu hỏi.");

        Message gameStartMsg = new Message(MessageType.S2C_GAME_STARTING, new Object[]{room.getRoomId(), room.getPlayer1(), room.getPlayer2()}); // Gửi kèm thông tin 2 player
        if (room.getHandler1() != null) room.getHandler1().sendMessage(gameStartMsg);
        if (room.getHandler2() != null) room.getHandler2().sendMessage(gameStartMsg);

        broadcastRoomList(); // Cập nhật trạng thái phòng cho sảnh chờ

        final Room finalRoom = room; // Cần biến final để dùng trong lambda
        timerScheduler.schedule(() -> {
            logger.info("Đã trì hoãn, bây giờ gửi câu hỏi đầu tiên cho phòng " + finalRoom.getRoomId());
            sendNextQuestionToRoom(finalRoom);
        }, 4000, TimeUnit.MILLISECONDS); // Gửi câu hỏi đầu tiên
    }

    private synchronized void sendNextQuestionToRoom(Room room) {
        if (room == null || !"PLAYING".equals(room.getStatus())) return;

        // Hủy timer của câu hỏi trước (nếu có)
        if (room.getQuestionTimerFuture() != null && !room.getQuestionTimerFuture().isDone()) {
            room.getQuestionTimerFuture().cancel(false);
        }

        int nextQuestionIndex = room.getCurrentQuestionIndexInGame();
        if (nextQuestionIndex < room.getGameQuestions().size() - 1) {
            nextQuestionIndex++; // Tăng index sau khi kiểm tra
            room.setCurrentQuestionIndexInGame(nextQuestionIndex);
            QuestionModel question = room.getGameQuestions().get(nextQuestionIndex);
            room.setCurrentQuestionInRoom(question);
            room.setPlayer1AnswerIndex(-1); // Reset trạng thái trả lời
            room.setPlayer2AnswerIndex(-1);

            int timeLimitSeconds = 60; // Thời gian cho mỗi câu hỏi
            Message questionMsg = new Message(MessageType.S2C_GAME_QUESTION, new Object[]{question, timeLimitSeconds});

            if (room.getHandler1() != null) room.getHandler1().sendMessage(questionMsg);
            if (room.getHandler2() != null) room.getHandler2().sendMessage(questionMsg);

            logger.info("Đã gửi câu hỏi " + (nextQuestionIndex + 1) + " (ID: " + question.getId() + ") cho phòng " + room.getRoomId());

            // Đặt hẹn giờ cho câu hỏi
            ScheduledFuture<?> timerFuture = timerScheduler.schedule(() -> {
                logger.info("Hết giờ cho câu hỏi " + (room.getCurrentQuestionIndexInGame() + 1) + " phòng " + room.getRoomId());
                processTimeUpForRoom(room);
            }, timeLimitSeconds, TimeUnit.SECONDS);
            room.setQuestionTimerFuture(timerFuture);
        } else {
            // Hết câu hỏi
            logger.info("Hết câu hỏi cho phòng " + room.getRoomId());
            endGameForRoom(room, "Đã hoàn thành tất cả câu hỏi.", null);
        }
    }

    public synchronized void handleSubmitAnswer(ClientHandler handler, Room room, int questionId, int answerIndex) {
        if (room == null || !"PLAYING".equals(room.getStatus()) || room.getCurrentQuestionInRoom() == null) {
            logger.warning("Nhận câu trả lời khi phòng không ở trạng thái PLAYING hoặc không có câu hỏi hiện tại. Phòng: " + (room != null ? room.getRoomId() : "null"));
            return;
        }
        if (room.getCurrentQuestionInRoom().getId() != questionId) {
            logger.warning("Nhận câu trả lời cho câu hỏi ID " + questionId + " nhưng câu hỏi hiện tại của phòng là " + room.getCurrentQuestionInRoom().getId());
            return; // Trả lời cho câu hỏi cũ, bỏ qua
        }

        PlayerModel p = handler.getPlayer();
        boolean alreadyAnswered = false;

        if (handler == room.getHandler1()) {
            if (room.getPlayer1AnswerIndex() == -1) room.setPlayer1AnswerIndex(answerIndex);
            else alreadyAnswered = true;
        } else if (handler == room.getHandler2()) {
            if (room.getPlayer2AnswerIndex() == -1) room.setPlayer2AnswerIndex(answerIndex);
            else alreadyAnswered = true;
        } else {
            return; // Handler không thuộc phòng
        }

        if (alreadyAnswered) {
            logger.info(p.getUsername() + " cố gắng trả lời lại câu hỏi " + questionId + ". Bỏ qua.");
            return;
        }

        logger.info(p.getUsername() + " đã trả lời câu " + (room.getCurrentQuestionIndexInGame() + 1) + " với lựa chọn " + answerIndex);

        // Thông báo cho đối thủ là người này đã trả lời (tùy chọn)
        // ClientHandler opponentHandler = room.getOpponentHandler(handler);
        // if (opponentHandler != null) {
        // opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_ANSWERED, p.getUsername()));
        // }


        // Kiểm tra xem cả hai đã trả lời chưa
        if (room.getPlayer1AnswerIndex() != -1 && room.getPlayer2AnswerIndex() != -1) {
            if (room.getQuestionTimerFuture() != null && !room.getQuestionTimerFuture().isDone()) {
                room.getQuestionTimerFuture().cancel(false); // Hủy timer vì cả 2 đã trả lời
                logger.info("Đã hủy timer cho câu hỏi " + (room.getCurrentQuestionIndexInGame() + 1) + " phòng " + room.getRoomId() + " vì cả 2 đã trả lời.");
            }
            processAnswersAndContinue(room);
        }
    }


    private synchronized void processTimeUpForRoom(Room room) {
        if (room == null || !"PLAYING".equals(room.getStatus())) return;
        logger.info("Xử lý hết giờ cho câu hỏi " + (room.getCurrentQuestionIndexInGame() + 1) + " phòng " + room.getRoomId());

        // Đánh dấu những người chưa trả lời là hết giờ (ví dụ, câu trả lời = 0)
        if (room.getPlayer1AnswerIndex() == -1) room.setPlayer1AnswerIndex(0); // 0 nghĩa là hết giờ
        if (room.getPlayer2AnswerIndex() == -1) room.setPlayer2AnswerIndex(0);

        processAnswersAndContinue(room);
    }

    private synchronized void processAnswersAndContinue(Room room) {
        if (room == null || !"PLAYING".equals(room.getStatus()) || room.getCurrentQuestionInRoom() == null) return;

        QuestionModel question = room.getCurrentQuestionInRoom();
        int correctAnswer = question.getCorrectAnswer();

        boolean p1Result = (room.getPlayer1AnswerIndex() == correctAnswer);
        boolean p2Result = (room.getPlayer2AnswerIndex() == correctAnswer);

        int pointsThisRound = 1000;

        if (p1Result) room.incrementPlayer1OnlineScore(pointsThisRound);
        if (p2Result) room.incrementPlayer2OnlineScore(pointsThisRound);


//        logger.info("SERVER: Chuẩn bị gửi S2C_ANSWER_RESULT cho phòng " + room.getRoomId() + " đến handler1 và handler2.");
        // ... (tính toán p1Result, p2Result) ...

        ClientHandler handlerP1 = room.getHandler1();
        ClientHandler handlerP2 = room.getHandler2();

        if (handlerP1 != null) {
            Object[] payloadP1 = new Object[]{
                    question.getId(),
                    room.getPlayer1AnswerIndex(), p1Result, // myChoice, myResult cho P1
                    room.getPlayer2AnswerIndex(), p2Result, // opponentChoice, opponentResult (là P2) cho P1
                    correctAnswer
            };
            handlerP1.sendMessage(new Message(MessageType.S2C_ANSWER_RESULT, payloadP1));
            // Gửi điểm: [điểm của tôi (P1), điểm của đối thủ (P2)]
            handlerP1.sendMessage(new Message(MessageType.S2C_UPDATE_GAME_SCORE, new Object[]{
                    room.getPlayer1OnlineScore(), room.getPlayer2OnlineScore()
            }));
        }

        if (handlerP2 != null) {
            Object[] payloadP2 = new Object[]{
                    question.getId(),
                    room.getPlayer2AnswerIndex(), p2Result,   // myChoice, myResult cho P2
                    room.getPlayer1AnswerIndex(), p1Result,   // opponentChoice, opponentResult (là P1) cho P2
                    correctAnswer
            };
            handlerP2.sendMessage(new Message(MessageType.S2C_ANSWER_RESULT, payloadP2));
            // Gửi điểm: [điểm của tôi (P2), điểm của đối thủ (P1)]
            handlerP2.sendMessage(new Message(MessageType.S2C_UPDATE_GAME_SCORE, new Object[]{
                    room.getPlayer2OnlineScore(), room.getPlayer1OnlineScore()
            }));
        }

        final Room finalRoom = room; // Cần biến final để dùng trong lambda
        final int delayMillisSeconds = 2500;
        logger.info("Đã xử lý và gửi kết quả câu hỏi " + (room.getCurrentQuestionIndexInGame() + 1) + " cho phòng " + room.getRoomId());

        ScheduledFuture<?> nextActionFuture = timerScheduler.schedule(() -> {
            // Đảm bảo rằng phòng vẫn đang trong trạng thái 'PLAYING' trước khi gửi câu hỏi tiếp theo.
            // Điều này quan trọng vì trong thời gian delay, một người chơi có thể đã rời trận,
            // và endGameForRoom có thể đã được gọi, thay đổi trạng thái phòng.
            synchronized (this) { // Đồng bộ hóa để kiểm tra trạng thái phòng một cách an toàn
                if ("PLAYING".equals(finalRoom.getStatus())) {
                    logger.info("Hết thời gian delay, gửi câu hỏi tiếp theo cho phòng " + finalRoom.getRoomId());
                    if (room.getHandler1() != null) {
                        room.getHandler1().sendMessage(new Message(MessageType.S2C_UPDATE_GAME_SCORE, new Object[]{
                                room.getPlayer1OnlineScore(), // myOnlineScore cho P1
                                room.getPlayer2OnlineScore()  // opponentOnlineScore cho P1
                        }));
                    }

                    if (room.getHandler2() != null) {
                        room.getHandler2().sendMessage(new Message(MessageType.S2C_UPDATE_GAME_SCORE, new Object[]{
                                room.getPlayer2OnlineScore(), // myOnlineScore cho P2
                                room.getPlayer1OnlineScore()  // opponentOnlineScore cho P2
                        }));
                    }
                    sendNextQuestionToRoom(finalRoom); // Gọi sendNextQuestionToRoom sau khi delay
                } else {
                    logger.info("Phòng " + finalRoom.getRoomId() + " không còn ở trạng thái PLAYING sau khi delay. Trạng thái hiện tại: " + finalRoom.getStatus() + ". Không gửi câu hỏi tiếp.");
                }
            }
        }, delayMillisSeconds, TimeUnit.MILLISECONDS);
    }

    public synchronized void endGameForRoom(Room room, String reason, ClientHandler leaver) {
        if (room == null || "FINISHED".equals(room.getStatus()) || "FINISHED_ERROR".equals(room.getStatus())) {
            logger.warning("endGameForRoom được gọi cho phòng " + (room != null ? room.getRoomId() : "null") + " nhưng phòng đã kết thúc hoặc không hợp lệ.");
            return; // Game đã kết thúc hoặc phòng không hợp lệ
        }
        logger.info("Kết thúc game cho phòng " + room.getRoomId() + ". Lý do: " + reason);
        room.setStatus("FINISHED");

        // Hủy timer nếu còn chạy
        if (room.getQuestionTimerFuture() != null && !room.getQuestionTimerFuture().isDone()) {
            room.getQuestionTimerFuture().cancel(true);
        }


        PlayerModel p1 = room.getPlayer1();
        PlayerModel p2 = room.getPlayer2();
        ClientHandler h1 = room.getHandler1();
        ClientHandler h2 = room.getHandler2();

        if (p1 == null || p2 == null || h1 == null || h2 == null) {
            logger.warning("Không đủ thông tin người chơi/handler để kết thúc game và trao thưởng cho phòng " + room.getRoomId());
            // Có thể chỉ xóa phòng nếu 1 người đã thoát hoàn toàn
            if (room.isEmpty() || room.getPlayerCount() < 2 && leaver != null) { // Nếu leaver làm phòng trống hoặc còn 1
                activeRooms.remove(room.getRoomId());
                logger.info("Phòng " + room.getRoomId() + " đã được xóa do không đủ người chơi khi kết thúc game.");
            }
            broadcastRoomList();
            return;
        }


        String winnerUsername = null;
        PlayerModel winnerModel = null;
        PlayerModel loserModel = null;
        int prize = room.getBetAmount() * 2; // Tổng tiền cược

        if (leaver != null) { // Nếu có người rời trận
            if (leaver == h1) { // p1 rời
                winnerModel = p2;
                loserModel = p1;
            } else { // p2 rời
                winnerModel = p1;
                loserModel = p2;
            }
            winnerUsername = winnerModel.getUsername();
            // Người rời trận không nhận được gì, tiền cược của họ thuộc về người thắng
            prize = room.getBetAmount(); // Chỉ tiền cược của người thua
        } else { // Kết thúc bình thường
            if (room.getPlayer1OnlineScore() > room.getPlayer2OnlineScore()) {
                winnerModel = p1;
                loserModel = p2;
            } else if (room.getPlayer2OnlineScore() > room.getPlayer1OnlineScore()) {
                winnerModel = p2;
                loserModel = p1;
            } else { // Hòa
                prize = room.getBetAmount(); // Mỗi người nhận lại tiền cược của mình
            }
            if (winnerModel != null) winnerUsername = winnerModel.getUsername();
        }


        try {
            if (winnerModel != null && loserModel != null) { // Có thắng có thua
                winnerModel.setRankScore(winnerModel.getRankScore() + room.getBetAmount()); // Thắng nhận tiền cược của đối thủ
                // loserModel.setRankScore(loserModel.getRankScore() - room.getBetAmount()); // Thua mất tiền cược (đã trừ khi vào phòng hoặc sẽ trừ)
                // Hiện tại, logic tiền cược là người thắng nhận tổng (hoặc nhận phần của người thua)
                // Giả sử tiền cược đã bị "khóa", giờ cộng cho người thắng
                playerService.updatePlayer(winnerModel); // Cập nhật DB
                // playerService.updatePlayer(loserModel); // Cập nhật DB nếu rankScore của người thua cũng thay đổi (ví dụ bị trừ)
                logger.info("Trao thưởng: " + winnerModel.getUsername() + " thắng " + room.getBetAmount() + " xu. " + loserModel.getUsername() + " thua.");

            } else { // Hòa
                // Hoàn tiền cược nếu có cơ chế "khóa" tiền cược
                // p1.setRankScore(p1.getRankScore() + room.getBetAmount()); // Giả sử hoàn tiền
                // p2.setRankScore(p2.getRankScore() + room.getBetAmount());
                // playerService.updatePlayer(p1);
                // playerService.updatePlayer(p2);
                logger.info("Game hòa cho phòng " + room.getRoomId() + ". Tiền cược có thể được hoàn (tùy logic).");
                prize = 0; // Không ai thắng thêm
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi cập nhật điểm/tiền cho người chơi sau game " + room.getRoomId(), e);
        }

        // Gửi thông báo kết thúc game và thông tin người chơi đã cập nhật
        if (h1 != null) {
            h1.sendMessage(new Message(MessageType.S2C_GAME_OVER, new Object[]{winnerUsername, (winnerModel == p1 ? prize : (winnerModel == null ? 0 : -room.getBetAmount())), (winnerModel == p1)}));
            h1.sendMessage(new Message(MessageType.S2C_UPDATE_PLAYER_INFO, playerService.getPlayerById(p1.getId()))); // Gửi PlayerModel mới nhất
        }
        if (h2 != null) {
            h2.sendMessage(new Message(MessageType.S2C_GAME_OVER, new Object[]{winnerUsername, (winnerModel == p2 ? prize : (winnerModel == null ? 0 : -room.getBetAmount())), (winnerModel == p2)}));
            h2.sendMessage(new Message(MessageType.S2C_UPDATE_PLAYER_INFO, playerService.getPlayerById(p2.getId())));
        }

        // Xóa phòng sau khi game kết thúc
        activeRooms.remove(room.getRoomId());
        logger.info("Phòng " + room.getRoomId() + " đã kết thúc và bị xóa.");
        broadcastRoomList();
    }
    // Trong Server.java
    public synchronized void processPlayerHelpRequest(Room room, ClientHandler handler, MessageType helpType, int clientQuestionId) {
        if (room == null || !"PLAYING".equals(room.getStatus()) || handler == null || handler.getPlayer() == null) {
            logger.warning("Yêu cầu trợ giúp không hợp lệ: phòng hoặc người chơi không hợp lệ.");
            if (handler != null) handler.sendMessage(new Message(MessageType.S2C_ERROR, "Yêu cầu trợ giúp không hợp lệ."));
            return;
        }

        QuestionModel currentQuestionInServer = room.getCurrentQuestionInRoom();
        if (currentQuestionInServer == null || currentQuestionInServer.getId() != clientQuestionId) {
            logger.warning(handler.getPlayer().getUsername() + " yêu cầu trợ giúp cho câu hỏi ID " + clientQuestionId +
                    " nhưng câu hỏi hiện tại của server là " + (currentQuestionInServer != null ? currentQuestionInServer.getId() : "null") +
                    " cho phòng " + room.getRoomId());
            handler.sendMessage(new Message(MessageType.S2C_ERROR, "Yêu cầu trợ giúp không khớp câu hỏi hiện tại."));
            return;
        }

        String helpTypeDescription = "";
        MessageType resultType = null;
        Object resultPayload = null;

        boolean canUseHelp = room.tryUseHelp(handler, helpType);

        if (canUseHelp) {
            switch (helpType) {
                case C2S_USE_HELP_5050:
                    helpTypeDescription = "đã sử dụng trợ giúp 50/50";
                    resultType = MessageType.S2C_HELP_RESULT_5050;
                    // Logic tính toán 2 đáp án sai
                    int correctAnswer5050 = currentQuestionInServer.getCorrectAnswer();
                    List<Integer> options = new ArrayList<>(List.of(1, 2, 3, 4));
                    options.remove(Integer.valueOf(correctAnswer5050)); // Bỏ đáp án đúng
                    Collections.shuffle(options); // Xáo trộn các đáp án sai
                    resultPayload = new Object[]{clientQuestionId, options.get(0), options.get(1)}; // Gửi 2 đáp án sai đầu tiên
                    break;

                case C2S_USE_HELP_CALL:
                    helpTypeDescription = "đã sử dụng Gọi điện thoại";
                    resultType = MessageType.S2C_HELP_RESULT_CALL;
                    // Với Lựa chọn A (client tự mở HelpCallFrame), server chỉ cần gửi xác nhận
                    // Payload chỉ cần là questionId để client xác nhận
                    resultPayload = new Object[]{clientQuestionId};
                    // Nếu Lựa chọn B (server gửi gợi ý), thì resultPayload sẽ chứa gợi ý đó.
                    break;

                case C2S_USE_HELP_AUDIENCE:
                    helpTypeDescription = "đã sử dụng Hỏi ý kiến khán giả";
                    resultType = MessageType.S2C_HELP_RESULT_AUDIENCE;
                    // Logic tính toán poll và mostVotedOption
                    Map<Integer, Double> poll = new HashMap<>();
                    int correctAnswerAudience = currentQuestionInServer.getCorrectAnswer();
                    double correctPercent = 0.5 + (new Random().nextDouble() * 0.3); // 50-80% cho đáp án đúng
                    double remainingPercent = 1.0 - correctPercent;
                    poll.put(correctAnswerAudience, correctPercent);

                    List<Integer> wrongOptions = new ArrayList<>(List.of(1,2,3,4));
                    wrongOptions.remove(Integer.valueOf(correctAnswerAudience));
                    Collections.shuffle(wrongOptions);

                    if (wrongOptions.size() == 3) {
                        double p1 = remainingPercent * (new Random().nextDouble() * 0.6 + 0.2); // 20-80% của phần còn lại
                        double p2 = remainingPercent * (new Random().nextDouble() * ( (1- (p1/remainingPercent) )*0.8 ) ); // còn lại
                        double p3 = remainingPercent - p1 - p2;
                        if(p3 <0) { //điều chỉnh nếu số âm
                            p3 =0; p2 = remainingPercent-p1;
                            if(p2 <0) {p2=0; p1=remainingPercent;}
                        }

                        poll.put(wrongOptions.get(0), p1);
                        poll.put(wrongOptions.get(1), p2);
                        poll.put(wrongOptions.get(2), p3);
                    } else { // Xử lý trường hợp ít hơn 3 đáp án sai (ít xảy ra với 4 lựa chọn)
                        for(Integer option : wrongOptions) poll.put(option, remainingPercent / wrongOptions.size());
                    }
                    // Làm tròn và đảm bảo tổng là 1.0 (có thể bỏ qua nếu không quá quan trọng)
                    resultPayload = new Object[]{clientQuestionId, poll, correctAnswerAudience /*mostVotedOptionIndex*/};
                    break;
            }

            // Gửi kết quả/xác nhận cho người yêu cầu
            handler.sendMessage(new Message(resultType, resultPayload));
            logger.info(handler.getPlayer().getUsername() + " " + helpTypeDescription + " cho câu hỏi ID " + clientQuestionId + " phòng " + room.getRoomId());

            // Thông báo cho đối thủ
            ClientHandler opponentHandler = room.getOpponentHandler(handler);
            if (opponentHandler != null) {
                opponentHandler.sendMessage(new Message(MessageType.S2C_OPPONENT_USED_HELP,
                        new Object[]{handler.getPlayer().getUsername(), helpTypeDescription}));
            }
            broadcastRoomList(); // Trạng thái trợ giúp có thể ảnh hưởng đến RoomInfo (nếu bạn thêm cờ vào RoomInfo)
        } else {
            // Gửi thông báo không thể sử dụng trợ giúp
            handler.sendMessage(new Message(MessageType.S2C_HELP_UNAVAILABLE, "Bạn đã sử dụng trợ giúp '" + helpType.toString().replace("C2S_USE_HELP_", "") + "' rồi."));
            logger.info(handler.getPlayer().getUsername() + " không thể dùng " + helpType + " (đã dùng) cho câu hỏi ID " + clientQuestionId + " phòng " + room.getRoomId());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
        // Để server chạy, có thể thêm shutdown hook để dừng server một cách an toàn
        Runtime.getRuntime().addShutdownHook(new Thread(server::stopServer));
    }
}