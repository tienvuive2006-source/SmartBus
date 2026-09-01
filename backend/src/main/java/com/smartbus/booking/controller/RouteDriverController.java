package com.smartbus.booking.controller;

import com.smartbus.booking.dto.RouteDriverConfigRequest;
import com.smartbus.booking.dto.RouteDriverConfigResponse;
import com.smartbus.booking.dto.DriverRouteAssignmentResponse;
import com.smartbus.booking.dto.DriverRouteConfigRequest;
import java.util.List;
import com.smartbus.booking.service.RouteDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/route-drivers")
@RequiredArgsConstructor
public class RouteDriverController {

    private final RouteDriverService routeDriverService;

    @GetMapping("/routes/{routeId}")
    public ResponseEntity<RouteDriverConfigResponse> getConfig(@PathVariable Long routeId) {
        return ResponseEntity.ok(routeDriverService.getConfig(routeId));
    }

    @PutMapping("/routes/{routeId}")
    public ResponseEntity<RouteDriverConfigResponse> updateConfig(
        @PathVariable Long routeId,
        @RequestBody RouteDriverConfigRequest request
    ) {
        return ResponseEntity.ok(routeDriverService.updateConfig(routeId, request));
    }

    @GetMapping("/drivers/{driverId}")
    public ResponseEntity<List<DriverRouteAssignmentResponse>> getDriverRoutes(@PathVariable Long driverId) {
        return ResponseEntity.ok(routeDriverService.getDriverRoutes(driverId));
    }

    @PutMapping("/drivers/{driverId}")
    public ResponseEntity<List<DriverRouteAssignmentResponse>> updateDriverRoutes(
        @PathVariable Long driverId,
        @RequestBody DriverRouteConfigRequest request
    ) {
        return ResponseEntity.ok(routeDriverService.updateDriverRoutes(driverId, request));
    }
}
