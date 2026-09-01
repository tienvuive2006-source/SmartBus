package com.smartbus.booking.service;

import com.smartbus.booking.dto.RouteDriverConfigRequest;
import com.smartbus.booking.dto.RouteDriverConfigResponse;
import com.smartbus.booking.dto.RouteDriverItemResponse;
import com.smartbus.booking.dto.DriverRouteAssignmentResponse;
import com.smartbus.booking.dto.DriverRouteConfigRequest;
import com.smartbus.booking.entity.Route;
import com.smartbus.booking.entity.RouteDriverAssignment;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.RouteDriverAssignmentRepository;
import com.smartbus.booking.repository.RouteRepository;
import com.smartbus.booking.repository.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RouteDriverService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final RouteDriverAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public RouteDriverConfigResponse getConfig(Long routeId) {
        Route route = requireRoute(routeId);
        List<RouteDriverAssignment> assignments =
            assignmentRepository.findByRouteIdOrderByRoleAndDriverName(routeId);
        boolean sharedWithReverseRoute = findReverseRoute(route).isPresent();

        if (assignments.isEmpty()) {
            return new RouteDriverConfigResponse(routeId, true, sharedWithReverseRoute, List.of());
        }

        List<RouteDriverItemResponse> drivers = assignments.stream()
            .map(this::toResponse)
            .toList();
        return new RouteDriverConfigResponse(routeId, false, sharedWithReverseRoute, drivers);
    }

    @Transactional
    public RouteDriverConfigResponse updateConfig(Long routeId, RouteDriverConfigRequest request) {
        Route route = requireRoute(routeId);
        List<Route> relatedRoutes = new ArrayList<>();
        relatedRoutes.add(route);
        findReverseRoute(route).ifPresent(relatedRoutes::add);

        relatedRoutes.forEach(item -> assignmentRepository.deleteByRouteId(item.getId()));
        assignmentRepository.flush();

        if (request.unrestricted()) {
            return getConfig(routeId);
        }

        Set<Long> primaryIds = uniqueIds(request.primaryDriverIds());
        Set<Long> backupIds = uniqueIds(request.backupDriverIds());
        backupIds.removeAll(primaryIds);
        Set<Long> allIds = new LinkedHashSet<>(primaryIds);
        allIds.addAll(backupIds);
        if (allIds.isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một tài xế chính hoặc dự phòng cho tuyến.");
        }

        List<User> drivers = userRepository.findAllById(allIds);
        if (drivers.size() != allIds.size() || drivers.stream().anyMatch(driver -> !"DRIVER".equalsIgnoreCase(driver.getRole()))) {
            throw new IllegalArgumentException("Danh sách có tài khoản không phải tài xế hoặc không tồn tại.");
        }

        List<RouteDriverAssignment> assignments = new ArrayList<>();
        for (Route relatedRoute : relatedRoutes) {
            for (User driver : drivers) {
                assignments.add(RouteDriverAssignment.builder()
                    .route(relatedRoute)
                    .driver(driver)
                    .role(primaryIds.contains(driver.getId())
                        ? RouteDriverAssignment.PRIMARY
                        : RouteDriverAssignment.BACKUP)
                    .build());
            }
        }
        assignmentRepository.saveAll(assignments);
        assignmentRepository.flush();
        return getConfig(routeId);
    }

    @Transactional(readOnly = true)
    public List<DriverRouteAssignmentResponse> getDriverRoutes(Long driverId) {
        requireDriver(driverId);
        return assignmentRepository.findByDriverIdOrderByRoleAndRouteName(driverId).stream()
            .map(assignment -> new DriverRouteAssignmentResponse(
                assignment.getRoute().getId(),
                assignment.getRoute().getName(),
                assignment.getRoute().getDeparturePoint(),
                assignment.getRoute().getArrivalPoint(),
                assignment.getRole()
            ))
            .toList();
    }

    @Transactional
    public List<DriverRouteAssignmentResponse> updateDriverRoutes(
        Long driverId,
        DriverRouteConfigRequest request
    ) {
        User driver = requireDriver(driverId);
        Set<Long> primaryIds = expandReverseRouteIds(uniqueIds(request.primaryRouteIds()));
        Set<Long> backupIds = expandReverseRouteIds(uniqueIds(request.backupRouteIds()));
        backupIds.removeAll(primaryIds);

        Set<Long> allIds = new LinkedHashSet<>(primaryIds);
        allIds.addAll(backupIds);
        List<Route> routes = routeRepository.findAllById(allIds);
        if (routes.size() != allIds.size()) {
            throw new IllegalArgumentException("Danh sách có tuyến đường không tồn tại.");
        }

        assignmentRepository.deleteByDriverId(driverId);
        assignmentRepository.flush();

        List<RouteDriverAssignment> assignments = routes.stream()
            .map(route -> RouteDriverAssignment.builder()
                .route(route)
                .driver(driver)
                .role(primaryIds.contains(route.getId())
                    ? RouteDriverAssignment.PRIMARY
                    : RouteDriverAssignment.BACKUP)
                .build())
            .toList();
        assignmentRepository.saveAll(assignments);
        assignmentRepository.flush();
        return getDriverRoutes(driverId);
    }

    private Set<Long> expandReverseRouteIds(Set<Long> selectedIds) {
        if (selectedIds.isEmpty()) return selectedIds;
        List<Route> selectedRoutes = routeRepository.findAllById(selectedIds);
        if (selectedRoutes.size() != selectedIds.size()) {
            throw new IllegalArgumentException("Danh sách có tuyến đường không tồn tại.");
        }
        Set<Long> expandedIds = new LinkedHashSet<>(selectedIds);
        selectedRoutes.forEach(route -> findReverseRoute(route)
            .map(Route::getId)
            .ifPresent(expandedIds::add));
        return expandedIds;
    }

    private java.util.Optional<Route> findReverseRoute(Route route) {
        return routeRepository.findFirstByDeparturePointIgnoreCaseAndArrivalPointIgnoreCase(
            route.getArrivalPoint(),
            route.getDeparturePoint()
        ).filter(reverse -> !reverse.getId().equals(route.getId()));
    }

    private Set<Long> uniqueIds(List<Long> ids) {
        return new LinkedHashSet<>(ids == null ? List.of() : ids);
    }

    private Route requireRoute(Long routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tuyến đường " + routeId));
    }

    private User requireDriver(Long driverId) {
        User driver = userRepository.findById(driverId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế " + driverId));
        if (!"DRIVER".equalsIgnoreCase(driver.getRole())) {
            throw new IllegalArgumentException("Tài khoản được chọn không phải tài xế.");
        }
        return driver;
    }

    private RouteDriverItemResponse toResponse(RouteDriverAssignment assignment) {
        User driver = assignment.getDriver();
        return new RouteDriverItemResponse(
            driver.getId(),
            driver.getFullName(),
            driver.getPhone(),
            driver.getAvatarUrl(),
            assignment.getRole()
        );
    }
}
