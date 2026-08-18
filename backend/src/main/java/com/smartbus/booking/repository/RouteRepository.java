package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    java.util.Optional<Route> findFirstByDeparturePointIgnoreCaseAndArrivalPointIgnoreCase(String departurePoint, String arrivalPoint);
}
