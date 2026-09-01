package com.smartbus.booking.dto;

import java.time.LocalDateTime;

public record VehicleMaintenanceRequest(Long busId, String maintenanceType, String description,
        LocalDateTime scheduledStart, LocalDateTime expectedEnd, Double estimatedCost,
        String garageName, String notes, Integer maintenanceIntervalKm) {
}
