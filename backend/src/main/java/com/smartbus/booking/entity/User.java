package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
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

    @Column(length = 1000)
    private String avatarUrl;

    @Builder.Default
    private Double walletBalance = 0.0; // Tích hợp ví tiền luôn cực xịn

    @Builder.Default
    @Column(name = "auth_provider")
    private String authProvider = "LOCAL"; // LOCAL hoặc GOOGLE

    @Transient
    private int ticketCount;

    @Transient
    private Double totalSpent;

    @Transient
    private long activeTripCount;

    @Builder.Default
    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Builder.Default
    @Column(name = "is_locked")
    private Boolean isLocked = false;

    @Builder.Default
    @Column(name = "driver_status")
    private String driverStatus = "FREE"; // FREE, DRIVING, ON_LEAVE, SUSPENDED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
