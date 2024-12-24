-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th12 08, 2024 lúc 04:22 AM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `ailatrieuphu`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `players`
--

CREATE TABLE `players` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `score` int(11) NOT NULL,
  `avatarpath` text DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `players`
--

INSERT INTO `players` (`id`, `username`, `score`, `avatarpath`) VALUES
(1, 'ngocvien', 32000000, 'src/main/resources/avatar/3.png'),
(2, 'thiennhan', 1000000, 'src/main/resources/avatar/9.png');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `questions`
--

CREATE TABLE `questions` (
  `id` int(11) NOT NULL,
  `question` text DEFAULT NULL,
  `option_a` text DEFAULT NULL,
  `option_b` text DEFAULT NULL,
  `option_c` text DEFAULT NULL,
  `option_d` text DEFAULT NULL,
  `correct_answer` int(11) DEFAULT NULL,
  `difficulty_level` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `questions`
--

INSERT INTO `questions` (`id`, `question`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `difficulty_level`) VALUES
(1, 'Thủ đô của Việt Nam là gì?', 'Hà Nội', 'Sài Gòn', 'Đà Nẵng', 'Hải Phòng', 1, 1),
(2, 'Ai là người sáng lập ra Apple?', 'Steve Jobs', 'Bill Gates', 'Elon Musk', 'Mark Zuckerberg', 1, 2),
(3, 'Số nguyên tố đầu tiên là gì?', '1', '2', '3', '5', 2, 1),
(4, 'Công ty nào sản xuất điện thoại Galaxy?', 'Apple', 'Samsung', 'Nokia', 'Xiaomi', 2, 1),
(5, 'Nước nào có diện tích lớn nhất thế giới?', 'Trung Quốc', 'Canada', 'Nga', 'Brazil', 3, 1),
(6, 'Ai là tác giả của cuốn tiểu thuyết \'Chiến tranh và hòa bình\'?', 'Lev Tolstoy', 'Victor Hugo', 'Fyodor Dostoevsky', 'Mark Twain', 1, 2),
(7, 'Bộ phim nào đã đoạt giải Oscar cho Phim hay nhất năm 2020?', '1917', 'Joker', 'Parasite', 'Once Upon a Time in Hollywood', 3, 2),
(8, '\'E = mc²\' là công thức nổi tiếng của nhà khoa học nào?', 'Isaac Newton', 'Nikola Tesla', 'Albert Einstein', 'Galileo Galilei', 3, 1),
(9, 'Ai là người phát hiện ra Penicillin?', 'Louis Pasteur', 'Alexander Fleming', 'Marie Curie', 'Thomas Edison', 2, 2),
(10, 'Năm nào diễn ra sự kiện 30 tháng 4 tại Việt Nam?', '1975', '1976', '1977', '1978', 1, 1),
(11, 'Ngày quốc tế phụ nữ được tổ chức vào ngày nào?', '1 tháng 3', '8 tháng 3', '12 tháng 3', '14 tháng 3', 2, 1),
(12, 'Ai là người vẽ bức tranh Mona Lisa?', 'Vincent van Gogh', 'Pablo Picasso', 'Leonardo da Vinci', 'Claude Monet', 3, 1),
(13, 'Cơ quan nào chịu trách nhiệm sản xuất tiền tại Việt Nam?', 'Ngân hàng Nhà nước', 'Kho bạc Nhà nước', 'Bộ Tài chính', 'Bộ Kế hoạch và Đầu tư', 1, 2),
(14, 'Tác phẩm \'Thép đã tôi thế đấy\' là của tác giả nào?', 'Alexandre Dumas', 'Maxim Gorky', 'Nikolai Ostrovsky', 'Lev Tolstoy', 3, 2),
(15, 'Đơn vị đo chiều dài chính của hệ thống SI là gì?', 'Mét', 'Kilômét', 'Cen-ti-mét', 'Milimét', 1, 1),
(16, 'Quốc gia nào có nền kinh tế lớn nhất thế giới?', 'Trung Quốc', 'Mỹ', 'Nhật Bản', 'Đức', 2, 1),
(17, 'Thành phố nào được biết đến là \'Kinh đô ánh sáng\'?', 'Paris', 'London', 'Tokyo', 'New York', 1, 1),
(18, '\'Thuyết tương đối\' được phát minh bởi ai?', 'Albert Einstein', 'Isaac Newton', 'Stephen Hawking', 'Galileo Galilei', 1, 1),
(19, 'Quốc gia nào tổ chức Olympic 2020?', 'Nhật Bản', 'Trung Quốc', 'Hàn Quốc', 'Mỹ', 1, 2),
(20, 'Biển nào lớn nhất thế giới?', 'Biển Đỏ', 'Biển Địa Trung Hải', 'Biển Đông', 'Biển Caspian', 4, 2),
(21, 'Tên của chú chó trong phim hoạt hình \'101 chú chó đốm\'?', 'Pongo', 'Perdita', 'Rolly', 'Lucky', 1, 1),
(22, 'Quốc gia nào phát minh ra trà?', 'Ấn Độ', 'Trung Quốc', 'Nhật Bản', 'Việt Nam', 2, 2),
(23, '\'Romeo và Juliet\' là tác phẩm của nhà văn nào?', 'William Shakespeare', 'Charles Dickens', 'Jane Austen', 'Victor Hugo', 1, 1),
(24, 'Ai là người sáng lập ra Facebook?', 'Mark Zuckerberg', 'Steve Jobs', 'Jeff Bezos', 'Larry Page', 1, 1),
(25, '\'Người phán xử\' là tên của bộ phim truyền hình của nước nào?', 'Trung Quốc', 'Mỹ', 'Việt Nam', 'Nhật Bản', 3, 1),
(26, 'Tên gọi đầy đủ của nhà văn Mikhail Bulgakov là gì?', 'Mikhail Alexandrovich', 'Mikhail Yefimovich', 'Mikhail Afanasievich', 'Mikhail Fyodorovich', 3, 3),
(27, 'Ai là chủ tịch nước Việt Nam hiện nay (2024)?', 'Nguyễn Phú Trọng', 'Trần Đại Quang', 'Nguyễn Xuân Phúc', 'Nguyễn Minh Triết', 3, 1),
(28, 'Quốc gia nào có nhiều hòn đảo nhất?', 'Indonesia', 'Nhật Bản', 'Philippines', 'Na Uy', 1, 3),
(29, 'Ai là người phát minh ra điện thoại?', 'Alexander Graham Bell', 'Thomas Edison', 'Nikola Tesla', 'James Watt', 1, 2),
(30, '\'Cô bé Lọ Lem\' trong truyện cổ tích có tên tiếng Anh là gì?', 'Cinderella', 'Rapunzel', 'Snow White', 'Little Red Riding Hood', 1, 1),
(31, 'Loài chim nào có khả năng bay lùi?', 'Chim Đại Bàng', 'Chim Sẻ', 'Chim Cánh Cụt', 'Chim Hút Mật', 4, 2),
(32, 'Thành phố nào được biết đến với tên gọi \'Thành phố tình yêu\'?', 'Venice', 'Paris', 'Prague', 'Rome', 2, 1),
(33, 'Cầu thủ nào được mệnh danh là \'Vua bóng đá\'?', 'Diego Maradona', 'Lionel Messi', 'Cristiano Ronaldo', 'Pele', 4, 1),
(34, 'Quốc gia nào có diện tích rừng lớn nhất thế giới?', 'Brazil', 'Canada', 'Nga', 'Mỹ', 3, 3),
(35, 'Tên gọi khác của sóng thần là gì?', 'Bão nhiệt đới', 'Động đất dưới biển', 'Đại dương sóng thần', 'Địa chấn', 2, 1),
(36, 'Vịnh nào nằm giữa Việt Nam và Thái Lan?', 'Vịnh Bắc Bộ', 'Vịnh Thái Lan', 'Vịnh Hạ Long', 'Vịnh Cam Ranh', 2, 1),
(37, 'Kỹ thuật nào được dùng để nhân giống cây trồng?', 'Ghép cây', 'Gieo hạt', 'Trồng cây con', 'Chia cây', 1, 3),
(38, 'Quốc gia nào nổi tiếng với lễ hội La Tomatina?', 'Pháp', 'Tây Ban Nha', 'Ý', 'Đức', 2, 2),
(39, '\'Thần Rừng\' là ai trong thần thoại Hy Lạp?', 'Pan', 'Zeus', 'Hades', 'Hermes', 1, 2),
(40, 'Nước nào nổi tiếng với nền văn hóa Samurai?', 'Trung Quốc', 'Nhật Bản', 'Hàn Quốc', 'Thái Lan', 2, 1),
(41, 'Nhân vật nào trong phim \'Harry Potter\' có tên đầy đủ là Tom Riddle?', 'Albus Dumbledore', 'Severus Snape', 'Voldemort', 'Sirius Black', 3, 1),
(42, 'Ai là cha của nhà vật lý học Isaac Newton?', 'Robert Newton', 'John Newton', 'William Newton', 'Thomas Newton', 2, 3),
(43, 'Biểu tượng của nước Nhật Bản là gì?', 'Chim Đại Bàng', 'Hoa Anh Đào', 'Núi Phú Sĩ', 'Rừng tre', 2, 1),
(44, 'Ai đã phát minh ra bóng đèn?', 'Alexander Graham Bell', 'Thomas Edison', 'Nikola Tesla', 'George Westinghouse', 2, 2),
(45, '\'Titanic\' là tên của gì?', 'Một con tàu', 'Một bộ phim', 'Một bài hát', 'Một cuốn sách', 1, 1),
(46, 'Quốc gia nào nổi tiếng với món sushi?', 'Hàn Quốc', 'Trung Quốc', 'Việt Nam', 'Nhật Bản', 4, 1),
(47, '\'Gone with the Wind\' là một cuốn tiểu thuyết nổi tiếng của ai?', 'J.K. Rowling', 'Jane Austen', 'Margaret Mitchell', 'George Orwell', 3, 2),
(48, 'Tổng thống đầu tiên của nước Mỹ là ai?', 'Abraham Lincoln', 'George Washington', 'Thomas Jefferson', 'John Adams', 2, 1),
(49, 'Ngày quốc khánh của Việt Nam là ngày nào?', '30 tháng 4', '1 tháng 5', '2 tháng 9', '19 tháng 5', 3, 1),
(50, 'Ai là người viết cuốn \'Đắc nhân tâm\'?', 'Dale Carnegie', 'Napoleon Hill', 'Stephen Covey', 'Zig Ziglar', 1, 2),
(51, '\'Quốc gia khởi nghiệp\' là tác phẩm của tác giả nào?', 'Phạm Nhật Vượng', 'Bill Gates', 'Steve Jobs', 'Shimon Peres', 4, 2),
(52, 'Ai là người phát minh ra máy bay?', 'Wright Brothers', 'Alexander Graham Bell', 'Thomas Edison', 'Nikola Tesla', 1, 2),
(53, 'Mặt trời là gì?', 'Một hành tinh', 'Một ngôi sao', 'Một vệ tinh', 'Một tiểu hành tinh', 2, 1),
(54, 'Quốc gia nào có nhiều kim tự tháp nhất thế giới?', 'Ai Cập', 'Mexico', 'Peru', 'Sudan', 4, 3),
(55, 'Thành phố nào được biết đến với tên gọi \'Thành phố Vĩnh cửu\'?', 'Rome', 'Athens', 'Jerusalem', 'Istanbul', 1, 1),
(56, 'Ai là tác giả của tác phẩm \'Don Quixote\'?', 'Leo Tolstoy', 'Miguel de Cervantes', 'William Shakespeare', 'Victor Hugo', 2, 2),
(57, 'Nước nào có diện tích nhỏ nhất thế giới?', 'Monaco', 'Vatican', 'San Marino', 'Liechtenstein', 2, 1),
(58, '\'Michelangelo\' là tên của ai?', 'Một nghệ sĩ', 'Một nhà khoa học', 'Một nhà văn', 'Một nhà phát minh', 1, 1),
(59, 'Quốc gia nào nổi tiếng với vũ điệu Tango?', 'Brazil', 'Argentina', 'Spain', 'Portugal', 2, 1),
(60, 'Ai đã viết cuốn tiểu thuyết \'Moby-Dick\'?', 'Herman Melville', 'Nathaniel Hawthorne', 'Mark Twain', 'Edgar Allan Poe', 1, 2),
(61, '\'Eiffel\' là tên của gì?', 'Một cây cầu', 'Một ngọn núi', 'Một công viên', 'Một tháp', 4, 1),
(62, 'Quốc gia nào có số lượng người nói tiếng Tây Ban Nha nhiều nhất?', 'Mexico', 'Tây Ban Nha', 'Argentina', 'Colombia', 1, 3),
(63, 'Ai đã phát minh ra máy ảnh?', 'Alexander Graham Bell', 'Thomas Edison', 'George Eastman', 'Joseph Nicéphore Niépce', 4, 3),
(64, '\'World Wide Web\' được phát minh bởi ai?', 'Tim Berners-Lee', 'Bill Gates', 'Steve Jobs', 'Mark Zuckerberg', 1, 2),
(65, 'Loài động vật nào có thể sống lâu nhất?', 'Rùa biển', 'Cá voi', 'Voi', 'Chim hải âu', 1, 3),
(66, '\'Google\' được sáng lập bởi ai?', 'Larry Page và Sergey Brin', 'Bill Gates', 'Steve Jobs', 'Mark Zuckerberg', 1, 1),
(67, 'Cơ quan nào chịu trách nhiệm về tiền tệ của Mỹ?', 'Bộ Tài chính', 'Ngân hàng Trung ương', 'Cục Dự trữ Liên bang', 'Cục Thuế vụ', 3, 2),
(68, 'Thành phố nào là thủ đô của Úc?', 'Sydney', 'Melbourne', 'Canberra', 'Brisbane', 3, 1),
(69, 'Ai là người phát minh ra máy điện thoại di động đầu tiên?', 'Martin Cooper', 'Steve Jobs', 'Bill Gates', 'Alexander Graham Bell', 1, 3),
(70, 'Quốc gia nào nổi tiếng với món pizza?', 'Tây Ban Nha', 'Pháp', 'Italia', 'Hy Lạp', 3, 1),
(71, 'Loài động vật nào được gọi là \'chúa tể rừng xanh\'?', 'Hổ', 'Sư tử', 'Báo', 'Voi', 2, 1),
(72, 'Vị vua nào được mệnh danh là \'Vua Bất Tử\' trong lịch sử Việt Nam?', 'Lý Thái Tổ', 'Trần Nhân Tông', 'Lê Lợi', 'Quang Trung', 4, 3),
(73, 'Eureka\' là câu nói nổi tiếng của nhà khoa học nào?', 'Isaac Newton', 'Archimedes', 'Galileo Galilei', 'Albert Einstein', 2, 1),
(74, 'Loài cá nào có thể sống trên cạn?', 'Cá vàng', 'Cá heo', 'Cá lóc', 'Cá rô phi', 3, 3),
(75, 'Ai là người phát minh ra phương trình x^2 + y^2 = r^2?', 'Pythagoras', 'Euclid', 'Diophantus', 'Galileo', 1, 2),
(76, 'Thành phố nào là thủ đô của Canada?', 'Toronto', 'Vancouver', 'Ottawa', 'Montreal', 3, 1),
(77, 'Sherlock Holmes\' là nhân vật do ai sáng tạo?', 'Agatha Christie', 'Arthur Conan Doyle', 'J.K. Rowling', 'Dan Brown', 2, 1),
(78, 'Quốc gia nào có dân số đông nhất thế giới?', 'Ấn Độ', 'Trung Quốc', 'Mỹ', 'Indonesia', 2, 1),
(79, 'Huyền thoại Rồng\' là bộ truyện tranh của nước nào?', 'Nhật Bản', 'Hàn Quốc', 'Trung Quốc', 'Việt Nam', 4, 2),
(80, 'Loại vật liệu nào được gọi là \'thép xanh\'?', 'Đồng', 'Nhôm', 'Thép không gỉ', 'Thép xanh', 4, 2),
(81, 'Bộ phim nào đã đoạt giải Oscar cho Phim hay nhất năm 2018?', 'La La Land', 'Moonlight', 'The Shape of Water', 'Green Book', 3, 2),
(82, 'Ai là người sáng lập ra hãng xe hơi Ford?', 'Henry Ford', 'Karl Benz', 'Enzo Ferrari', 'Ferdinand Porsche', 1, 2),
(83, 'Ngày của mẹ được tổ chức vào tháng nào?', 'Tháng 2', 'Tháng 5', 'Tháng 6', 'Tháng 10', 2, 1),
(84, 'Thành phố nào có tên gọi là \'Big Apple\'?', 'Los Angeles', 'Miami', 'New York', 'San Francisco', 3, 1),
(85, 'Khoa học nào nghiên cứu về sự sống dưới nước?', 'Sinh học biển', 'Hải dương học', 'Thủy sinh học', 'Động vật học', 1, 3),
(86, 'Ai là tổng thống đầu tiên của nước Việt Nam?', 'Lê Duẩn', 'Trường Chinh', 'Hồ Chí Minh', 'Tôn Đức Thắng', 3, 1),
(87, 'Phú Quốc\' là một hòn đảo thuộc tỉnh nào của Việt Nam?', 'Kiên Giang', 'Khánh Hòa', 'Bình Thuận', 'Bà Rịa - Vũng Tàu', 1, 1),
(88, 'Nước nào có nền văn hóa cà phê phong phú nhất?', 'Colombia', 'Brazil', 'Ethiopia', 'Việt Nam', 2, 2),
(89, 'Ai là người phát minh ra máy phát thanh?', 'Nikola Tesla', 'Heinrich Hertz', 'Alexander Graham Bell', 'Guglielmo Marconi', 4, 3),
(90, 'Tháp nghiêng Pisa\' nằm ở nước nào?', 'Pháp', 'Tây Ban Nha', 'Ý', 'Đức', 3, 1),
(91, 'Quốc gia nào tổ chức World Cup 2014?', 'Nam Phi', 'Brazil', 'Đức', 'Nga', 2, 1),
(92, 'Ai là người sáng lập ra tập đoàn Alibaba?', 'Jack Ma', 'Jeff Bezos', 'Richard Branson', 'Elon Musk', 1, 2),
(93, 'Tỉnh nào ở Việt Nam có nhiều danh thắng \'Tràng An\'?', 'Ninh Bình', 'Quảng Ninh', 'Thái Nguyên', 'Hà Giang', 1, 2),
(94, 'Loài chim nào không biết bay?', 'Đại bàng', 'Công', 'Đà điểu', 'Cánh cụt', 4, 1),
(95, 'Tác giả của cuốn sách \'Nhà giả kim\' là ai?', 'Gabriel Garcia Marquez', 'Paulo Coelho', 'Haruki Murakami', 'Dan Brown', 2, 2),
(96, 'Oktoberfest\' là lễ hội nổi tiếng của quốc gia nào?', 'Áo', 'Hà Lan', 'Đức', 'Thụy Sĩ', 3, 1),
(97, 'Quốc gia nào có diện tích lớn nhất châu Phi?', 'Nam Phi', 'Algeria', 'Nigeria', 'Sudan', 2, 3),
(98, 'Thành phố nào được mệnh danh là \'Thành phố của gió\'?', 'Chicago', 'San Francisco', 'Dallas', 'Houston', 1, 1),
(99, 'Loại trái cây nào chứa nhiều Vitamin C nhất?', 'Táo', 'Chuối', 'Cam', 'Dâu tây', 3, 1),
(100, 'Ai là tác giả của cuốn tiểu thuyết \'Lolita\'?', 'Vladimir Nabokov', 'Leo Tolstoy', 'Fyodor Dostoevsky', 'Victor Hugo', 1, 3);

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `players`
--
ALTER TABLE `players`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `questions`
--
ALTER TABLE `questions`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `players`
--
ALTER TABLE `players`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
