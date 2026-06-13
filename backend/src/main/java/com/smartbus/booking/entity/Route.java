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
    
    @Column(length = 1000)
    private String imageUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String routeData;
}
