package com.smartbus.booking.controller;

import com.smartbus.booking.entity.Booking;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.repository.BookingRepository;
import com.smartbus.booking.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inspector")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InspectorController {

    private final TripService tripService;
    private final BookingRepository bookingRepository;
    private final com.smartbus.booking.repository.InspectorRepository inspectorRepository;
    private final com.smartbus.booking.repository.TripRepository tripRepository;

    // 0. Lấy danh sách tất cả lơ xe (Dành cho Admin chọn)
    @GetMapping("/all")
    public ResponseEntity<List<com.smartbus.booking.entity.Inspector>> getAllInspectors() {
        return ResponseEntity.ok(inspectorRepository.findAll());
    }

    // 0.5. Phân công lơ xe cho chuyến xe
    @PutMapping("/assign-to-trip/{tripId}/{inspectorId}")
    public ResponseEntity<?> assignInspector(@PathVariable("tripId") Long tripId, @PathVariable("inspectorId") Long inspectorId) {
        return tripRepository.findById(tripId).map(trip -> {
            return inspectorRepository.findById(inspectorId).map(inspector -> {
                trip.setInspector(inspector);
                tripRepository.save(trip);
                return ResponseEntity.ok(Map.of("message", "Phân công thành công!"));
            }).orElse(ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy Lơ xe")));
        }).orElse(ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy chuyến xe")));
    }

    // 0.6. Hủy phân công lơ xe
    @PutMapping("/unassign-trip/{tripId}")
    public ResponseEntity<?> unassignInspector(@PathVariable("tripId") Long tripId) {
        return tripRepository.findById(tripId).map(trip -> {
            trip.setInspector(null);
            tripRepository.save(trip);
            return ResponseEntity.ok(Map.of("message", "Hủy phân công thành công!"));
        }).orElse(ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy chuyến xe")));
    }

    // 1. Lấy danh sách chuyến xe được phân công cho Lơ xe này
    @GetMapping("/trips/{inspectorId}")
    public ResponseEntity<List<Trip>> getTripsByInspector(@PathVariable("inspectorId") Long inspectorId) {
        return ResponseEntity.ok(tripService.getTripsByInspectorId(inspectorId));
    }

    // 2. Cập nhật trạng thái chuyến xe (Bắt đầu, Kết thúc...)
    @PutMapping("/trips/{tripId}/status")
    public ResponseEntity<Trip> updateTripStatus(
            @PathVariable("tripId") Long tripId,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        return ResponseEntity.ok(tripService.updateTripStatus(tripId, status));
    }

    // 3. Lấy danh sách hành khách (booking) của một chuyến xe
    @GetMapping("/trips/{tripId}/bookings")
    public ResponseEntity<List<Booking>> getBookingsByTrip(@PathVariable("tripId") Long tripId) {
        return ResponseEntity.ok(bookingRepository.findByTripId(tripId).stream()
                .filter(b -> !"CANCELLED".equals(b.getStatus()))
                .collect(java.util.stream.Collectors.toList()));
    }

    // 4. Quét mã QR / Check-in vé (Cập nhật trạng thái vé thành CHECKED_IN)
    @PutMapping("/bookings/{bookingId}/checkin")
    public ResponseEntity<?> checkInBooking(@PathVariable("bookingId") Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    // Cập nhật trạng thái
                    booking.setStatus("CHECKED_IN");
                    bookingRepository.save(booking);
                    return ResponseEntity.ok(Map.of("message", "Check-in thành công!", "booking", booking));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
