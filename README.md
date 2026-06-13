# 🚌 SmartBus - Hệ Thống Đặt Vé Xe Khách Trung - Nam

Chào mừng bạn đến với dự án SmartBus! Đây là hệ thống quản lý và đặt vé xe khách hiện đại được xây dựng bằng Spring Boot (Backend) và Vue 3 (Frontend).

## 🛠 Yêu Cầu Hệ Thống
Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt:
- **Java 21** hoặc mới hơn.
- **Node.js** (Phiên bản LTS).
- **Microsoft SQL Server** (SSMS).
- **Maven** (Cần cài đặt Maven trên máy và cấu hình biến môi trường PATH).

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Dự Án

### 1. Tải dự án về máy
```bash
git clone https://github.com/tienvuive2006-source/SmartBus.git
cd SmartBus
```

### 2. Cấu hình & Chạy Backend (Java Spring Boot)
1. Mở Microsoft SQL Server Management Studio (SSMS) và tạo một database mới tên là: `smart_bus_booking`.
2. Truy cập vào file: `backend/src/main/resources/application.properties`.
3. Cập nhật `username` và `password` SQL Server của bạn (nếu dùng tài khoản sa, hoặc sửa cổng nếu khác 1433):
   ```properties
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   ```
4. Để sử dụng tính năng gửi Email (Nhận mã QR qua mail), hãy cập nhật `spring.mail.password` thành Mật khẩu ứng dụng Gmail của bạn.
5. Mở terminal tại thư mục `backend` và chạy lệnh:
   ```bash
   mvn spring-boot:run
   ```
   *(Lưu ý: Do dự án không đính kèm Maven Wrapper, bạn phải dùng lệnh `mvn` trực tiếp)*

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
- **Thanh toán tự động 100%**: Tích hợp SePay tự động nhận diện thanh toán chuyển khoản ngân hàng qua mã QR.
- **Hoàn tiền thông minh**: Tự động hoàn tiền 90% vào Ví điện tử khi khách hàng hủy vé và giải phóng ghế theo thời gian thực.
- **Quản lý lộ trình**: Bản đồ tương tác, tự động tìm tọa độ và chỉ đường thông minh.
- **Quản lý đội xe**: Upload hình ảnh xe thật lên Cloudinary.
- **Đặt vé trực tuyến**: Sơ đồ ghế ngồi động, gửi vé điện tử QR Code qua Email ngay khi thanh toán.
- **Giao diện Admin / Lơ xe**: Dashboard thống kê chuyên nghiệp, App lơ xe cho nhân viên soát vé quét mã QR lên xe.

---

## 📝 Ghi Chú cho Nhà Phát Triển
- Dự án sử dụng **Cloudinary** để lưu trữ ảnh. Nếu bạn muốn dùng tài khoản riêng, hãy cập nhật API Key trong code Frontend.
- Mọi thắc mắc vui lòng liên hệ qua GitHub Issue!

**Chúc bạn trải nghiệm dự án vui vẻ!** 🚌💨
