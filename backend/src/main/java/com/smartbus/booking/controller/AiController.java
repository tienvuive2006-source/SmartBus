package com.smartbus.booking.controller;

import com.smartbus.booking.service.AiService;
import com.smartbus.booking.service.AiArticleGeneratorService;
import com.smartbus.booking.dto.AiArticleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;
    private final AiArticleGeneratorService aiArticleGeneratorService;

    @PostMapping("/chat")
    public ResponseEntity<?> processChat(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String message = payload.get("message");
        String sessionId = payload.get("sessionId");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nội dung tin nhắn không được để trống"));
        }
        message = message.trim();
        if (message.length() > 1000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tin nhắn không được vượt quá 1000 ký tự"));
        }
        if (sessionId == null || sessionId.isBlank() || sessionId.length() > 128) {
            sessionId = UUID.randomUUID().toString();
        }
        
        Map<String, Object> response = aiService.processMessage(message, sessionId, authHeader);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-article")
    public ResponseEntity<?> generateArticle(@RequestBody AiArticleRequest request) {
        if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Topic is required"));
        }
        
        try {
            Map<String, String> aiResult = aiArticleGeneratorService.generateArticle(request.getTopic());
            return ResponseEntity.ok(aiResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
