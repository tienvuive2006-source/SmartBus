package com.smartbus.booking.service;

import com.smartbus.booking.dto.RouteBusTypeConfigRequest;
import com.smartbus.booking.dto.RouteBusTypeConfigResponse;
import com.smartbus.booking.entity.BusType;
import com.smartbus.booking.entity.Route;
import com.smartbus.booking.entity.RouteBusTypeAssignment;
import com.smartbus.booking.repository.BusTypeRepository;
import com.smartbus.booking.repository.RouteBusTypeAssignmentRepository;
import com.smartbus.booking.repository.RouteRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RouteBusTypeService {

    private final RouteRepository routeRepository;
    private final BusTypeRepository busTypeRepository;
    private final RouteBusTypeAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public RouteBusTypeConfigResponse getConfig(Long routeId) {
        requireRoute(routeId);
        List<RouteBusTypeAssignment> assignments =
            assignmentRepository.findByRouteIdOrderByDefaultTypeDescBusTypeNameAsc(routeId);

        if (assignments.isEmpty()) {
            return new RouteBusTypeConfigResponse(routeId, true, busTypeRepository.findAll(), null);
        }

        List<BusType> busTypes = assignments.stream()
            .map(RouteBusTypeAssignment::getBusType)
            .toList();
        Long defaultBusTypeId = assignments.stream()
            .filter(RouteBusTypeAssignment::isDefaultType)
            .map(assignment -> assignment.getBusType().getId())
            .findFirst()
            .orElse(null);

        return new RouteBusTypeConfigResponse(routeId, false, busTypes, defaultBusTypeId);
    }

    @Transactional
    public RouteBusTypeConfigResponse updateConfig(Long routeId, RouteBusTypeConfigRequest request) {
        Route route = requireRoute(routeId);
        assignmentRepository.deleteByRouteId(routeId);
        // Execute removals before inserting the replacement rows. Without this
        // flush Hibernate may insert first and hit the (route_id, bus_type_id)
        // unique constraint while the previous rows still exist.
        assignmentRepository.flush();

        if (request.unrestricted()) {
            return getConfig(routeId);
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(
            request.busTypeIds() == null ? List.of() : request.busTypeIds()
        );
        if (uniqueIds.isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một dòng xe cho tuyến.");
        }

        List<BusType> busTypes = busTypeRepository.findAllById(uniqueIds);
        if (busTypes.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("Có dòng xe không tồn tại.");
        }
        if (request.defaultBusTypeId() != null && !uniqueIds.contains(request.defaultBusTypeId())) {
            throw new IllegalArgumentException("Dòng xe mặc định phải nằm trong danh sách được phép.");
        }

        Long defaultId = request.defaultBusTypeId() != null
            ? request.defaultBusTypeId()
            : busTypes.get(0).getId();
        List<RouteBusTypeAssignment> assignments = busTypes.stream()
            .map(busType -> RouteBusTypeAssignment.builder()
                .route(route)
                .busType(busType)
                .defaultType(busType.getId().equals(defaultId))
                .build())
            .toList();
        assignmentRepository.saveAll(assignments);
        assignmentRepository.flush();
        return getConfig(routeId);
    }

    private Route requireRoute(Long routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tuyến đường " + routeId));
    }
}
