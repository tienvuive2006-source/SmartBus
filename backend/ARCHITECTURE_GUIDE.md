# Hướng dẫn cấu trúc mã nguồn Backend & Tiêu chuẩn Code (Clean Architecture)

Dự án SmartBus Backend được cấu trúc theo mô hình đa tầng (N-Tier Architecture / MVC) kết hợp với các nguyên tắc Clean Code để đảm bảo tính mở rộng, dễ bảo trì và phục vụ tốt cho đồ án tốt nghiệp.

Mọi thành viên khi code cần tuân thủ nghiêm ngặt vai trò của từng tầng dưới đây, **tuyệt đối không code vượt cấp** (ví dụ: Controller gọi thẳng Repository hoặc nhét logic tính toán vào Controller).

## 📂 Kiến trúc thư mục

### 1. `entity/` (Data Layer)
* **Vai trò:** Nơi định nghĩa các thực thể (Entities) ánh xạ trực tiếp với các bảng trong cơ sở dữ liệu PostgreSQL (Supabase).
* **Quy tắc:** Chỉ chứa các trường dữ liệu và Annotation của JPA/Hibernate (`@Entity`, `@Table`, `@Column`, `@ManyToOne`, v.v.). Không chứa bất kỳ logic tính toán nào ở đây.

### 2. `repository/` (Data Access Layer)
* **Vai trò:** Lớp giao tiếp trực tiếp với Database.
* **Quy tắc:** Chứa các interface kế thừa từ `JpaRepository`. Tuyệt đối không viết logic nghiệp vụ. Sử dụng `@Query` hoặc `@EntityGraph` để tối ưu hóa truy vấn, tránh lỗi N+1 Query.

### 3. `service/` (Business Logic Layer)
* **Vai trò:** Lớp chứa **toàn bộ logic nghiệp vụ (Business Logic)** cốt lõi của hệ thống.
* **Quy tắc:** 
  * Nơi thực hiện các thuật toán: tính giá vé, kiểm tra ghế trống, xử lý hoàn tiền, v.v.
  * Controller **bắt buộc** phải gọi qua Service, không được phép gọi thẳng Repository.
  * Tách biệt rõ ràng Interface Service và Class Impl (nếu dự án quy mô lớn) để dễ dàng viết Unit Test.

### 4. `controller/` (Presentation / API Layer)
* **Vai trò:** Lớp ngoài cùng giao tiếp với Frontend (Vue 3) thông qua RESTful API.
* **Quy tắc nghiêm ngặt:** 
  * **CHỈ** làm nhiệm vụ: Nhận request (DTO), validate dữ liệu đầu vào, gọi `Service` tương ứng, và trả về Response JSON (DTO).
  * **CẤM** viết các vòng lặp xử lý dữ liệu, cấm gọi trực tiếp `Repository`, cấm tính toán giá tiền hay gửi email trực tiếp từ Controller.

### 5. `dto/` (Data Transfer Object)
* **Vai trò:** Đối tượng chuyên chở dữ liệu giữa Client và Server.
* **Quy tắc:** Nhận Request (RequestDTO) và trả về Response (ResponseDTO). Giúp bảo mật dữ liệu, ẩn đi các thông tin nhạy cảm của Entity (như password, cấu trúc bảng).

### 6. `exception/` (Global Error Handling)
* **Vai trò:** Xử lý ngoại lệ (Exception) tập trung toàn cục.
* **Quy tắc:** Chứa `GlobalExceptionHandler` (sử dụng `@ControllerAdvice`). Thay vì `try-catch` thủ công ở mọi Controller và ném lỗi 500 kèm nội dung thô ra ngoài (gây rò rỉ bảo mật), tất cả lỗi sẽ được gom về đây để định dạng lại thành mã lỗi chuẩn trước khi trả về Frontend.

### 7. `config/`
* **Vai trò:** Chứa các cấu hình của hệ thống (Spring Security, JWT, CORS, WebSocket, Redis/Cache nếu có).
* **Quy tắc:** Gom nhóm các `@Configuration` vào đây để dễ quản lý, tránh vứt lung tung ở thư mục gốc.

### 8. `annotation/` & `aspect/` (AOP - Aspect Oriented Programming)
* **Vai trò:** Chứa các Annotation tự định nghĩa (ví dụ: `@AuditAction`) và các logic cắt ngang (Cross-cutting concerns) như ghi log tự động (Audit Logging).
* **Quy tắc:** Dùng để giảm thiểu code lặp lại. Thay vì gọi hàm `logService.save()` ở 100 nơi, chỉ cần gắn `@AuditAction` lên method cần log.

---

## 🛑 Những "Anti-pattern" (Lỗi tối kỵ) cần tránh
1. **Fat Controller:** Viết quá 50 dòng code trong 1 hàm của Controller. Việc xử lý nghiệp vụ, DB phải đẩy xuống Service.
2. **Cứng hóa (Hardcode):** Viết cứng giá tiền (ví dụ: `500000.0`), danh sách tỉnh thành hoặc secret key trực tiếp vào file Java. Phải đưa vào Database hoặc cấu hình.
3. **Tiêm phụ thuộc sai cách:** Lạm dụng `@Autowired` trên field. Khuyến khích dùng Constructor Injection (hoặc `@RequiredArgsConstructor` của Lombok).
4. **Trả lỗi thô (Raw Exception):** Trả thẳng `e.getMessage()` của SQL/Hệ thống cho Frontend. Phải bọc lại bằng Custom Exception.

Tuân thủ nghiêm ngặt tài liệu này sẽ giúp hệ thống SmartBus đạt tiêu chuẩn công nghiệp và dễ dàng bảo vệ trước bất kỳ hội đồng đánh giá nào.
