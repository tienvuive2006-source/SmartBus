import { test, expect } from '@playwright/test';

// -----------------------------------------------------------------
// KỊCH BẢN 1: ĐĂNG NHẬP ADMIN & KIỂM TRA TRANG QUẢN TRỊ (RẤT ỔN ĐỊNH)
// -----------------------------------------------------------------
test('Kịch bản 1: Đăng nhập Admin và xem Nhật ký hoạt động', async ({ page }) => {
  // 1. Mở trang chủ
  await page.goto('http://localhost:5173/');

  // 2. Bấm vào nút Đăng nhập trên góc phải (Icon hình người)
  await page.getByRole('button', { name: 'person_outline' }).click();

  // 3. Điền thông tin tài khoản Admin
  await page.getByRole('textbox', { name: 'Nhập số điện thoại của bạn' }).fill('admin');
  await page.getByRole('textbox', { name: 'Nhập mật khẩu bảo mật' }).fill('123456');
  
  // 4. Bấm nút Đăng nhập
  await page.getByRole('button', { name: 'ĐĂNG NHẬP NGAY' }).click();

  // 5. Kiểm tra xem có chuyển hướng vào được Trang Quản trị không
  // Chờ cho chữ "Tổng quan" xuất hiện ở menu bên trái
  await expect(page.getByText('Tổng quan').first()).toBeVisible({ timeout: 10000 });

  // 6. Chuyển sang Tab Nhật ký Hoạt động
  await page.getByRole('link', { name: 'Nhật ký Hoạt động' }).click();

  // 7. Chờ xem cái Tab "Lỗi hệ thống" (tab màu đỏ) có xuất hiện không
  await expect(page.getByRole('button', { name: 'warning Lỗi hệ thống' })).toBeVisible();
});

// -----------------------------------------------------------------
// KỊCH BẢN 2: KHÁCH HÀNG TÌM KIẾM CHUYẾN XE (KHÔNG ĐẶT VÉ ĐỂ TRÁNH LỖI HẾT GHẾ)
// -----------------------------------------------------------------
test('Kịch bản 2: Khách hàng tìm chuyến xe từ Đà Nẵng đi Quảng Nam', async ({ page }) => {
  // 1. Mở trang chủ
  await page.goto('http://localhost:5173/');

  // 2. Điền thông tin tìm kiếm: Điểm đi và Điểm đến (Dùng Regex để tìm khớp chữ)
  // Lấy ô chọn Điểm Khởi Hành (Có thể phải điều chỉnh tuỳ theo thiết kế UI hiện tại)
  const departureInput = page.getByPlaceholder('Tìm điểm khởi hành...');
  await departureInput.fill('Đà Nẵng');
  await page.getByText('Bến xe Trung tâm Đà Nẵng').click();

  const arrivalInput = page.getByPlaceholder('Tìm điểm đến...');
  await arrivalInput.fill('Quảng Nam');
  await page.getByText('Bến xe Tam Kỳ, Quảng Nam').click();

  // 3. Bấm nút Tìm Chuyến
  await page.getByRole('button', { name: 'Tìm Chuyến Xe' }).click();

  // 4. Đợi trang Kết quả tìm kiếm tải xong (Kiểm tra chữ "Lọc kết quả" xuất hiện)
  await expect(page.getByText('Lọc kết quả')).toBeVisible({ timeout: 10000 });

  // 5. Kiểm tra xem có Nút "Chọn chỗ" nào xuất hiện không (Tức là có xe chạy)
  const chonChoButtons = page.getByRole('button', { name: 'Chọn chỗ' });
  await expect(chonChoButtons.first()).toBeVisible();
});