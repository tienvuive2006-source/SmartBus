package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "round_trip_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundTripGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupId; // e.g. RT001

    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
