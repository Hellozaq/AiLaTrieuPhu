package org.example.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "players")
public class PlayerModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "rank_score", nullable = false)
    private int rankScore;

    @Column(name = "avatarpath")
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

    public PlayerModel(int id, String username, String passwordHash, int score, int rankScore, String avatarPath) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.score = score;
        this.rankScore = rankScore;
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
                ", passwordHash='" + passwordHash + '\'' +
                ", score=" + score +
                ", rankScore=" + rankScore +
                ", avatarPath='" + avatarPath + '\'' +
                '}';
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getRankScore() {
        return rankScore;
    }

    public void setRankScore(int rankScore) {
        this.rankScore = rankScore;
    }

    public String getAvatarPath() {
        return avatarPath;
    }
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
}
