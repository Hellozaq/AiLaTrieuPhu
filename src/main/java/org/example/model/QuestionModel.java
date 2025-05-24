package org.example.model;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "questions")
public class QuestionModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "question")
    private String question;

    @Column(name = "option_a")
    private String optionA;

    @Column(name = "option_b")
    private String optionB;

    @Column(name = "option_c")
    private String optionC;

    @Column(name = "option_d")
    private String optionD;

    @Column(name = "correct_answer")
    private int correctAnswer;

    @Column(name = "difficulty_level")
    private int difficultyLevel;

    public QuestionModel() {
    }

    public QuestionModel(int id, String question, String optionA, String optionB, String optionC, String optionD, int correctAnswer, int difficultyLevel) {
        this.id = id;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.difficultyLevel = difficultyLevel;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    @Override
    public String toString() {
        return "QuestionModel{" +
                "id=" + id +
                ", question='" + question + '\'' +
//                ", optionA='" + optionA + '\'' +
//                ", optionB='" + optionB + '\'' +
//                ", optionC='" + optionC + '\'' +
//                ", optionD='" + optionD + '\'' +
//                ", correctAnswer=" + correctAnswer +
                ", difficultyLevel=" + difficultyLevel +
                '}';
    }
}


