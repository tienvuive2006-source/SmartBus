package com.smartbus.booking.controller;

import com.smartbus.booking.entity.AuditLog;
import com.smartbus.booking.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ResponseEntity<org.springframework.data.domain.Page<AuditLog>> getLogs(
            @RequestParam(name = "type", defaultValue = "audit") String type,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        
        if ("error".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(auditLogRepository.findAllByActionNameOrderByIdDesc("SYSTEM_ERROR", pageable));
        }
        return ResponseEntity.ok(auditLogRepository.findAllByActionNameNotOrderByIdDesc("SYSTEM_ERROR", pageable));
    }
    
    @GetMapping("/error-count")
    public ResponseEntity<Long> getErrorCount() {
        return ResponseEntity.ok(auditLogRepository.countByActionName("SYSTEM_ERROR"));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteLog(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        auditLogRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/clear-errors")
    public ResponseEntity<Void> clearErrorLogs() {
        auditLogRepository.deleteAllByActionName("SYSTEM_ERROR");
        return ResponseEntity.noContent().build();
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/clear-normal")
    public ResponseEntity<Void> clearNormalLogs() {
        auditLogRepository.deleteAllByActionNameNot("SYSTEM_ERROR");
        return ResponseEntity.noContent().build();
    }
}
