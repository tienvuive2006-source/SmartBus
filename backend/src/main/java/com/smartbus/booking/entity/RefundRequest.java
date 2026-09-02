package com.smartbus.booking.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refund_requests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_refund_requests_booking", columnNames = "booking_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "userReview"})
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @Column(nullable = false, length = 30)
    private String refundMethod; // WALLET, BANK_TRANSFER

    @Column(nullable = false)
    private Double refundAmount;

    @Column(nullable = false, length = 30)
    private String status; // PENDING, APPROVED, COMPLETED, NEEDS_INFO

    @Column(length = 100)
    private String bankName;

    @Column(length = 40)
    private String bankAccountNumber;

    @Column(length = 150)
    private String bankAccountName;

    @Column(length = 120)
    private String transactionCode;

    @Column(length = 500)
    private String proofUrl;

    @Column(length = 500)
    private String adminNote;

    @Column(length = 150)
    private String processedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
