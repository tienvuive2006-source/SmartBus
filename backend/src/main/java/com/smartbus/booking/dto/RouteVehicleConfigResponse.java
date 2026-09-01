package com.smartbus.booking.dto;

import java.util.List;

public record RouteVehicleConfigResponse(
    Long routeId,
    boolean unrestricted,
    List<RouteVehicleItemResponse> vehicles
) {
}
