package com.smartbus.booking.controller;

import com.smartbus.booking.config.JwtService;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    // ============================================================
    // API ĐĂNG KÝ
    // ============================================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User userRequest) {
        // Kiểm tra số điện thoại đã tồn tại chưa
        if (userRepository.findByPhone(userRequest.getPhone()).isPresent()) {
            return ResponseEntity.badRequest().body("Số điện thoại này đã được đăng ký tài khoản khác!");
        }

        // Tạo user mới với mật khẩu đã được BCrypt hash
        User newUser = User.builder()
                .phone(userRequest.getPhone())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .fullName(userRequest.getFullName())
                .role("USER")
                .walletBalance(500000.0) // Khuyến mãi 500k vào ví mới tạo!
                .build();

        userRepository.save(newUser);

        // Tạo JWT token ngay sau khi đăng ký thành công
        String token = jwtService.generateToken(
                newUser.getId(),
                newUser.getPhone(),
                newUser.getRole(),
                newUser.getFullName()
        );

        return ResponseEntity.ok(buildAuthResponse(newUser, token));
    }

    // ============================================================
    // API ĐĂNG NHẬP
    // ============================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        Optional<User> userOpt = userRepository.findByPhone(loginRequest.getPhone());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Không tìm thấy số điện thoại này trên hệ thống!");
        }

        User user = userOpt.get();

        // Kiểm tra mật khẩu với BCrypt
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Mật khẩu nhập vào chưa chính xác!");
        }

        // Tạo JWT token
        String token = jwtService.generateToken(
                user.getId(),
                user.getPhone(),
                user.getRole(),
                user.getFullName()
        );

        return ResponseEntity.ok(buildAuthResponse(user, token));
    }

    // ============================================================
    // API LẤY THÔNG TIN USER TỪ TOKEN (ME)
    // ============================================================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token không hợp lệ!");
        }

        try {
            String token = authHeader.substring(7);
            String phone = jwtService.extractPhone(token);
            Optional<User> userOpt = userRepository.findByPhone(phone);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Không tìm thấy người dùng!");
            }

            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "phone", user.getPhone(),
                    "fullName", user.getFullName(),
                    "role", user.getRole(),
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "walletBalance", user.getWalletBalance()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token đã hết hạn hoặc không hợp lệ!");
        }
    }

    // ============================================================
    // API LẤY LỊCH SỬ ĐẶT VÉ CỦA USER TỪ TOKEN (ME)
    // ============================================================
    @Autowired
    private com.smartbus.booking.repository.BookingRepository bookingRepository;

    @GetMapping("/me/bookings")
    public ResponseEntity<?> getMyBookings(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token không hợp lệ!");
        }

        try {
            String token = authHeader.substring(7);
            String phone = jwtService.extractPhone(token);
            Optional<User> userOpt = userRepository.findByPhone(phone);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Không tìm thấy người dùng!");
            }

            User user = userOpt.get();
            return ResponseEntity.ok(bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token đã hết hạn hoặc không hợp lệ!");
        }
    }

    // ============================================================
    // API HỦY VÉ & HOÀN TIỀN VÀO VÍ (ME)
    // ============================================================
    @Autowired
    private com.smartbus.booking.service.SeatService seatService;

    @PostMapping("/me/bookings/{id}/cancel")
    public ResponseEntity<?> cancelMyBooking(@PathVariable("id") Long id, @RequestBody Map<String, String> payload, @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token không hợp lệ!");
        }

        try {
            String token = authHeader.substring(7);
            String phone = jwtService.extractPhone(token);
            Optional<User> userOpt = userRepository.findByPhone(phone);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Không tìm thấy người dùng!");
            }

            User user = userOpt.get();
            Optional<com.smartbus.booking.entity.Booking> bookingOpt = bookingRepository.findById(id);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Không tìm thấy vé!");
            }
            com.smartbus.booking.entity.Booking booking = bookingOpt.get();
            
            // Check ownership
            if (booking.getUser() == null || !booking.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Bạn không có quyền hủy vé này!");
            }

            // Check status
            if (!"PAID".equals(booking.getStatus()) && !"PENDING".equals(booking.getStatus())) {
                return ResponseEntity.status(400).body("Vé này không ở trạng thái cho phép hủy!");
            }

            // Calculate refund if paid and not cash
            double refundAmount = 0.0;
            if ("PAID".equals(booking.getStatus()) && !"CASH".equals(booking.getPaymentMethod())) {
                refundAmount = booking.getTotalPrice() * 0.9;
                user.setWalletBalance(user.getWalletBalance() + refundAmount);
                userRepository.save(user);
            }

            // Free seats
            seatService.releaseSeats(booking.getTrip().getId(), booking.getSeatNumbers());

            // Update booking status
            booking.setStatus("CANCELLED");
            booking.setCancellationReason(payload.get("reason"));
            bookingRepository.save(booking);

            return ResponseEntity.ok(Map.of(
                    "message", "Hủy vé thành công!",
                    "refundAmount", refundAmount,
                    "walletBalance", user.getWalletBalance()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // ============================================================
    // Helper: Build response JSON
    // ============================================================
    private Map<String, Object> buildAuthResponse(User user, String token) {
        return Map.of(
                "token", token,
                "id", user.getId(),
                "phone", user.getPhone(),
                "fullName", user.getFullName(),
                "role", user.getRole(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "walletBalance", user.getWalletBalance()
        );
    }
}
