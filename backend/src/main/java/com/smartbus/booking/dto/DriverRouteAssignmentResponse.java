package com.smartbus.booking.dto;

public record DriverRouteAssignmentResponse(
    Long routeId,
    String routeName,
    String departurePoint,
    String arrivalPoint,
    String role
) {
}
