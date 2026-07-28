package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_reconciliations")
@Data
@NoArgsConstructor
public class FundReconciliation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fundType; // CASH, BANK_TRANSFER, WALLET

    @Column(nullable = false)
    private Double systemBalance; // Số dư trên phần mềm

    @Column(nullable = false)
    private Double actualBalance; // Số dư thực tế

    @Column(nullable = false)
    private Double discrepancy; // Chênh lệch (actual - system)

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private String status; // COMPLETED

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private String performedBy; // Admin name who performed reconciliation
}
