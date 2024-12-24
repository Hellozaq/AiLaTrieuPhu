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
//        QuestionModel[] questions = new QuestionModel[]{
//                new QuestionModel(1, "Quốc gia nào có thành phố Venice nổi tiếng với các kênh rạch?", "Ý", "Pháp", "Tây Ban Nha", "Hy Lạp", 1,1),
//                new QuestionModel(2, "Tác giả của tác phẩm '1984' là ai?", "George Orwell", "Aldous Huxley", "Ray Bradbury", "J.R.R. Tolkien", 1,1),
//                new QuestionModel(3, "Thành phố nào là thủ đô của Canada?", "Toronto", "Ottawa", "Vancouver", "Montreal", 2,1),
//                new QuestionModel(4, "Bảo tàng Louvre nổi tiếng nằm ở thành phố nào?", "London", "Paris", "New York", "Berlin", 2,1),
//                new QuestionModel(5, "Bộ phim nào có nhân vật chính là Tony Stark?", "Captain America", "Iron Man", "Thor", "Spider-Man", 2,1),
//                new QuestionModel(6, "Cuốn sách 'The Catcher in the Rye' được viết bởi ai?", "F. Scott Fitzgerald", "Harper Lee", "J.D. Salinger", "John Steinbeck", 3,1),
//                new QuestionModel(7, "Quốc gia nào nổi tiếng với món sushi?", "Hàn Quốc", "Trung Quốc", "Thái Lan", "Nhật Bản", 4,1),
//                new QuestionModel(8, "Bộ phim nào của Pixar có nhân vật chính là Woody?", "Toy Story", "Finding Nemo", "Cars", "The Incredibles", 1,1),
//                new QuestionModel(9, "Nhà vật lý nào đã phát minh ra thuyết tương đối?", "Isaac Newton", "Galileo Galilei", "Nikola Tesla", "Albert Einstein", 4,1),
//                new QuestionModel(10, "Nước nào có diện tích lớn nhất ở Châu Phi?", "Nam Phi", "Algeria", "Sudan", "Nigeria", 2,1),
//                new QuestionModel(11, "Hành tinh nào có tên gọi là 'Sao X' trong hệ Mặt Trời?", "Sao Thiên Vương", "Sao Hải Vương", "Sao Thổ", "Sao Diêm Vương", 4,1),
//                new QuestionModel(12, "Bộ phim nào có bài hát 'Let It Go'?", "Moana", "Tangled", "Coco", "Frozen", 4,1),
//                new QuestionModel(13, "Ai là người sáng lập ra hãng xe Ford?", "Karl Benz", "Henry Ford", "Enzo Ferrari", "Ferdinand Porsche", 2,1),
//                new QuestionModel(14, "Cuốn sách 'To Kill a Mockingbird' được viết bởi ai?", "Harper Lee", "Mark Twain", "J.D. Salinger", "F. Scott Fitzgerald", 1,1),
//                new QuestionModel(15, "Ai là vị tổng thống đầu tiên của Hoa Kỳ?", "Abraham Lincoln", "George Washington", "Thomas Jefferson", "John Adams", 2,1)
//        };
//        QuestionModel[] questions = new QuestionModel[15];

//        this.questions= Arrays.asList(questions);
    }



    public List<QuestionModel> getQuestions() {
        return questions;
    }



}
