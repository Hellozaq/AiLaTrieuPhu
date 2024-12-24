package org.example.model;
public class QuestionModel {
    private int id;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int correctAnswer;
    private int difficultyLevel;

    // Constructors, getters, and setters
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


//public class QuestionModel {
//    private int id;
//    private String question;
//    private String answerA;
//    private String answerB;
//    private String answerC;
//    private String answerD;
//    private int answerRight;
//    public QuestionModel(){}
//    public QuestionModel(int id, String question, String answerA, String answerB, String answerC, String answerD, int answerRight) {
//        this.id = id;
//        this.question = question;
//        this.answerA = answerA;
//        this.answerB = answerB;
//        this.answerC = answerC;
//        this.answerD = answerD;
//        this.answerRight = answerRight;
//    }
//
//    @Override
//    public String toString() {
//        return "Question{" +
//                "id=" + id +
//                ", question='" + question + '\'' +
//                ", answerA='" + answerA + '\'' +
//                ", answerB='" + answerB + '\'' +
//                ", answerC='" + answerC + '\'' +
//                ", answerD='" + answerD + '\'' +
//                ", answerRight=" + answerRight +
//                '}';
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public String getQuestion() {
//        return question;
//    }
//
//    public void setQuestion(String question) {
//        this.question = question;
//    }
//
//    public String getAnswerA() {
//        return answerA;
//    }
//
//    public void setAnswerA(String answerA) {
//        this.answerA = answerA;
//    }
//
//    public String getAnswerB() {
//        return answerB;
//    }
//
//    public void setAnswerB(String answerB) {
//        this.answerB = answerB;
//    }
//
//    public String getAnswerC() {
//        return answerC;
//    }
//
//    public void setAnswerC(String answerC) {
//        this.answerC = answerC;
//    }
//
//    public String getAnswerD() {
//        return answerD;
//    }
//
//    public void setAnswerD(String answerD) {
//        this.answerD = answerD;
//    }
//
//    public int getAnswerRight() {
//        return answerRight;
//    }
//
//    public void setAnswerRight(int answerRight) {
//        this.answerRight = answerRight;
//    }
//}
