package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g. GIAM50K

    @Column(nullable = false)
    private Double discountAmount;

    @Column(nullable = false)
    private Integer pointsCost;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Transient
    @Builder.Default
    private Long usageCount = 0L;
}
