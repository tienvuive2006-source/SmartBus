package com.smartbus.booking.service;

import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    private static final Duration CODE_LIFETIME = Duration.ofMinutes(10);
    private static final Duration RESEND_DELAY = Duration.ofMinutes(1);
    private static final int MAX_ATTEMPTS = 5;

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, PendingReset> pendingResets = new ConcurrentHashMap<>();

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public PasswordResetService(JavaMailSender mailSender, UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public synchronized void sendCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);

        LocalDateTime now = LocalDateTime.now();
        PendingReset existing = pendingResets.get(normalizedEmail);
        if (existing != null && existing.sentAt().plus(RESEND_DELAY).isAfter(now)) {
            long seconds = Math.max(1, Duration.between(now, existing.sentAt().plus(RESEND_DELAY)).toSeconds());
            throw new IllegalStateException("Vui lòng chờ " + seconds + " giây trước khi gửi lại mã.");
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        String code = String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));

        if (user != null) {
            sendResetEmail(normalizedEmail, user.getFullName(), code);
        }

        // Lưu cả yêu cầu không có tài khoản để thời gian phản hồi và giới hạn gửi lại
        // không làm lộ email nào đang tồn tại trong hệ thống.
        pendingResets.put(normalizedEmail, new PendingReset(
                user == null ? null : user.getId(),
                passwordEncoder.encode(code),
                now,
                now.plus(CODE_LIFETIME),
                0
        ));
    }

    @Transactional
    public synchronized void resetPassword(String email, String code, String newPassword) {
        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);
        validatePassword(newPassword);

        String normalizedCode = code == null ? "" : code.trim();
        if (!normalizedCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("Mã xác nhận phải gồm 6 chữ số.");
        }

        PendingReset pending = pendingResets.get(normalizedEmail);
        LocalDateTime now = LocalDateTime.now();
        if (pending == null || pending.userId() == null || pending.expiresAt().isBefore(now)) {
            pendingResets.remove(normalizedEmail);
            throw new IllegalArgumentException("Mã xác nhận không đúng hoặc đã hết hạn.");
        }
        if (pending.failedAttempts() >= MAX_ATTEMPTS) {
            pendingResets.remove(normalizedEmail);
            throw new IllegalArgumentException("Bạn đã nhập sai quá nhiều lần. Vui lòng gửi mã mới.");
        }
        if (!passwordEncoder.matches(normalizedCode, pending.codeHash())) {
            pendingResets.replace(normalizedEmail, pending, new PendingReset(
                    pending.userId(), pending.codeHash(), pending.sentAt(), pending.expiresAt(),
                    pending.failedAttempts() + 1
            ));
            throw new IllegalArgumentException("Mã xác nhận không đúng hoặc đã hết hạn.");
        }

        User user = userRepository.findById(pending.userId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không còn tồn tại."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        pendingResets.remove(normalizedEmail);
    }

    private void sendResetEmail(String recipient, String fullName, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            if (senderEmail != null && !senderEmail.isBlank()) {
                helper.setFrom(senderEmail, "Trung Nam Smart Bus");
            }
            helper.setTo(recipient);
            helper.setSubject("Mã đặt lại mật khẩu Trung Nam Smart Bus");
            helper.setText("""
                    <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;color:#15345b">
                      <h2 style="color:#075fcc">Đặt lại mật khẩu</h2>
                      <p>Xin chào %s,</p>
                      <p>Mã xác nhận để đặt lại mật khẩu của bạn là:</p>
                      <p style="font-size:30px;font-weight:800;letter-spacing:8px;color:#075fcc">%s</p>
                      <p>Mã có hiệu lực trong 10 phút. Không cung cấp mã này cho bất kỳ ai.</p>
                      <p>Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.</p>
                    </div>
                    """.formatted(escapeHtml(fullName), code), true);
            mailSender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateEmail(String email) {
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ email hợp lệ.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        if (password.length() > 100) {
            throw new IllegalArgumentException("Mật khẩu mới không được vượt quá 100 ký tự.");
        }
    }

    private String escapeHtml(String value) {
        if (value == null || value.isBlank()) return "bạn";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private record PendingReset(
            Long userId,
            String codeHash,
            LocalDateTime sentAt,
            LocalDateTime expiresAt,
            int failedAttempts
    ) {}
}
