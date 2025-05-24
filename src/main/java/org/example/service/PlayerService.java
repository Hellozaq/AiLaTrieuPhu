package org.example.service;

import org.example.conf.HibernateUtil;
import org.example.model.PlayerModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PlayerService {
    public PlayerService() {}

    public void save(PlayerModel player) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(player);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    

    public PlayerModel findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            PlayerModel player = session.createQuery(
                "FROM PlayerModel WHERE username = :username", PlayerModel.class)
                .setParameter("username", username)
                .uniqueResult();
            if (player != null) {
                return player;
            } else {
                PlayerModel newPlayer = new PlayerModel(username);
                save(newPlayer);
                return newPlayer;
            }
        }
    }

    public void updateMaxScore(PlayerModel player, int score) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            PlayerModel existing = session.createQuery(
                "FROM PlayerModel WHERE username = :username", PlayerModel.class)
                .setParameter("username", player.getUsername())
                .uniqueResult();
            if (existing != null && existing.getScore() < score) {
                existing.setScore(score);
                session.update(existing);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public void updateAvatar(PlayerModel player) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            PlayerModel existing = session.createQuery(
                "FROM PlayerModel WHERE username = :username", PlayerModel.class)
                .setParameter("username", player.getUsername())
                .uniqueResult();
            if (existing != null) {
                existing.setAvatarPath(player.getAvatarPath());
                session.update(existing);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public List<PlayerModel> getRankingTop10() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM PlayerModel ORDER BY rankScore DESC", PlayerModel.class)
                .setMaxResults(10)
                .list();
        }
    }

    public PlayerModel getPlayerById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            PlayerModel player = session.get(PlayerModel.class, id);
            if (player != null) {
                return player;
            }
            return null;
        }
    }

        public void updatePlayer(PlayerModel player) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            PlayerModel existing = session.createQuery(
                "FROM PlayerModel WHERE username = :username", PlayerModel.class)
                .setParameter("username", player.getUsername())
                .uniqueResult();
            if (existing != null) {
                existing.setAvatarPath(player.getAvatarPath());
                existing.setScore(player.getScore());
                existing.setRankScore(player.getRankScore());
                existing.setPasswordHash(player.getPasswordHash());
                // Thêm các trường khác nếu cần
                session.update(existing);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}