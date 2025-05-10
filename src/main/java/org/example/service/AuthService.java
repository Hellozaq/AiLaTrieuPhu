package org.example.service;

import org.example.conf.HibernateUtil;
import org.example.model.PlayerModel;
import org.example.util.EncryptionUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class AuthService {
    public boolean register(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            PlayerModel existing = session.createQuery(
                "FROM PlayerModel WHERE username = :username", PlayerModel.class)
                .setParameter("username", username)
                .uniqueResult();
            if (existing != null) return false;

            String encryptedPassword = EncryptionUtil.encrypt(password);
            PlayerModel player = new PlayerModel();
            player.setUsername(username);
            player.setPasswordHash(encryptedPassword);

            Transaction tx = session.beginTransaction();
            session.save(player);
            tx.commit();
            return true;
        }
    }

    public PlayerModel login(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            PlayerModel player = session.createQuery(
                "FROM PlayerModel WHERE username = :username", PlayerModel.class)
                .setParameter("username", username)
                .uniqueResult();
            if (player == null) return null;

            String encryptedPassword = EncryptionUtil.encrypt(password);
            if (encryptedPassword.equals(player.getPasswordHash())) {
                return player;
            }
            return null;
        }
    }
}