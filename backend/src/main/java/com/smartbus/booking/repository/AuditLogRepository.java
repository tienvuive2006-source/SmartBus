package com.smartbus.booking.repository;

import com.smartbus.booking.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByIdDesc();
    
    org.springframework.data.domain.Page<AuditLog> findAllByActionNameNotOrderByIdDesc(String actionName, org.springframework.data.domain.Pageable pageable);
    
    org.springframework.data.domain.Page<AuditLog> findAllByActionNameOrderByIdDesc(String actionName, org.springframework.data.domain.Pageable pageable);
    
    long countByActionName(String actionName);
    
    @org.springframework.transaction.annotation.Transactional
    void deleteAllByActionName(String actionName);

    @org.springframework.transaction.annotation.Transactional
    void deleteAllByActionNameNot(String actionName);
}
