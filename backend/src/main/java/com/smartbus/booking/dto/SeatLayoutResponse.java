package com.smartbus.booking.dto;

import java.util.List;

public record SeatLayoutResponse(
        Long busTypeId,
        String busTypeName,
        Integer seatCount,
        boolean configured,
        List<SeatLayoutItemResponse> seats
) {
}
