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

    public UserController(UserRepository userRepository, 
                          com.smartbus.booking.repository.BookingRepository bookingRepository, 
                          com.smartbus.booking.repository.InspectorRepository inspectorRepository,
                          com.smartbus.booking.repository.TripRepository tripRepository,
                          com.smartbus.booking.repository.ReviewRepository reviewRepository,
                          PasswordEncoder passwordEncoder,
                          org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate,
                          com.smartbus.booking.repository.LeaveRequestRepository leaveRequestRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.inspectorRepository = inspectorRepository;
        this.tripRepository = tripRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
        this.messagingTemplate = messagingTemplate;
        this.leaveRequestRepository = leaveRequestRepository;
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

        // Check if phone already exists
        if (userUpdates.getPhone() != null && !userUpdates.getPhone().equals(existingUser.getPhone())) {
            if (userRepository.findByPhone(userUpdates.getPhone()).isPresent()) {
                return ResponseEntity.status(400).body(java.util.Map.of("message", "Số điện thoại này đã được sử dụng bởi một tài khoản khác. Vui lòng chọn số khác!"));
            }
        }

        existingUser.setFullName(userUpdates.getFullName());
        existingUser.setPhone(userUpdates.getPhone());
        existingUser.setEmail(userUpdates.getEmail());
        existingUser.setAvatarUrl(userUpdates.getAvatarUrl());
        
        if (isAdmin) {
            existingUser.setRole(userUpdates.getRole());
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

        // Không cho phép xoá ADMIN
        if ("ADMIN".equals(targetUser.getRole())) {
            return ResponseEntity.status(400).body(java.util.Map.of("message", "Không thể xoá tài khoản Quản trị viên (ADMIN)!"));
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
            
            // Xác định trạng thái
            String status;
            if (driver.getIsLocked() != null && driver.getIsLocked()) {
                status = "SUSPENDED";
            } else if (onLeaveDrivers.contains(driver.getPhone())) {
                status = "ON_LEAVE";
            } else {
                // Kiểm tra có chuyến IN_PROGRESS không
                boolean isDriving = allTrips.stream().anyMatch(t -> 
                    driver.getPhone().equals(t.getAssignedDriverUsername()) && "IN_PROGRESS".equals(t.getStatus()));
                if (isDriving) {
                    status = "DRIVING";
                } else {
                    status = "FREE";
                }
            }
            driverInfo.put("status", status);
            
            // Đếm số chuyến trong ngày
            long tripCount = allTrips.stream()
                .filter(t -> driver.getPhone().equals(t.getAssignedDriverUsername()) && !"CANCELLED".equals(t.getStatus()))
                .count();
            driverInfo.put("tripCount", tripCount);
            
            result.add(driverInfo);
        }
        
        return ResponseEntity.ok(result);
    }
}
