# Ai La Trieu Phu

## Giới thiệu
"Ai Là Triệu Phú" là một trò chơi mô phỏng dựa trên gameshow nổi tiếng cùng tên, được phát triển để cung cấp trải nghiệm giải trí và kiểm tra kiến thức cho người chơi. Trò chơi được xây dựng bằng ngôn ngữ lập trình Java, với giao diện đồ họa thân thiện và dễ sử dụng.

## Các tính năng chính
- **Câu hỏi đa dạng:** Hệ thống câu hỏi phong phú, trải rộng nhiều lĩnh vực kiến thức.
- **Giao diện thân thiện:** Giao diện người dùng dễ hiểu và dễ thao tác.
- **Chức năng trợ giúp:** Bao gồm các trợ giúp như 50:50, hỏi ý kiến khán giả, gọi điện cho người thân.
- **Âm thanh sống động:** Hiệu ứng âm thanh mô phỏng chương trình thực tế.

## Công nghệ sử dụng
- **Ngôn ngữ lập trình:** Java
- **Thư viện giao diện:** Java Swing
- **Cơ sở dữ liệu:** MySQL (dùng để lưu trữ câu hỏi và thông tin người chơi)

## Yêu cầu hệ thống
- **Hệ điều hành:** Windows, macOS, Linux
- **Java Runtime Environment (JRE):** Phiên bản 8 trở lên
- **MySQL Server:** Phiên bản 5.7 trở lên

## Hướng dẫn cài đặt
1. Clone dự án về máy:
   ```bash
   git clone https://github.com/ngcvien/AiLaTrieuPhu.git
   ```
2. Import dự án vào IDE (IntelliJ IDEA, Eclipse, hoặc NetBeans).
3. Cài đặt MySQL và tạo cơ sở dữ liệu:
   - Import file `database.sql` trong thư mục dự án vào MySQL.
4. Cấu hình kết nối cơ sở dữ liệu trong file `config.properties`.
5. Chạy dự án từ IDE hoặc sử dụng câu lệnh:
   ```bash
   java -jar AiLaTrieuPhu.jar
   ```

## Hướng dẫn chơi
1. Khởi động trò chơi.
2. Đọc và trả lời câu hỏi được hiển thị trên màn hình.
3. Sử dụng các trợ giúp khi cần thiết để đạt số điểm cao nhất.
4. Trả lời đúng tất cả 15 câu hỏi để trở thành "Triệu Phú"!

## Đóng góp
Nếu bạn muốn đóng góp cho dự án, hãy làm theo các bước sau:
1. Fork dự án.
2. Tạo nhánh mới cho tính năng hoặc sửa lỗi bạn muốn thêm:
   ```bash
   git checkout -b feature/ten-tinh-nang
   ```
3. Commit thay đổi và đẩy lên repository của bạn:
   ```bash
   git push origin feature/ten-tinh-nang
   ```
4. Tạo Pull Request để yêu cầu tích hợp vào dự án chính.


## Liên hệ
Nếu bạn có bất kỳ câu hỏi hoặc góp ý nào, vui lòng liên hệ qua email: [vienhn.24ic@vku.udn.vn](mailto:vienhn.24ic@vku.udn.vn).
