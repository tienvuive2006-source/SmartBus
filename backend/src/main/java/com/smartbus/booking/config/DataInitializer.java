package com.smartbus.booking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Tạo sẵn hoặc cập nhật lại mật khẩu Admin mặc định để luôn khớp BCrypt 123456
        Optional<User> adminOpt = userRepository.findByPhone("0909090909");
        if (adminOpt.isEmpty()) {
            User demoUser = User.builder()
                    .fullName("Khách Hàng VIP")
                    .phone("0909090909")
                    .password(passwordEncoder.encode("123456")) // Mật khẩu được BCrypt hash
                    .role("ADMIN")
                    .walletBalance(9999999.9)
                    .build();
            userRepository.save(demoUser);
            System.out.println("✅ Đã khởi tạo tài khoản Admin Demo! (SĐT: 0909090909 | MK: 123456)");
        } else {
            User admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode("123456")); // Đồng bộ lại mật khẩu BCrypt hash
            admin.setRole("ADMIN"); // Đảm bảo quyền admin
            userRepository.save(admin);
            System.out.println("✅ Đã đồng bộ/cập nhật mật khẩu tài khoản Admin Demo thành 123456 (BCrypt)!");
        }

        // 2. Tự động quét tất cả các user khác, nếu mật khẩu dạng plain-text (không bắt đầu bằng $2a$) thì tự động BCrypt hash lại
        for (User user : userRepository.findAll()) {
            String rawPassword = user.getPassword();
            if (rawPassword != null && !rawPassword.startsWith("$2a$")) {
                String encodedPassword = passwordEncoder.encode(rawPassword);
                user.setPassword(encodedPassword);
                userRepository.save(user);
                System.out.println("✅ Đã tự động chuyển đổi mật khẩu plain-text sang BCrypt cho tài khoản SĐT: " + user.getPhone() + " (" + rawPassword + " -> " + encodedPassword + ")");
            }
        }

        System.out.println("🚀 Hệ thống JWT + BCrypt khởi động thành công!");
    }
}
