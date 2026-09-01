package com.smartbus.booking.repository;

import com.smartbus.booking.entity.VehicleMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleMaintenanceRepository extends JpaRepository<VehicleMaintenance, Long> {
    List<VehicleMaintenance> findAllByOrderByCreatedAtDesc();
    boolean existsByBusIdAndStatusIn(Long busId, List<String> statuses);
}
