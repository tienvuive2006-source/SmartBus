package com.smartbus.booking.controller;

import com.smartbus.booking.dto.MileageAdjustmentRequest;
import com.smartbus.booking.dto.VehicleMaintenanceCompleteRequest;
import com.smartbus.booking.dto.VehicleMaintenanceRequest;
import com.smartbus.booking.entity.Bus;
import com.smartbus.booking.entity.VehicleMaintenance;
import com.smartbus.booking.entity.VehicleMileageLog;
import com.smartbus.booking.repository.UserRepository;
import com.smartbus.booking.service.VehicleMaintenanceService;
import com.smartbus.booking.service.VehicleMileageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicle-maintenance")
@RequiredArgsConstructor
public class VehicleMaintenanceController {
    private final VehicleMaintenanceService maintenanceService;
    private final VehicleMileageService mileageService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<VehicleMaintenance>> getAll() { return ResponseEntity.ok(maintenanceService.getAll()); }
    @PostMapping
    public ResponseEntity<VehicleMaintenance> create(@RequestBody VehicleMaintenanceRequest request) { return ResponseEntity.ok(maintenanceService.create(request)); }
    @PatchMapping("/{id}/start")
    public ResponseEntity<VehicleMaintenance> start(@PathVariable("id") Long id) { return ResponseEntity.ok(maintenanceService.start(id)); }
    @PatchMapping("/{id}/complete")
    public ResponseEntity<VehicleMaintenance> complete(@PathVariable("id") Long id,
            @RequestBody VehicleMaintenanceCompleteRequest request, Authentication authentication) {
        String performedBy = resolvePerformedBy(authentication);
        return ResponseEntity.ok(maintenanceService.complete(id, request, performedBy));
    }
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<VehicleMaintenance> cancel(@PathVariable("id") Long id) { return ResponseEntity.ok(maintenanceService.cancel(id)); }
    @GetMapping("/buses/{busId}/mileage")
    public ResponseEntity<List<VehicleMileageLog>> getMileage(@PathVariable("busId") Long busId) { return ResponseEntity.ok(mileageService.getLogs(busId)); }
    @PatchMapping("/buses/{busId}/mileage")
    public ResponseEntity<Bus> adjustMileage(@PathVariable("busId") Long busId, @RequestBody MileageAdjustmentRequest request) {
        return ResponseEntity.ok(mileageService.adjustMileage(busId, request.currentMileage(), request.note()));
    }

    private String resolvePerformedBy(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return "SYSTEM";
        String principal = authentication.getName();
        try {
            return userRepository.findById(Long.valueOf(principal))
                    .map(user -> user.getFullName() == null || user.getFullName().isBlank() ? principal : user.getFullName())
                    .orElse(principal);
        } catch (NumberFormatException ignored) {
            return principal;
        }
    }
}
