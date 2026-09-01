package com.smartbus.booking.dto;

public record SeatReleaseRequest(Long tripId, String seatNumber, String holdToken) {
}
