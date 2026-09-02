package com.smartbus.booking.controller;

import com.smartbus.booking.entity.Booking;
import com.smartbus.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.smartbus.booking.repository.TripRepository;
import com.smartbus.booking.repository.UserRepository;
import com.smartbus.booking.service.LoyaltyPointPolicy;

@RestController
@RequestMapping("/admin/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.smartbus.booking.repository.ReviewRepository reviewRepository;

    @Autowired
    private com.smartbus.booking.service.SeatService seatService;

    @Autowired
    private com.smartbus.booking.service.EmailService emailService;

    @Autowired
    private com.smartbus.booking.service.FundService fundService;

    @Autowired
    private com.smartbus.booking.repository.PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private com.smartbus.booking.repository.RoundTripGroupRepository roundTripGroupRepository;

    @Autowired
    private com.smartbus.booking.repository.UserVoucherRepository userVoucherRepository;

    @Autowired
    private com.smartbus.booking.service.RouteStopService routeStopService;

    @Autowired
    private com.smartbus.booking.repository.SeatRepository seatRepository;

    @GetMapping
    public List<Booking> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        enrichSeatTypes(bookings);
        return bookings;
    }

    @GetMapping("/page")
    public ResponseEntity<?> getBookingsPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo,
            @RequestParam(value = "departurePoint", required = false) String departurePoint,
            @RequestParam(value = "arrivalPoint", required = false) String arrivalPoint) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        String safeSearch = search == null || search.isBlank() ? "" : search.trim();
        String safeStatus = status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)
                ? "" : status.trim().toUpperCase();
        String safePaymentMethod = paymentMethod == null || paymentMethod.isBlank() || "ALL".equalsIgnoreCase(paymentMethod)
                ? "" : paymentMethod.trim().toUpperCase();
        String safeDeparturePoint = departurePoint == null ? "" : departurePoint.trim();
        String safeArrivalPoint = arrivalPoint == null ? "" : arrivalPoint.trim();
        java.time.LocalDateTime createdFrom = parseFilterDate(dateFrom, false);
        java.time.LocalDateTime createdTo = parseFilterDate(dateTo, true);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                safePage, safeSize, org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<Booking> result = bookingRepository.searchAdminBookings(
                safeSearch, safeStatus, safePaymentMethod, createdFrom, createdTo,
                safeDeparturePoint, safeArrivalPoint, pageable);

        java.util.Map<String, Long> statusCounts = new java.util.HashMap<>();
        bookingRepository.countByStatusGrouped().forEach(row ->
                statusCounts.put(String.valueOf(row[0]).toUpperCase(), ((Number) row[1]).longValue()));

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        enrichSeatTypes(result.getContent());
        response.put("content", result.getContent());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("statusCounts", statusCounts);
        response.put("routeOptions", bookingRepository.findAdminRouteOptions().stream()
                .map(row -> java.util.Map.of(
                        "departurePoint", String.valueOf(row[0]),
                        "arrivalPoint", String.valueOf(row[1])))
                .toList());
        return ResponseEntity.ok(response);
    }

    private java.time.LocalDateTime parseFilterDate(String value, boolean endExclusive) {
        if (value == null || value.isBlank()) {
            return endExclusive
                    ? java.time.LocalDate.of(9999, 12, 31).atStartOfDay()
                    : java.time.LocalDate.of(1970, 1, 1).atStartOfDay();
        }
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(value.trim());
            return endExclusive ? date.plusDays(1).atStartOfDay() : date.atStartOfDay();
        } catch (java.time.format.DateTimeParseException exception) {
            throw new IllegalArgumentException("Ngày lọc không đúng định dạng yyyy-MM-dd.");
        }
    }

    private void enrichSeatTypes(List<Booking> bookings) {
        List<Long> tripIds = bookings.stream()
                .filter(booking -> booking.getTrip() != null && booking.getTrip().getId() != null)
                .map(booking -> booking.getTrip().getId())
                .distinct()
                .toList();
        if (tripIds.isEmpty()) return;

        Map<Long, Map<String, com.smartbus.booking.entity.SeatType>> typesByTrip = seatRepository
                .findByTripIdInOrderByTripIdAscSeatNumberAsc(tripIds)
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        seat -> seat.getTrip().getId(),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toMap(
                                com.smartbus.booking.entity.Seat::getSeatNumber,
                                com.smartbus.booking.entity.Seat::getSeatType,
                                (first, ignored) -> first,
                                java.util.LinkedHashMap::new)));

        for (Booking booking : bookings) {
            if (booking.getTrip() == null || booking.getSeatNumbers() == null) continue;
            Long tripId = booking.getTrip().getId();
            Map<String, com.smartbus.booking.entity.SeatType> tripSeatTypes = typesByTrip.getOrDefault(
                    tripId, java.util.Collections.emptyMap());
            Map<String, com.smartbus.booking.entity.SeatType> bookingSeatTypes = new java.util.LinkedHashMap<>();
            for (String seatNumber : booking.getSeatNumbers()) {
                bookingSeatTypes.put(
                        seatNumber,
                        tripSeatTypes.getOrDefault(seatNumber, com.smartbus.booking.entity.SeatType.STANDARD));
            }
            booking.setSeatTypes(bookingSeatTypes);
        }
    }

    @jakarta.annotation.PostConstruct
    public void fixOldCashBookings() {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            for (Booking b : bookings) {
                if ("CASH".equals(b.getPaymentMethod()) && "PAID".equals(b.getStatus())) {
                    b.setStatus("PENDING");
                    bookingRepository.save(b);
                }
            }
            System.out.println("✅ Đã fix xong dữ liệu cũ: Đổi CASH + PAID thành PENDING");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_BOOKING_STATUS", entityName = "Booking")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    String oldStatus = booking.getStatus();
                    String newStatus = payload.get("status");

                    // Xử lý HOÀN TIỀN và GIẢI PHÓNG GHẾ nếu vé bị HỦY
                    if ("CANCELLED".equals(newStatus) && !"CANCELLED".equals(oldStatus)) {
                        
                        // 1. Giải phóng ghế để khách khác có thể đặt lại
                        if (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()) {
                            seatService.releaseSeats(booking.getTrip().getId(), booking.getSeatNumbers());
                        }

                        // 2. Hoàn tiền tự động vào Ví SkyPay cho các phương thức trả trước (Quét QR hoặc dùng Ví)
                        // Giúp khách hàng nhận lại được tiền ngay lập tức để đặt chuyến khác thay vì chờ chuyển khoản tay
                        if (booking.getUser() != null) {
                            boolean isPaidViaWallet = "WALLET".equals(booking.getPaymentMethod());
                            boolean isPaidViaQR = "BANK_TRANSFER".equals(booking.getPaymentMethod());
                            boolean isAlreadyPaid = "PAID".equals(oldStatus) || "CHECKED_IN".equals(oldStatus);
                            
                            // Hoàn tiền vào ví
                            if (isPaidViaWallet || (isPaidViaQR) || isAlreadyPaid) {
                                com.smartbus.booking.entity.User user = booking.getUser();
                                double currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
                                
                                double refundAmount = booking.getTotalPrice() != null ? (booking.getTotalPrice() * 0.95) : 0.0;
                                
                                user.setWalletBalance(currentBalance + refundAmount);
                                userRepository.save(user);
                            }

                            // Trừ điểm thưởng nếu đã thanh toán
                            if (isAlreadyPaid) {
                                com.smartbus.booking.entity.User user = booking.getUser();
                                int earnedPoints = LoyaltyPointPolicy.pointsFor(booking.getTotalPrice() != null ? booking.getTotalPrice() : 0);
                                user.setLoyaltyPoints(Math.max(0, (user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0) - earnedPoints));
                                userRepository.save(user);
                            }
                        }

                        // Hoàn trả Voucher
                        if (booking.getAppliedUserVoucherId() != null) {
                            userVoucherRepository.findById(booking.getAppliedUserVoucherId()).ifPresent(uv -> {
                                uv.setIsUsed(false);
                                userVoucherRepository.save(uv);
                            });
                        }
                    }

                    // Nếu đổi thành trạng thái CANCELLED, xóa đánh giá nếu có
                    if ("CANCELLED".equals(newStatus)) {
                        reviewRepository.findByBookingId(booking.getId()).ifPresent(review -> {
                            reviewRepository.delete(review);
                        });
                    }
                    
                    // Tích điểm khi đơn chuyển sang Đã thanh toán (PAID) hoặc Đã lên xe (CHECKED_IN)
                    if (("PAID".equals(newStatus) || "CHECKED_IN".equals(newStatus)) 
                        && (!"PAID".equals(oldStatus) && !"CHECKED_IN".equals(oldStatus))) {
                        
                        String performedBy = "Admin";
                        try {
                            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                                Long currentUserId = Long.parseLong(auth.getName());
                                com.smartbus.booking.entity.User currentUser = userRepository.findById(currentUserId).orElse(null);
                                if (currentUser != null && !"ADMIN".equals(currentUser.getRole())) {
                                    performedBy = currentUser.getFullName();
                                }
                            }
                        } catch (Exception ignored) {}

                        // Fallback nếu không xác định được
                        if ("Admin".equals(performedBy) && ("BANK_TRANSFER".equals(booking.getPaymentMethod()) || "WALLET".equals(booking.getPaymentMethod()))) {
                            if (booking.getUser() != null) {
                                performedBy = booking.getUser().getFullName();
                            } else {
                                performedBy = booking.getCustomerName();
                            }
                        }

                        // Ghi nhận doanh thu
                        fundService.recordTransaction(booking.getPaymentMethod(), "INCOME", booking.getTotalPrice(), "Thanh toán vé #" + booking.getId(), String.valueOf(booking.getId()), performedBy);

                        if (booking.getUser() != null && booking.getTotalPrice() != null) {
                             com.smartbus.booking.entity.User user = booking.getUser();
                             int earnedPoints = LoyaltyPointPolicy.pointsFor(booking.getTotalPrice());
                             user.setLoyaltyPoints((user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0) + earnedPoints);
                             userRepository.save(user);
                        }
                    }

                    booking.setStatus(newStatus);
                    bookingRepository.save(booking);
                    return ResponseEntity.ok().body(Map.of("message", "Cập nhật trạng thái thành công"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @com.smartbus.booking.annotation.AuditAction(action = "CREATE_BOOKING", entityName = "Booking")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> payload) {
        try {
            requireCurrentAuthenticatedPhone();
            Booking booking = new Booking();
            booking.setCustomerName((String) payload.get("customerName"));
            booking.setCustomerPhone((String) payload.get("customerPhone"));
            booking.setCustomerEmail((String) payload.get("customerEmail"));
            booking.setSeatNumbers((List<String>) payload.get("seatNumbers"));
            Long tripId = Long.valueOf(((Map<?, ?>) payload.get("trip")).get("id").toString());
            com.smartbus.booking.entity.Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe"));

            // KỂM TRA: Không cho phép đặt vé nếu chuyến xe đã khởi hành (vượt quá thời gian hiện tại)
            if (trip.getDepartureDate() != null && trip.getDepartureTime() != null) {
                try {
                    String[] timeParts = trip.getDepartureTime().split(":");
                    int hour = Integer.parseInt(timeParts[0].trim());
                    int minute = Integer.parseInt(timeParts[1].trim());
                    java.time.LocalDate depDate = java.time.LocalDate.parse(trip.getDepartureDate());
                    java.time.LocalDateTime departureDateTime = depDate.atTime(hour, minute);
                    
                    if (LocalDateTime.now().isAfter(departureDateTime)) {
                        throw new RuntimeException("Chuyến xe này đã khởi hành (quá hạn), không thể tiếp tục đặt vé!");
                    }
                } catch (Exception e) {
                    if (e.getMessage().contains("quá hạn")) {
                        throw e; // Bắn lỗi ra ngoài để frontend hiển thị
                    }
                    System.err.println("Không thể kiểm tra ngày giờ khởi hành: " + e.getMessage());
                }
            }

            // Tính toán lại giá vé trên Server để bảo mật (Không tin tưởng Frontend)
            List<String> selectedSeats = (List<String>) payload.get("seatNumbers");
            double serverCalculatedPrice = 0.0;
            if (selectedSeats != null && !selectedSeats.isEmpty()) {
                serverCalculatedPrice = (trip.getPrice() != null ? trip.getPrice() : 0.0) * selectedSeats.size();
            }

            // Xử lý áp dụng mã giảm giá (Voucher)
            if (payload.get("userVoucherId") != null && payload.get("user") != null) {
                Long userVoucherId = Long.valueOf(payload.get("userVoucherId").toString());
                Long customerId = Long.valueOf(((Map<?, ?>) payload.get("user")).get("id").toString());
                
                com.smartbus.booking.entity.UserVoucher uv = userVoucherRepository.findById(userVoucherId).orElse(null);
                if (uv != null && !uv.getIsUsed() && uv.getUser().getId().equals(customerId) && uv.getVoucher().getIsActive()) {
                    double discountAmount = uv.getVoucher().getDiscountAmount();
                    serverCalculatedPrice = Math.max(0, serverCalculatedPrice - discountAmount);
                    booking.setDiscountAmount(discountAmount);
                    booking.setAppliedUserVoucherId(uv.getId());
                    
                    // Đánh dấu đã sử dụng
                    uv.setIsUsed(true);
                    userVoucherRepository.save(uv);
                }
            }

            booking.setTotalPrice(serverCalculatedPrice);
            String paymentMethod = (String) payload.get("paymentMethod");
            rejectGuestCashPayment(paymentMethod);
            booking.setPaymentMethod(paymentMethod);
            booking.setStatus((String) payload.get("status"));
            booking.setCreatedAt(LocalDateTime.now());
            booking.setTrip(trip);

            // 1. GÁN USER VÀ TRỪ TIỀN VÍ (NẾU LÀ WALLET)
            if (payload.get("user") != null) {
                Object userIdObj = ((Map<?, ?>) payload.get("user")).get("id");
                if (userIdObj != null) {
                    Long userId = Long.valueOf(userIdObj.toString());
                    com.smartbus.booking.entity.User user = requireCompletedBookingPhone(userId);
                    
                    if ("WALLET".equals(payload.get("paymentMethod"))) {
                        double balance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
                        
                        if (balance < serverCalculatedPrice) {
                            throw new RuntimeException("Số dư Ví hệ thống không đủ để thực hiện giao dịch này!");
                        }
                        
                        user.setWalletBalance(balance - serverCalculatedPrice);
                        userRepository.save(user);
                    }
                    booking.setUser(user);
                }
            }

            // 2. KHÓA GHẾ & ĐỒNG BỘ (Seat Locking)
            if (selectedSeats != null && !selectedSeats.isEmpty()) {
                seatService.bookSeats(tripId, selectedSeats);
            }

            Booking saved = bookingRepository.save(booking);
            messagingTemplate.convertAndSend("/topic/admin/bookings/new", "NEW_BOOKING");

            // Tích điểm và Ghi nhận doanh thu ngay nếu trạng thái là PAID hoặc CHECKED_IN
            if ("PAID".equals(saved.getStatus()) || "CHECKED_IN".equals(saved.getStatus())) {
                 String performedBy = "Admin";
                 try {
                     org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                     if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                         Long currentUserId = Long.parseLong(auth.getName());
                         com.smartbus.booking.entity.User currentUser = userRepository.findById(currentUserId).orElse(null);
                         if (currentUser != null && !"ADMIN".equals(currentUser.getRole())) {
                             performedBy = currentUser.getFullName();
                         }
                     }
                 } catch (Exception ignored) {}

                 if ("Admin".equals(performedBy)) {
                     if ("CHECKED_IN".equals(saved.getStatus())) {
                         performedBy = "Lơ xe thu";
                     } else if ("BANK_TRANSFER".equals(saved.getPaymentMethod()) || "WALLET".equals(saved.getPaymentMethod())) {
                         if (saved.getUser() != null) {
                             performedBy = saved.getUser().getFullName();
                         } else {
                             performedBy = saved.getCustomerName();
                         }
                     }
                 }

                 String description = "CHECKED_IN".equals(saved.getStatus()) ? "Thu tiền vé tại xe - Vé #" + saved.getId() : "Thanh toán vé #" + saved.getId();
                 
                 fundService.recordTransaction(saved.getPaymentMethod(), "INCOME", saved.getTotalPrice(), description, String.valueOf(saved.getId()), performedBy);

                 if (saved.getUser() != null) {
                     com.smartbus.booking.entity.User user = saved.getUser();
                     int earnedPoints = LoyaltyPointPolicy.pointsFor(saved.getTotalPrice());
                     user.setLoyaltyPoints((user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0) + earnedPoints);
                     userRepository.save(user);
                 }
            }

            // 3. GỬI EMAIL XÁC NHẬN KÈM MÃ QR
            try {
                Boolean sendEmail = true;
                if (payload.containsKey("sendEmail")) {
                    sendEmail = Boolean.valueOf(payload.get("sendEmail").toString());
                }
                if (sendEmail && saved.getCustomerEmail() != null && !saved.getCustomerEmail().trim().isEmpty() && !saved.getCustomerEmail().equals("no-email@smartbus.com")) {
                    emailService.sendBookingConfirmation(saved);
                }
            } catch (Exception ex) {
                System.err.println("❌ Lỗi kích hoạt gửi email: " + ex.getMessage());
            }

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            } catch (Exception ex) {
                // Ignore if no transaction
            }
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping("/create-roundtrip")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> createRoundTrip(@RequestBody Map<String, Object> payload) {
        try {
            requireCurrentAuthenticatedPhone();
            Long outboundTripId = Long.valueOf(payload.get("outboundTripId").toString());
            List<String> outboundSeats = (List<String>) payload.get("outboundSeats");
            
            Long returnTripId = payload.containsKey("returnTripId") && payload.get("returnTripId") != null ? 
                                Long.valueOf(payload.get("returnTripId").toString()) : null;
            List<String> returnSeats = payload.containsKey("returnSeats") && payload.get("returnSeats") != null ? 
                                       (List<String>) payload.get("returnSeats") : null;
            String holdToken = payload.get("holdToken") != null
                    ? payload.get("holdToken").toString()
                    : "PAYMENT-" + java.util.UUID.randomUUID();

            Long customerId = extractPayloadUserId(payload);
            if (customerId != null) {
                requireCompletedBookingPhone(customerId);
            }

            // 1. Tính toán giá
            com.smartbus.booking.entity.Trip outTrip = tripRepository.findById(outboundTripId).orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi"));
            double totalAmount = (outTrip.getPrice() != null ? outTrip.getPrice() : 0.0) * outboundSeats.size();
            
            if (returnTripId != null && returnSeats != null) {
                com.smartbus.booking.entity.Trip retTrip = tripRepository.findById(returnTripId).orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến về"));
                totalAmount += (retTrip.getPrice() != null ? retTrip.getPrice() : 0.0) * returnSeats.size();
            }
            
            // 2. Giữ ghế (Seat Holding)
            LocalDateTime holdExpiresAt = seatService.holdSeats(outboundTripId, outboundSeats, holdToken);
            if (returnTripId != null && returnSeats != null) {
                LocalDateTime returnHoldExpiresAt = seatService.holdSeats(returnTripId, returnSeats, holdToken);
                if (returnHoldExpiresAt.isBefore(holdExpiresAt)) {
                    holdExpiresAt = returnHoldExpiresAt;
                }
            }

            // Áp dụng voucher
            Long userVoucherId = payload.containsKey("userVoucherId") && payload.get("userVoucherId") != null ? 
                                Long.valueOf(payload.get("userVoucherId").toString()) : null;
            if (userVoucherId != null && payload.get("user") != null) {
                Long voucherCustomerId = customerId != null ? customerId : Long.valueOf(((Map<?, ?>) payload.get("user")).get("id").toString());
                com.smartbus.booking.entity.UserVoucher uv = userVoucherRepository.findById(userVoucherId).orElse(null);
                if (uv != null && !uv.getIsUsed() && uv.getUser().getId().equals(voucherCustomerId) && uv.getVoucher().getIsActive()) {
                    totalAmount = Math.max(0, totalAmount - uv.getVoucher().getDiscountAmount());
                }
            }
            
            // 3. Tạo PaymentOrder
            String paymentCode = "RT" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + (int)(Math.random() * 90 + 10);
            com.smartbus.booking.entity.PaymentOrder po = new com.smartbus.booking.entity.PaymentOrder();
            po.setPaymentCode(paymentCode);
            po.setTotalAmount(totalAmount);
            po.setStatus("PENDING");
            po.setCreatedAt(LocalDateTime.now());
            
            if (payload.get("user") != null) {
                Object userIdObj = ((Map<?, ?>) payload.get("user")).get("id");
                if (userIdObj != null) {
                    po.setCustomerId(Long.valueOf(userIdObj.toString()));
                }
            }
            
            com.smartbus.booking.entity.PaymentOrder savedPo = paymentOrderRepository.save(po);
            
            return ResponseEntity.ok(Map.of(
                "paymentOrderId", savedPo.getId(),
                "paymentCode", savedPo.getPaymentCode(),
                "totalAmount", savedPo.getTotalAmount(),
                "expiresAt", holdExpiresAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toString()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/confirm-roundtrip")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> confirmRoundTrip(@RequestBody Map<String, Object> payload) {
        try {
            requireCurrentAuthenticatedPhone();
            Long paymentOrderId = Long.valueOf(payload.get("paymentOrderId").toString());
            com.smartbus.booking.entity.PaymentOrder po = paymentOrderRepository.findById(paymentOrderId)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));

            if (po.getCustomerId() != null) {
                requireCompletedBookingPhone(po.getCustomerId());
            }
                
            if ("COMPLETED".equals(po.getStatus())) {
                return ResponseEntity.ok(Map.of("message", "Giao dịch này đã được xử lý thành công trước đó."));
            }
            
            String paymentMethod = (String) payload.get("paymentMethod");
            rejectGuestCashPayment(paymentMethod);
            
            // Nếu là WALLET thì trừ tiền
            if ("WALLET".equals(paymentMethod) && po.getCustomerId() != null) {
                com.smartbus.booking.entity.User user = userRepository.findById(po.getCustomerId()).orElseThrow();
                double balance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
                if (balance < po.getTotalAmount()) {
                    throw new RuntimeException("Số dư Ví hệ thống không đủ!");
                }
                user.setWalletBalance(balance - po.getTotalAmount());
                userRepository.save(user);
            }
            
            // Tạo RoundTripGroup
            com.smartbus.booking.entity.RoundTripGroup rtg = new com.smartbus.booking.entity.RoundTripGroup();
            rtg.setGroupId("GRP-" + java.util.UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 16).toUpperCase());
            rtg.setCustomerId(po.getCustomerId());
            rtg.setCreatedAt(LocalDateTime.now());
            roundTripGroupRepository.save(rtg);
            
            Map<String, Object> customerInfo = (Map<String, Object>) payload.get("customerInfo");
            
            // Lấy thông tin voucher nếu có
            Long userVoucherId = payload.containsKey("userVoucherId") && payload.get("userVoucherId") != null ? 
                                Long.valueOf(payload.get("userVoucherId").toString()) : null;
            Double voucherDiscountAmount = 0.0;
            if (userVoucherId != null) {
                com.smartbus.booking.entity.UserVoucher uv = userVoucherRepository.findById(userVoucherId).orElse(null);
                if (uv != null && !uv.getIsUsed() && uv.getUser().getId().equals(po.getCustomerId()) && uv.getVoucher().getIsActive()) {
                    voucherDiscountAmount = uv.getVoucher().getDiscountAmount();
                    uv.setIsUsed(true);
                    userVoucherRepository.save(uv);
                } else {
                    userVoucherId = null; // Reset nếu không hợp lệ
                }
            }
            
            // Tạo vé OUTBOUND
            Long outboundTripId = Long.valueOf(payload.get("outboundTripId").toString());
            List<String> outboundSeats = (List<String>) payload.get("outboundSeats");
            
            // Chia đôi discount nếu là khứ hồi
            Double outDiscount = voucherDiscountAmount;
            Double retDiscount = 0.0;
            if (payload.containsKey("returnTripId") && payload.get("returnTripId") != null) {
                outDiscount = voucherDiscountAmount / 2;
                retDiscount = voucherDiscountAmount / 2;
            }
            
            Booking outboundBooking = createSingleBookingFromPayload(outboundTripId, outboundSeats, customerInfo, paymentMethod, "OUTBOUND", rtg.getGroupId(), po.getCustomerId(), userVoucherId, outDiscount,
                    payload.get("outboundPickupStopId"), payload.get("outboundDropoffStopId"));
            
            // Tạo vé RETURN
            Booking returnBooking = null;
            if (payload.containsKey("returnTripId") && payload.get("returnTripId") != null) {
                Long returnTripId = Long.valueOf(payload.get("returnTripId").toString());
                List<String> returnSeats = (List<String>) payload.get("returnSeats");
                returnBooking = createSingleBookingFromPayload(returnTripId, returnSeats, customerInfo, paymentMethod, "RETURN", rtg.getGroupId(), po.getCustomerId(), userVoucherId, retDiscount,
                        payload.get("returnPickupStopId"), payload.get("returnDropoffStopId"));
            }
            
            po.setStatus("COMPLETED");
            paymentOrderRepository.save(po);
            
            java.util.List<String> ticketCodes = new java.util.ArrayList<>();
            ticketCodes.add(outboundBooking.getTicketCode());
            if (returnBooking != null) ticketCodes.add(returnBooking.getTicketCode());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "groupId", rtg.getGroupId(),
                    "bookingCode", outboundBooking.getTicketCode(),
                    "ticketCodes", ticketCodes));
            
        } catch (Exception e) {
             e.printStackTrace();
             org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
             return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private Long extractPayloadUserId(Map<String, Object> payload) {
        if (!(payload.get("user") instanceof Map<?, ?>)) return null;
        Map<?, ?> userPayload = (Map<?, ?>) payload.get("user");
        Object userId = userPayload.get("id");
        return userId == null ? null : Long.valueOf(userId.toString());
    }

    private void requireCurrentAuthenticatedPhone() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) return;
        try {
            requireCompletedBookingPhone(Long.valueOf(authentication.getName()));
        } catch (NumberFormatException ignored) {
            // Các cơ chế xác thực khác không dùng ID số sẽ được kiểm tra bằng payload bên dưới.
        }
    }

    private void rejectGuestCashPayment(String paymentMethod) {
        if (!"CASH".equalsIgnoreCase(paymentMethod)) return;
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean guest = authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName());
        if (guest) {
            throw new IllegalArgumentException(
                    "Khách vãng lai không được thanh toán khi lên xe. Vui lòng đăng nhập hoặc thanh toán bằng QR.");
        }
    }

    private com.smartbus.booking.entity.User requireCompletedBookingPhone(Long userId) {
        com.smartbus.booking.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if ("GOOGLE".equalsIgnoreCase(user.getAuthProvider())
                && (user.getPhone() == null || user.getPhone().startsWith("GG_"))) {
            throw new RuntimeException("Vui lòng cập nhật số điện thoại trước khi mua vé.");
        }
        return user;
    }
    
    private Booking createSingleBookingFromPayload(Long tripId, List<String> seats, Map<String, Object> cust, String pm, String tripType, String groupId, Long customerId, Long userVoucherId, Double discountAmount, Object pickupStopId, Object dropoffStopId) {
        com.smartbus.booking.entity.Trip trip = tripRepository.findById(tripId).orElseThrow();
        Booking b = new Booking();
        b.setCustomerName((String) cust.get("customerName"));
        b.setCustomerPhone((String) cust.get("customerPhone"));
        b.setCustomerEmail((String) cust.get("customerEmail"));
        b.setSeatNumbers(seats);
        
        double originalPrice = (trip.getPrice() != null ? trip.getPrice() : 0.0) * seats.size();
        b.setTotalPrice(Math.max(0, originalPrice - discountAmount));
        
        if (userVoucherId != null && discountAmount > 0) {
            b.setAppliedUserVoucherId(userVoucherId);
            b.setDiscountAmount(discountAmount);
        }
        
        b.setPaymentMethod(pm);
        if ("CASH".equals(pm)) {
            b.setStatus("PENDING");
        } else {
            b.setStatus("PAID");
        }
        b.setCreatedAt(LocalDateTime.now());
        b.setTrip(trip);
        b.setTripType(tripType);
        b.setRoundTripGroupId(groupId);
        if (customerId != null) {
             b.setUser(userRepository.findById(customerId).orElse(null));
        }
        
        seatService.bookSeats(tripId, seats); // Khóa ghế vĩnh viễn (sẽ tự động xóa Reservation)
        
        Booking saved = bookingRepository.save(b);
        routeStopService.saveBookingSelection(saved, pickupStopId, dropoffStopId);
        messagingTemplate.convertAndSend("/topic/admin/bookings/new", "NEW_BOOKING");
        
        // Tích điểm và Ghi nhận doanh thu ngay nếu trạng thái là PAID hoặc CHECKED_IN
        if ("PAID".equals(saved.getStatus()) || "CHECKED_IN".equals(saved.getStatus())) {
             String performedBy = "Admin";
             try {
                 org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                 if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                     Long currentUserId = Long.parseLong(auth.getName());
                     com.smartbus.booking.entity.User currentUser = userRepository.findById(currentUserId).orElse(null);
                     if (currentUser != null && !"ADMIN".equals(currentUser.getRole())) {
                         performedBy = currentUser.getFullName();
                     }
                 }
             } catch (Exception ignored) {}

             if ("Admin".equals(performedBy)) {
                 if ("CHECKED_IN".equals(saved.getStatus())) {
                     performedBy = "Lơ xe thu";
                 } else if ("BANK_TRANSFER".equals(saved.getPaymentMethod()) || "WALLET".equals(saved.getPaymentMethod())) {
                     if (saved.getUser() != null) {
                         performedBy = saved.getUser().getFullName();
                     } else {
                         performedBy = saved.getCustomerName();
                     }
                 }
             }

             String description = "CHECKED_IN".equals(saved.getStatus()) ? "Thu tiền vé tại xe - Vé #" + saved.getId() : "Thanh toán vé #" + saved.getId();
                 
             fundService.recordTransaction(saved.getPaymentMethod(), "INCOME", saved.getTotalPrice(), description, String.valueOf(saved.getId()), performedBy);

             if (saved.getUser() != null) {
                 com.smartbus.booking.entity.User user = saved.getUser();
                 int earnedPoints = LoyaltyPointPolicy.pointsFor(saved.getTotalPrice());
                 user.setLoyaltyPoints((user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0) + earnedPoints);
                 userRepository.save(user);
             }
        }
        
        try {
            if (b.getCustomerEmail() != null && !b.getCustomerEmail().isEmpty() && !b.getCustomerEmail().equals("no-email@smartbus.com")) {
                emailService.sendBookingConfirmation(saved);
            }
        } catch (Exception e) {}
        return saved;
    }

    @org.springframework.beans.factory.annotation.Value("${sepay.token}")
    private String sepayToken;

    @GetMapping("/check-payment")
    public ResponseEntity<?> checkPayment(
            @RequestParam("expectedContent") String expectedContent, 
            @RequestParam("expectedAmount") Double expectedAmount,
            @RequestParam(value = "sessionStartTime", required = false) String sessionStartTimeStr) {
        try {
            String urlStr = "https://my.sepay.vn/userapi/transactions/list";
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + sepayToken);
            conn.setRequestProperty("Content-Type", "application/json");

            if (conn.getResponseCode() == 200) {
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(response.toString());
                com.fasterxml.jackson.databind.JsonNode transactions = rootNode.get("transactions");
                
                if (transactions != null && transactions.isArray()) {
                    String expectedCompact = expectedContent.toLowerCase().replaceAll("[^a-z0-9]", "");
                    java.util.regex.Pattern pattern = null;
                    if (!expectedCompact.isEmpty()) {
                        StringBuilder regexBuilder = new StringBuilder("(^|[^a-z0-9])");
                        char[] chars = expectedCompact.toCharArray();
                        for (int i = 0; i < chars.length; i++) {
                            regexBuilder.append(chars[i]);
                            if (i < chars.length - 1) {
                                regexBuilder.append("[^a-z0-9]*");
                            }
                        }
                        regexBuilder.append("([^a-z0-9]|$)");
                        pattern = java.util.regex.Pattern.compile(regexBuilder.toString());
                    }

                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    
                    for (com.fasterxml.jackson.databind.JsonNode txn : transactions) {
                        String rawContent = txn.get("transaction_content").asText().toLowerCase();
                        String normalizedContent = java.text.Normalizer.normalize(rawContent, java.text.Normalizer.Form.NFD)
                                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace("đ", "d");
                                
                        double amountIn = Double.parseDouble(txn.get("amount_in").asText());
                        String txnDateStr = txn.get("transaction_date").asText();
                        java.time.LocalDateTime txnDate = java.time.LocalDateTime.parse(txnDateStr, formatter);
                        
                        // Chặn trường hợp nhận nhầm giao dịch (Collision) bằng Regex Boundary
                        boolean isContentMatch = pattern != null && pattern.matcher(normalizedContent).find();
                        
                        // Chỉ xét giao dịch sau khi khách hàng bắt đầu phiên (trừ hao 2 phút do đồng hồ lệch)
                        boolean isTimeValid = false; // Mặc định từ chối để bảo mật
                        if (sessionStartTimeStr != null) {
                            try {
                                java.time.Instant sessionStart = java.time.Instant.parse(sessionStartTimeStr);
                                java.time.ZoneId zoneVn = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
                                java.time.Instant txnInstant = txnDate.atZone(zoneVn).toInstant();
                                
                                isTimeValid = txnInstant.isAfter(sessionStart.minus(2, java.time.temporal.ChronoUnit.MINUTES));
                            } catch (Exception e) {
                                isTimeValid = false; // Định dạng sai -> Chặn giao dịch
                            }
                        } else {
                            isTimeValid = txnDate.isAfter(LocalDateTime.now().minusMinutes(15));
                        }
                        
                        if (isContentMatch && amountIn == expectedAmount && isTimeValid) {
                            return ResponseEntity.ok(Map.of("success", true, "message", "Payment found"));
                        }
                    }
                }
                return ResponseEntity.ok(Map.of("success", false, "message", "Not found yet"));
            } else {
                return ResponseEntity.status(500).body(Map.of("success", false, "error", "SePay API error: " + conn.getResponseCode()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/track")
    public ResponseEntity<?> trackBooking(@RequestParam("code") String code, @RequestParam("phone") String phone) {
        String normalizedCode = code == null ? "" : code.trim().toUpperCase();
        String normalizedPhone = phone == null ? "" : phone.replaceAll("\\s+", "");
        if (normalizedCode.isBlank() || !normalizedPhone.matches("\\d{10,11}")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã vé hoặc số điện thoại không hợp lệ."));
        }

        Optional<Booking> booking = bookingRepository
                .findByTicketCodeIgnoreCaseAndCustomerPhone(normalizedCode, normalizedPhone);
        if (booking.isEmpty() && normalizedCode.startsWith("GRP-")) {
            booking = bookingRepository
                    .findByRoundTripGroupIdAndCustomerPhone(normalizedCode, normalizedPhone)
                    .stream().findFirst();
        }
        return booking.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
