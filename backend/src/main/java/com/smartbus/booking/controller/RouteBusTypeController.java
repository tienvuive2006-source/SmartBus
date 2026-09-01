package com.smartbus.booking.controller;

import com.smartbus.booking.dto.RouteBusTypeConfigRequest;
import com.smartbus.booking.dto.RouteBusTypeConfigResponse;
import com.smartbus.booking.service.RouteBusTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/route-bus-types")
@RequiredArgsConstructor
public class RouteBusTypeController {

    private final RouteBusTypeService routeBusTypeService;

    @GetMapping("/routes/{routeId}")
    public ResponseEntity<RouteBusTypeConfigResponse> getConfig(@PathVariable Long routeId) {
        return ResponseEntity.ok(routeBusTypeService.getConfig(routeId));
    }

    @PutMapping("/routes/{routeId}")
    public ResponseEntity<RouteBusTypeConfigResponse> updateConfig(
        @PathVariable Long routeId,
        @RequestBody RouteBusTypeConfigRequest request
    ) {
        return ResponseEntity.ok(routeBusTypeService.updateConfig(routeId, request));
    }
}
