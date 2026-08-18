package com.smartbus.booking.repository;

import com.smartbus.booking.entity.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketExchangeReservationRepository extends JpaRepository<SeatReservation, Long> {
    List<SeatReservation> findByTripIdAndExpiredAtAfter(Long tripId, LocalDateTime now);
}
