package com.smartbus.booking.dto;

import com.smartbus.booking.entity.SeatType;

public record SeatLayoutItemRequest(String seatNumber, Integer seatFloor, SeatType seatType) {
}
