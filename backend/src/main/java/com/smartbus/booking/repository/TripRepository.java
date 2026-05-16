package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    // Hỗ trợ tìm kiếm linh hoạt theo điểm đi và điểm đến
    // Hỗ trợ tìm kiếm linh hoạt theo điểm đi, điểm đến và ngày đi
    List<Trip> findByDeparturePointContainingIgnoreCaseAndArrivalPointContainingIgnoreCaseAndDepartureDateContaining(
            String departurePoint, String arrivalPoint, String departureDate);
}
