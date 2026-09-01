package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_mileage_log", uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_mileage_trip", columnNames = "trip_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleMileageLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Trip trip;

    @Column(nullable = false, length = 30)
    private String type;
    @Column(name = "previous_mileage", nullable = false)
    private Double previousMileage;
    @Column(name = "distance_added", nullable = false)
    private Double distanceAdded;
    @Column(name = "new_mileage", nullable = false)
    private Double newMileage;
    @Column(length = 500)
    private String note;
    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;
}
