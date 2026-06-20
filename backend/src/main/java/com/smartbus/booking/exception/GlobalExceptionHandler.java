package com.smartbus.booking.exception;

import com.smartbus.booking.entity.AuditLog;
import com.smartbus.booking.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AuditLogRepository auditLogRepository;

    public GlobalExceptionHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        // Bỏ qua lỗi ngắt kết nối đột ngột từ client (Ví dụ: user tắt trình duyệt khi đang tải)
        String exMessage = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (ex.getClass().getName().contains("ClientAbortException") || 
            exMessage.contains("broken pipe") || 
            exMessage.contains("connection reset")) {
            return null;
        }
        // 1. Lấy thông tin User ID
        Long userId = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                userId = Long.parseLong(auth.getName());
            }
        } catch (Exception ignored) {}

        // 2. Lấy IP
        String ipAddress = "UNKNOWN";
        if (request != null) {
            ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            } else {
                ipAddress = ipAddress.split(",")[0].trim();
            }
        }
        String requestURI = request.getRequestURI();

        // 3. Chuẩn bị nội dung chi tiết
        StringBuilder details = new StringBuilder();
        details.append("🔥 URL BỊ LỖI: ").append(requestURI).append("\n");
        details.append("⚠️ NGUYÊN NHÂN: ").append(ex.getMessage()).append("\n");
        
        if (ex.getStackTrace() != null && ex.getStackTrace().length > 0) {
            details.append("📍 TỌA ĐỘ SẬP NGUỒN: ").append(ex.getStackTrace()[0].toString());
        }

        // 4. Ghi log vào Database
        try {
            AuditLog log = AuditLog.builder()
                    .userId(userId)
                    .actionName("SYSTEM_ERROR")
                    .entityName("GlobalExceptionHandler")
                    .entityId("CRITICAL")
                    .details(details.toString())
                    .ipAddress(ipAddress)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);
        } catch (Exception ignored) {
            // Không làm gián đoạn nếu ghi log thất bại
        }

        // 5. Đóng gói trả về Frontend thật đẹp
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", ex.getMessage() != null ? ex.getMessage() : "Lỗi hệ thống không xác định");
        errorResponse.put("path", requestURI);
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        // Phân loại lỗi cơ bản
        if (ex instanceof IllegalArgumentException) {
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Mặc định trả về 500 Internal Server Error
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
