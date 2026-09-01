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
    name = "route_driver_assignments",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_route_driver_assignment",
        columnNames = {"route_id", "driver_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteDriverAssignment {

    public static final String PRIMARY = "PRIMARY";
    public static final String BACKUP = "BACKUP";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String role = PRIMARY;
}
