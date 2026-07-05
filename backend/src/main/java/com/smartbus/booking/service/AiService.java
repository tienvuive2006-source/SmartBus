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

    private final EntityExtractionService entityExtractionService;
    private final List<AiIntentHandler> intentHandlers; // Spring Boot will auto-inject the 3 handlers in @Order
    
    private final Map<String, ChatContext> activeSessions = new java.util.concurrent.ConcurrentHashMap<>();

    public Map<String, Object> processMessage(String message, String sessionId, String authHeader) {
        String lowerMsg = message.toLowerCase();
        String nonAccentMsg = entityExtractionService.removeAccents(lowerMsg);
        
        ChatContext ctx = activeSessions.computeIfAbsent(sessionId, k -> new ChatContext());
        ctx.lastUpdated = System.currentTimeMillis();
        
        // 1. EXTRACTION: Bóc tách thực thể (Địa danh, ngày tháng) trước
        entityExtractionService.extractBookingEntities(nonAccentMsg, ctx);

        // 2. ROUTING: Tìm Handler phù hợp nhất để xử lý
        for (AiIntentHandler handler : intentHandlers) {
            if (handler.canHandle(nonAccentMsg)) {
                Map<String, Object> response = handler.handle(nonAccentMsg, authHeader, ctx, sessionId);
                
                // Chỉ xóa session khi AI thực sự thực hiện hành động tìm kiếm (đã đủ thông tin từ/đến)
                if (response != null && "search".equals(response.get("action"))) {
                     activeSessions.remove(sessionId);
                }
                
                if (response != null) return response;
            }
        }
        
        return Map.of("text", "Xin lỗi, tôi chưa hiểu ý bạn.", "action", "none");
    }
}
