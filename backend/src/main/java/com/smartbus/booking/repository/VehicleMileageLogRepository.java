package com.smartbus.booking.repository;

import com.smartbus.booking.entity.VehicleMileageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleMileageLogRepository extends JpaRepository<VehicleMileageLog, Long> {
    boolean existsByTripId(Long tripId);
    List<VehicleMileageLog> findByBusIdOrderByRecordedAtDesc(Long busId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update VehicleMileageLog log set log.trip = null where log.trip.id = :tripId")
    int detachTrip(@Param("tripId") Long tripId);
}
