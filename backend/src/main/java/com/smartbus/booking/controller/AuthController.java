package com.smartbus.booking.controller;

import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    // API ĐĂNG KÝ
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User userRequest) {
        // Kiểm tra số điện thoại đã tồn tại chưa
        if (userRepository.findByPhone(userRequest.getPhone()).isPresent()) {
            return ResponseEntity.badRequest().body("Số điện thoại này đã được đăng ký tài khoản khác!");
        }

        // Khởi tạo người dùng mặc định
        User newUser = User.builder()
                .phone(userRequest.getPhone())
                .password(userRequest.getPassword()) // Mã hoá mật khẩu thực tế sẽ dùng BCrypt
                .fullName(userRequest.getFullName())
                .role("USER")
                .walletBalance(500000.0) // Khuyến mãi ngay 500k vào ví mới tạo cho oách!
                .build();

        userRepository.save(newUser);
        return ResponseEntity.ok(newUser);
    }

    // API ĐĂNG NHẬP
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        Optional<User> userOpt = userRepository.findByPhone(loginRequest.getPhone());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Không tìm thấy số điện thoại này trên hệ thống!");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(401).body("Mật khẩu nhập vào chưa chính xác!");
        }

        return ResponseEntity.ok(user);
    }
}
