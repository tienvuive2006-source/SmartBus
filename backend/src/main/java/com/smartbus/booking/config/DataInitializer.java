package com.smartbus.booking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Tạo sẵn 1 tài khoản Test mặc định cực tiện để test đăng nhập!
        if (userRepository.findByPhone("0909090909").isEmpty()) {
            User demoUser = User.builder()
                    .fullName("Khách Hàng VIP")
                    .phone("0909090909")
                    .password("123456")
                    .role("ADMIN")
                    .walletBalance(9999999.9)
                    .build();
            userRepository.save(demoUser);
            System.out.println("✅ Đã khởi tạo tài khoản Demo mặc định thành công! (SĐT: 0909090909 | MK: 123456)");
        }
        System.out.println("🚀 Hệ thống Database thật khởi động sạch sẽ - Sẵn sàng nhận dữ liệu!");
    }
}
