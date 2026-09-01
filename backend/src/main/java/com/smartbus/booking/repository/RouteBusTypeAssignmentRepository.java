package com.smartbus.booking.repository;

import com.smartbus.booking.entity.RouteBusTypeAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RouteBusTypeAssignmentRepository extends JpaRepository<RouteBusTypeAssignment, Long> {

    @Query("""
        select assignment
        from RouteBusTypeAssignment assignment
        join fetch assignment.busType busType
        where assignment.route.id = :routeId
        order by assignment.defaultType desc, busType.name asc
        """)
    List<RouteBusTypeAssignment> findByRouteIdOrderByDefaultTypeDescBusTypeNameAsc(
        @Param("routeId") Long routeId
    );

    void deleteByRouteId(Long routeId);
}
