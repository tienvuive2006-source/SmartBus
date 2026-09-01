package com.smartbus.booking.controller;

import com.smartbus.booking.dto.RouteVehicleConfigRequest;
import com.smartbus.booking.dto.RouteVehicleConfigResponse;
import com.smartbus.booking.service.RouteVehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/route-vehicles")
@RequiredArgsConstructor
public class RouteVehicleController {

    private final RouteVehicleService routeVehicleService;

    @GetMapping("/routes/{routeId}")
    public ResponseEntity<RouteVehicleConfigResponse> getConfig(@PathVariable Long routeId) {
        return ResponseEntity.ok(routeVehicleService.getConfig(routeId));
    }

    @PutMapping("/routes/{routeId}")
    public ResponseEntity<RouteVehicleConfigResponse> updateConfig(
        @PathVariable Long routeId,
        @RequestBody RouteVehicleConfigRequest request
    ) {
        return ResponseEntity.ok(routeVehicleService.updateConfig(routeId, request));
    }
}
