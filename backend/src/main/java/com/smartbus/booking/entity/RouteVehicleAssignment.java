package com.smartbus.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "route_vehicle_assignments",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_route_vehicle_assignment",
        columnNames = {"route_id", "bus_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteVehicleAssignment {

    public static final String PRIMARY = "PRIMARY";
    public static final String BACKUP = "BACKUP";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String role = PRIMARY;
}
