package com.smartbus.booking.controller;

import com.smartbus.booking.dto.RouteStopRequest;
import com.smartbus.booking.entity.Booking;
import com.smartbus.booking.repository.BookingRepository;
import com.smartbus.booking.service.RouteStopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/route-stops")
@RequiredArgsConstructor
public class RouteStopController {
    private final RouteStopService routeStopService;
    private final BookingRepository bookingRepository;

    @GetMapping("/routes/{routeId}")
    public ResponseEntity<?> getRouteStops(@PathVariable("routeId") Long routeId) {
        return ResponseEntity.ok(routeStopService.getRouteStops(routeId));
    }

    @PutMapping("/routes/{routeId}")
    public ResponseEntity<?> replaceRouteStops(@PathVariable("routeId") Long routeId, @RequestBody List<RouteStopRequest> stops) {
        return ResponseEntity.ok(routeStopService.replaceRouteStops(routeId, stops));
    }

    @GetMapping("/trips/{tripId}")
    public ResponseEntity<?> getTripStops(@PathVariable("tripId") Long tripId) {
        return ResponseEntity.ok(routeStopService.getResolvedTripStops(tripId));
    }

    @GetMapping("/me/bookings")
    public ResponseEntity<?> getMySelections(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        Long userId = Long.valueOf(authentication.getName());
        List<Long> bookingIds = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(Booking::getId).toList();
        return ResponseEntity.ok(routeStopService.getSelections(bookingIds));
    }

    @GetMapping("/admin/bookings")
    public ResponseEntity<?> getAdminSelections(@RequestParam(value = "ids") List<Long> bookingIds) {
        return ResponseEntity.ok(routeStopService.getSelections(bookingIds));
    }

    @GetMapping("/driver/trips/{tripId}/passengers")
    public ResponseEntity<?> getDriverStopPassengers(@PathVariable("tripId") Long tripId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return ResponseEntity.ok(routeStopService.getDriverStopPassengers(
                tripId, Long.valueOf(authentication.getName()), admin));
    }
}
