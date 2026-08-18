package com.smartbus.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverStopPassengerDto {
    private Long bookingId;
    private Long pickupStopId;
    private Long dropoffStopId;
    private String customerName;
    private String customerPhone;
    private List<String> seatNumbers;
    private int passengerCount;
}
