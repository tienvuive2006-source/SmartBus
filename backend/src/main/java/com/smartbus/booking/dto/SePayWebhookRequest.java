package com.smartbus.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SePayWebhookRequest {
    
    @JsonProperty("id")
    private Long id;

    @JsonProperty("gateway")
    private String gateway;

    @JsonProperty("transactionDate")
    private String transactionDate; // Format: "2023-08-25 09:27:08"

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("subAccount")
    private String subAccount;

    @JsonProperty("amountIn")
    private Double amountIn;

    @JsonProperty("amountOut")
    private Double amountOut;

    @JsonProperty("accumulated")
    private Double accumulated;

    @JsonProperty("code")
    private String code;

    @JsonProperty("transactionContent")
    private String transactionContent;

    @JsonProperty("referenceCode")
    private String referenceCode;

    @JsonProperty("body")
    private String body;
}
