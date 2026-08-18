package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketExchangeTripRepository extends JpaRepository<Trip, Long> {

    @Query("select t from Trip t " +
            "where lower(t.departurePoint) = lower(:departurePoint) " +
            "and lower(t.arrivalPoint) = lower(:arrivalPoint) " +
            "and t.departureDate >= :today " +
            "and t.id <> :currentTripId " +
            "and t.isVisible = true " +
            "and t.status not in ('CANCELLED', 'COMPLETED') " +
            "order by t.departureDate, t.departureTime")
    List<Trip> findExchangeCandidates(
            @Param("departurePoint") String departurePoint,
            @Param("arrivalPoint") String arrivalPoint,
            @Param("today") String today,
            @Param("currentTripId") Long currentTripId
    );
}
