package com.smartbus.booking.controller;

import com.smartbus.booking.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<?> processChat(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        String sessionId = payload.getOrDefault("sessionId", "default_session");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }
        
        Map<String, Object> response = aiService.processMessage(message, sessionId);
        return ResponseEntity.ok(response);
    }
}
