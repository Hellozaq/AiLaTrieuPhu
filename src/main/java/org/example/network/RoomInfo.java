package org.example.network;

import org.example.model.PlayerModel; // Cần import PlayerModel

import java.io.Serializable;

public class RoomInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String roomId;
    private String player1Name; // Chỉ cần tên để hiển thị trong danh sách
    private String player2Name;
    private int playerCount;
    private String status; // e.g., "Đang chờ", "Đủ người", "Đang chơi"
    private int betAmount;

    // Constructor, getters (không cần setters cho DTO này nếu chỉ dùng để truyền dữ liệu)

    public RoomInfo(String roomId, String player1Name, String player2Name, int playerCount, String status, int betAmount) {
        this.roomId = roomId;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.playerCount = playerCount;
        this.status = status;
        this.betAmount = betAmount;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public String getStatus() {
        return status;
    }

    public int getBetAmount() {
        return betAmount;
    }

    @Override
    public String toString() {
        return String.format("Phòng %s [%s xu] (%s/2) - %s %s",
                roomId,
                betAmount,
                playerCount,
                player1Name != null ? player1Name : "Trống",
                (playerCount == 2 && player2Name != null) ? "& " + player2Name : (playerCount == 1 && player1Name != null ? "" : "")
        );
    }
}