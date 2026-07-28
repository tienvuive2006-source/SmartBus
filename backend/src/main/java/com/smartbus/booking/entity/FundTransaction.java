package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fund_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String fundType; // CASH, BANK_TRANSFER, WALLET

    @Column(nullable = false, length = 20)
    private String transactionType; // INCOME, EXPENSE

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(length = 100)
    private String referenceId; // e.g., Booking ID

    @Column(length = 100)
    private String performedBy; // Username or "SYSTEM"

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();
}
