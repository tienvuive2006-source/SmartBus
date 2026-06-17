package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId; // Admin or User who performed the action

    @Column(name = "action_name", nullable = false)
    private String actionName; // e.g., "UPDATE_TRIP", "CANCEL_BOOKING"

    @Column(name = "entity_name")
    private String entityName; // e.g., "Trip", "Booking"

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON or String representation of changes

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
