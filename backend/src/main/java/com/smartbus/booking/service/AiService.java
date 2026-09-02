package com.smartbus.booking.service;

import com.smartbus.booking.service.ai.AiIntentHandler;
import com.smartbus.booking.service.ai.ChatContext;
import com.smartbus.booking.service.ai.EntityExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final long SESSION_TTL_MS = 30 * 60 * 1000L;
    private static final int MAX_SESSIONS = 10_000;

    private final EntityExtractionService entityExtractionService;
    private final List<AiIntentHandler> intentHandlers; // Spring Boot will auto-inject the 3 handlers in @Order
    
    private final Map<String, ChatContext> activeSessions = new java.util.concurrent.ConcurrentHashMap<>();

    public Map<String, Object> processMessage(String message, String sessionId, String authHeader) {
        long now = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> now - entry.getValue().lastUpdated > SESSION_TTL_MS);
        if (activeSessions.size() >= MAX_SESSIONS && !activeSessions.containsKey(sessionId)) {
            activeSessions.entrySet().stream()
                    .min(java.util.Comparator.comparingLong(entry -> entry.getValue().lastUpdated))
                    .ifPresent(entry -> activeSessions.remove(entry.getKey()));
        }

        String lowerMsg = message.toLowerCase();
        String nonAccentMsg = entityExtractionService.removeAccents(lowerMsg);
        
        ChatContext ctx = activeSessions.computeIfAbsent(sessionId, k -> new ChatContext());
        ctx.lastUpdated = System.currentTimeMillis();
        
        // Chọn ý định trước để câu hỏi FAQ không vô tình làm bẩn ngữ cảnh tìm vé.
        for (AiIntentHandler handler : intentHandlers) {
            if (handler.canHandle(nonAccentMsg)) {
                if (handler.usesBookingContext()) {
                    entityExtractionService.extractBookingEntities(nonAccentMsg, ctx);
                }
                Map<String, Object> response = handler.handle(nonAccentMsg, authHeader, ctx, sessionId);
                if (response != null) return response;
            }
        }
        
        return Map.of("text", "Xin lỗi, tôi chưa hiểu ý bạn.", "action", "none");
    }
}
