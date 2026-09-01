package com.smartbus.booking.controller;

import com.smartbus.booking.dto.DriverRouteAssignmentResponse;
import com.smartbus.booking.dto.DriverRouteConfigRequest;
import com.smartbus.booking.dto.RouteInspectorConfigResponse;
import com.smartbus.booking.service.RouteInspectorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/route-inspectors")
@RequiredArgsConstructor
public class RouteInspectorController {

    private final RouteInspectorService routeInspectorService;

    @GetMapping("/routes/{routeId}")
    public ResponseEntity<RouteInspectorConfigResponse> getConfig(@PathVariable Long routeId) {
        return ResponseEntity.ok(routeInspectorService.getConfig(routeId));
    }

    @GetMapping("/inspectors/{inspectorId}")
    public ResponseEntity<List<DriverRouteAssignmentResponse>> getInspectorRoutes(
        @PathVariable Long inspectorId
    ) {
        return ResponseEntity.ok(routeInspectorService.getInspectorRoutes(inspectorId));
    }

    @PutMapping("/inspectors/{inspectorId}")
    public ResponseEntity<List<DriverRouteAssignmentResponse>> updateInspectorRoutes(
        @PathVariable Long inspectorId,
        @RequestBody DriverRouteConfigRequest request
    ) {
        return ResponseEntity.ok(routeInspectorService.updateInspectorRoutes(inspectorId, request));
    }
}
