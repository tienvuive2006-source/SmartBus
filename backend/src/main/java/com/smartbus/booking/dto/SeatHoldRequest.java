package com.smartbus.booking.dto;

public record SeatHoldRequest(Long tripId, String seatNumber, String holdToken) {
}
