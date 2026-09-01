package com.smartbus.booking.repository;

import com.smartbus.booking.entity.RouteDriverAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RouteDriverAssignmentRepository extends JpaRepository<RouteDriverAssignment, Long> {

    @Query("""
        select assignment
        from RouteDriverAssignment assignment
        join fetch assignment.driver driver
        where assignment.route.id = :routeId
        order by assignment.role asc, driver.fullName asc
        """)
    List<RouteDriverAssignment> findByRouteIdOrderByRoleAndDriverName(@Param("routeId") Long routeId);

    @Query("""
        select assignment
        from RouteDriverAssignment assignment
        join fetch assignment.route route
        where assignment.driver.id = :driverId
        order by assignment.role asc, route.name asc
        """)
    List<RouteDriverAssignment> findByDriverIdOrderByRoleAndRouteName(@Param("driverId") Long driverId);

    void deleteByRouteId(Long routeId);

    void deleteByDriverId(Long driverId);
}
