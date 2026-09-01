package com.smartbus.booking.dto;

import com.smartbus.booking.entity.BusType;
import java.util.List;

public record RouteBusTypeConfigResponse(
    Long routeId,
    boolean unrestricted,
    List<BusType> busTypes,
    Long defaultBusTypeId
) {
}
