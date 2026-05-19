# 🚌 SmartBus - Hệ Thống Đặt Vé Xe Khách Trung - Nam

Chào mừng bạn đến với dự án SmartBus! Đây là hệ thống quản lý và đặt vé xe khách hiện đại được xây dựng bằng Spring Boot (Backend) và Vue 3 (Frontend).

## 🛠 Yêu Cầu Hệ Thống
Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt:
- **Java 17** hoặc mới hơn.
- **Node.js** (Phiên bản LTS).
- **MySQL Server** (Hoặc XAMPP/WampServer).
- **Maven** (Đã tích hợp sẵn trong thư mục backend qua `mvnw`).

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Dự Án

### 1. Tải dự án về máy
```bash
git clone https://github.com/tienvuive2006-source/SmartBus.git
cd SmartBus
```

### 2. Cấu hình & Chạy Backend (Java Spring Boot)
1. Mở MySQL và tạo một database mới tên là: `smart_bus_db`.
2. Truy cập vào file: `backend/src/main/resources/application.properties`.
3. Cập nhật `username` và `password` MySQL của bạn:
   ```properties
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   ```
4. Mở terminal tại thư mục `backend` và chạy lệnh:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(Nếu dùng Windows, hãy dùng lệnh `mvnw.cmd spring-boot:run`)*

### 3. Cài đặt & Chạy Frontend (Vue 3 + Vite)
1. Mở một terminal mới tại thư mục `frontend`.
2. Cài đặt các thư viện cần thiết:
   ```bash
   npm install
   ```
3. Chạy dự án ở chế độ phát triển:
   ```bash
   npm run dev
   ```
4. Truy cập vào đường dẫn hiện ra (thường là `http://localhost:5173`) để trải nghiệm!

---

## 📸 Tính Năng Nổi Bật
- **Quản lý lộ trình**: Bản đồ tương tác, tự động tìm tọa độ và chỉ đường thông minh.
- **Quản lý đội xe**: Upload hình ảnh xe thật lên Cloudinary.
- **Đặt vé trực tuyến**: Sơ đồ ghế ngồi thời gian thực, đồng bộ hóa trạng thái đặt chỗ.
- **Giao diện Admin**: Dashboard thống kê chuyên nghiệp, quản lý người dùng và doanh thu.

---

## 📝 Ghi Chú cho Nhà Phát Triển
- Dự án sử dụng **Cloudinary** để lưu trữ ảnh. Nếu bạn muốn dùng tài khoản riêng, hãy cập nhật API Key trong code Frontend.
- Mọi thắc mắc vui lòng liên hệ qua GitHub Issue!

**Chúc bạn trải nghiệm dự án vui vẻ!** 🚌💨
