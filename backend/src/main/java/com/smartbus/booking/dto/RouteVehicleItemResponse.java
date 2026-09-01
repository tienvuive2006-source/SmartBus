package com.smartbus.booking.dto;

public record RouteVehicleItemResponse(
    Long busId,
    String licensePlate,
    String busType,
    String status,
    String currentStation,
    String role
) {
}
