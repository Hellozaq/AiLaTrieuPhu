package org.example.service;

import org.example.conf.HibernateUtil;
import org.example.model.QuestionModel;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestionService {
    private static final Logger logger = Logger.getLogger(QuestionService.class.getName());

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
        List<QuestionModel> finalQuestions = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            for (int level = 1; level <= 3; level++) {
                List<QuestionModel> questionsForLevel = session.createQuery(
                                "FROM QuestionModel WHERE difficultyLevel = :level",
                                QuestionModel.class)
                        .setParameter("level", level)
                        .list();

                if (questionsForLevel.isEmpty()) {
                    logger.warning("Không tìm thấy câu hỏi nào cho mức độ khó: " + level);
                }

                Collections.shuffle(questionsForLevel); // Xáo trộn danh sách câu hỏi của mức độ này

                // Lấy tối đa 5 câu hỏi từ danh sách đã xáo trộn
                int questionsToTake = Math.min(5, questionsForLevel.size());
                for (int i = 0; i < questionsToTake; i++) {
                    finalQuestions.add(questionsForLevel.get(i));
                }
                logger.info("Đã lấy " + questionsToTake + " câu hỏi cho mức độ " + level + ". Tổng số hiện tại: " + finalQuestions.size());

            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi khi lấy câu hỏi từ database", e);
            // Trả về danh sách rỗng hoặc ném lại lỗi tùy theo yêu cầu thiết kế của bạn
            // return new ArrayList<>(); // hoặc throw new RuntimeException("Database error", e);
        }
        logger.info("Tổng số câu hỏi lấy được: " + finalQuestions.size());
        return finalQuestions;
    }
}