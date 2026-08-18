package com.smartbus.booking.dto;

import lombok.Data;

@Data
public class RefundActionRequest {
    private String transactionCode;
    private String proofUrl;
    private String note;
}
