package com.smartbus.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RefundRequestResponse {
    private Long id;
    private BookingSummary booking;
    private UserSummary user;
    private String refundMethod;
    private Double refundAmount;
    private String status;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String transactionCode;
    private String proofUrl;
    private String adminNote;
    private String processedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    @Data
    @Builder
    public static class BookingSummary {
        private Long id;
        private String customerName;
        private String customerPhone;
    }

    @Data
    @Builder
    public static class UserSummary {
        private Long id;
        private String fullName;
        private String phone;
    }
}
