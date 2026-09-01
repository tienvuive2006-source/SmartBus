package com.smartbus.booking.dto;

import com.smartbus.booking.entity.SeatType;

public record SeatLayoutItemResponse(String seatNumber, Integer seatFloor, SeatType seatType) {
}
