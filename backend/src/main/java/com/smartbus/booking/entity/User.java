package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Builder.Default
    private String role = "USER"; // USER, ADMIN, hoặc INSPECTOR

    @Column(nullable = true)
    private String email;

    @Builder.Default
    private Double walletBalance = 0.0; // Tích hợp ví tiền luôn cực xịn

    @Transient
    private int ticketCount;
}
