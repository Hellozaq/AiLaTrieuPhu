package org.example.service;

import org.example.conf.HibernateUtil;
import org.example.model.QuestionModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class QuestionService {
    public void saveQuestion(QuestionModel question) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(question);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public List<QuestionModel> get15Questions() {
        List<QuestionModel> questions = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Lấy 5 câu hỏi mỗi mức độ khó
            for (int level = 1; level <= 3; level++) {
                List<QuestionModel> qs = session.createQuery(
                        "FROM QuestionModel WHERE difficultyLevel = :level ORDER BY function('RAND')",
                        QuestionModel.class)
                        .setParameter("level", level)
                        .setMaxResults(5)
                        .list();
                questions.addAll(qs);
            }
        }
        return questions;
    }
}