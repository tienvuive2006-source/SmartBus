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

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

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

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> payload) {
        try {
            Booking booking = new Booking();
            booking.setCustomerName((String) payload.get("customerName"));
            booking.setCustomerPhone((String) payload.get("customerPhone"));
            booking.setCustomerEmail((String) payload.get("customerEmail"));
            booking.setSeatNumbers((List<String>) payload.get("seatNumbers"));
            booking.setTotalPrice(Double.valueOf(payload.get("totalPrice").toString()));
            booking.setPaymentMethod((String) payload.get("paymentMethod"));
            booking.setStatus((String) payload.get("status"));
            booking.setCreatedAt(LocalDateTime.now());

            Long tripId = Long.valueOf(((Map) payload.get("trip")).get("id").toString());
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

            booking.setTrip(trip);

            // 1. GÁN USER VÀ TRỪ TIỀN VÍ (NẾU LÀ WALLET)
            if (payload.get("user") != null) {
                Object userIdObj = ((Map<?, ?>) payload.get("user")).get("id");
                if (userIdObj != null) {
                    Long userId = Long.valueOf(userIdObj.toString());
                    com.smartbus.booking.entity.User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                    
                    if ("WALLET".equals(payload.get("paymentMethod"))) {
                        double price = Double.valueOf(payload.get("totalPrice").toString());
                        double balance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
                        
                        if (balance < price) {
                            throw new RuntimeException("Số dư Ví hệ thống không đủ để thực hiện giao dịch này!");
                        }
                        
                        user.setWalletBalance(balance - price);
                        userRepository.save(user);
                    }
                    booking.setUser(user);
                }
            }

            // 2. KHÓA GHẾ & ĐỒNG BỘ (Seat Locking)
            List<String> selectedSeats = (List<String>) payload.get("seatNumbers");
            if (selectedSeats != null && !selectedSeats.isEmpty()) {
                seatService.bookSeats(tripId, selectedSeats);
            }

            Booking saved = bookingRepository.save(booking);

            // 3. GỬI EMAIL XÁC NHẬN KÈM MÃ QR
            try {
                if (saved.getCustomerEmail() != null && !saved.getCustomerEmail().trim().isEmpty()) {
                    emailService.sendBookingConfirmation(saved);
                }
            } catch (Exception ex) {
                System.err.println("❌ Lỗi kích hoạt gửi email: " + ex.getMessage());
            }

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }
    @GetMapping("/check-payment")
    public ResponseEntity<?> checkPayment(
            @RequestParam("expectedContent") String expectedContent, 
            @RequestParam("expectedAmount") Double expectedAmount,
            @RequestParam(value = "sessionStartTime", required = false) String sessionStartTimeStr) {
        try {
            String sepayToken = "M2M5CO1H4ND6MK0YACOVST1AWEOLSVMFWEPSHP5PVIFC77AYV9XLQZLJUEYQZ4D2";
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
                    String normalizedExpected = expectedContent.toLowerCase().replaceAll("[^a-z0-9]", "");
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    
                    for (com.fasterxml.jackson.databind.JsonNode txn : transactions) {
                        String content = txn.get("transaction_content").asText().toLowerCase().replaceAll("[^a-z0-9]", "");
                        double amountIn = Double.parseDouble(txn.get("amount_in").asText());
                        String txnDateStr = txn.get("transaction_date").asText();
                        java.time.LocalDateTime txnDate = java.time.LocalDateTime.parse(txnDateStr, formatter);
                        
                        // Chặn trường hợp nhận nhầm giao dịch cũ bằng cách chỉ khớp chính xác nội dung
                        boolean isContentMatch = content.contains(normalizedExpected);
                        
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
}
