package com.smartbus.booking.dto;

import java.util.List;

public record RouteDriverConfigResponse(
    Long routeId,
    boolean unrestricted,
    boolean sharedWithReverseRoute,
    List<RouteDriverItemResponse> drivers
) {
}
