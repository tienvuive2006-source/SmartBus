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

    @Column(nullable = true)
    private Double departureLat;

    @Column(nullable = true)
    private Double departureLng;

    @Column(nullable = true)
    private Double arrivalLat;

    @Column(nullable = true)
    private Double arrivalLng;

    @Column(nullable = false)
    private String departureTime;

    @Column(nullable = true) // Hỗ trợ đồng bộ ngược cho các bản ghi cũ
    private String departureDate; // Định dạng chuẩn ISO: YYYY-MM-DD

    @Column(nullable = false)
    private String arrivalTime;

    @Column(nullable = true)
    private String duration;

    @Column(nullable = true)
    private Double price;

    @Column(nullable = true)
    private Double rating;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = true, columnDefinition = "LONGTEXT")
    private String imageUrl;

    private Boolean instantConfirmation;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore // Tránh lặp vô hạn JSON
    private java.util.List<Seat> seats;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Booking> bookings;
}
