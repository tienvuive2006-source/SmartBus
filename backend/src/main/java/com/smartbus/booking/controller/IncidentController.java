package com.smartbus.booking.controller;

import com.smartbus.booking.entity.Incident;
import com.smartbus.booking.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class IncidentController {

    @Autowired
    private IncidentRepository incidentRepository;

    // Lấy danh sách sự cố cho Admin
    @GetMapping("/admin/incidents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Incident>> getAllIncidents() {
        return ResponseEntity.ok(incidentRepository.findAll());
    }

    // Tài xế báo cáo sự cố mới
    @PostMapping("/driver/incidents")
    @PreAuthorize("hasAnyRole('DRIVER', 'INSPECTOR')")
    public ResponseEntity<?> reportIncident(@RequestBody Incident incident) {
        incident.setStatus("PENDING");
        Incident savedIncident = incidentRepository.save(incident);
        return ResponseEntity.ok(savedIncident);
    }

    // Lấy danh sách sự cố của 1 chuyến xe (Dành cho tài xế/lơ xe xem lại)
    @GetMapping("/driver/incidents/trip/{tripId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'INSPECTOR')")
    public ResponseEntity<List<Incident>> getIncidentsByTrip(@PathVariable("tripId") Long tripId) {
        return ResponseEntity.ok(incidentRepository.findByTripIdOrderByCreatedAtDesc(tripId));
    }

    // Admin xử lý sự cố
    @PutMapping("/admin/incidents/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resolveIncident(@PathVariable("id") Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));
        incident.setStatus("RESOLVED");
        incidentRepository.save(incident);
        return ResponseEntity.ok(Map.of("message", "Incident resolved successfully"));
    }
}
