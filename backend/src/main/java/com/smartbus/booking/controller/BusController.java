package com.smartbus.booking.controller;

import com.smartbus.booking.entity.Bus;
import com.smartbus.booking.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buses")
@RequiredArgsConstructor
@CrossOrigin("*") // Đảm bảo kết nối mượt mà từ cổng Frontend
public class BusController {

    private final BusService busService;

    // 1. Lấy tất cả xe
    @GetMapping
    public ResponseEntity<List<Bus>> getAllBuses() {
        return ResponseEntity.ok(busService.getAllBuses());
    }

    // 2. Khởi tạo xe mới
    @PostMapping
    public ResponseEntity<Bus> createBus(@RequestBody Bus bus) {
        return ResponseEntity.ok(busService.saveBus(bus));
    }

    // 3. Cập nhật thông tin xe (Bọc explicit path variable cho Spring Boot 3.2)
    @PutMapping("/{id}")
    public ResponseEntity<Bus> updateBus(@PathVariable("id") Long id, @RequestBody Bus bus) {
        return ResponseEntity.ok(busService.updateBus(id, bus));
    }

    // 4. Xóa sổ xe khỏi kho (Bọc explicit path variable cho Spring Boot 3.2)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBus(@PathVariable("id") Long id) {
        busService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }
}
