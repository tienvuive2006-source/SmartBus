package com.smartbus.booking.dto;

public record InspectorListItemResponse(
    Long id,
    String fullName,
    String phone,
    String employeeCode,
    String avatarUrl
) {}
