package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    boolean existsByAssignedLicensePlateIgnoreCaseAndStatusIgnoreCase(String licensePlate, String status);
    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"inspector"})
    List<Trip> findAll();
    
    // Hỗ trợ tìm kiếm linh hoạt theo điểm đi và điểm đến
    List<Trip> findByDepartureDateContaining(String departureDate);

    // Kéo những chuyến xe trong một ngày cụ thể, HOẶC từ ngày hôm nay trở đi nếu không nhập ngày (Ngăn Out of Memory)
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"inspector"})
    @org.springframework.data.jpa.repository.Query("SELECT t FROM Trip t WHERE (:date = '' AND t.departureDate >= :today) OR (:date != '' AND t.departureDate LIKE %:date%)")
    List<Trip> findTripsSafely(@org.springframework.data.repository.query.Param("date") String date, @org.springframework.data.repository.query.Param("today") String today);

    // Hỗ trợ tìm kiếm linh hoạt theo điểm đi, điểm đến và ngày đi
    List<Trip> findByDeparturePointContainingIgnoreCaseAndArrivalPointContainingIgnoreCaseAndDepartureDateContaining(
            String departurePoint, String arrivalPoint, String departureDate);

    // Lấy danh sách chuyến xe được phân công cho một nhân viên soát vé
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"inspector"})
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

    // ==========================================================
    // KIỂM TRA XUNG ĐỘT LỊCH TÀI XẾ & XE (CONFLICT DETECTION)
    // ==========================================================

    /**
     * Tìm tất cả chuyến xe của 1 tài xế trong cùng 1 ngày (dùng để tính buffer_time ở Service).
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Trip t WHERE (t.assignedDriverUsername = :driverUsername " +
        "OR t.secondaryDriverUsername = :driverUsername) " +
        "AND t.departureDate = :departureDate " +
        "AND t.status NOT IN ('CANCELLED', 'COMPLETED') " +
        "AND (:excludeTripId IS NULL OR t.id != :excludeTripId)"
    )
    List<Trip> findTripsForDriverOnDate(
        @org.springframework.data.repository.query.Param("driverUsername") String driverUsername,
        @org.springframework.data.repository.query.Param("departureDate") String departureDate,
        @org.springframework.data.repository.query.Param("excludeTripId") Long excludeTripId
    );

    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Trip t WHERE (t.assignedDriverUsername = :driverUsername " +
        "OR t.secondaryDriverUsername = :driverUsername) " +
        "AND t.departureDate BETWEEN :fromDate AND :toDate " +
        "AND t.status NOT IN ('CANCELLED', 'COMPLETED') " +
        "AND (:excludeTripId IS NULL OR t.id != :excludeTripId)"
    )
    List<Trip> findTripsForDriverInDateRange(
        @org.springframework.data.repository.query.Param("driverUsername") String driverUsername,
        @org.springframework.data.repository.query.Param("fromDate") String fromDate,
        @org.springframework.data.repository.query.Param("toDate") String toDate,
        @org.springframework.data.repository.query.Param("excludeTripId") Long excludeTripId
    );

    /**
     * Tìm tất cả chuyến xe của 1 phương tiện (biển số) trong cùng 1 ngày.
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Trip t WHERE t.assignedLicensePlate = :licensePlate " +
        "AND t.departureDate = :departureDate " +
        "AND t.status NOT IN ('CANCELLED', 'COMPLETED') " +
        "AND (:excludeTripId IS NULL OR t.id != :excludeTripId)"
    )
    List<Trip> findTripsForBusOnDate(
        @org.springframework.data.repository.query.Param("licensePlate") String licensePlate,
        @org.springframework.data.repository.query.Param("departureDate") String departureDate,
        @org.springframework.data.repository.query.Param("excludeTripId") Long excludeTripId
    );

    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Trip t WHERE t.assignedLicensePlate = :licensePlate " +
        "AND t.departureDate BETWEEN :fromDate AND :toDate " +
        "AND t.status NOT IN ('CANCELLED', 'COMPLETED') " +
        "AND (:excludeTripId IS NULL OR t.id != :excludeTripId)"
    )
    List<Trip> findTripsForBusInDateRange(
        @org.springframework.data.repository.query.Param("licensePlate") String licensePlate,
        @org.springframework.data.repository.query.Param("fromDate") String fromDate,
        @org.springframework.data.repository.query.Param("toDate") String toDate,
        @org.springframework.data.repository.query.Param("excludeTripId") Long excludeTripId
    );

    /**
     * Tìm tất cả chuyến xe của 1 lơ xe trong cùng 1 ngày.
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Trip t WHERE t.inspector.id = :inspectorId " +
        "AND t.departureDate = :departureDate " +
        "AND t.status NOT IN ('CANCELLED', 'COMPLETED') " +
        "AND (:excludeTripId IS NULL OR t.id != :excludeTripId)"
    )
    List<Trip> findTripsForInspectorOnDate(
        @org.springframework.data.repository.query.Param("inspectorId") Long inspectorId,
        @org.springframework.data.repository.query.Param("departureDate") String departureDate,
        @org.springframework.data.repository.query.Param("excludeTripId") Long excludeTripId
    );

    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Trip t WHERE t.inspector.id = :inspectorId " +
        "AND t.departureDate BETWEEN :fromDate AND :toDate " +
        "AND t.status NOT IN ('CANCELLED', 'COMPLETED') " +
        "AND (:excludeTripId IS NULL OR t.id != :excludeTripId)"
    )
    List<Trip> findTripsForInspectorInDateRange(
        @org.springframework.data.repository.query.Param("inspectorId") Long inspectorId,
        @org.springframework.data.repository.query.Param("fromDate") String fromDate,
        @org.springframework.data.repository.query.Param("toDate") String toDate,
        @org.springframework.data.repository.query.Param("excludeTripId") Long excludeTripId
    );

    /**
     * Lấy tất cả chuyến xe được phân công cho 1 tài xế (Dùng cho Driver Dashboard)
     */
    List<Trip> findByAssignedDriverUsername(String assignedDriverUsername);

    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Trip t WHERE t.assignedDriverUsername = :driverUsername " +
        "OR t.secondaryDriverUsername = :driverUsername"
    )
    List<Trip> findByEitherDriverUsername(
        @org.springframework.data.repository.query.Param("driverUsername") String driverUsername
    );

    @org.springframework.data.jpa.repository.Query("SELECT t.assignedDriverUsername, COUNT(t) FROM Trip t WHERE t.assignedDriverUsername IN :phones AND t.status IN ('ASSIGNED', 'PENDING', 'IN_PROGRESS') GROUP BY t.assignedDriverUsername")
    List<Object[]> countActiveTripsByDriverPhones(@org.springframework.data.repository.query.Param("phones") List<String> phones);

    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM Trip t WHERE (t.assignedDriverUsername IN :phones OR t.secondaryDriverUsername IN :phones) " +
        "AND t.status IN ('ASSIGNED', 'PENDING', 'IN_PROGRESS')"
    )
    List<Trip> findActiveTripsByDriverPhones(@org.springframework.data.repository.query.Param("phones") List<String> phones);

    @org.springframework.data.jpa.repository.Query("SELECT t.inspector.userAccount.id, COUNT(t) FROM Trip t WHERE t.inspector.userAccount.id IN :userIds AND t.status IN ('ASSIGNED', 'PENDING', 'IN_PROGRESS') GROUP BY t.inspector.userAccount.id")
    List<Object[]> countActiveTripsByInspectorUserIds(@org.springframework.data.repository.query.Param("userIds") List<Long> userIds);
}
