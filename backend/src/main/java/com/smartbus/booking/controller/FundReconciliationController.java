package com.smartbus.booking.controller;

import com.smartbus.booking.entity.FundReconciliation;
import com.smartbus.booking.service.FundReconciliationService;
import com.smartbus.booking.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/fund-reconciliations")
@CrossOrigin("*")
@RequiredArgsConstructor
public class FundReconciliationController {

    private final FundReconciliationService reconciliationService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<FundReconciliation>> getHistory(
            @RequestParam(name = "startDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {
        return ResponseEntity.ok(reconciliationService.getHistory(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<?> createReconciliation(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> payload) {
        
        try {
            String performedBy = "Admin"; // Default
            if (token != null && token.startsWith("Bearer ")) {
                performedBy = jwtService.extractPhone(token.substring(7));
            }

            String fundType = (String) payload.get("fundType");
            Double actualBalance = Double.valueOf(payload.get("actualBalance").toString());
            String reason = (String) payload.get("reason");

            if (fundType == null || actualBalance == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "fundType and actualBalance are required"));
            }

            FundReconciliation saved = reconciliationService.createReconciliation(fundType, actualBalance, reason, performedBy);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
