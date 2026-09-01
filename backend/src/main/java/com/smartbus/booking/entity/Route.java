package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String departurePoint;

    @Column(nullable = false)
    private String arrivalPoint;

    private Double departureLat;
    private Double departureLng;
    private Double arrivalLat;
    private Double arrivalLng;

    private String duration;

    private Double basePrice;
    
    @Column(length = 1000)
    private String imageUrl;

    @Builder.Default
    @Column(name = "is_visible", columnDefinition = "boolean default true")
    private Boolean isVisible = true;

    @Builder.Default
    @Column(name = "round_trip_enabled", columnDefinition = "boolean default false")
    private Boolean roundTripEnabled = false;

    @Column(columnDefinition = "TEXT")
    private String routeData;
}
