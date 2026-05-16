package com.smartbus.booking.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.service.TripService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Đảm bảo Frontend Vue không bị chặn CORS
public class TripController {

    private final TripService tripService;

    // 1. LẤY TOÀN BỘ DANH SÁCH (READ ALL)
    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.ok(tripService.getAllTrips());
    }

    // 2. TÌM KIẾM CHUYẾN XE
    @GetMapping("/search")
    public ResponseEntity<List<Trip>> searchTrips(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "date", required = false) String date) {
        return ResponseEntity.ok(tripService.searchTrips(from, to, date));
    }

    // 3. CHI TIẾT 1 CHUYẾN XE (READ SINGLE)
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable("id") Long id) {
        Optional<Trip> tripOpt = tripService.getAllTrips().stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
        
        if (tripOpt.isPresent()) {
            return ResponseEntity.ok(tripOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. TẠO MỚI CHUYẾN XE (CREATE)
    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        return ResponseEntity.ok(tripService.saveTrip(trip));
    }

    // 5. CẬP NHẬT CHUYẾN XE (UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable("id") Long id, @RequestBody Trip trip) {
        return ResponseEntity.ok(tripService.updateTrip(id, trip));
    }

    // 6. XÓA CHUYẾN XE (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable("id") Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }
}
