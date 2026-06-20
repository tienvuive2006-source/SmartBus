package com.smartbus.booking.aspect;

import com.smartbus.booking.annotation.AuditAction;
import com.smartbus.booking.entity.AuditLog;
import com.smartbus.booking.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditLogAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @AfterReturning(pointcut = "@annotation(auditAction)", returning = "result")
    public void logAuditActivity(JoinPoint joinPoint, AuditAction auditAction, Object result) {
        try {
            Long userId = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                try {
                    userId = Long.parseLong(auth.getName());
                } catch (NumberFormatException ignored) {
                }
            }

            HttpServletRequest request = null;
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();
            }
            String ipAddress = "UNKNOWN";
            if (request != null) {
                ipAddress = request.getHeader("X-Forwarded-For");
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getRemoteAddr();
                } else {
                    ipAddress = ipAddress.split(",")[0].trim();
                }
            }

            // Lấy tham số truyền vào hàm để làm details (ở mức cơ bản)
            StringBuilder details = new StringBuilder("Method args: ");
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg != null) {
                    details.append(arg.toString()).append(" | ");
                }
            }

            if (result != null) {
                if (result instanceof org.springframework.http.ResponseEntity) {
                    Object body = ((org.springframework.http.ResponseEntity<?>) result).getBody();
                    if (body != null) {
                        details.append("\n--- KẾT QUẢ TRẢ VỀ (SAU KHI XỬ LÝ) ---\n").append(body.toString());
                    }
                } else {
                    details.append("\n--- KẾT QUẢ TRẢ VỀ (SAU KHI XỬ LÝ) ---\n").append(result.toString());
                }
            }

            // Hàm tiện ích che giấu mật khẩu và token bằng Regex
            String finalDetails = details.toString()
                    .replaceAll("(?i)(password\\s*[=:]\\s*)[^,\\}\\]\\n\\r]+", "$1***")
                    .replaceAll("(?i)(token\\s*[=:]\\s*)[^,\\}\\]\\n\\r]+", "$1***");

            AuditLog log = AuditLog.builder()
                    .userId(userId)
                    .actionName(auditAction.action())
                    .entityName(auditAction.entityName())
                    .entityId("N/A") // Trích xuất thêm nếu cần
                    .details(finalDetails)
                    .ipAddress(ipAddress)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(log);

        } catch (Exception e) {
            // Không làm gián đoạn luồng chính nếu lưu log thất bại
            System.err.println("Failed to save audit log: " + e.getMessage());
        }
    }

}
