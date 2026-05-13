# Hướng dẫn cấu trúc mã nguồn Backend (MVC Architecture)

Dự án của bạn được cấu trúc theo mô hình 3 lớp chuẩn mực để phục vụ đồ án tốt nghiệp:

### 📂 1. `entity/`
* **Vai trò:** Nơi định nghĩa các bảng cơ sở dữ liệu SQL.
* **Nhiệm vụ:** Mỗi class Java ở đây (ví dụ: `User`, `Ticket`, `Bus`) sẽ đại diện cho 1 bảng trong SQL Server. Dùng @Entity để Hibernate tự động sinh bảng.

### 📂 2. `repository/`
* **Vai trò:** Nơi truy vấn dữ liệu.
* **Nhiệm vụ:** Chứa các interface kế thừa từ `JpaRepository`. Giúp bạn tự động có sẵn các hàm: `findAll()`, `findById()`, `save()`, `delete()` mà không cần viết SQL.

### 📂 3. `service/`
* **Vai trò:** Nơi xử lý logic nghiệp vụ cốt lõi.
* **Nhiệm vụ:** Chứa thuật toán, tính toán giá vé, kiểm tra ghế trống, kiểm tra logic bảo mật trước khi đưa dữ liệu vào Repository.

### 📂 4. `controller/`
* **Vai trò:** Nơi nhận/phản hồi tín hiệu API.
* **Nhiệm vụ:** Dùng `@RestController`, nhận yêu cầu từ Frontend Vue 3 (qua Axios), gọi Service tương ứng để xử lý và trả kết quả JSON về cho Vue 3.

### 📂 5. `dto/` (Data Transfer Object)
* **Vai trò:** Trung chuyển dữ liệu tối giản.
* **Nhiệm vụ:** Đóng gói dữ liệu gọn gàng để truyền qua API, tránh làm lộ các thông tin nhạy cảm (ví dụ: mật khẩu, các quan hệ bảng phức tạp không cần thiết cho Frontend).
