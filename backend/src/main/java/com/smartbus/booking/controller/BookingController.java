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
    private com.smartbus.booking.service.SeatService seatService;

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    booking.setStatus(payload.get("status"));
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
            booking.setTrip(trip);

            // 1. TRỪ TIỀN VÍ (Wallet Deduction)
            if (payload.get("paymentMethod").equals("WALLET") && payload.get("user") != null) {
                Object userIdObj = ((Map<?, ?>) payload.get("user")).get("id");
                if (userIdObj != null) {
                    Long userId = Long.valueOf(userIdObj.toString());
                    com.smartbus.booking.entity.User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                    
                    double price = Double.valueOf(payload.get("totalPrice").toString());
                    double balance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
                    
                    if (balance < price) {
                        throw new RuntimeException("Số dư Ví Saomaifly không đủ để thực hiện giao dịch này!");
                    }
                    
                    user.setWalletBalance(balance - price);
                    userRepository.save(user);
                    booking.setUser(user);
                }
            }

            // 2. KHÓA GHẾ & ĐỒNG BỘ (Seat Locking)
            List<String> selectedSeats = (List<String>) payload.get("seatNumbers");
            if (selectedSeats != null && !selectedSeats.isEmpty()) {
                seatService.bookSeats(tripId, selectedSeats);
            }

            Booking saved = bookingRepository.save(booking);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }
}
