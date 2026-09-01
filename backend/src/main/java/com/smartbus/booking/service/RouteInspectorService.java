package com.smartbus.booking.service;

import com.smartbus.booking.dto.DriverRouteAssignmentResponse;
import com.smartbus.booking.dto.DriverRouteConfigRequest;
import com.smartbus.booking.dto.RouteInspectorConfigResponse;
import com.smartbus.booking.dto.RouteInspectorItemResponse;
import com.smartbus.booking.entity.Route;
import com.smartbus.booking.entity.RouteInspectorAssignment;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.RouteInspectorAssignmentRepository;
import com.smartbus.booking.repository.RouteRepository;
import com.smartbus.booking.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RouteInspectorService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final RouteInspectorAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public RouteInspectorConfigResponse getConfig(Long routeId) {
        Route route = requireRoute(routeId);
        List<RouteInspectorAssignment> assignments =
            assignmentRepository.findByRouteIdOrderByRoleAndInspectorName(routeId);
        List<RouteInspectorItemResponse> inspectors = assignments.stream()
            .map(assignment -> {
                User inspector = assignment.getInspector();
                return new RouteInspectorItemResponse(
                    inspector.getId(),
                    inspector.getFullName(),
                    inspector.getPhone(),
                    inspector.getAvatarUrl(),
                    assignment.getRole()
                );
            })
            .toList();
        return new RouteInspectorConfigResponse(
            routeId,
            assignments.isEmpty(),
            findReverseRoute(route).isPresent(),
            inspectors
        );
    }

    @Transactional(readOnly = true)
    public List<DriverRouteAssignmentResponse> getInspectorRoutes(Long inspectorId) {
        requireInspector(inspectorId);
        return assignmentRepository.findByInspectorIdOrderByRoleAndRouteName(inspectorId).stream()
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
    public List<DriverRouteAssignmentResponse> updateInspectorRoutes(
        Long inspectorId,
        DriverRouteConfigRequest request
    ) {
        User inspector = requireInspector(inspectorId);
        Set<Long> primaryIds = expandReverseRouteIds(uniqueIds(request.primaryRouteIds()));
        Set<Long> backupIds = expandReverseRouteIds(uniqueIds(request.backupRouteIds()));
        backupIds.removeAll(primaryIds);

        Set<Long> allIds = new LinkedHashSet<>(primaryIds);
        allIds.addAll(backupIds);
        List<Route> routes = routeRepository.findAllById(allIds);
        if (routes.size() != allIds.size()) {
            throw new IllegalArgumentException("Danh sách có tuyến đường không tồn tại.");
        }

        assignmentRepository.deleteByInspectorId(inspectorId);
        assignmentRepository.flush();
        List<RouteInspectorAssignment> assignments = routes.stream()
            .map(route -> RouteInspectorAssignment.builder()
                .route(route)
                .inspector(inspector)
                .role(primaryIds.contains(route.getId())
                    ? RouteInspectorAssignment.PRIMARY
                    : RouteInspectorAssignment.BACKUP)
                .build())
            .toList();
        assignmentRepository.saveAll(assignments);
        assignmentRepository.flush();
        return getInspectorRoutes(inspectorId);
    }

    private Set<Long> expandReverseRouteIds(Set<Long> selectedIds) {
        if (selectedIds.isEmpty()) return selectedIds;
        List<Route> selectedRoutes = routeRepository.findAllById(selectedIds);
        if (selectedRoutes.size() != selectedIds.size()) {
            throw new IllegalArgumentException("Danh sách có tuyến đường không tồn tại.");
        }
        Set<Long> expandedIds = new LinkedHashSet<>(selectedIds);
        selectedRoutes.forEach(route -> findReverseRoute(route)
            .filter(reverse -> !reverse.getId().equals(route.getId()))
            .map(Route::getId)
            .ifPresent(expandedIds::add));
        return expandedIds;
    }

    private Set<Long> uniqueIds(List<Long> ids) {
        return new LinkedHashSet<>(ids == null ? List.of() : ids);
    }

    private Route requireRoute(Long routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tuyến đường " + routeId));
    }

    private java.util.Optional<Route> findReverseRoute(Route route) {
        return routeRepository.findFirstByDeparturePointIgnoreCaseAndArrivalPointIgnoreCase(
            route.getArrivalPoint(), route.getDeparturePoint());
    }

    private User requireInspector(Long inspectorId) {
        User inspector = userRepository.findById(inspectorId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lơ xe " + inspectorId));
        if (!"INSPECTOR".equalsIgnoreCase(inspector.getRole())) {
            throw new IllegalArgumentException("Tài khoản được chọn không phải lơ xe.");
        }
        return inspector;
    }
}
