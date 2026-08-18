package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketExchangeSeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.trip.id = :tripId order by s.seatNumber")
    List<Seat> findTripSeatsForUpdate(@Param("tripId") Long tripId);

    List<Seat> findByTripIdOrderBySeatNumberAsc(Long tripId);

    long countByTripIdAndIsBookedFalse(Long tripId);
}
