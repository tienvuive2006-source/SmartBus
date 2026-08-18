package com.smartbus.booking.dto;

import lombok.Data;

import java.util.List;

@Data
public class TicketExchangeRequest {
    private String exchangeType;
    private Long newTripId;
    private List<String> newSeatNumbers;
    private String reason;
    private String paymentMethod;
}
