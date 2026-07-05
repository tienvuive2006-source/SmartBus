package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    private String imageUrl; // Link hình ảnh minh hoạ của xe
}
