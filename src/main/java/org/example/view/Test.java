package org.example.view;

import org.example.conf.Database;
import org.example.model.DatabaseProperties;
import org.example.model.QuestionModel;
import org.example.service.QuestionService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Test {
    public static void main(String[] args) throws SQLException {
//        GameFrame.display();

        try{
            DatabaseProperties prop = new DatabaseProperties();
            prop.setDatabaseName("ailatrieuphu");
            prop.setHostname("localhost");
            prop.setPort(3306);
            prop.setPassword("");
            prop.setUsername("root");
            Database.connect(prop);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        QuestionService questionService = new QuestionService();
        List<QuestionModel> questionModels =questionService.get15Questions();
        for(QuestionModel questionModel : questionModels){
            System.out.println(questionModel);
        }
    }
}

