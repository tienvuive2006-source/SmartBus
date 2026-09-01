package com.smartbus.booking.dto;

import java.util.List;

public record RouteVehicleConfigRequest(
    boolean unrestricted,
    List<Long> primaryBusIds,
    List<Long> backupBusIds
) {
}
