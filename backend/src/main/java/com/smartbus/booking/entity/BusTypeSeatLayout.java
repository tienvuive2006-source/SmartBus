package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "bus_type_seat_layout",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bus_type_seat_layout_number",
                columnNames = {"bus_type_id", "seat_number"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusTypeSeatLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_type_id", nullable = false)
    private BusType busType;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Column(name = "seat_floor", nullable = false)
    private Integer seatFloor;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 20)
    @Builder.Default
    private SeatType seatType = SeatType.STANDARD;
}
