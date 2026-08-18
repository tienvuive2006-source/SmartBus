package com.smartbus.booking.repository;

import com.smartbus.booking.entity.RefundRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    boolean existsByBookingId(Long bookingId);
    Optional<RefundRequest> findByBookingId(Long bookingId);
    List<RefundRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<RefundRequest> findAllByOrderByCreatedAtDesc();
    List<RefundRequest> findByStatusOrderByCreatedAtDesc(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT refund FROM RefundRequest refund WHERE refund.id = :id")
    Optional<RefundRequest> findByIdForUpdate(@Param("id") Long id);
}
