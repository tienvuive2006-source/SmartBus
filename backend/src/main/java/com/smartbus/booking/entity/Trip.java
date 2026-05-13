package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String busType;

    @Column(nullable = false)
    private String departurePoint;

    @Column(nullable = false)
    private String arrivalPoint;

    @Column(nullable = false)
    private String departureTime;

    @Column(nullable = false)
    private String arrivalTime;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false)
    private Integer availableSeats;

    private Boolean instantConfirmation;
}
