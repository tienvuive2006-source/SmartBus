package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_exchanges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, length = 20)
    private String exchangeType;

    @Column(nullable = false)
    private Long oldTripId;

    @Column(nullable = false)
    private Long newTripId;

    @Column(nullable = false, length = 500)
    private String oldSeatNumbers;

    @Column(nullable = false, length = 500)
    private String newSeatNumbers;

    @Column(nullable = false)
    private Double oldPrice;

    @Column(nullable = false)
    private Double newPrice;

    @Column(nullable = false)
    private Double priceDifference;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 30)
    private String paymentMethod;

    @Column(unique = true, length = 80)
    private String paymentCode;

    @Column(length = 500)
    private String reservationIds;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime exchangedAt;
}
