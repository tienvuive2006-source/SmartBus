package com.smartbus.booking.dto;

import java.util.List;

public record DriverRouteConfigRequest(
    List<Long> primaryRouteIds,
    List<Long> backupRouteIds
) {
}
