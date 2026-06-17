package com.smartbus.booking.controller;

import com.smartbus.booking.entity.Route;
import com.smartbus.booking.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @com.smartbus.booking.annotation.AuditAction(action = "CREATE_ROUTE", entityName = "Route")
    @PostMapping
    public ResponseEntity<Route> createRoute(@RequestBody Route route) {
        return ResponseEntity.ok(routeService.saveRoute(route));
    }

    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_ROUTE", entityName = "Route")
    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable("id") Long id, @RequestBody Route route) {
        return ResponseEntity.ok(routeService.updateRoute(id, route));
    }

    @com.smartbus.booking.annotation.AuditAction(action = "DELETE_ROUTE", entityName = "Route")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable("id") Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}
