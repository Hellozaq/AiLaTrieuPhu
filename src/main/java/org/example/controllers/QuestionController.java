package org.example.controllers;

import org.example.model.QuestionModel;
import org.example.service.QuestionService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionController {
    private QuestionService questionService;
    private List<QuestionModel> questions;
    private int[] answers;

    public QuestionController() throws SQLException {
        questionService = new QuestionService();
        questions=questionService.get15Questions();

    }



    public List<QuestionModel> getQuestions() {
        return questions;
    }



}
