package com.smartbus.booking.controller;

import com.smartbus.booking.entity.BusType;
import com.smartbus.booking.service.BusTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bus-types")
@RequiredArgsConstructor
@CrossOrigin("*") // Hỗ trợ kết nối API đa nguồn Frontend 
public class BusTypeController {

    private final BusTypeService busTypeService;

    @GetMapping
    public ResponseEntity<List<BusType>> getAllBusTypes() {
        return ResponseEntity.ok(busTypeService.getAllBusTypes());
    }

    @PostMapping
    public ResponseEntity<BusType> createBusType(@RequestBody BusType busType) {
        return ResponseEntity.ok(busTypeService.saveBusType(busType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusType> updateBusType(@PathVariable("id") Long id, @RequestBody BusType busType) {
        return ResponseEntity.ok(busTypeService.updateBusType(id, busType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusType(@PathVariable("id") Long id) {
        busTypeService.deleteBusType(id);
        return ResponseEntity.noContent().build();
    }
}
