package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inspectors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inspector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column
    private String employeeCode;

    @Column(name = "current_station", length = 500)
    private String currentStation;

    // Liên kết với tài khoản đăng nhập bên bảng Users
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User userAccount;
}
