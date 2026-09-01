package com.smartbus.booking.dto;

public record RouteInspectorItemResponse(
    Long userId,
    String fullName,
    String phone,
    String avatarUrl,
    String role
) {}
