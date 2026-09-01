package com.smartbus.booking.repository;

import com.smartbus.booking.entity.RouteVehicleAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RouteVehicleAssignmentRepository extends JpaRepository<RouteVehicleAssignment, Long> {

    @Query("""
        select assignment
        from RouteVehicleAssignment assignment
        join fetch assignment.bus bus
        where assignment.route.id = :routeId
        order by assignment.role desc, bus.licensePlate asc
        """)
    List<RouteVehicleAssignment> findByRouteIdOrderByRoleDescBusLicensePlateAsc(
        @Param("routeId") Long routeId
    );

    void deleteByRouteId(Long routeId);
}
