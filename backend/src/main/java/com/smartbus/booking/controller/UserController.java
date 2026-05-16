package com.smartbus.booking.controller;

import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    // 🛡️ CONSTRUCTOR THUẦN TÚY (Pure Vanilla Java): Khỏi cần cài Lombok cũng chạy
    // cực êm!
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. Lấy toàn bộ danh sách Người dùng
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // 1.5 Lấy chi tiết 1 Người dùng duy nhất (Đồng bộ thời gian thực)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 2. Cập nhật thông tin Người dùng (Số dư ví, Quyền hạn)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody User userUpdates) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = userOpt.get();
        existingUser.setFullName(userUpdates.getFullName());
        existingUser.setPhone(userUpdates.getPhone());
        existingUser.setRole(userUpdates.getRole());
        existingUser.setWalletBalance(userUpdates.getWalletBalance());
        
        // Nếu có đổi mật khẩu mới (không trống)
        if (userUpdates.getPassword() != null && !userUpdates.getPassword().trim().isEmpty()) {
            existingUser.setPassword(userUpdates.getPassword());
        }

        User savedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(savedUser);
    }

    // 3. Xoá người dùng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("Xoá người dùng thành công!");
    }
}
