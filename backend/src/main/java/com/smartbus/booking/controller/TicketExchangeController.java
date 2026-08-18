package com.smartbus.booking.controller;

import com.smartbus.booking.dto.TicketExchangeRequest;
import com.smartbus.booking.service.TicketExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket-exchanges")
@RequiredArgsConstructor
public class TicketExchangeController {

    private final TicketExchangeService ticketExchangeService;

    @GetMapping("/admin")
    public ResponseEntity<?> getAdminExchangeHistory() {
        return ResponseEntity.ok(ticketExchangeService.getAdminExchangeHistory());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyCompletedExchanges(Authentication authentication) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ticketExchangeService.getMyCompletedExchanges(currentUserId(authentication)));
    }

    @GetMapping("/bookings/{bookingId}/options")
    public ResponseEntity<?> getOptions(@PathVariable("bookingId") Long bookingId, Authentication authentication) {
        return ResponseEntity.ok(ticketExchangeService.getOptions(bookingId, currentUserId(authentication)));
    }

    @GetMapping("/bookings/{bookingId}/trips/{tripId}/seats")
    public ResponseEntity<?> getTripSeats(
            @PathVariable("bookingId") Long bookingId,
            @PathVariable("tripId") Long tripId,
            Authentication authentication) {
        return ResponseEntity.ok(ticketExchangeService.getTripSeats(bookingId, tripId, currentUserId(authentication)));
    }

    @PostMapping("/bookings/{bookingId}")
    public ResponseEntity<?> exchange(
            @PathVariable("bookingId") Long bookingId,
            @RequestBody TicketExchangeRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ticketExchangeService.exchange(bookingId, currentUserId(authentication), request));
    }

    @GetMapping("/payments/{exchangeId}/status")
    public ResponseEntity<?> checkPayment(
            @PathVariable("exchangeId") Long exchangeId,
            Authentication authentication) {
        return ResponseEntity.ok(ticketExchangeService.checkQrPayment(exchangeId, currentUserId(authentication)));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Vui lòng đăng nhập để đổi vé.");
        }
        return Long.parseLong(authentication.getName());
    }
}
