package org.example.model;

public class PlayerModel {
    private int id;
    private String username;
    private int score;
    private String avatarPath="";

    public PlayerModel() {}

    public PlayerModel(String username) {
        this.username = username;
    }

    public PlayerModel(int id, String username, int score, String avatarPath) {
        this.id = id;
        this.username = username;
        this.score = score;
        this.avatarPath = avatarPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "PlayerModel{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", score=" + score +
                '}';
    }
    public String getAvatarPath() {
        return avatarPath;
    }
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
}
