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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "route_inspector_assignments",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_route_inspector_assignment",
        columnNames = {"route_id", "inspector_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteInspectorAssignment {

    public static final String PRIMARY = "PRIMARY";
    public static final String BACKUP = "BACKUP";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspector_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User inspector;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String role = PRIMARY;
}
