package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "route_stops", indexes = {
        @Index(name = "idx_route_stops_route", columnList = "route_id, stop_order"),
        @Index(name = "idx_route_stops_trip", columnList = "trip_id, stop_order")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteStop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "route_id")
    private Long routeId;
    @Column(name = "trip_id")
    private Long tripId;
    @Column(nullable = false)
    private String name;
    @Column(length = 500)
    private String address;
    @Column(nullable = false, length = 20)
    private String stopType;
    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;
    @Column(name = "offset_minutes", nullable = false)
    private Integer offsetMinutes;
    private Double latitude;
    private Double longitude;
}
