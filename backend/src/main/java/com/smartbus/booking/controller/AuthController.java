package com.smartbus.booking.controller;

import com.smartbus.booking.config.JwtService;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.UserRepository;
import com.smartbus.booking.service.LoyaltyPointPolicy;
import com.smartbus.booking.dto.BookingCancellationRequest;
import com.smartbus.booking.dto.RegistrationRequest;
import com.smartbus.booking.service.RegistrationVerificationService;
import com.smartbus.booking.service.RefundRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationVerificationService registrationVerificationService;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthController(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder,
                          RegistrationVerificationService registrationVerificationService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.registrationVerificationService = registrationVerificationService;
    }

    // ============================================================
    // API ĐĂNG KÝ
    // ============================================================
    @PostMapping("/register/send-code")
    public ResponseEntity<?> sendRegistrationCode(@RequestBody Map<String, String> request) {
        String phone = normalizePhone(request.get("phone"));
        String email = normalizeEmail(request.get("email"));
        String validationError = validatePublicRegistrationContact(phone, email);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        try {
            registrationVerificationService.sendCode(email, phone);
            return ResponseEntity.ok(Map.of(
                    "message", "Mã xác nhận đã được gửi đến email của bạn.",
                    "expiresInSeconds", 600,
                    "resendAfterSeconds", 60
            ));
        } catch (IllegalStateException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @com.smartbus.booking.annotation.AuditAction(action = "REGISTER_USER", entityName = "User")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest userRequest) {
        String username = normalizeUsername(userRequest.username());
        if (username != null && !username.matches("[a-z][a-z0-9._-]{3,29}")) {
            return ResponseEntity.badRequest().body("Tên đăng nhập phải bắt đầu bằng chữ và có 4-30 ký tự không dấu.");
        }
        if (username != null && userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return ResponseEntity.badRequest().body("Tên đăng nhập này đã được sử dụng.");
        }

        String phone = normalizePhone(userRequest.phone());
        if (!phone.matches("(?:\\+84|0)\\d{9}")) {
            return ResponseEntity.badRequest().body("Số điện thoại phải gồm 10 chữ số hoặc bắt đầu bằng +84.");
        }
        // Kiểm tra số điện thoại đã tồn tại chưa
        if (userRepository.findByPhone(phone).isPresent()) {
            return ResponseEntity.badRequest().body("Số điện thoại này đã được đăng ký tài khoản khác!");
        }
        
        // Kiểm tra email đã tồn tại chưa (nếu có nhập)
        String email = normalizeEmail(userRequest.email());
        if (!email.isEmpty()) {
            if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
                return ResponseEntity.badRequest().body("Email này đã được sử dụng bởi tài khoản khác!");
            }
        }

        boolean adminRegistration = isAdminRequest();
        if (!adminRegistration) {
            if (email.isEmpty()) {
                return ResponseEntity.badRequest().body("Email là bắt buộc để nhận mã xác nhận.");
            }
            try {
                registrationVerificationService.verify(email, phone, userRequest.verificationCode());
            } catch (IllegalArgumentException exception) {
                return ResponseEntity.badRequest().body(exception.getMessage());
            }
        }

        if (userRequest.fullName() == null || userRequest.fullName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập họ và tên.");
        }
        if (userRequest.password() == null || userRequest.password().length() < 6) {
            return ResponseEntity.badRequest().body("Mật khẩu phải có ít nhất 6 ký tự.");
        }

        // Tạo user mới với mật khẩu đã được BCrypt hash
        User newUser = User.builder()
                .phone(phone)
                .username(username)
                .password(passwordEncoder.encode(userRequest.password()))
                .fullName(userRequest.fullName().trim())
                .email(email.isEmpty() ? null : email)
                .role("USER")
                .lastLoginAt(LocalDateTime.now())
                .walletBalance(0.0) // Số dư mặc định
                .build();

        userRepository.save(newUser);
        if (!adminRegistration) {
            registrationVerificationService.consume(email);
        }

        // Tạo JWT token ngay sau khi đăng ký thành công
        String token = jwtService.generateToken(
                newUser.getId(),
                newUser.getPhone(),
                newUser.getRole(),
                newUser.getFullName()
        );

        return ResponseEntity.ok(buildAuthResponse(newUser, token));
    }

    private String validatePublicRegistrationContact(String phone, String email) {
        if (!phone.matches("(?:\\+84|0)\\d{9}")) {
            return "Số điện thoại phải gồm 10 chữ số hoặc bắt đầu bằng +84.";
        }
        if (userRepository.findByPhone(phone).isPresent()) {
            return "Số điện thoại này đã được đăng ký tài khoản khác!";
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return "Vui lòng nhập địa chỉ email hợp lệ.";
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return "Email này đã được sử dụng bởi tài khoản khác!";
        }
        return null;
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[\\s.-]", "");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private boolean isAdminRequest() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    // ============================================================
    // API ĐĂNG NHẬP
    // ============================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        String identifier = loginRequest.getUsername() != null && !loginRequest.getUsername().isBlank()
                ? loginRequest.getUsername().trim()
                : String.valueOf(loginRequest.getPhone()).trim();
        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> userRepository.findByPhone(identifier));

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Không tìm thấy tên đăng nhập hoặc số điện thoại này trên hệ thống.");
        }

        User user = userOpt.get();

        if (user.getIsLocked() != null && user.getIsLocked()) {
            return ResponseEntity.status(403).body("Tài khoản của bạn đã bị khóa! Vui lòng liên hệ tổng đài.");
        }

        // Kiểm tra mật khẩu với BCrypt
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Mật khẩu nhập vào chưa chính xác!");
        }

        // Tạo JWT token
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getId(),
                user.getPhone(),
                user.getRole(),
                user.getFullName()
        );

        return ResponseEntity.ok(buildAuthResponse(user, token));
    }

    // ============================================================
    // API ĐĂNG NHẬP GOOGLE
    // ============================================================
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> payload) {
        try {
            String credential = payload.get("credential");
            if (credential == null || credential.isEmpty()) {
                return ResponseEntity.badRequest().body("Thiếu thông tin Google Credential");
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken != null) {
                GoogleIdToken.Payload googlePayload = idToken.getPayload();
                String email = googlePayload.getEmail();
                String name = (String) googlePayload.get("name");
                String picture = (String) googlePayload.get("picture");

                // Tìm user theo email
                Optional<User> userOpt = userRepository.findByEmail(email);
                User user;

                if (userOpt.isPresent()) {
                    user = userOpt.get();
                    if (user.getIsLocked() != null && user.getIsLocked()) {
                        return ResponseEntity.status(403).body("Tài khoản của bạn đã bị khóa! Vui lòng liên hệ tổng đài.");
                    }
                    
                    // Đồng bộ nguồn đăng nhập và ảnh đại diện mới nhất từ Google.
                    if (!"GOOGLE".equals(user.getAuthProvider())) {
                        user.setAuthProvider("GOOGLE");
                    }
                    if (picture != null && !picture.isBlank()) {
                        user.setAvatarUrl(picture.trim());
                    }
                    user.setLastLoginAt(LocalDateTime.now());
                    userRepository.save(user);
                } else {
                    // Chưa có thì tạo mới, sinh sđt ngẫu nhiên (hoặc đánh dấu là GG)
                    user = User.builder()
                            .email(email)
                            .fullName(name)
                            .phone("GG_" + UUID.randomUUID().toString().substring(0, 10))
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .role("USER")
                            .authProvider("GOOGLE")
                            .lastLoginAt(LocalDateTime.now())
                            .avatarUrl(picture != null && !picture.isBlank() ? picture.trim() : null)
                            .walletBalance(0.0) // Số dư mặc định
                            .build();
                    user = userRepository.save(user);
                }

                String token = jwtService.generateToken(
                        user.getId(),
                        user.getPhone(),
                        user.getRole(),
                        user.getFullName()
                );

                return ResponseEntity.ok(buildAuthResponse(user, token));
            } else {
                return ResponseEntity.status(401).body("Xác thực Google thất bại (Token không hợp lệ)");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi xác thực Google: " + e.getMessage());
        }
    }

    // ============================================================
    // API BỔ SUNG SỐ ĐIỆN THOẠI CHO TÀI KHOẢN GOOGLE
    // ============================================================
    @PutMapping("/me/phone")
    @Transactional
    public ResponseEntity<?> completeGooglePhone(
            @RequestBody Map<String, String> payload,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Phiên đăng nhập không hợp lệ."));
        }

        try {
            String phone = Optional.ofNullable(payload.get("phone")).orElse("").trim();
            if (!phone.matches("^0[35789][0-9]{8}$")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Số điện thoại phải gồm 10 chữ số và đúng định dạng số di động Việt Nam."));
            }

            Long userId = jwtService.extractUserId(authHeader.substring(7));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));

            if (!"GOOGLE".equalsIgnoreCase(user.getAuthProvider())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Chức năng này chỉ dành cho tài khoản đăng nhập bằng Google."));
            }
            if (user.getPhone() != null && !user.getPhone().startsWith("GG_")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Tài khoản đã có số điện thoại hợp lệ."));
            }
            if (userRepository.findByPhone(phone).filter(found -> !found.getId().equals(userId)).isPresent()) {
                return ResponseEntity.status(409).body(Map.of(
                        "message", "Số điện thoại này đã được sử dụng bởi tài khoản khác."));
            }

            user.setPhone(phone);
            User savedUser = userRepository.save(user);
            String refreshedToken = jwtService.generateToken(
                    savedUser.getId(), savedUser.getPhone(), savedUser.getRole(), savedUser.getFullName());
            return ResponseEntity.ok(buildAuthResponse(savedUser, refreshedToken));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Phiên đăng nhập đã hết hạn hoặc không hợp lệ."));
        }
    }

    // ============================================================
    // API LẤY THÔNG TIN USER TỪ TOKEN (ME)
    // ============================================================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token không hợp lệ!");
        }

        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Không tìm thấy người dùng!");
            }

            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "phone", user.getPhone(),
                    "fullName", user.getFullName(),
                    "role", user.getRole(),
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "walletBalance", user.getWalletBalance() != null ? user.getWalletBalance() : 0.0,
                    "loyaltyPoints", user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0,
                    "authProvider", user.getAuthProvider() != null ? user.getAuthProvider() : "LOCAL",
                    "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token đã hết hạn hoặc không hợp lệ!");
        }
    }

    // ============================================================
    // API LẤY LỊCH SỬ ĐẶT VÉ CỦA USER TỪ TOKEN (ME)
    // ============================================================
    @Autowired
    private com.smartbus.booking.repository.BookingRepository bookingRepository;
    
    @Autowired
    private com.smartbus.booking.repository.ReviewRepository reviewRepository;

    @GetMapping("/me/bookings")
    public ResponseEntity<?> getMyBookings(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token không hợp lệ!");
        }

        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Không tìm thấy người dùng!");
            }

            User user = userOpt.get();
            List<com.smartbus.booking.entity.Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            
            // Extract all booking IDs
            List<Long> bookingIds = bookings.stream().map(com.smartbus.booking.entity.Booking::getId).collect(java.util.stream.Collectors.toList());
            
            // Fetch all reviews for these bookings in a single query
            List<com.smartbus.booking.entity.Review> reviews = java.util.Collections.emptyList();
            if (!bookingIds.isEmpty()) {
                reviews = reviewRepository.findByBookingIdIn(bookingIds);
            }
            
            // Map reviews to bookings
            java.util.Map<Long, com.smartbus.booking.entity.Review> reviewMap = new java.util.HashMap<>();
            for (com.smartbus.booking.entity.Review r : reviews) {
                if (r.getBooking() != null) {
                    reviewMap.put(r.getBooking().getId(), r);
                }
            }
            
            // Populate isReviewed flag and review content
            for (com.smartbus.booking.entity.Booking b : bookings) {
                com.smartbus.booking.entity.Review r = reviewMap.get(b.getId());
                if (r != null) {
                    b.setReviewed(true);
                    r.setBooking(null);
                    r.setUser(null);
                    b.setUserReview(r);
                } else {
                    b.setReviewed(false);
                }
            }
            
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token đã hết hạn hoặc không hợp lệ!");
        }
    }

    // ============================================================
    // API HỦY VÉ & HOÀN TIỀN VÀO VÍ (ME)
    // ============================================================
    @Autowired
    private com.smartbus.booking.service.FundService fundService;

    @Autowired
    private com.smartbus.booking.service.SeatService seatService;

    @Autowired
    private com.smartbus.booking.repository.UserVoucherRepository userVoucherRepository;

    @Autowired
    private RefundRequestService refundRequestService;

    @com.smartbus.booking.annotation.AuditAction(action = "CANCEL_BOOKING", entityName = "Booking")
    @PostMapping("/me/bookings/{id}/cancel")
    @Transactional
    public ResponseEntity<?> cancelMyBooking(@PathVariable("id") String idStr, @RequestBody BookingCancellationRequest payload, @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token không hợp lệ!");
        }

        try {
            if (payload == null || payload.getReason() == null || payload.getReason().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng nhập lý do hủy vé.");
            }
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Không tìm thấy người dùng!");
            }

            User user = userOpt.get();
            List<com.smartbus.booking.entity.Booking> bookingsToCancel = new java.util.ArrayList<>();
            
            if (idStr.startsWith("GRP")) {
                bookingsToCancel = bookingRepository.findByRoundTripGroupId(idStr);
            } else {
                try {
                    Long id = Long.parseLong(idStr);
                    bookingRepository.findById(id).ifPresent(bookingsToCancel::add);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("Mã vé không hợp lệ!");
                }
            }

            if (bookingsToCancel.isEmpty()) {
                return ResponseEntity.status(404).body("Không tìm thấy vé!");
            }
            
            Map<Long, Double> refundByBooking = new java.util.HashMap<>();
            for (com.smartbus.booking.entity.Booking booking : bookingsToCancel) {
                if (booking.getUser() == null || !booking.getUser().getId().equals(user.getId())) {
                    return ResponseEntity.status(403).body("Bạn không có quyền hủy vé này!");
                }
                if (!"PAID".equals(booking.getStatus()) && !"PENDING".equals(booking.getStatus())) {
                    return ResponseEntity.status(400).body("Vé " + booking.getId() + " không ở trạng thái cho phép hủy!");
                }
                if (refundRequestService.existsForBooking(booking.getId())) {
                    return ResponseEntity.status(400).body("Vé " + booking.getId() + " đã có yêu cầu hoàn tiền!");
                }

                long hoursToDeparture = 24;
                try {
                    if (booking.getTrip().getDepartureDate() != null && booking.getTrip().getDepartureTime() != null) {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        java.time.LocalDateTime departureDateTime = java.time.LocalDateTime.parse(
                                booking.getTrip().getDepartureDate() + " " + booking.getTrip().getDepartureTime(), formatter);
                        hoursToDeparture = java.time.temporal.ChronoUnit.HOURS.between(java.time.LocalDateTime.now(), departureDateTime);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi parse ngày giờ chuyến đi: " + e.getMessage());
                }

                if (hoursToDeparture < 12) {
                    return ResponseEntity.status(400).body("Không thể hủy vé vì chuyến đi sắp khởi hành (dưới 12 tiếng)!");
                }

                double refundAmount = 0.0;
                if ("PAID".equals(booking.getStatus()) && !"CASH".equals(booking.getPaymentMethod())) {
                    refundAmount = booking.getTotalPrice() * (hoursToDeparture < 24 ? 0.7 : 0.95);
                }
                refundByBooking.put(booking.getId(), refundAmount);
            }

            boolean hasRefund = refundByBooking.values().stream().anyMatch(amount -> amount > 0);
            String refundMethod = refundRequestService.validateMethod(payload, hasRefund);
            double totalRefundAmount = refundByBooking.values().stream().mapToDouble(Double::doubleValue).sum();

            for (com.smartbus.booking.entity.Booking booking : bookingsToCancel) {
                double refundAmount = refundByBooking.getOrDefault(booking.getId(), 0.0);

                if ("PAID".equals(booking.getStatus())) {
                    int earnedPoints = LoyaltyPointPolicy.pointsFor(booking.getTotalPrice());
                    user.setLoyaltyPoints(Math.max(0, (user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0) - earnedPoints));
                }

                // Refund voucher
                if (booking.getAppliedUserVoucherId() != null) {
                    userVoucherRepository.findById(booking.getAppliedUserVoucherId()).ifPresent(uv -> {
                        uv.setIsUsed(false);
                        userVoucherRepository.save(uv);
                    });
                }

                // Free seats
                seatService.releaseSeats(booking.getTrip().getId(), booking.getSeatNumbers());

                // Update booking status
                booking.setStatus("CANCELLED");
                booking.setRefundAmount(refundAmount);
                booking.setCancellationReason(payload.getReason());
                bookingRepository.save(booking);

                if (refundAmount > 0) {
                    refundRequestService.create(booking, user, refundAmount, refundMethod, payload);
                    if ("WALLET".equals(refundMethod)) {
                        fundService.recordTransaction(
                                booking.getPaymentMethod(), "EXPENSE", refundAmount,
                                "Hoàn tiền hủy vé #" + booking.getId() + " vào ví",
                                "REFUND-BOOKING-" + booking.getId(), "SYSTEM");
                    }
                }

                // Xóa đánh giá (nếu có) do chuyến đi bị hủy
                reviewRepository.findByBookingId(booking.getId()).ifPresent(review -> {
                    reviewRepository.delete(review);
                });
            }

            if (totalRefundAmount > 0 && "WALLET".equals(refundMethod)) {
                double currentBalance = user.getWalletBalance() == null ? 0.0 : user.getWalletBalance();
                user.setWalletBalance(currentBalance + totalRefundAmount);
            }
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "BANK_TRANSFER".equals(refundMethod)
                            ? "Đã hủy vé và gửi yêu cầu hoàn tiền qua ngân hàng."
                            : "Hủy vé thành công!",
                    "refundAmount", totalRefundAmount,
                    "refundMethod", refundMethod,
                    "refundStatus", "BANK_TRANSFER".equals(refundMethod) ? "PENDING" : "COMPLETED",
                    "walletBalance", user.getWalletBalance() == null ? 0.0 : user.getWalletBalance()
            ));
        } catch (IllegalArgumentException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // ============================================================
    // Helper: Build response JSON
    // ============================================================
    private Map<String, Object> buildAuthResponse(User user, String token) {
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("token", token);
        response.put("id", user.getId());
        response.put("username", user.getUsername() != null ? user.getUsername() : "");
        response.put("phone", user.getPhone());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());
        response.put("email", user.getEmail() != null ? user.getEmail() : "");
        response.put("walletBalance", user.getWalletBalance() != null ? user.getWalletBalance() : 0.0);
        response.put("loyaltyPoints", user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0);
        response.put("authProvider", user.getAuthProvider() != null ? user.getAuthProvider() : "LOCAL");
        response.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        return response;
    }

    private String normalizeUsername(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
