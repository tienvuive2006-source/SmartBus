package com.smartbus.booking.service.ai;

import java.util.Map;

public interface AiIntentHandler {
    boolean canHandle(String nonAccentMsg);
    Map<String, Object> handle(String nonAccentMsg, String authHeader, ChatContext ctx, String sessionId);
}
