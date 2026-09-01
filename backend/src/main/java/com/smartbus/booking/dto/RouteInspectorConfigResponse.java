package com.smartbus.booking.dto;

import java.util.List;

public record RouteInspectorConfigResponse(
    Long routeId,
    boolean unrestricted,
    boolean sharedWithReverseRoute,
    List<RouteInspectorItemResponse> inspectors
) {}
