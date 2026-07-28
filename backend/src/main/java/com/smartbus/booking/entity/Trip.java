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

    @Column(nullable = true)
    private String assignedLicensePlate; // Biển số xe thực tế được phân công để chạy chuyến này

    @Column(nullable = true)
    private String assignedDriverUsername; // Tên đăng nhập của tài xế được phân công

    @Column(nullable = true)
    private String assignedDriverFullName; // Tên hiển thị của tài xế được phân công

    @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
    @Builder.Default
    private Boolean driverAccepted = false; // Trạng thái tài xế chấp nhận chuyến

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

    @Column(nullable = false, columnDefinition = "int default 24")
    @Builder.Default
    private Integer totalSeats = 24;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String routeData;

    private Boolean instantConfirmation;

    @Column(nullable = false, columnDefinition = "boolean DEFAULT true")
    @Builder.Default
    private Boolean isVisible = true; // Trạng thái hiển thị trên Home

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore // Tránh lặp vô hạn JSON
    @lombok.ToString.Exclude
    private java.util.List<Seat> seats;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    private java.util.List<Booking> bookings;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inspector_id", nullable = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "userAccount"})
    private Inspector inspector;


    @Column(nullable = true)
    @Builder.Default
    private String status = "PENDING"; // PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
}
