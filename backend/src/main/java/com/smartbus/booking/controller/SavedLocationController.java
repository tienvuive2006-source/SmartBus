package com.smartbus.booking.controller;

import com.smartbus.booking.entity.SavedLocation;
import com.smartbus.booking.service.SavedLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/saved-locations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SavedLocationController {

    private final SavedLocationService service;

    @GetMapping
    public ResponseEntity<List<SavedLocation>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @com.smartbus.booking.annotation.AuditAction(action = "CREATE_SAVED_LOCATION", entityName = "SavedLocation")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SavedLocation request) {
        try {
            return ResponseEntity.ok(service.create(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_SAVED_LOCATION", entityName = "SavedLocation")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody SavedLocation request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @com.smartbus.booking.annotation.AuditAction(action = "DELETE_SAVED_LOCATION", entityName = "SavedLocation")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
