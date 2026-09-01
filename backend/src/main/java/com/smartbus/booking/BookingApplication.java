package com.smartbus.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class BookingApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

	public static void main(String[] args) {
		// VS Code/DevTools đôi khi giữ RestartClassLoader cũ sau khi biên dịch,
		// làm các class hợp lệ như JwtAuthFilter bị báo ClassNotFoundException.
		// Chạy trực tiếp BookingApplication ổn định hơn khi tắt cơ chế restart này.
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(BookingApplication.class, args);
		System.out.println("=============================================");
		System.out.println("   🚀 SMART BUS BOOKING BACKEND IS READY!     ");
		System.out.println("   📡 REST API running at http://localhost:8080/api");
		System.out.println("=============================================");
	}

}
