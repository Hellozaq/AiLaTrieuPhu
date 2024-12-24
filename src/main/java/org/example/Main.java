package org.example;

import org.example.conf.Database;
import org.example.model.DatabaseProperties;
import org.example.view.WelcomeFrame;

import java.util.Random;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
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
        WelcomeFrame.display();
    }
}


