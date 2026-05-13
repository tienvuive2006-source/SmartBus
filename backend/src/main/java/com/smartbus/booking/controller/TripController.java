package com.smartbus.booking.controller;

import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Đảm bảo Frontend Vue không bị chặn CORS
public class TripController {

    private final TripService tripService;

    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.ok(tripService.getAllTrips());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Trip>> searchTrips(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(tripService.searchTrips(from, to));
    }

    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        return ResponseEntity.ok(tripService.saveTrip(trip));
    }
}
