package com.smartbus.booking.controller;

import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final com.smartbus.booking.repository.BookingRepository bookingRepository;
    private final com.smartbus.booking.repository.InspectorRepository inspectorRepository;
    private final com.smartbus.booking.repository.TripRepository tripRepository;
    private final com.smartbus.booking.repository.ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    private final com.smartbus.booking.repository.LeaveRequestRepository leaveRequestRepository;
    private final com.smartbus.booking.service.WalletService walletService;

    public UserController(UserRepository userRepository, 
                          com.smartbus.booking.repository.BookingRepository bookingRepository, 
                          com.smartbus.booking.repository.InspectorRepository inspectorRepository,
                          com.smartbus.booking.repository.TripRepository tripRepository,
                          com.smartbus.booking.repository.ReviewRepository reviewRepository,
                          PasswordEncoder passwordEncoder,
                          org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate,
                          com.smartbus.booking.repository.LeaveRequestRepository leaveRequestRepository,
                          com.smartbus.booking.service.WalletService walletService) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.inspectorRepository = inspectorRepository;
        this.tripRepository = tripRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
        this.messagingTemplate = messagingTemplate;
        this.leaveRequestRepository = leaveRequestRepository;
        this.walletService = walletService;
    }

    // 1. Lấy toàn bộ danh sách Người dùng
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        // Tối ưu hóa N+1: Lấy trực tiếp tổng số vé từng User từ Database
        List<Object[]> ticketCounts = bookingRepository.countTicketsPerUser();
        java.util.Map<Long, Integer> userTicketCounts = new java.util.HashMap<>();
        
        for (Object[] row : ticketCounts) {
            Long userId = (Long) row[0];
            Number count = (Number) row[1];
            userTicketCounts.put(userId, count.intValue());
        }
        
        for (User user : users) {
            user.setTicketCount(userTicketCounts.getOrDefault(user.getId(), 0));
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/page")
    public ResponseEntity<?> getUsersPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "locked", required = false) Boolean locked) {
        String safeRole = role == null || role.isBlank() || "ALL".equalsIgnoreCase(role)
                ? "" : role.trim().toUpperCase();
        String safeSearch = search == null || search.isBlank() ? "" : search.trim();
        String safeProvider = provider == null || provider.isBlank() || "ALL".equalsIgnoreCase(provider)
                ? "" : provider.trim().toUpperCase();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(0, page), Math.min(100, Math.max(1, size)),
                org.springframework.data.domain.Sort.by("id").descending());
        org.springframework.data.domain.Page<User> result = userRepository.searchAdminUsers(
                safeRole, safeSearch, safeProvider, locked, pageable);

        List<User> users = result.getContent();
        List<Long> userIds = users.stream().map(User::getId).toList();
        if (!userIds.isEmpty()) {
            java.util.Map<Long, Integer> ticketCounts = new java.util.HashMap<>();
            bookingRepository.countTicketsPerUserIds(userIds).forEach(row ->
                    ticketCounts.put((Long) row[0], ((Number) row[1]).intValue()));

            java.util.Map<Long, Double> totalSpentByUser = new java.util.HashMap<>();
            bookingRepository.sumTotalSpentPerUserIds(userIds).forEach(row ->
                    totalSpentByUser.put((Long) row[0], ((Number) row[1]).doubleValue()));

            java.util.Map<String, Long> driverTripCounts = new java.util.HashMap<>();
            List<String> phones = users.stream().map(User::getPhone).filter(java.util.Objects::nonNull).toList();
            if (!phones.isEmpty()) {
                tripRepository.findActiveTripsByDriverPhones(phones).forEach(trip -> {
                    if (trip.getAssignedDriverUsername() != null) {
                        driverTripCounts.merge(trip.getAssignedDriverUsername(), 1L, Long::sum);
                    }
                    if (trip.getSecondaryDriverUsername() != null
                            && !trip.getSecondaryDriverUsername().equals(trip.getAssignedDriverUsername())) {
                        driverTripCounts.merge(trip.getSecondaryDriverUsername(), 1L, Long::sum);
                    }
                });
            }

            java.util.Map<Long, Long> inspectorTripCounts = new java.util.HashMap<>();
            tripRepository.countActiveTripsByInspectorUserIds(userIds).forEach(row ->
                    inspectorTripCounts.put((Long) row[0], ((Number) row[1]).longValue()));

            users.forEach(user -> {
                user.setTicketCount(ticketCounts.getOrDefault(user.getId(), 0));
                user.setTotalSpent(totalSpentByUser.getOrDefault(user.getId(), 0.0));
                user.setActiveTripCount("DRIVER".equalsIgnoreCase(user.getRole())
                        ? driverTripCounts.getOrDefault(user.getPhone(), 0L)
                        : inspectorTripCounts.getOrDefault(user.getId(), 0L));
            });
        }

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", users);
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        if (!safeRole.isBlank()) {
            java.util.Map<String, Object> summary = new java.util.LinkedHashMap<>();
            summary.put("totalUsers", userRepository.countByRoleIgnoreCase(safeRole));
            summary.put("activeUsers", userRepository.countByRoleIgnoreCaseAndIsLockedFalse(safeRole));
            summary.put("googleUsers", userRepository.countByRoleIgnoreCaseAndAuthProviderIgnoreCase(safeRole, "GOOGLE"));
            summary.put("totalWalletBalance", java.util.Optional.ofNullable(userRepository.sumWalletBalanceByRole(safeRole)).orElse(0.0));
            summary.put("totalSpent", java.util.Optional.ofNullable(bookingRepository.sumTotalSpentByUserRole(safeRole)).orElse(0.0));
            response.put("summary", summary);
        }
        return ResponseEntity.ok(response);
    }

    // 1.2 Lấy danh sách Người dùng theo vai trò
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable("role") String role) {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> role.equalsIgnoreCase(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(users);
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
    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_USER", entityName = "User")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody User userUpdates) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = userOpt.get();
        
        String currentUserIdStr = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = Long.parseLong(currentUserIdStr);
        
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !existingUser.getId().equals(currentUserId)) {
            return ResponseEntity.status(403).body("Không có quyền cập nhật tài khoản của người khác!");
        }

        if (isAdmin && "USER".equalsIgnoreCase(existingUser.getRole()) && !existingUser.getId().equals(currentUserId)) {
            return ResponseEntity.status(403).body(java.util.Map.of(
                    "message", "Quản trị viên chỉ được điều chỉnh số dư ví của khách hàng qua chức năng quản lý ví."));
        }

        // Check if phone already exists
        if (userUpdates.getPhone() != null && !userUpdates.getPhone().equals(existingUser.getPhone())) {
            String normalizedPhone = userUpdates.getPhone().replaceAll("[\\s.-]", "");
            if (!normalizedPhone.matches("(?:\\+84|0)\\d{9}")) {
                return ResponseEntity.status(400).body(java.util.Map.of("message", "Số điện thoại phải gồm 10 chữ số hoặc bắt đầu bằng +84."));
            }
            userUpdates.setPhone(normalizedPhone);
            if (userRepository.findByPhone(normalizedPhone).isPresent()) {
                return ResponseEntity.status(400).body(java.util.Map.of("message", "Số điện thoại này đã được sử dụng bởi một tài khoản khác. Vui lòng chọn số khác!"));
            }
        }

        if (userUpdates.getUsername() != null && !userUpdates.getUsername().isBlank()) {
            String username = userUpdates.getUsername().trim().toLowerCase(java.util.Locale.ROOT);
            if (!username.matches("[a-z][a-z0-9._-]{3,29}")) {
                return ResponseEntity.status(400).body(java.util.Map.of("message", "Tên đăng nhập phải bắt đầu bằng chữ và có 4-30 ký tự không dấu."));
            }
            Optional<User> usernameOwner = userRepository.findByUsernameIgnoreCase(username);
            if (usernameOwner.isPresent() && !usernameOwner.get().getId().equals(existingUser.getId())) {
                return ResponseEntity.status(400).body(java.util.Map.of("message", "Tên đăng nhập này đã được sử dụng."));
            }
            existingUser.setUsername(username);
        }

        existingUser.setFullName(userUpdates.getFullName());
        existingUser.setPhone(userUpdates.getPhone());
        existingUser.setEmail(userUpdates.getEmail());
        existingUser.setAvatarUrl(userUpdates.getAvatarUrl());
        
        if (isAdmin) {
            existingUser.setRole(userUpdates.getRole());
            existingUser.setDriverStatus(userUpdates.getDriverStatus());
            existingUser.setGender(userUpdates.getGender());
            existingUser.setDateOfBirth(userUpdates.getDateOfBirth());
            existingUser.setCitizenId(userUpdates.getCitizenId());
            existingUser.setCitizenIdIssueDate(userUpdates.getCitizenIdIssueDate());
            existingUser.setAddress(userUpdates.getAddress());
            existingUser.setEmergencyContactName(userUpdates.getEmergencyContactName());
            existingUser.setEmergencyContactPhone(userUpdates.getEmergencyContactPhone());
            existingUser.setDriverLicenseClass(userUpdates.getDriverLicenseClass());
            existingUser.setDriverLicenseNumber(userUpdates.getDriverLicenseNumber());
            existingUser.setDriverLicenseIssueDate(userUpdates.getDriverLicenseIssueDate());
            existingUser.setDriverLicenseExpiryDate(userUpdates.getDriverLicenseExpiryDate());
            existingUser.setDrivingExperienceYears(userUpdates.getDrivingExperienceYears());
            existingUser.setDriverShift(userUpdates.getDriverShift());
            existingUser.setDriverNotes(userUpdates.getDriverNotes());
            if (userUpdates.getWalletBalance() != null) {
                existingUser.setWalletBalance(userUpdates.getWalletBalance());
                // Push WebSocket notification
                messagingTemplate.convertAndSend("/topic/wallet/" + existingUser.getId(), "UPDATE");
            }
        }
        
        // Nếu có đổi mật khẩu mới (không trống)
        if (userUpdates.getPassword() != null && !userUpdates.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userUpdates.getPassword()));
        }

        User savedUser = userRepository.save(existingUser);

        // Auto-sync Inspector entity for Staff (Lơ xe or Tài xế)
        if ("INSPECTOR".equals(savedUser.getRole()) || "DRIVER".equals(savedUser.getRole())) {
            Optional<com.smartbus.booking.entity.Inspector> existingInsp = inspectorRepository.findByUserAccountId(savedUser.getId());
            if (existingInsp.isEmpty()) {
                com.smartbus.booking.entity.Inspector newInsp = com.smartbus.booking.entity.Inspector.builder()
                        .fullName(savedUser.getFullName())
                        .phone(savedUser.getPhone())
                        .employeeCode("NV" + savedUser.getId())
                        .userAccount(savedUser)
                        .build();
                inspectorRepository.save(newInsp);
            } else {
                // ĐỒNG BỘ: Cập nhật lại Tên và SĐT cho Inspector nếu Admin sửa User
                com.smartbus.booking.entity.Inspector insp = existingInsp.get();
                insp.setFullName(savedUser.getFullName());
                insp.setPhone(savedUser.getPhone());
                inspectorRepository.save(insp);
            }
        }

        return ResponseEntity.ok(savedUser);
    }

    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_USER_WALLET", entityName = "User")
    @PutMapping("/{id}/wallet")
    public ResponseEntity<?> updateUserWallet(
            @PathVariable("id") Long id,
            @RequestBody java.util.Map<String, Object> payload) {
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).body(java.util.Map.of(
                    "message", "Chỉ quản trị viên mới được điều chỉnh số dư ví."));
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        if (!"USER".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(400).body(java.util.Map.of(
                    "message", "Chỉ được điều chỉnh ví của tài khoản khách hàng."));
        }

        Object rawBalance = payload.get("walletBalance");
        if (rawBalance == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", "Vui lòng nhập số dư ví."));
        }

        final java.math.BigDecimal walletBalance;
        try {
            walletBalance = new java.math.BigDecimal(String.valueOf(rawBalance)).stripTrailingZeros();
        } catch (NumberFormatException exception) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", "Số dư ví không hợp lệ."));
        }
        if (walletBalance.signum() < 0 || walletBalance.scale() > 0) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", "Số dư ví phải là số nguyên không âm."));
        }

        double walletBalanceValue = walletBalance.doubleValue();
        if (!Double.isFinite(walletBalanceValue)) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", "Số dư ví vượt quá giới hạn cho phép."));
        }

        user.setWalletBalance(walletBalanceValue);
        User savedUser = userRepository.save(user);
        messagingTemplate.convertAndSend("/topic/wallet/" + savedUser.getId(), "UPDATE");
        return ResponseEntity.ok(java.util.Map.of(
                "id", savedUser.getId(),
                "walletBalance", savedUser.getWalletBalance()));
    }

    // 2.5. Khóa/Mở khóa tài khoản
    @com.smartbus.booking.annotation.AuditAction(action = "TOGGLE_USER_LOCK", entityName = "User")
    @PutMapping("/{id}/lock")
    public ResponseEntity<?> toggleUserLock(@PathVariable("id") Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        if ("ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(400).body(java.util.Map.of("message", "Không thể khóa tài khoản Quản trị viên (ADMIN)!"));
        }
        user.setIsLocked(user.getIsLocked() == null ? true : !user.getIsLocked());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    // 3. Xoá người dùng
    @com.smartbus.booking.annotation.AuditAction(action = "DELETE_USER", entityName = "User")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User targetUser = userOpt.get();

        if ("ADMIN".equalsIgnoreCase(targetUser.getRole())) {
            String currentUserIdValue = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            Long currentUserId = Long.parseLong(currentUserIdValue);
            if (targetUser.getId().equals(currentUserId)) {
                return ResponseEntity.status(400).body(java.util.Map.of(
                        "message", "Không thể xoá tài khoản quản trị viên đang đăng nhập."));
            }
            if (userRepository.countByRoleIgnoreCase("ADMIN") <= 1) {
                return ResponseEntity.status(400).body(java.util.Map.of(
                        "message", "Hệ thống phải còn ít nhất một tài khoản quản trị viên."));
            }
        }

        // Kiểm tra xem khách hàng đã mua vé chưa, nếu có vé thì cấm xoá
        List<com.smartbus.booking.entity.Booking> userBookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(id);
        if (!userBookings.isEmpty()) {
            return ResponseEntity.status(400).body(java.util.Map.of("message", "Không thể xoá khách hàng đã có lịch sử mua vé!"));
        }

        // Unlink or delete inspector if this user is an inspector
        Optional<com.smartbus.booking.entity.Inspector> insp = inspectorRepository.findByUserAccountId(id);
        if (insp.isPresent()) {
            com.smartbus.booking.entity.Inspector inspector = insp.get();
            // Unlink trips assigned to this inspector
            List<com.smartbus.booking.entity.Trip> trips = tripRepository.findByInspectorId(inspector.getId());
            for (com.smartbus.booking.entity.Trip t : trips) {
                t.setInspector(null);
                tripRepository.save(t);
            }
            inspectorRepository.delete(inspector);
        }
        
        userRepository.deleteById(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Xoá người dùng thành công!"));
    }

    // 4. Lấy lịch sử giao dịch/đặt vé của người dùng
    @GetMapping("/{id}/bookings")
    public ResponseEntity<?> getUserBookings(@PathVariable("id") Long id) {
        String currentUserIdStr = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = Long.parseLong(currentUserIdStr);
        
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = userOpt.get();
        if (!isAdmin && !existingUser.getId().equals(currentUserId)) {
            return ResponseEntity.status(403).body("Không có quyền xem lịch sử của người khác!");
        }

        List<com.smartbus.booking.entity.Booking> userBookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(id);
        
        // Populate isReviewed flag and review content (Tối ưu N+1 Query)
        if (!userBookings.isEmpty()) {
            List<Long> bookingIds = userBookings.stream().map(com.smartbus.booking.entity.Booking::getId).collect(java.util.stream.Collectors.toList());
            List<com.smartbus.booking.entity.Review> reviews = reviewRepository.findByBookingIdIn(bookingIds);
            java.util.Map<Long, com.smartbus.booking.entity.Review> reviewMap = reviews.stream()
                    .collect(java.util.stream.Collectors.toMap(r -> r.getBooking().getId(), r -> r));
            
            for (com.smartbus.booking.entity.Booking b : userBookings) {
                com.smartbus.booking.entity.Review r = reviewMap.get(b.getId());
                if (r != null) {
                    b.setReviewed(true);
                    // Prevent infinite recursion by nullifying booking and user within the transient Review
                    r.setBooking(null);
                    r.setUser(null);
                    b.setUserReview(r);
                } else {
                    b.setReviewed(false);
                }
            }
        }
        
        return ResponseEntity.ok(userBookings);
    }
    // 5. LẤY TRẠNG THÁI TÀI XẾ THỜI GIAN THỰC (Driver Status API)
    @GetMapping("/drivers/status")
    public ResponseEntity<?> getDriversWithStatus(@RequestParam(value = "date", required = false) String date) {
        String targetDate = (date != null && !date.isEmpty()) ? date : java.time.LocalDate.now().toString();
        
        // Lấy tất cả tài xế
        List<User> drivers = userRepository.findAll().stream()
                .filter(u -> "DRIVER".equalsIgnoreCase(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
        
        // Lấy tất cả chuyến xe trong ngày
        List<com.smartbus.booking.entity.Trip> allTrips = tripRepository.findAll().stream()
                .filter(t -> targetDate.equals(t.getDepartureDate()))
                .collect(java.util.stream.Collectors.toList());
        
        // Lấy nghỉ phép APPROVED 
        List<com.smartbus.booking.entity.LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeavesInRange(targetDate, targetDate);
        java.util.Set<String> onLeaveDrivers = approvedLeaves.stream()
                .map(com.smartbus.booking.entity.LeaveRequest::getDriverUsername)
                .collect(java.util.stream.Collectors.toSet());
        
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (User driver : drivers) {
            java.util.Map<String, Object> driverInfo = new java.util.HashMap<>();
            driverInfo.put("id", driver.getId());
            driverInfo.put("phone", driver.getPhone());
            driverInfo.put("fullName", driver.getFullName());
            driverInfo.put("avatarUrl", driver.getAvatarUrl());
            driverInfo.put("currentStation", driver.getCurrentStation() != null ? driver.getCurrentStation() : "");
            
            // Xác định trạng thái
            String status;
            if (driver.getIsLocked() != null && driver.getIsLocked()) {
                status = "SUSPENDED";
            } else if (onLeaveDrivers.contains(driver.getPhone())) {
                status = "ON_LEAVE";
            } else {
                // Kiểm tra có chuyến IN_PROGRESS không
                boolean isDriving = allTrips.stream().anyMatch(t -> 
                    (driver.getPhone().equals(t.getAssignedDriverUsername())
                            || driver.getPhone().equals(t.getSecondaryDriverUsername()))
                            && "IN_PROGRESS".equals(t.getStatus()));
                if (isDriving) {
                    status = "DRIVING";
                } else {
                    status = "FREE";
                }
            }
            driverInfo.put("status", status);
            
            // Đếm số chuyến trong ngày
            long tripCount = allTrips.stream()
                .filter(t -> (driver.getPhone().equals(t.getAssignedDriverUsername())
                        || driver.getPhone().equals(t.getSecondaryDriverUsername()))
                        && !"CANCELLED".equals(t.getStatus()))
                .count();
            driverInfo.put("tripCount", tripCount);
            
            result.add(driverInfo);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/check-topup")
    public ResponseEntity<?> checkTopupPayment(
            @RequestParam("userId") Long userId,
            @RequestParam("expectedAmount") Double expectedAmount,
            @RequestParam(value = "sessionStartTime", required = false) String sessionStartTimeStr) {
        
        boolean success = walletService.checkAndProcessTopupPolling(userId, expectedAmount, sessionStartTimeStr);
        if (success) {
            return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Payment found and processed"));
        } else {
            return ResponseEntity.ok(java.util.Map.of("success", false, "message", "Not found yet"));
        }
    }
}
