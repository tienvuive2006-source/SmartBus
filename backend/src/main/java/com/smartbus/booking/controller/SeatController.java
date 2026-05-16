package com.smartbus.booking.controller;

import com.smartbus.booking.entity.Seat;
import com.smartbus.booking.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Đảm bảo Vue truy cập được
public class SeatController {

    private final SeatService seatService;

    // Trả về sơ đồ 24 ghế của chuyến xe cụ thể
    @GetMapping("/{tripId}/seats")
    public ResponseEntity<List<Seat>> getSeatsByTrip(@PathVariable("tripId") Long tripId) {
        List<Seat> seats = seatService.getSeatsByTripId(tripId);
        return ResponseEntity.ok(seats);
    }

    // API THANH TOÁN THẬT: Khóa ghế trong SQL Server
    @PostMapping("/{tripId}/book-seats")
    public ResponseEntity<String> bookSeats(
            @PathVariable("tripId") Long tripId, 
            @RequestBody List<String> seatNumbers) {
        seatService.bookSeats(tripId, seatNumbers);
        return ResponseEntity.ok("Xác nhận giữ ghế thành công!");
    }
}
