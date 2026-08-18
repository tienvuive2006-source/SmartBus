package com.smartbus.booking.controller;

import com.smartbus.booking.dto.RefundActionRequest;
import com.smartbus.booking.dto.RefundBankUpdateRequest;
import com.smartbus.booking.service.RefundRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/refund-requests")
@RequiredArgsConstructor
public class RefundRequestController {

    private final RefundRequestService service;

    @GetMapping("/me")
    public ResponseEntity<?> mine(Authentication authentication) {
        return ResponseEntity.ok(service.findMine(userId(authentication)));
    }

    @GetMapping("/admin")
    public ResponseEntity<?> admin(@RequestParam(value = "status", defaultValue = "ALL") String status) {
        return ResponseEntity.ok(service.findAdmin(status));
    }

    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable("id") Long id, @RequestBody RefundActionRequest request, Authentication authentication) {
        return action(() -> service.approve(id, userId(authentication), request));
    }

    @PutMapping("/admin/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable("id") Long id, @RequestBody RefundActionRequest request, Authentication authentication) {
        return action(() -> service.complete(id, userId(authentication), request));
    }

    @PutMapping("/admin/{id}/request-info")
    public ResponseEntity<?> requestInformation(@PathVariable("id") Long id, @RequestBody RefundActionRequest request, Authentication authentication) {
        return action(() -> service.requestInformation(id, userId(authentication), request));
    }

    @PutMapping("/me/{id}/resubmit")
    public ResponseEntity<?> resubmit(@PathVariable("id") Long id, @RequestBody RefundBankUpdateRequest request, Authentication authentication) {
        return action(() -> service.resubmit(id, userId(authentication), request));
    }

    private ResponseEntity<?> action(java.util.concurrent.Callable<?> callable) {
        try {
            return ResponseEntity.ok(callable.call());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Không thể xử lý yêu cầu hoàn tiền."));
        }
    }

    private Long userId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Vui lòng đăng nhập.");
        }
        return Long.parseLong(authentication.getName());
    }
}
