package com.smartbus.booking.controller;

import com.smartbus.booking.entity.AuditLog;
import com.smartbus.booking.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getLogs() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByIdDesc());
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        auditLogRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/clear-errors")
    public ResponseEntity<Void> clearErrorLogs() {
        auditLogRepository.deleteAllByActionName("SYSTEM_ERROR");
        return ResponseEntity.noContent().build();
    }
}
