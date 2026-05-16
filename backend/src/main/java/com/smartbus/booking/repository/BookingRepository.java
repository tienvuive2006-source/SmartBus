package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByOrderByCreatedAtDesc();
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
}
