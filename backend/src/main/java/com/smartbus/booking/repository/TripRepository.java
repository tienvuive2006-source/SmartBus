package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    // Hỗ trợ tìm kiếm linh hoạt theo điểm đi và điểm đến
    List<Trip> findByDepartureDateContaining(String departureDate);

    // Kéo những chuyến xe trong một ngày cụ thể, HOẶC từ ngày hôm nay trở đi nếu không nhập ngày (Ngăn Out of Memory)
    @org.springframework.data.jpa.repository.Query("SELECT t FROM Trip t WHERE (:date = '' AND t.departureDate >= :today) OR (:date != '' AND t.departureDate LIKE %:date%)")
    List<Trip> findTripsSafely(@org.springframework.data.repository.query.Param("date") String date, @org.springframework.data.repository.query.Param("today") String today);

    // Hỗ trợ tìm kiếm linh hoạt theo điểm đi, điểm đến và ngày đi
    List<Trip> findByDeparturePointContainingIgnoreCaseAndArrivalPointContainingIgnoreCaseAndDepartureDateContaining(
            String departurePoint, String arrivalPoint, String departureDate);

    // Lấy danh sách chuyến xe được phân công cho một nhân viên soát vé
    List<Trip> findByInspectorId(Long inspectorId);

    // ==========================================================
    // CÁC QUERY TỐI ƯU HIỆU NĂNG CHO TRANG CHỦ (TRÁNH N+1 VÀ QUÁ TẢI)
    // ==========================================================
    
    @org.springframework.data.jpa.repository.Query("SELECT t.departurePoint, COUNT(t) FROM Trip t WHERE t.isVisible = true GROUP BY t.departurePoint")
    List<Object[]> getDepartureStats();

    @org.springframework.data.jpa.repository.Query("SELECT t.arrivalPoint, COUNT(t) FROM Trip t WHERE t.isVisible = true GROUP BY t.arrivalPoint")
    List<Object[]> getArrivalStats();

    @org.springframework.data.jpa.repository.Query("SELECT t.companyName, COUNT(t), MIN(t.price) FROM Trip t WHERE t.isVisible = true GROUP BY t.companyName")
    List<Object[]> getCompanyStats();

    @org.springframework.data.jpa.repository.Query("SELECT MIN(t.id), t.departurePoint, t.arrivalPoint, MIN(t.imageUrl), MIN(t.departureDate), MIN(t.price), MIN(t.busType), t.companyName FROM Trip t WHERE t.isVisible = true GROUP BY t.departurePoint, t.arrivalPoint, t.companyName")
    List<Object[]> getPopularRoutes();
}
