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

    @com.smartbus.booking.annotation.AuditAction(action = "CREATE_BUS_TYPE", entityName = "BusType")
    @PostMapping
    public ResponseEntity<BusType> createBusType(@RequestBody BusType busType) {
        return ResponseEntity.ok(busTypeService.saveBusType(busType));
    }

    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_BUS_TYPE", entityName = "BusType")
    @PutMapping("/{id}")
    public ResponseEntity<BusType> updateBusType(@PathVariable("id") Long id, @RequestBody BusType busType) {
        return ResponseEntity.ok(busTypeService.updateBusType(id, busType));
    }

    @com.smartbus.booking.annotation.AuditAction(action = "DELETE_BUS_TYPE", entityName = "BusType")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusType(@PathVariable("id") Long id) {
        busTypeService.deleteBusType(id);
        return ResponseEntity.noContent().build();
    }
}
