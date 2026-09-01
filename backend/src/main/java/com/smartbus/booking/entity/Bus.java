package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "bus")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String licensePlate; // Biển số xe (NVARCHAR trong SQL Server nhờ flag Hibernate)

    @Column(nullable = false)
    private String busType; // Loại xe (Limousine VIP, Giường Nằm, etc.)

    @Column(nullable = false)
    private String status; // Trạng thái: "ĐANG CHẠY", "BẢO TRÌ", "ĐANG NGHỈ"
    
    private String currentStation; // Trạm/Vị trí hiện tại của xe

    @Column(name = "inspection_expiry_date")
    private LocalDate inspectionExpiryDate; // Ngày hết hạn đăng kiểm

    private String imageUrl; // Link hình ảnh minh hoạ của xe

    @Builder.Default
    @Column(name = "current_mileage", nullable = false, columnDefinition = "double precision default 0")
    private Double currentMileage = 0.0;

    @Builder.Default
    @Column(name = "last_maintenance_mileage", nullable = false, columnDefinition = "double precision default 0")
    private Double lastMaintenanceMileage = 0.0;

    @Builder.Default
    @Column(name = "maintenance_interval_km", nullable = false, columnDefinition = "integer default 10000")
    private Integer maintenanceIntervalKm = 10000;

    @Builder.Default
    @Column(name = "maintenance_alert_level", nullable = false, columnDefinition = "integer default 0")
    private Integer maintenanceAlertLevel = 0;
}
