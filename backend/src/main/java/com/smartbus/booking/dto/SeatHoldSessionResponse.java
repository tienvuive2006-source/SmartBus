package com.smartbus.booking.dto;

import java.time.Instant;
import java.util.List;

public record SeatHoldSessionResponse(List<HeldSeat> seats, Instant expiresAt) {
    public record HeldSeat(Long tripId, String seatNumber) {
    }
}
