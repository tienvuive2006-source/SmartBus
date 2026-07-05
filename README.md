# 🎓 ĐỒ ÁN TỐT NGHIỆP: HỆ THỐNG QUẢN LÝ VÀ ĐẶT VÉ XE KHÁCH TRUNG - NAM (SMARTBUS)

<div align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Vue.js-35495E?style=for-the-badge&logo=vue.js&logoColor=4FC08D" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white" alt="Vercel"/>
  <img src="https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white" alt="Render"/>
</div>

---



---

## 📖 GIỚI THIỆU DỰ ÁN
**SmartBus** là một giải pháp chuyển đổi số toàn diện dành cho các nhà xe khách, cung cấp nền tảng web hiện đại để khách hàng dễ dàng tra cứu, đặt vé và thanh toán trực tuyến. Đồng thời, hệ thống cung cấp một trang quản trị (Admin Dashboard) mạnh mẽ giúp chủ nhà xe quản lý đội xe, tuyến đường, nhân sự và kiểm soát doanh thu một cách hiệu quả, tự động hóa cao.

Dự án được xây dựng với kiến trúc Client-Server, API RESTful hiện đại, đáp ứng tốt các yêu cầu về tính chính xác, hiệu năng, bảo mật và trải nghiệm người dùng (UX/UI) mượt mà.

---

## 💻 CÔNG NGHỆ SỬ DỤNG

### Backend (RESTful API)
- **Ngôn ngữ:** Java 21
- **Framework:** Spring Boot 3.x, Spring Data JPA
- **Bảo mật:** Spring Security, JWT Token (Authentication & Authorization)
- **Cơ sở dữ liệu:** PostgreSQL (Supabase)
- **Tiện ích:** Lombok, Maven, Jackson

### Frontend (User Interface)
- **Framework:** Vue 3 (Composition API)
- **Build Tool:** Vite
- **Quản lý trạng thái & Router:** Vue Router
- **Style:** CSS3 / Tailwind CSS (Giao diện đáp ứng - Responsive)

### Tích hợp Dịch vụ Bên ngoài (3rd Party Services)
- **Thanh toán tự động:** SePay API (Tự động nhận diện biến động số dư và trạng thái chuyển khoản ngân hàng theo thời gian thực)
- **Lưu trữ đám mây:** Cloudinary (Tối ưu hóa không gian lưu trữ hình ảnh xe, tuyến đường và thông tin hệ thống)
- **Trợ lý ảo AI Chatbot:** Cung cấp trải nghiệm hỗ trợ khách hàng thông minh, tư vấn lịch trình nhanh chóng
- **Email Service:** JavaMailSender (Hệ thống tự động gửi vé điện tử chứa mã QR, thông báo đặt/hủy vé tới email khách)

---

## 🌟 TÍNH NĂNG NỔI BẬT (CHI TIẾT TỪ A-Z)

### 1. Phân hệ Hệ Thống & Bảo Mật Chung
- **Xác thực & Phân quyền (Auth & Authorization):** Đăng nhập, đăng ký, đăng xuất an toàn với mã hóa mật khẩu (Bcrypt) và JSON Web Token (JWT).
- **Phân quyền chặt chẽ:** Tách biệt quyền truy cập theo 3 vai trò: Admin, User (Khách hàng), và Inspector (Lơ xe).
- **Ví điện tử nội bộ (E-Wallet):** Hệ thống ví tiền ảo tích hợp thẳng vào tài khoản người dùng để thanh toán nhanh hoặc nhận hoàn tiền.
- **Bảo mật giao dịch:** Chống đặt trùng ghế trong cùng một thời điểm (Concurrency Control).
- **Kiểm thử tự động (E2E Automation Testing):** Tích hợp công cụ Playwright (Microsoft) để tự động hóa kịch bản kiểm thử các luồng nghiệp vụ cốt lõi.

### 2. Phân hệ Khách hàng (Customer UI)
- **Trang chủ & Tra cứu chuyến đi:**
  - Tìm kiếm chuyến xe theo Điểm đi, Điểm đến và Ngày khởi hành.
  - Gợi ý các tuyến đường phổ biến bằng hình ảnh trực quan (được lưu trữ trên Cloudinary).
- **Kết quả tìm kiếm & Lọc:**
  - Hiển thị danh sách chuyến xe khả dụng.
  - Bộ lọc động theo loại xe, thời gian, giá tiền.
  - Cho phép thay đổi ngày xem chuyến xe bằng Date Picker mà không cần tải lại trang.
- **Sơ đồ ghế ngồi động (Dynamic Seat Map):**
  - Hiển thị sơ đồ ghế chính xác theo thiết kế của từng loại xe (Giường nằm 3 dãy, Limousine, Ghế ngồi...).
  - Cập nhật trạng thái ghế (Trống, Đang chọn, Đã bán) theo thời gian thực.
