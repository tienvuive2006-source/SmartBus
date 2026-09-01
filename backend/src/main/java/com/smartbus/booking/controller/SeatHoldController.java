package com.smartbus.booking.controller;

import com.smartbus.booking.dto.*;
import com.smartbus.booking.service.SeatHoldService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/seat-holds")
public class SeatHoldController {
    private final SeatHoldService seatHoldService;

    public SeatHoldController(SeatHoldService seatHoldService) { this.seatHoldService = seatHoldService; }

    @PostMapping
    public ResponseEntity<?> hold(@RequestBody SeatHoldRequest request) {
        try {
            return ResponseEntity.ok(seatHoldService.hold(request.tripId(), request.seatNumber(), request.holdToken()));
        } catch (SeatHoldService.SeatUnavailableException conflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", conflict.getMessage()));
        } catch (IllegalArgumentException invalid) {
            return ResponseEntity.badRequest().body(Map.of("error", invalid.getMessage()));
        }
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@RequestBody SeatReleaseRequest request) {
        seatHoldService.release(request.tripId(), request.seatNumber(), request.holdToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/release-session")
    public ResponseEntity<Void> releaseSession(@RequestBody Map<String, String> request) {
        seatHoldService.releaseSession(request.get("holdToken"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session")
    public ResponseEntity<SeatHoldSessionResponse> getSession(@RequestParam("holdToken") String holdToken) {
        return ResponseEntity.ok(seatHoldService.getSession(holdToken));
    }
}
