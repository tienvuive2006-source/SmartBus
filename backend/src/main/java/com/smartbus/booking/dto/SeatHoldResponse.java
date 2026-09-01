package com.smartbus.booking.dto;

import java.time.Instant;

public record SeatHoldResponse(Long tripId, String seatNumber, String status, Instant expiresAt) {
}