- **Thanh toán & Đặt vé (Checkout):**
  - Hỗ trợ đa dạng phương thức: Tiền mặt, Ví điện tử, Chuyển khoản ngân hàng.
  - **Auto-Payment (SePay):** Tự động sinh mã QR ngân hàng động chứa số tiền và nội dung chuyển khoản. Tự động nhận diện và chốt vé ngay khi khách hàng chuyển khoản thành công.
  - **Tùy chọn Email thông minh:** Tích hợp công tắc Bật/Tắt nhận vé qua Email, giúp khách hàng linh hoạt bỏ qua bước nhập Email để thanh toán cực nhanh.
- **Vé điện tử & Gửi Email:**
  - Hiển thị vé điện tử với mã QR sinh tự động.
  - Gửi thông tin vé và mã QR trực tiếp vào Email của khách hàng ngay sau khi thanh toán.
- **Quản lý Tài khoản cá nhân (Profile):**
  - Cập nhật thông tin cá nhân.
  - Xem số dư và quản lý Ví điện tử.
- **Lịch sử Đặt vé & Hoàn hủy (History):**
  - Xem danh sách vé đã đặt và trạng thái.
  - **Tự động hoàn tiền (Auto-Refund):** Cho phép khách hàng tự hủy vé (trước giờ khởi hành). Hệ thống tự động giải phóng ghế và hoàn trả 90% giá trị vé vào Ví điện tử của khách.
- **Đánh giá & Phản hồi (Reviews):**
  - Cho phép khách hàng chấm điểm (Rating) và để lại bình luận cho các chuyến xe đã hoàn thành.
- **Trợ lý ảo AI (Chatbot AI):**
  - Tích hợp khung chat AI hỗ trợ tư vấn lịch trình, chính sách nhà xe và giải đáp câu hỏi tự động.
- **Hệ thống thông báo (Notifications):** Theo dõi các cảnh báo hoặc cập nhật từ hệ thống.

### 3. Phân hệ Quản trị viên (Admin Panel)
- **Bảng điều khiển (Admin Dashboard):**
  - Giao diện thiết kế cao cấp (Emerald/Gold Theme) với thanh điều hướng (Sidebar) thông minh.
  - Thống kê tổng quan bằng biểu đồ: Tổng doanh thu, Số vé bán ra, Tỉ lệ lấp đầy, Số lượng khách hàng.
- **Bản đồ Giám sát Tài xế (Real-time Driver Map):**
  - Theo dõi vị trí và trạng thái của toàn bộ tài xế theo thời gian thực trên bản đồ số.
  - Tích hợp thuật toán Spiderfy xử lý hiển thị chống trùng lặp (overlap) tại các trạm trung chuyển.
- **Phân công Lịch trình Tài xế (Driver Scheduling):**
  - Tách biệt quản lý phương tiện (Vehicle) và tài xế (Driver).
  - Thuật toán tự động phát hiện xung đột thời gian (Collision Detection), chặn giao việc trùng lặp.
  - Gửi thông báo (Notification) thay đổi lịch trình ngay lập tức đến ứng dụng của tài xế.
- **Quản lý Tuyến đường (Route Manager):**
  - Thêm, sửa, xóa các điểm đi/đến.
  - Tích hợp chuỗi tọa độ (Polyline/Geometry) để vẽ bản đồ lộ trình (lưu file chuỗi lên Cloudinary để tối ưu hóa DB).
  - Tải ảnh đại diện cho các tuyến đường phổ biến.
- **Quản lý Chuyến xe (Trip Manager):**
  - Thiết lập lịch trình cụ thể: Chọn tuyến đường, gán Loại xe, cài đặt ngày giờ khởi hành, giá vé.
  - Quản lý trạng thái chuyến: Đang lên lịch (Scheduled), Đang chạy (In Progress), Đã hoàn thành, Đã hủy.
  - Phân công Lơ xe (Inspector) và Tài xế (Driver) cho từng chuyến.
- **Quản lý Loại xe & Đội xe (Bus/Fleet Manager):**
  - Định nghĩa các loại xe: Số lượng ghế, sơ đồ phân bố (Layout).
  - Tải và lưu trữ ảnh thực tế của phương tiện.
- **Quản lý Đặt vé (Booking Manager):**
  - Xem toàn bộ danh sách đơn đặt vé của khách hàng theo thời gian thực.
  - Bộ lọc tìm kiếm vé theo mã vé, số điện thoại, trạng thái.
  - Xuất báo cáo danh sách đặt vé sang định dạng **PDF**.
- **Quản lý Người dùng (User Manager):**
  - Bố cục quản lý động theo từng nhóm (Khách hàng, Tài xế, Lơ xe, Admin).
  - Xem danh sách toàn bộ người dùng, số dư ví điện tử của từng người.
  - Phân quyền tài khoản, Xóa/Khóa tài khoản.
- **Quản lý Đánh giá (Review Manager):**
  - Kiểm duyệt phản hồi của khách hàng.
  - Xóa hoặc ẩn các đánh giá không phù hợp.
