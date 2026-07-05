package com.smartbus.booking.controller;

import com.smartbus.booking.entity.Booking;
import com.smartbus.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.smartbus.booking.repository.TripRepository;
import com.smartbus.booking.repository.UserRepository;

@RestController
@RequestMapping("/admin/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.smartbus.booking.repository.ReviewRepository reviewRepository;

    @Autowired
    private com.smartbus.booking.service.SeatService seatService;

    @Autowired
    private com.smartbus.booking.service.EmailService emailService;

    @Autowired
    private com.smartbus.booking.repository.PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private com.smartbus.booking.repository.RoundTripGroupRepository roundTripGroupRepository;

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
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
                            
                            // Nếu đã thanh toán qua Ví, hoặc quét mã QR (thường mặc định PENDING chờ duyệt/PAID) 
                            // Ở đây ta hoàn tiền nếu Phương thức là Ví hoặc nếu đơn QR/Tiền mặt đã được đánh dấu PAID
                            if (isPaidViaWallet || (isPaidViaQR) || isAlreadyPaid) {
                                com.smartbus.booking.entity.User user = booking.getUser();
                                double currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
                                
                                // Áp dụng chính sách phí hủy vé 10%, hoàn lại 90%
                                double refundAmount = booking.getTotalPrice() != null ? (booking.getTotalPrice() * 0.9) : 0.0;
                                
                                user.setWalletBalance(currentBalance + refundAmount);
                                userRepository.save(user);
                            }
                        }
                    }

                    // Nếu đổi thành trạng thái CANCELLED, xóa đánh giá nếu có
                    if ("CANCELLED".equals(newStatus)) {
                        reviewRepository.findByBookingId(booking.getId()).ifPresent(review -> {
                            reviewRepository.delete(review);
                        });
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
            booking.setTotalPrice(serverCalculatedPrice);
            booking.setPaymentMethod((String) payload.get("paymentMethod"));
            booking.setStatus((String) payload.get("status"));
            booking.setCreatedAt(LocalDateTime.now());
            booking.setTrip(trip);

            // 1. GÁN USER VÀ TRỪ TIỀN VÍ (NẾU LÀ WALLET)
            if (payload.get("user") != null) {
                Object userIdObj = ((Map<?, ?>) payload.get("user")).get("id");
                if (userIdObj != null) {
                    Long userId = Long.valueOf(userIdObj.toString());
                    com.smartbus.booking.entity.User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                    
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
            Long outboundTripId = Long.valueOf(payload.get("outboundTripId").toString());
            List<String> outboundSeats = (List<String>) payload.get("outboundSeats");
            
            Long returnTripId = payload.containsKey("returnTripId") && payload.get("returnTripId") != null ? 
                                Long.valueOf(payload.get("returnTripId").toString()) : null;
            List<String> returnSeats = payload.containsKey("returnSeats") && payload.get("returnSeats") != null ? 
                                       (List<String>) payload.get("returnSeats") : null;

            // 1. Tính toán giá
            com.smartbus.booking.entity.Trip outTrip = tripRepository.findById(outboundTripId).orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi"));
            double totalAmount = (outTrip.getPrice() != null ? outTrip.getPrice() : 0.0) * outboundSeats.size();
            
            if (returnTripId != null && returnSeats != null) {
                com.smartbus.booking.entity.Trip retTrip = tripRepository.findById(returnTripId).orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến về"));
                totalAmount += (retTrip.getPrice() != null ? retTrip.getPrice() : 0.0) * returnSeats.size();
            }
            
            // 2. Giữ ghế (Seat Holding)
            seatService.holdSeats(outboundTripId, outboundSeats);
            if (returnTripId != null && returnSeats != null) {
                seatService.holdSeats(returnTripId, returnSeats);
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
                "totalAmount", savedPo.getTotalAmount()
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
            Long paymentOrderId = Long.valueOf(payload.get("paymentOrderId").toString());
            com.smartbus.booking.entity.PaymentOrder po = paymentOrderRepository.findById(paymentOrderId)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));
                
            if ("COMPLETED".equals(po.getStatus())) {
                return ResponseEntity.ok(Map.of("message", "Giao dịch này đã được xử lý thành công trước đó."));
            }
            
            String paymentMethod = (String) payload.get("paymentMethod");
            
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
            rtg.setGroupId("GRP" + po.getPaymentCode());
            rtg.setCustomerId(po.getCustomerId());
            rtg.setCreatedAt(LocalDateTime.now());
            roundTripGroupRepository.save(rtg);
            
            Map<String, Object> customerInfo = (Map<String, Object>) payload.get("customerInfo");
            
            // Tạo vé OUTBOUND
            Long outboundTripId = Long.valueOf(payload.get("outboundTripId").toString());
            List<String> outboundSeats = (List<String>) payload.get("outboundSeats");
            createSingleBookingFromPayload(outboundTripId, outboundSeats, customerInfo, paymentMethod, "OUTBOUND", rtg.getGroupId(), po.getCustomerId());
            
            // Tạo vé RETURN
            if (payload.containsKey("returnTripId") && payload.get("returnTripId") != null) {
                Long returnTripId = Long.valueOf(payload.get("returnTripId").toString());
                List<String> returnSeats = (List<String>) payload.get("returnSeats");
                createSingleBookingFromPayload(returnTripId, returnSeats, customerInfo, paymentMethod, "RETURN", rtg.getGroupId(), po.getCustomerId());
            }
            
            po.setStatus("COMPLETED");
            paymentOrderRepository.save(po);
            
            return ResponseEntity.ok(Map.of("success", true, "groupId", rtg.getGroupId()));
            
        } catch (Exception e) {
             e.printStackTrace();
             org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
             return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    private void createSingleBookingFromPayload(Long tripId, List<String> seats, Map<String, Object> cust, String pm, String tripType, String groupId, Long customerId) {
        com.smartbus.booking.entity.Trip trip = tripRepository.findById(tripId).orElseThrow();
        Booking b = new Booking();
        b.setCustomerName((String) cust.get("customerName"));
        b.setCustomerPhone((String) cust.get("customerPhone"));
        b.setCustomerEmail((String) cust.get("customerEmail"));
        b.setSeatNumbers(seats);
        b.setTotalPrice((trip.getPrice() != null ? trip.getPrice() : 0.0) * seats.size());
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
        
        try {
            if (b.getCustomerEmail() != null && !b.getCustomerEmail().isEmpty() && !b.getCustomerEmail().equals("no-email@smartbus.com")) {
                emailService.sendBookingConfirmation(saved);
            }
        } catch (Exception e) {}
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
    public ResponseEntity<?> trackBooking(@RequestParam("code") Long code, @RequestParam("phone") String phone) {
        return bookingRepository.findByIdAndCustomerPhone(code, phone)
                .map(booking -> ResponseEntity.ok(booking))
                .orElseGet(() -> ResponseEntity.status(404).body((Booking) null));
    }
}
