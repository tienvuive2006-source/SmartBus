package com.smartbus.booking.dto;

import java.util.List;

public record RouteBusTypeConfigRequest(
    boolean unrestricted,
    List<Long> busTypeIds,
    Long defaultBusTypeId
) {
}
