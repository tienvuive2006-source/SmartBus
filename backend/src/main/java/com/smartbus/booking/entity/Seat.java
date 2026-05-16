package com.smartbus.booking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatNumber; // Ví dụ: A1, A2, B1, B2

    @Column(nullable = false)
    private Integer seatFloor; // 1 hoặc 2 (Tầng 1, Tầng 2)

    @Column(nullable = false)
    private Boolean isBooked; // true: Đã bán, false: Trống

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonIgnore // Tránh lặp vô tận vòng tròn khi render JSON
    private Trip trip;
}
