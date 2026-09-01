package com.smartbus.booking.dto;

public record RouteDriverItemResponse(
    Long driverId,
    String fullName,
    String phone,
    String avatarUrl,
    String role
) {
}
