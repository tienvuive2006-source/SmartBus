package com.smartbus.booking.dto;

import java.util.List;

public record RouteDriverConfigRequest(
    boolean unrestricted,
    List<Long> primaryDriverIds,
    List<Long> backupDriverIds
) {
}
