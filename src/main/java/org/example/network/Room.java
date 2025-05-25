package org.example.network;

import org.example.model.PlayerModel;
import org.example.model.QuestionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class Room {
    private String roomId;
    private PlayerModel player1;
    private PlayerModel player2;
    private ClientHandler handler1;
    private ClientHandler handler2;
    private String status; // "WAITING", "READY_TO_START", "PLAYING", "FINISHED"
    private int betAmount;
    private boolean player1Ready;
    private boolean player2Ready;

    private transient List<QuestionModel> gameQuestions; // Danh sách câu hỏi cho ván này (transient vì không cần serialize nếu Room được gửi đi)
    private int currentQuestionIndexInGame;
    private QuestionModel currentQuestionInRoom;
    private int player1AnswerIndex; // -1: chưa trả lời, 0: hết giờ, 1-4: lựa chọn
    private int player2AnswerIndex;
    private int player1OnlineScore;
    private int player2OnlineScore;
    private transient ScheduledFuture<?> questionTimerFuture; // Để hủy timer nếu cần

    private boolean player1Used5050 = false;
    private boolean player1UsedCall = false;
    private boolean player1UsedAudience = false;

    private boolean player2Used5050 = false;
    private boolean player2UsedCall = false;
    private boolean player2UsedAudience = false;


    public Room(PlayerModel player1, ClientHandler handler1, int betAmount) {
        this.roomId = UUID.randomUUID().toString().substring(0, 6); // ID phòng ngẫu nhiên ngắn
        this.player1 = player1;
        this.handler1 = handler1;
        this.betAmount = betAmount;
        this.status = "WAITING"; // Chờ người chơi 2
        this.player1Ready = false;
        this.gameQuestions = new ArrayList<>();
        resetGameStats();
        resetHelpUsage();
    }
    public void resetGameStats() {
        this.currentQuestionIndexInGame = -1;
        this.currentQuestionInRoom = null;
        this.player1AnswerIndex = -1; // -1 là chưa trả lời
        this.player2AnswerIndex = -1;
        this.player1OnlineScore = 0;
        this.player2OnlineScore = 0;
        if (questionTimerFuture != null && !questionTimerFuture.isDone()) {
            questionTimerFuture.cancel(true);
        }
        questionTimerFuture = null;
        resetHelpUsage();
    }
    public void resetHelpUsage() {
        this.player1Used5050 = false;
        this.player1UsedCall = false;
        this.player1UsedAudience = false;
        this.player2Used5050 = false;
        this.player2UsedCall = false;
        this.player2UsedAudience = false;
    }

    // Getters cho các thuộc tính game
    public List<QuestionModel> getGameQuestions() { return gameQuestions; }
    public int getCurrentQuestionIndexInGame() { return currentQuestionIndexInGame; }
    public QuestionModel getCurrentQuestionInRoom() { return currentQuestionInRoom; }
    public int getPlayer1AnswerIndex() { return player1AnswerIndex; }
    public int getPlayer2AnswerIndex() { return player2AnswerIndex; }
    public int getPlayer1OnlineScore() { return player1OnlineScore; }
    public int getPlayer2OnlineScore() { return player2OnlineScore; }
    public ScheduledFuture<?> getQuestionTimerFuture() { return questionTimerFuture; }

    // Setters cho các thuộc tính game (sẽ được gọi bởi Server)
    public void setGameQuestions(List<QuestionModel> questions) { this.gameQuestions = questions; }
    public void setCurrentQuestionIndexInGame(int index) { this.currentQuestionIndexInGame = index; }
    public void setCurrentQuestionInRoom(QuestionModel question) { this.currentQuestionInRoom = question; }

    public synchronized void setPlayer1AnswerIndex(int answerIndex) { this.player1AnswerIndex = answerIndex; }
    public synchronized void setPlayer2AnswerIndex(int answerIndex) { this.player2AnswerIndex = answerIndex; }

    public synchronized void incrementPlayer1OnlineScore(int points) { this.player1OnlineScore += points; }
    public synchronized void incrementPlayer2OnlineScore(int points) { this.player2OnlineScore += points; }
    public void setQuestionTimerFuture(ScheduledFuture<?> future) { this.questionTimerFuture = future;}


    // Getters
    public String getRoomId() { return roomId; }
    public PlayerModel getPlayer1() { return player1; }
    public PlayerModel getPlayer2() { return player2; }
    public ClientHandler getHandler1() { return handler1; }
    public ClientHandler getHandler2() { return handler2; }
    public String getStatus() { return status; }
    public int getBetAmount() { return betAmount; }
    public boolean isPlayer1Ready() { return player1Ready; }
    public boolean isPlayer2Ready() { return player2Ready; }


    // Setters and Logic
    public void setStatus(String status) { this.status = status; }

    public synchronized boolean addPlayer2(PlayerModel player2, ClientHandler handler2) {
        if (this.player2 == null) {
            this.player2 = player2;
            this.handler2 = handler2;
            this.status = "WAITING_READY";
            this.player2Ready = false;
            return true;
        }
        return false; // Phòng đã đủ người
    }

    public synchronized void removePlayer(ClientHandler handlerToRemove) {
        if (handlerToRemove == handler1) {
            logger.info("Player 1 (" + (player1 != null ? player1.getUsername() : "N/A") + ") is leaving room " + roomId);
            player1 = player2;
            handler1 = handler2;
            player1Ready = player2Ready;
            // Nếu player1 là người duy nhất và rời đi, player1 sẽ là null
        } else if (handlerToRemove == handler2) {
            logger.info("Player 2 (" + (player2 != null ? player2.getUsername() : "N/A") + ") is leaving room " + roomId);
        } else {
            return; // Handler không thuộc phòng này
        }
        player2 = null;
        handler2 = null;
        player2Ready = false; // Reset trạng thái sẵn sàng của slot 2

        if (player1 == null) { // Không còn ai trong phòng
            status = "EMPTY";
        } else { // Vẫn còn player1
            status = "WAITING"; // Chờ người chơi mới
            player1Ready = false; // Player1 cũng cần sẵn sàng lại nếu muốn chơi với người mới
        }
        resetGameStats(); // Nếu ai đó rời đi giữa chừng, reset trạng thái game
    }

    public boolean isFull() {
        return player1 != null && player2 != null;
    }

    public boolean isEmpty() {
        return player1 == null && player2 == null;
    }

    public int getPlayerCount() {
        int count = 0;
        if (player1 != null) count++;
        if (player2 != null) count++;
        return count;
    }

    public synchronized void setPlayerReady(ClientHandler handler, boolean ready) {
        if (handler == handler1 && player1 != null) {
            this.player1Ready = ready;
        } else if (handler == handler2 && player2 != null) {
            this.player2Ready = ready;
        }
        checkIfBothReady();
    }

    private void checkIfBothReady() {
        if (isFull() && player1Ready && player2Ready) {
            this.status = "READY_TO_START";
        } else if (isFull()) {
            this.status = "WAITING_READY";
        }
    }

    public ClientHandler getOpponentHandler(ClientHandler requesterHandler) {
        if (handler1 == requesterHandler) return handler2;
        if (handler2 == requesterHandler) return handler1;
        return null;
    }

    public PlayerModel getOpponent(PlayerModel requester) {
        if (player1 != null && player1.getId() == requester.getId()) return player2;
        if (player2 != null && player2.getId() == requester.getId()) return player1;
        return null;
    }

    public boolean isPlayer1Used5050() { return player1Used5050; }
    public boolean isPlayer1UsedCall() { return player1UsedCall; }
    public boolean isPlayer1UsedAudience() { return player1UsedAudience; }
    public boolean isPlayer2Used5050() { return player2Used5050; }
    public boolean isPlayer2UsedCall() { return player2UsedCall; }
    public boolean isPlayer2UsedAudience() { return player2UsedAudience; }

    public void setPlayer1Used5050(boolean player1Used5050) { this.player1Used5050 = player1Used5050; }
    public void setPlayer1UsedCall(boolean player1UsedCall) { this.player1UsedCall = player1UsedCall; }
    public void setPlayer1UsedAudience(boolean player1UsedAudience) { this.player1UsedAudience = player1UsedAudience; }
    public void setPlayer2Used5050(boolean player2Used5050) { this.player2Used5050 = player2Used5050; }
    public void setPlayer2UsedCall(boolean player2UsedCall) { this.player2UsedCall = player2UsedCall; }
    public void setPlayer2UsedAudience(boolean player2UsedAudience) { this.player2UsedAudience = player2UsedAudience; }


    public RoomInfo getRoomInfo() {
        return new RoomInfo(
                roomId,
                player1 != null ? player1.getUsername() : null,
                player2 != null ? player2.getUsername() : null,
                getPlayerCount(),
                status,
                betAmount
        );
    }


    // Phương thức tiện ích để kiểm tra và đặt cờ sử dụng trợ giúp
    public synchronized boolean tryUseHelp(ClientHandler handler, MessageType helpType) {
        boolean canUse = false;
        if (handler == handler1) { // Player 1
            switch (helpType) {
                case C2S_USE_HELP_5050: if (!player1Used5050) { player1Used5050 = true; canUse = true; } break;
                case C2S_USE_HELP_CALL: if (!player1UsedCall) { player1UsedCall = true; canUse = true; } break;
                case C2S_USE_HELP_AUDIENCE: if (!player1UsedAudience) { player1UsedAudience = true; canUse = true; } break;
            }
        } else if (handler == handler2) { // Player 2
            switch (helpType) {
                case C2S_USE_HELP_5050: if (!player2Used5050) { player2Used5050 = true; canUse = true; } break;
                case C2S_USE_HELP_CALL: if (!player2UsedCall) { player2UsedCall = true; canUse = true; } break;
                case C2S_USE_HELP_AUDIENCE: if (!player2UsedAudience) { player2UsedAudience = true; canUse = true; } break;
            }
        }
        return canUse;
    }
    private static final  Logger logger = Logger.getLogger(Room.class.getName()); // Thêm logger

}