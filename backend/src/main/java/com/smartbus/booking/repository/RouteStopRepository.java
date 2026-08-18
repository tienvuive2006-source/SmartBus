package com.smartbus.booking.repository;

import com.smartbus.booking.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    List<RouteStop> findByRouteIdOrderByStopOrderAsc(Long routeId);
    List<RouteStop> findByTripIdOrderByStopOrderAsc(Long tripId);
    void deleteByRouteId(Long routeId);
}
