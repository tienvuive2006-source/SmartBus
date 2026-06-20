package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    @EntityGraph(attributePaths = {"trip", "trip.inspector", "seatNumbers", "user"})
    List<Booking> findAllByOrderByCreatedAtDesc();
    
    @EntityGraph(attributePaths = {"trip", "trip.inspector", "seatNumbers", "user"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Booking> findByTripId(Long tripId);

    @org.springframework.data.jpa.repository.Query("SELECT b.user.id, SUM(SIZE(b.seatNumbers)) FROM Booking b WHERE b.status != 'CANCELLED' AND b.user IS NOT NULL GROUP BY b.user.id")
    List<Object[]> countTicketsPerUser();

    @EntityGraph(attributePaths = {"seatNumbers"})
    List<Booking> findByStatusIn(List<String> statuses);
}
