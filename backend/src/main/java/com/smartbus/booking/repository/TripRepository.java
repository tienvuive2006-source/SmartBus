package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    // Hỗ trợ tìm kiếm linh hoạt theo điểm đi và điểm đến
    List<Trip> findByDepartureDateContaining(String departureDate);

    // Hỗ trợ tìm kiếm linh hoạt theo điểm đi, điểm đến và ngày đi
    List<Trip> findByDeparturePointContainingIgnoreCaseAndArrivalPointContainingIgnoreCaseAndDepartureDateContaining(
            String departurePoint, String arrivalPoint, String departureDate);

    // Lấy danh sách chuyến xe được phân công cho một nhân viên soát vé
    List<Trip> findByInspectorId(Long inspectorId);
}
