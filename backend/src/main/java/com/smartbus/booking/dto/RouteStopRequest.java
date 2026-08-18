package com.smartbus.booking.dto;

import lombok.Data;

@Data
public class RouteStopRequest {
    private String name;
    private String address;
    private String stopType;
    private Integer stopOrder;
    private Integer offsetMinutes;
    private Double latitude;
    private Double longitude;
}
