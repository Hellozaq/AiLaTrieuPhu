package org.example.service;

import org.example.conf.Database;
import org.example.model.PlayerModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerService {
    public PlayerService() {}

    public PlayerModel playerMaper(ResultSet rs) throws SQLException {
        PlayerModel player = new PlayerModel(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getInt("score"),
                rs.getString("avatarpath")
        );
        return player;
    }

    public void save(PlayerModel player) throws SQLException {

        PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT INTO players (username, score) VALUES (?, ?)"
        );
        ps.setString(1, player.getUsername());
        ps.setInt(2, player.getScore());
        ps.executeUpdate();
    }

    public PlayerModel findByUsername(String username) throws SQLException {
        PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT * FROM players WHERE username = ?"
        );
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int id = rs.getInt("id");
            int score = rs.getInt("score");
            String avatar = rs.getString("avatarpath");
            PlayerModel player = new PlayerModel(id,username,score,avatar);
            return player;
        }else {
            PlayerModel newPlayer = new PlayerModel(username);
            save(newPlayer);
            return newPlayer;
        }
    }

    public void updateMaxScore(PlayerModel player, int score) throws SQLException {
        PreparedStatement preparedStatement = Database.getConnection().prepareStatement(
                "select * from players where username = ?"
        );
        preparedStatement.setString(1, player.getUsername());
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            if (resultSet.getInt("score") < score) {
                PreparedStatement ps = Database.getConnection().prepareStatement(
                        "UPDATE players SET score = ? WHERE username = ?"
                );
                ps.setInt(1, score);
                ps.setString(2, player.getUsername());
                ps.executeUpdate();
            }
        }
    }

    public void updateAvatar(PlayerModel player) throws SQLException {
        PreparedStatement preparedStatement = Database.getConnection().prepareStatement(
                "UPDATE players SET avatarpath = ? WHERE username = ?"
        );
        preparedStatement.setString(1, player.getAvatarPath());
        preparedStatement.setString(2, player.getUsername());
        preparedStatement.executeUpdate();
    }

    public List<PlayerModel> getRankingTop10() throws SQLException {
        List<PlayerModel> top10 = new ArrayList<>();
        PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT * FROM players ORDER BY score DESC limit 10"
        );
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            top10.add(playerMaper(rs));
        }
        return top10;
    }

}
