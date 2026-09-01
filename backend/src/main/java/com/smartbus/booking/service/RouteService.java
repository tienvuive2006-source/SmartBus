package com.smartbus.booking.service;

import com.smartbus.booking.entity.Route;
import com.smartbus.booking.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final com.smartbus.booking.repository.RouteBusTypeAssignmentRepository routeBusTypeAssignmentRepository;
    private final com.smartbus.booking.repository.RouteVehicleAssignmentRepository routeVehicleAssignmentRepository;
    private final com.smartbus.booking.repository.RouteDriverAssignmentRepository routeDriverAssignmentRepository;

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route saveRoute(Route route) {
        return routeRepository.save(route);
    }

    public Route updateRoute(Long id, Route route) {
        route.setId(id);
        return routeRepository.save(route);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteRoute(Long id) {
        routeDriverAssignmentRepository.deleteByRouteId(id);
        routeVehicleAssignmentRepository.deleteByRouteId(id);
        routeBusTypeAssignmentRepository.deleteByRouteId(id);
        routeRepository.deleteById(id);
    }
}
