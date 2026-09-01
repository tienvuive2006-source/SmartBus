package com.smartbus.booking.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationVerificationService {

    private static final Duration CODE_LIFETIME = Duration.ofMinutes(10);
    private static final Duration RESEND_DELAY = Duration.ofSeconds(60);
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, PendingVerification> pendingCodes = new ConcurrentHashMap<>();

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public RegistrationVerificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public synchronized void sendCode(String email, String phone) {
        String normalizedEmail = normalizeEmail(email);
        Instant now = Instant.now();
        PendingVerification existing = pendingCodes.get(normalizedEmail);
        if (existing != null && now.isBefore(existing.nextSendAt())) {
            long seconds = Math.max(1, Duration.between(now, existing.nextSendAt()).toSeconds());
            throw new IllegalStateException("Vui lòng chờ " + seconds + " giây trước khi gửi lại mã.");
        }

        String code = String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
        sendVerificationEmail(normalizedEmail, code);
        pendingCodes.put(normalizedEmail, new PendingVerification(
                hash(code), phone, now.plus(CODE_LIFETIME), now.plus(RESEND_DELAY), 0
        ));
    }

    public void verify(String email, String phone, String code) {
        String normalizedEmail = normalizeEmail(email);
        PendingVerification pending = pendingCodes.get(normalizedEmail);
        if (pending == null) {
            throw new IllegalArgumentException("Bạn chưa yêu cầu mã xác nhận hoặc mã đã hết hạn.");
        }
        if (Instant.now().isAfter(pending.expiresAt())) {
            pendingCodes.remove(normalizedEmail, pending);
            throw new IllegalArgumentException("Mã xác nhận đã hết hạn. Vui lòng gửi mã mới.");
        }
        if (!pending.phone().equals(phone)) {
            throw new IllegalArgumentException("Số điện thoại đã thay đổi. Vui lòng gửi lại mã xác nhận.");
        }
        if (pending.failedAttempts() >= MAX_FAILED_ATTEMPTS) {
            pendingCodes.remove(normalizedEmail, pending);
            throw new IllegalArgumentException("Bạn đã nhập sai quá nhiều lần. Vui lòng gửi mã mới.");
        }
        if (code == null || !MessageDigest.isEqual(
                pending.codeHash().getBytes(StandardCharsets.UTF_8),
                hash(code.trim()).getBytes(StandardCharsets.UTF_8))) {
            pendingCodes.replace(normalizedEmail, pending, new PendingVerification(
                    pending.codeHash(), pending.phone(), pending.expiresAt(), pending.nextSendAt(),
                    pending.failedAttempts() + 1
            ));
            throw new IllegalArgumentException("Mã xác nhận không chính xác.");
        }
    }

    public void consume(String email) {
        pendingCodes.remove(normalizeEmail(email));
    }

    private void sendVerificationEmail(String recipient, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            if (senderEmail != null && !senderEmail.isBlank()) {
                helper.setFrom(senderEmail, "Trung Nam Smart Bus");
            }
            helper.setTo(recipient);
            helper.setSubject("Mã xác nhận đăng ký Trung Nam Smart Bus");
            helper.setText("""
                    <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:28px;color:#0f2942">
                      <h2 style="margin:0 0 12px">Xác nhận email đăng ký</h2>
                      <p style="line-height:1.6">Nhập mã dưới đây để hoàn tất tài khoản Trung Nam Smart Bus:</p>
                      <div style="margin:24px 0;padding:18px;border-radius:12px;background:#f0f7ff;text-align:center;font-size:32px;font-weight:800;letter-spacing:8px;color:#1263ce">%s</div>
                      <p style="font-size:13px;color:#64748b">Mã có hiệu lực trong 10 phút. Không cung cấp mã này cho người khác.</p>
                    </div>
                    """.formatted(code), true);
            mailSender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể gửi email xác nhận. Vui lòng kiểm tra địa chỉ email và thử lại.", exception);
        }
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Không thể tạo mã xác nhận.", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private record PendingVerification(
            String codeHash,
            String phone,
            Instant expiresAt,
            Instant nextSendAt,
            int failedAttempts
    ) {}
}
