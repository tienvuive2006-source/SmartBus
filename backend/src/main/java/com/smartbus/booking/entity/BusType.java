package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bus_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Tên dòng xe (Ví dụ: Limousine VIP 21 Phòng)

    private Integer seatCount; // Số lượng ghế tiêu chuẩn (Để mặc định khởi tạo)
    
    private String description; // Mô tả sơ lược về tiện nghi
    
    private Double priceMultiplier = 1.0; // Hệ số nhân giá
    
    private String imageUrl; // Hình ảnh mẫu cho dòng xe này
}
