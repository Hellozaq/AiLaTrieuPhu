package org.example.service;

import org.example.conf.Database;
import org.example.model.QuestionModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {
    public void saveQuestion(QuestionModel question) throws SQLException {
        PreparedStatement pstmt = Database.getConnection().prepareStatement(
                "INSERT INTO questions VALUES (?,?,?,?,?,?,?,?)");
        pstmt.setInt(1, question.getId());
        pstmt.setString(2, question.getQuestion());
        pstmt.setString(3, question.getOptionA());
        pstmt.setString(4, question.getOptionB());
        pstmt.setString(5, question.getOptionC());
        pstmt.setString(6, question.getOptionD());
        pstmt.setInt(7, question.getCorrectAnswer());
        pstmt.setInt(8, question.getDifficultyLevel());
        pstmt.executeUpdate();
    }

    public QuestionModel questionMapper(ResultSet rs) throws SQLException {
        QuestionModel questionModel = new QuestionModel(
                rs.getInt("id"),
                rs.getString("question"),
                rs.getString("option_a"),
                rs.getString("option_b"),
                rs.getString("option_c"),
                rs.getString("option_d"),
                rs.getInt("correct_answer"),
                rs.getInt("difficulty_level")
        );
        return questionModel;
    }
    public List<QuestionModel> get15Questions() throws SQLException {
        List<QuestionModel> questions = new ArrayList<>();
        PreparedStatement pstmt = Database.getConnection().prepareStatement(
                "SELECT * FROM `questions` WHERE difficulty_level=1 ORDER BY rand() LIMIT 5;");
        ResultSet rs = pstmt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        String name = rsmd.getColumnName(1);
        System.out.println(name);

        while (rs.next()) {
            questions.add(questionMapper(rs));
        }
        PreparedStatement pstmt2 = Database.getConnection().prepareStatement(
                "SELECT * FROM `questions` WHERE difficulty_level=2 ORDER BY rand() LIMIT 5;");
        ResultSet rs2 = pstmt2.executeQuery();
        while (rs2.next()) {
            questions.add(questionMapper(rs2));
        }
        PreparedStatement pstmt3 = Database.getConnection().prepareStatement(
                "SELECT * FROM `questions` WHERE difficulty_level=3 ORDER BY rand() LIMIT 5;");
        ResultSet rs3 = pstmt3.executeQuery();
        while (rs3.next()) {
            questions.add(questionMapper(rs3));
        }
        return questions;

    }
}
