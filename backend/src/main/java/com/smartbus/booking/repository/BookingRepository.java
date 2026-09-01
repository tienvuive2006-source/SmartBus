package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    @EntityGraph(attributePaths = {"trip", "trip.inspector", "seatNumbers", "user"})
    List<Booking> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"trip", "trip.inspector", "user"})
    @org.springframework.data.jpa.repository.Query(
            value = "SELECT b FROM Booking b WHERE " +
                    "(:status = '' OR UPPER(b.status) = :status) AND " +
                    "(:paymentMethod = '' OR UPPER(b.paymentMethod) = :paymentMethod) AND " +
                    "b.createdAt >= :createdFrom AND b.createdAt < :createdTo AND " +
                    "(:departurePoint = '' OR LOWER(b.trip.departurePoint) = LOWER(:departurePoint)) AND " +
                    "(:arrivalPoint = '' OR LOWER(b.trip.arrivalPoint) = LOWER(:arrivalPoint)) AND " +
                    "(:search = '' OR LOWER(COALESCE(b.customerName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR COALESCE(b.customerPhone, '') LIKE CONCAT('%', :search, '%') " +
                    "OR STR(b.id) LIKE CONCAT('%', :search, '%'))",
            countQuery = "SELECT COUNT(b) FROM Booking b WHERE " +
                    "(:status = '' OR UPPER(b.status) = :status) AND " +
                    "(:paymentMethod = '' OR UPPER(b.paymentMethod) = :paymentMethod) AND " +
                    "b.createdAt >= :createdFrom AND b.createdAt < :createdTo AND " +
                    "(:departurePoint = '' OR LOWER(b.trip.departurePoint) = LOWER(:departurePoint)) AND " +
                    "(:arrivalPoint = '' OR LOWER(b.trip.arrivalPoint) = LOWER(:arrivalPoint)) AND " +
                    "(:search = '' OR LOWER(COALESCE(b.customerName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR COALESCE(b.customerPhone, '') LIKE CONCAT('%', :search, '%') " +
                    "OR STR(b.id) LIKE CONCAT('%', :search, '%'))")
    Page<Booking> searchAdminBookings(
            @Param("search") String search,
            @Param("status") String status,
            @Param("paymentMethod") String paymentMethod,
            @Param("createdFrom") java.time.LocalDateTime createdFrom,
            @Param("createdTo") java.time.LocalDateTime createdTo,
            @Param("departurePoint") String departurePoint,
            @Param("arrivalPoint") String arrivalPoint,
            Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT b.trip.departurePoint, b.trip.arrivalPoint FROM Booking b " +
                    "WHERE b.trip IS NOT NULL ORDER BY b.trip.departurePoint, b.trip.arrivalPoint")
    List<Object[]> findAdminRouteOptions();
    
    @EntityGraph(attributePaths = {"trip", "trip.inspector", "seatNumbers", "user"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Booking> findByTripId(Long tripId);

    @org.springframework.data.jpa.repository.Query("SELECT b.user.id, SUM(SIZE(b.seatNumbers)) FROM Booking b WHERE b.status != 'CANCELLED' AND b.user IS NOT NULL GROUP BY b.user.id")
    List<Object[]> countTicketsPerUser();

    @org.springframework.data.jpa.repository.Query("SELECT b.user.id, SUM(SIZE(b.seatNumbers)) FROM Booking b WHERE b.status != 'CANCELLED' AND b.user.id IN :userIds GROUP BY b.user.id")
    List<Object[]> countTicketsPerUserIds(@Param("userIds") List<Long> userIds);

    @org.springframework.data.jpa.repository.Query("SELECT b.user.id, COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.user.id IN :userIds AND b.status IN ('PAID', 'CHECKED_IN', 'COMPLETED') GROUP BY b.user.id")
    List<Object[]> sumTotalSpentPerUserIds(@Param("userIds") List<Long> userIds);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.user IS NOT NULL AND UPPER(b.user.role) = :role AND b.status IN ('PAID', 'CHECKED_IN', 'COMPLETED')")
    Double sumTotalSpentByUserRole(@Param("role") String role);

    @org.springframework.data.jpa.repository.Query("SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status")
    List<Object[]> countByStatusGrouped();

    @EntityGraph(attributePaths = {"seatNumbers"})
    List<Booking> findByStatusIn(List<String> statuses);

    List<Booking> findByRoundTripGroupId(String roundTripGroupId);

    @EntityGraph(attributePaths = {"trip", "seatNumbers", "trip.busType"})
    Optional<Booking> findByIdAndCustomerPhone(Long id, String customerPhone);
}
