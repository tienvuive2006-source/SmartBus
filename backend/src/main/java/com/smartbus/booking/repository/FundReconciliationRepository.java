package com.smartbus.booking.repository;

import com.smartbus.booking.entity.FundReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundReconciliationRepository extends JpaRepository<FundReconciliation, Long> {
    List<FundReconciliation> findAllByOrderByCreatedAtDesc();
    List<FundReconciliation> findAllByCreatedAtBetweenOrderByCreatedAtDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
