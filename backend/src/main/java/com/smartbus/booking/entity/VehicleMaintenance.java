package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_maintenance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleMaintenance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;
    @Column(name = "maintenance_type", nullable = false, length = 60)
    private String maintenanceType;
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "SCHEDULED";
    @Column(length = 1000)
    private String description;
    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;
    @Column(name = "expected_end")
    private LocalDateTime expectedEnd;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(name = "odometer_at_service")
    private Double odometerAtService;
    @Column(name = "estimated_cost")
    private Double estimatedCost;
    @Column(name = "actual_cost")
    private Double actualCost;
    @Column(name = "garage_name", length = 200)
    private String garageName;
    @Column(length = 1000)
    private String notes;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
