package com.smartbus.booking.dto;

public record VehicleMaintenanceCompleteRequest(Double actualCost, Double currentMileage, String notes, String fundType) {
}
