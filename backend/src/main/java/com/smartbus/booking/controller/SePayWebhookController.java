package com.smartbus.booking.controller;

import com.smartbus.booking.dto.SePayWebhookRequest;
import com.smartbus.booking.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class SePayWebhookController {

    private final WalletService walletService;

    @PostMapping("/sepay")
    public ResponseEntity<Map<String, Object>> handleSePayWebhook(@RequestBody SePayWebhookRequest request) {
        System.out.println("Nhận Webhook từ SePay: " + request.getTransactionContent() + " - Số tiền: " + request.getAmountIn());
        
        try {
            walletService.processTopupWebhook(request);
            // SePay yêu cầu trả về HTTP 200 OK kèm theo json body success: true
            return ResponseEntity.ok(Map.of("success", true, "message", "Webhook received successfully"));
        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý Webhook SePay: " + e.getMessage());
            // Trả về 200 để SePay không gửi lại (hoặc 500 tùy chiến lược, thường webhook lỗi nên log lại và trả 200)
            return ResponseEntity.ok(Map.of("success", false, "message", "Internal server error during webhook processing"));
        }
    }
}
