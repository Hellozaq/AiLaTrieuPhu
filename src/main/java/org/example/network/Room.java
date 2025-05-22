package org.example.network;

import org.example.model.PlayerModel;

import java.util.UUID;

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

    public Room(PlayerModel player1, ClientHandler handler1, int betAmount) {
        this.roomId = UUID.randomUUID().toString().substring(0, 6); // ID phòng ngẫu nhiên ngắn
        this.player1 = player1;
        this.handler1 = handler1;
        this.betAmount = betAmount;
        this.status = "WAITING"; // Chờ người chơi 2
        this.player1Ready = false;
    }

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
            this.status = "WAITING_READY"; // Đủ người, chờ sẵn sàng
            this.player2Ready = false;
            return true;
        }
        return false; // Phòng đã đủ người
    }

    public synchronized void removePlayer(PlayerModel player) {
        if (player != null) {
            if (player.equals(player1)) {
                player1 = player2; // Đẩy player2 lên làm player1
                handler1 = handler2;
                player1Ready = player2Ready;
            }
            player2 = null;
            handler2 = null;
            player2Ready = false;
            status = (player1 == null) ? "EMPTY" : "WAITING";
        }
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

    public void setPlayerReady(PlayerModel player, boolean ready) {
        if (player.equals(player1)) {
            this.player1Ready = ready;
        } else if (player.equals(player2)) {
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
}