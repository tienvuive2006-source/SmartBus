package com.smartbus.booking.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // Cho phép Frontend bất kỳ cổng nào (như 5174) có thể truy cập trong quá trình dev
public class HealthCheckController {

    @GetMapping("/health")
    public Map<String, Object> checkHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Hệ thống Backend Spring Boot đang hoạt động ổn định!");
        status.put("timestamp", LocalDateTime.now());
        status.put("environment", "Development");
        status.put("project", "SkyBus - Graduation Project");
        return status;
    }
}
