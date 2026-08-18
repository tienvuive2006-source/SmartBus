package com.smartbus.booking.dto;

import lombok.Data;

@Data
public class RefundBankUpdateRequest {
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
}
