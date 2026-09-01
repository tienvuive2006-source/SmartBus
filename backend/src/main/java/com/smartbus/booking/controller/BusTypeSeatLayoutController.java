package com.smartbus.booking.controller;

import com.smartbus.booking.annotation.AuditAction;
import com.smartbus.booking.dto.SeatLayoutItemRequest;
import com.smartbus.booking.dto.SeatLayoutResponse;
import com.smartbus.booking.service.BusTypeSeatLayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bus-types/{busTypeId}/seat-layout")
@RequiredArgsConstructor
public class BusTypeSeatLayoutController {

    private final BusTypeSeatLayoutService layoutService;

    @GetMapping
    public ResponseEntity<SeatLayoutResponse> getLayout(@PathVariable("busTypeId") Long busTypeId) {
        return ResponseEntity.ok(layoutService.getLayout(busTypeId));
    }

    @AuditAction(action = "UPDATE_BUS_TYPE_SEAT_LAYOUT", entityName = "BusType")
    @PutMapping
    public ResponseEntity<SeatLayoutResponse> saveLayout(
            @PathVariable("busTypeId") Long busTypeId,
            @RequestBody List<SeatLayoutItemRequest> seats
    ) {
        return ResponseEntity.ok(layoutService.saveLayout(busTypeId, seats));
    }
}