- **Nhật ký Hoạt động & Báo lỗi (Audit Logs & Diagnostic):**
  - Ứng dụng AOP (Aspect-Oriented Programming) tự động ghi vết (Log) toàn bộ lịch sử thao tác thay đổi dữ liệu của Quản trị viên.
  - Tự động bắt lỗi toàn cục (Global Exception Handling) và đánh dấu cảnh báo đỏ (System Error) trên Dashboard để phát hiện và gỡ lỗi nhanh chóng.

### 4. Phân hệ Tài xế (Driver Workspace)
- **Quản lý Lịch trình Chủ động:** Xem danh sách chuyến xe được phân công, lịch chạy sắp tới trong ngày.
- **Nhận cảnh báo & Thông báo Thời gian thực:** Nhận thông báo tự động (Push Notification) khi có thay đổi lịch trình từ Admin.
- **Cập nhật Hành trình:** Chuyển đổi trạng thái chuyến đi, đồng bộ tín hiệu định vị trực tiếp về bản đồ điều hành của Trung tâm.

### 5. Phân hệ Lơ xe / Nhân viên soát vé (Inspector)
- **Bảng điều khiển Lơ xe (Inspector Dashboard):**
  - Xem danh sách các chuyến xe được phân công phụ trách trong ngày.
- **Sơ đồ chuyến đi trực tiếp (Trip Check-in):**
  - Xem tổng quan sơ đồ ghế của chuyến xe đang chạy để biết ghế nào trống, ghế nào có khách.
- **Quét mã QR Vé điện tử (QR Scanner):**
  - Bật camera thiết bị di động (điện thoại/tablet) để quét mã QR vé do khách hàng cung cấp.
  - Tự động nhận diện tính hợp lệ của vé và cập nhật trạng thái "Đã lên xe" (CHECKED_IN).

### 6. Cải tiến Kỹ thuật & Tối ưu Trải nghiệm (Mới cập nhật)
- **Thiết kế Thích ứng (Responsive UI/UX):** 
  - Giao diện đặt vé (Trip Cards) và thanh điều hướng (Navbar) tự động co giãn và hiển thị tối ưu trên đa màn hình (Điện thoại, Tablet, PC).
  - Tối ưu hóa trạng thái cuộn (Scroll state) và hiển thị thông minh các thành phần như Chuông thông báo (Notification Bell).
- **Hệ thống Thông báo & Email Mượt mà:**
  - Hoàn thiện luồng xác nhận đặt vé tự động qua Email (EmailService).
  - Giải quyết triệt để lỗi đồng bộ thời gian thực (Reflection / `-parameters`) trong Spring Boot giúp tín hiệu WebSockets hoạt động ổn định.
- **Tương thích Hệ thống Cũ (Legacy PHP System):**
  - Cấu hình và hỗ trợ chạy song song với mã nguồn PHP 5.6 (XAMPP) cũ của nhà xe, giải quyết các lỗi thẻ mở ngắn (`short_open_tag`) và nâng cấp dần lên nền tảng mới.

---

## ⚙️ HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY DỰ ÁN (LOCAL)

### Yêu cầu môi trường:
- **Java 21+**
- **Node.js LTS**
- **Maven**
- Cài đặt **PostgreSQL** (hoặc dùng URL kết nối thẳng đến Supabase PostgreSQL)

### 1. Tải dự án về máy
```bash
git clone https://github.com/tienvuive2006-source/SmartBus.git
cd SmartBus
```

### 2. Khởi chạy Backend (Spring Boot)
1. Mở file cấu hình tại `backend/src/main/resources/application.properties`.
2. Đảm bảo cấu hình đúng chuỗi kết nối Database (PostgreSQL/Supabase) cùng các API Key liên quan (Cloudinary, JWT Secret, JavaMail Password).
3. Mở terminal tại thư mục `backend` và chạy lệnh:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   *Backend sẽ khởi chạy và lắng nghe tại cổng `http://localhost:8080`.*

### 3. Khởi chạy Frontend (Vue 3)
1. Mở terminal mới tại thư mục `frontend`.
2. Cài đặt các gói phụ thuộc (Dependencies):
   ```bash
   npm install
   ```
3. Chạy dự án ở chế độ Development:
   ```bash
   npm run dev
   ```
4. Truy cập vào `http://localhost:5173` trên trình duyệt để trải nghiệm ứng dụng.

---

## 🚀 TRIỂN KHAI THỰC TẾ (DEPLOYMENT)
Dự án đã được triển khai (Deploy) thành công lên môi trường Cloud thực tế:
- **Frontend:** Lưu trữ và tự động triển khai tĩnh trên nền tảng **Vercel** (Tham khảo: [smart-bus-navy.vercel.app](https://smart-bus-navy.vercel.app/))
- **Backend:** Chạy dưới dạng Web Service 24/7 trên **Render**
- **Database:** Hoạt động ổn định với Cloud PostgreSQL của **Supabase**

---

## 📞 LIÊN HỆ
Dự án được xây dựng và phát triển với tâm huyết phục vụ báo cáo Đồ án Tốt nghiệp.
Mọi góp ý, thắc mắc về luồng nghiệp vụ hoặc kiến trúc hệ thống, xin vui lòng tạo Issue trên kho lưu trữ GitHub này.

