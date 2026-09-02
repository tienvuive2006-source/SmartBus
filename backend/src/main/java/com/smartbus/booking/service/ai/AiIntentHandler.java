package com.smartbus.booking.service.ai;

import java.util.Map;

public interface AiIntentHandler {
    boolean canHandle(String nonAccentMsg);
    default boolean usesBookingContext() {
        return false;
    }
    Map<String, Object> handle(String nonAccentMsg, String authHeader, ChatContext ctx, String sessionId);
}
