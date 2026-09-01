package com.smartbus.booking.service;

import com.smartbus.booking.dto.RouteVehicleConfigRequest;
import com.smartbus.booking.dto.RouteVehicleConfigResponse;
import com.smartbus.booking.dto.RouteVehicleItemResponse;
import com.smartbus.booking.entity.Bus;
import com.smartbus.booking.entity.Route;
import com.smartbus.booking.entity.RouteVehicleAssignment;
import com.smartbus.booking.repository.BusRepository;
import com.smartbus.booking.repository.RouteBusTypeAssignmentRepository;
import com.smartbus.booking.repository.RouteRepository;
import com.smartbus.booking.repository.RouteVehicleAssignmentRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RouteVehicleService {

    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final RouteBusTypeAssignmentRepository busTypeAssignmentRepository;
    private final RouteVehicleAssignmentRepository vehicleAssignmentRepository;

    @Transactional(readOnly = true)
    public RouteVehicleConfigResponse getConfig(Long routeId) {
        requireRoute(routeId);
        List<RouteVehicleAssignment> assignments =
            vehicleAssignmentRepository.findByRouteIdOrderByRoleDescBusLicensePlateAsc(routeId);
        if (assignments.isEmpty()) {
            return new RouteVehicleConfigResponse(routeId, true, List.of());
        }

        List<RouteVehicleItemResponse> vehicles = assignments.stream()
            .map(this::toResponse)
            .toList();
        return new RouteVehicleConfigResponse(routeId, false, vehicles);
    }

    @Transactional
    public RouteVehicleConfigResponse updateConfig(Long routeId, RouteVehicleConfigRequest request) {
        Route route = requireRoute(routeId);
        vehicleAssignmentRepository.deleteByRouteId(routeId);
        // Ensure old assignments are removed before their replacements are
        // inserted, otherwise the route/bus unique key can be hit on updates.
        vehicleAssignmentRepository.flush();

        if (request.unrestricted()) {
            return getConfig(routeId);
        }

        Set<Long> primaryIds = uniqueIds(request.primaryBusIds());
        Set<Long> backupIds = uniqueIds(request.backupBusIds());
        backupIds.removeAll(primaryIds);

        Set<Long> allIds = new LinkedHashSet<>(primaryIds);
        allIds.addAll(backupIds);
        if (allIds.isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một xe chính hoặc xe dự phòng.");
        }

        List<Bus> buses = busRepository.findAllById(allIds);
        if (buses.size() != allIds.size()) {
            throw new IllegalArgumentException("Có phương tiện không tồn tại.");
        }
        validateAllowedBusTypes(routeId, buses);

        Map<Long, Bus> busById = buses.stream()
            .collect(Collectors.toMap(Bus::getId, Function.identity()));
        List<RouteVehicleAssignment> assignments = allIds.stream()
            .map(busId -> RouteVehicleAssignment.builder()
                .route(route)
                .bus(busById.get(busId))
                .role(primaryIds.contains(busId)
                    ? RouteVehicleAssignment.PRIMARY
                    : RouteVehicleAssignment.BACKUP)
                .build())
            .toList();
        vehicleAssignmentRepository.saveAll(assignments);
        vehicleAssignmentRepository.flush();
        return getConfig(routeId);
    }

    private void validateAllowedBusTypes(Long routeId, List<Bus> buses) {
        var busTypeAssignments =
            busTypeAssignmentRepository.findByRouteIdOrderByDefaultTypeDescBusTypeNameAsc(routeId);
        if (busTypeAssignments.isEmpty()) return;

        Set<String> allowedNames = busTypeAssignments.stream()
            .map(assignment -> assignment.getBusType().getName().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
        boolean hasInvalidBus = buses.stream()
            .anyMatch(bus -> !allowedNames.contains(bus.getBusType().toLowerCase(Locale.ROOT)));
        if (hasInvalidBus) {
            throw new IllegalArgumentException("Có xe không thuộc dòng xe được phép của tuyến.");
        }
    }

    private Set<Long> uniqueIds(List<Long> ids) {
        return new LinkedHashSet<>(ids == null ? List.of() : ids);
    }

    private Route requireRoute(Long routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tuyến đường " + routeId));
    }

    private RouteVehicleItemResponse toResponse(RouteVehicleAssignment assignment) {
        Bus bus = assignment.getBus();
        return new RouteVehicleItemResponse(
            bus.getId(),
            bus.getLicensePlate(),
            bus.getBusType(),
            bus.getStatus(),
            bus.getCurrentStation(),
            assignment.getRole()
        );
    }
}
