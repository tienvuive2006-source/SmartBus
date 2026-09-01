package com.smartbus.booking.repository;

import com.smartbus.booking.entity.RouteInspectorAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RouteInspectorAssignmentRepository extends JpaRepository<RouteInspectorAssignment, Long> {

    @Query("""
        select assignment
        from RouteInspectorAssignment assignment
        join fetch assignment.inspector inspector
        where assignment.route.id = :routeId
        order by assignment.role asc, inspector.fullName asc
        """)
    List<RouteInspectorAssignment> findByRouteIdOrderByRoleAndInspectorName(@Param("routeId") Long routeId);

    @Query("""
        select assignment
        from RouteInspectorAssignment assignment
        join fetch assignment.route route
        where assignment.inspector.id = :inspectorId
        order by assignment.role asc, route.name asc
        """)
    List<RouteInspectorAssignment> findByInspectorIdOrderByRoleAndRouteName(@Param("inspectorId") Long inspectorId);

    void deleteByInspectorId(Long inspectorId);
}
