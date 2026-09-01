package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    // Lấy toàn bộ danh sách ghế của 1 chuyến xe cụ thể, sắp xếp theo số thứ tự ghế
    List<Seat> findByTripIdOrderBySeatNumberAsc(Long tripId);

    List<Seat> findByTripIdInOrderByTripIdAscSeatNumberAsc(List<Long> tripIds);

    @Query("SELECT s FROM Seat s JOIN s.trip t WHERE LOWER(t.busType) = LOWER(:busTypeName)")
    List<Seat> findAllByTripBusType(@Param("busTypeName") String busTypeName);
}
