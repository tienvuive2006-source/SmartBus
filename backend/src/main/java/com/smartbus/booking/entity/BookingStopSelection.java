package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_stop_selections", uniqueConstraints = @UniqueConstraint(columnNames = "booking_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingStopSelection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;
    private Long pickupStopId;
    private Long dropoffStopId;
    @Column(nullable = false)
    private String pickupName;
    @Column(nullable = false)
    private String dropoffName;
    private String pickupAddress;
    private String dropoffAddress;
    private Integer pickupOffsetMinutes;
    private Integer dropoffOffsetMinutes;
}
