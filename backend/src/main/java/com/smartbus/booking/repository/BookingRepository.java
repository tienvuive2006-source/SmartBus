package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    @EntityGraph(attributePaths = {"trip", "trip.inspector", "seatNumbers"})
    List<Booking> findAllByOrderByCreatedAtDesc();
    
    @EntityGraph(attributePaths = {"trip", "trip.inspector", "seatNumbers"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Booking> findByTripId(Long tripId);
}
