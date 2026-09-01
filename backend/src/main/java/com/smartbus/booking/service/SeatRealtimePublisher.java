package com.smartbus.booking.service;

import com.smartbus.booking.dto.SeatRealtimeEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class SeatRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public SeatRealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(Long tripId, String seatNumber, String status, LocalDateTime expiresAt) {
        messagingTemplate.convertAndSend(
                "/topic/trips/" + tripId + "/seats",
                new SeatRealtimeEvent(
                        tripId,
                        seatNumber,
                        status,
                        expiresAt == null ? null : expiresAt.atZone(ZoneId.systemDefault()).toInstant()
                )
        );
    }
}
