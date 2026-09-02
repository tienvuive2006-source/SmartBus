package com.smartbus.booking.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.dto.TripPairAssignmentRequest;
import com.smartbus.booking.service.TripService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Đảm bảo Frontend Vue không bị chặn CORS
public class TripController {

    private final TripService tripService;
    private final com.smartbus.booking.repository.TripRepository tripRepository;

    // 1. LẤY TOÀN BỘ DANH SÁCH (READ ALL)
    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.ok(tripService.getAllTrips());
    }

    // 1.5 TỔNG HỢP DỮ LIỆU TRANG CHỦ (TỐI ƯU HIỆU NĂNG)
    @GetMapping("/home-summary")
    public ResponseEntity<java.util.Map<String, Object>> getHomeSummary() {
        java.util.Map<String, Object> response = new java.util.HashMap<>();

        // 1. Departure Stats
        List<Object[]> depStats = tripRepository.getDepartureStats();
        List<String> allDepPoints = new java.util.ArrayList<>();
        java.util.Map<String, java.util.Map<String, Object>> stationsMap = new java.util.HashMap<>();
        
        for (Object[] row : depStats) {
            String name = (String) row[0];
            long count = ((Number) row[1]).longValue(); // SAFE CAST (Tránh sập nguồn ClassCastException)
            if (name != null) {
                allDepPoints.add(name);
                stationsMap.put(name, new java.util.HashMap<>(java.util.Map.of("name", name, "depCount", count, "arrCount", 0L)));
            }
        }

        // 2. Arrival Stats
        List<Object[]> arrStats = tripRepository.getArrivalStats();
        List<String> allArrPoints = new java.util.ArrayList<>();
        
        for (Object[] row : arrStats) {
            String name = (String) row[0];
            long count = ((Number) row[1]).longValue(); // SAFE CAST (Tránh sập nguồn ClassCastException)
            if (name != null) {
                allArrPoints.add(name);
                if (!stationsMap.containsKey(name)) {
                    stationsMap.put(name, new java.util.HashMap<>(java.util.Map.of("name", name, "depCount", 0L, "arrCount", count)));
                } else {
                    stationsMap.get(name).put("arrCount", count);
                }
            }
        }

        // 3. Company Stats
        List<Object[]> compStats = tripRepository.getCompanyStats();
        List<java.util.Map<String, Object>> uniqueCompanies = new java.util.ArrayList<>();
        for (Object[] row : compStats) {
            if (row[0] != null) {
                uniqueCompanies.add(java.util.Map.of(
                    "name", row[0],
                    "count", row[1],
                    "minPrice", row[2] != null ? row[2] : 0.0
                ));
            }
        }

        // 4. Unique Trips (Popular Routes bypass)
        List<Object[]> popStats = tripRepository.getPopularRoutes();
        List<java.util.Map<String, Object>> uniqueTrips = new java.util.ArrayList<>();
        for (Object[] row : popStats) {
            if (row[1] != null && row[2] != null) {
                uniqueTrips.add(java.util.Map.of(
                    "id", row[0] != null ? row[0] : 0L,
                    "departurePoint", row[1],
                    "arrivalPoint", row[2],
                    "imageUrl", row[3] != null ? row[3] : "",
                    "departureDate", row[4] != null ? row[4] : "",
                    "ticketPrice", row[5] != null ? row[5] : 0.0,
                    "busType", row[6] != null ? row[6] : "",
                    "companyName", row[7] != null ? row[7] : ""
                ));
            }
        }

        response.put("allDeparturePoints", allDepPoints);
        response.put("allArrivalPoints", allArrPoints);
        response.put("uniqueStations", new java.util.ArrayList<>(stationsMap.values()));
        response.put("uniqueCompanies", uniqueCompanies);
        response.put("uniqueTrips", uniqueTrips);

        return ResponseEntity.ok(response);
    }

    // 2. TÌM KIẾM CHUYẾN XE
    @GetMapping("/search")
    public ResponseEntity<List<Trip>> searchTrips(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "date", required = false) String date) {
        return ResponseEntity.ok(tripService.searchTrips(from, to, date));
    }

    // 3. CHI TIẾT 1 CHUYẾN XE (READ SINGLE)
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable("id") Long id) {
        Optional<Trip> tripOpt = tripRepository.findById(id);
        
        if (tripOpt.isPresent()) {
            return ResponseEntity.ok(tripOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. TẠO MỚI CHUYẾN XE (CREATE)
    @com.smartbus.booking.annotation.AuditAction(action = "CREATE_TRIP", entityName = "Trip")
    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        return ResponseEntity.ok(tripService.saveTrip(trip));
    }

    // 5. CẬP NHẬT CHUYẾN XE (UPDATE)
    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_TRIP", entityName = "Trip")
    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable("id") Long id, @RequestBody Trip trip) {
        return ResponseEntity.ok(tripService.updateTrip(id, trip));
    }

    @com.smartbus.booking.annotation.AuditAction(action = "ASSIGN_TRIP_PAIR", entityName = "Trip")
    @PutMapping("/{id}/assign-pair")
    public ResponseEntity<List<Trip>> assignTripPair(
            @PathVariable("id") Long id,
            @RequestBody TripPairAssignmentRequest request) {
        return ResponseEntity.ok(tripService.assignTripPair(id, request));
    }

    // 6. XÓA CHUYẾN XE (DELETE)
    @com.smartbus.booking.annotation.AuditAction(action = "DELETE_TRIP", entityName = "Trip")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable("id") Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    @com.smartbus.booking.annotation.AuditAction(action = "BULK_DELETE_TRIPS", entityName = "Trip")
    @DeleteMapping("/bulk")
    public ResponseEntity<java.util.Map<String, Object>> deleteTrips(
            @RequestBody java.util.Map<String, List<Long>> body) {
        int deletedCount = tripService.deleteTrips(body.get("ids"));
        return ResponseEntity.ok(java.util.Map.of(
                "deletedCount", deletedCount,
                "message", "Đã xóa " + deletedCount + " chuyến xe."));
    }

    // 7. CẬP NHẬT TRẠNG THÁI TÀI XẾ CHẤP NHẬN CHUYẾN
    @com.smartbus.booking.annotation.AuditAction(action = "ACCEPT_TRIP", entityName = "Trip")
    @PatchMapping("/{id}/accept")
    public ResponseEntity<Trip> acceptTrip(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tripService.acceptTrip(id));
    }

    // 8. TÀI XẾ TỪ CHỐI CHUYẾN
    @com.smartbus.booking.annotation.AuditAction(action = "REJECT_TRIP", entityName = "Trip")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Trip> rejectTrip(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tripService.rejectTrip(id));
    }

    // 9. CẬP NHẬT TRẠNG THÁI CHUYẾN ĐI (SCHEDULED, IN_PROGRESS, COMPLETED)
    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_TRIP_STATUS", entityName = "Trip")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Trip> updateTripStatus(@PathVariable("id") Long id, @RequestBody java.util.Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(tripService.updateTripStatus(id, status));
    }

    // 7. BẬT/TẮT HIỂN THỊ CHUYẾN XE (TOGGLE VISIBILITY)
    @com.smartbus.booking.annotation.AuditAction(action = "TOGGLE_TRIP_VISIBILITY", entityName = "Trip")
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Trip> toggleVisibility(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tripService.toggleVisibility(id));
    }
}
