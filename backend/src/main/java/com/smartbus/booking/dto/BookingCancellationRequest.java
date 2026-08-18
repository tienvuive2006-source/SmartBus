package com.smartbus.booking.dto;

import lombok.Data;

@Data
public class BookingCancellationRequest {
    private String reason;
    private String refundMethod;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
}
