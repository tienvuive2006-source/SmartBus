package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
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

    @Column(unique = true, length = 50)
    private String username;

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

    // Driver profile fields. They stay nullable so existing staff accounts remain valid.
    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "citizen_id", length = 20)
    private String citizenId;

    @Column(name = "citizen_id_issue_date")
    private LocalDate citizenIdIssueDate;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 30)
    private String emergencyContactPhone;

    @Column(name = "driver_license_class", length = 20)
    private String driverLicenseClass;

    @Column(name = "driver_license_number", length = 50)
    private String driverLicenseNumber;

    @Column(name = "driver_license_issue_date")
    private LocalDate driverLicenseIssueDate;

    @Column(name = "driver_license_expiry_date")
    private LocalDate driverLicenseExpiryDate;

    @Column(name = "driving_experience_years")
    private Integer drivingExperienceYears;

    @Column(name = "driver_shift", length = 50)
    private String driverShift;

    @Column(name = "driver_notes", length = 1000)
    private String driverNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
