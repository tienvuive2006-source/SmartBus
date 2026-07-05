package com.smartbus.booking.repository;

import com.smartbus.booking.entity.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
    List<SeatReservation> findByTripId(Long tripId);
    
    @Modifying
    @Query("DELETE FROM SeatReservation s WHERE s.expiredAt < :now")
    void deleteExpiredReservations(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM SeatReservation s WHERE s.tripId = :tripId AND s.seatNumber IN :seatNumbers")
    void deleteByTripIdAndSeatNumbers(@Param("tripId") Long tripId, @Param("seatNumbers") List<String> seatNumbers);
}
