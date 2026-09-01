package com.smartbus.booking.service;

import com.smartbus.booking.entity.Bus;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.entity.VehicleMileageLog;
import com.smartbus.booking.repository.BusRepository;
import com.smartbus.booking.repository.TripRepository;
import com.smartbus.booking.repository.VehicleMileageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VehicleMileageService {
    private static final Pattern DISTANCE_PATTERN = Pattern.compile("\\(([\\d.,]+)\\s*km\\)", Pattern.CASE_INSENSITIVE);
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final VehicleMileageLogRepository mileageLogRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void recordCompletedTrip(Trip trip) {
        if (!"COMPLETED".equalsIgnoreCase(trip.getStatus()) || trip.getAssignedLicensePlate() == null) return;
        if (Boolean.TRUE.equals(trip.getMileageRecorded()) || mileageLogRepository.existsByTripId(trip.getId())) {
            if (!Boolean.TRUE.equals(trip.getMileageRecorded())) {
                trip.setMileageRecorded(true);
                tripRepository.save(trip);
            }
            return;
        }

        Bus bus = busRepository.findByLicensePlateIgnoreCase(trip.getAssignedLicensePlate())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe " + trip.getAssignedLicensePlate()));
        double distance = resolveDistance(trip);
        if (distance <= 0) throw new IllegalArgumentException("Chuyến chưa có khoảng cách hợp lệ để cộng kilomet.");

        double previous = safe(bus.getCurrentMileage());
        double current = round(previous + distance);
        bus.setCurrentMileage(current);
        updateAlertLevel(bus, true);
        busRepository.save(bus);

        mileageLogRepository.save(VehicleMileageLog.builder()
                .bus(bus).trip(trip).type("TRIP")
                .previousMileage(previous).distanceAdded(distance).newMileage(current)
                .note("Hoàn thành chuyến #" + trip.getId()).build());
        trip.setDistanceKm(distance);
        trip.setMileageRecorded(true);
        tripRepository.save(trip);
    }

    @Transactional
    public Bus adjustMileage(Long busId, Double currentMileage, String note) {
        if (currentMileage == null || currentMileage < 0) throw new IllegalArgumentException("Số kilomet phải lớn hơn hoặc bằng 0.");
        Bus bus = getBus(busId);
        double previous = safe(bus.getCurrentMileage());
        double current = round(currentMileage);
        bus.setCurrentMileage(current);
        updateAlertLevel(bus, current >= previous);
        busRepository.save(bus);
        mileageLogRepository.save(VehicleMileageLog.builder()
                .bus(bus).type("MANUAL").previousMileage(previous)
                .distanceAdded(round(current - previous)).newMileage(current)
                .note(note == null || note.isBlank() ? "Điều chỉnh công-tơ-mét" : note.trim()).build());
        return bus;
    }

    @Transactional
    public Bus resetAfterMaintenance(Bus bus, Double odometer, String note) {
        double previous = safe(bus.getCurrentMileage());
        double current = odometer == null ? previous : round(odometer);
        if (current < 0) throw new IllegalArgumentException("Số kilomet bảo trì không hợp lệ.");
        bus.setCurrentMileage(current);
        bus.setLastMaintenanceMileage(current);
        bus.setMaintenanceAlertLevel(0);
        busRepository.save(bus);
        mileageLogRepository.save(VehicleMileageLog.builder()
                .bus(bus).type("MAINTENANCE").previousMileage(previous)
                .distanceAdded(round(current - previous)).newMileage(current)
                .note(note == null || note.isBlank() ? "Chốt kilomet sau bảo trì" : note.trim()).build());
        return bus;
    }

    public List<VehicleMileageLog> getLogs(Long busId) {
        getBus(busId);
        return mileageLogRepository.findByBusIdOrderByRecordedAtDesc(busId);
    }

    private void updateAlertLevel(Bus bus, boolean notify) {
        int interval = bus.getMaintenanceIntervalKm() == null || bus.getMaintenanceIntervalKm() <= 0 ? 10000 : bus.getMaintenanceIntervalKm();
        double travelled = Math.max(0, safe(bus.getCurrentMileage()) - safe(bus.getLastMaintenanceMileage()));
        int level = travelled >= interval ? 100 : travelled >= interval * .8 ? 80 : 0;
        int oldLevel = bus.getMaintenanceAlertLevel() == null ? 0 : bus.getMaintenanceAlertLevel();
        bus.setMaintenanceAlertLevel(level);
        if (notify && level > oldLevel) publishWarning(bus, level, travelled, interval);
    }

    private void publishWarning(Bus bus, int level, double travelled, int interval) {
        double remaining = round(interval - travelled);
        String title = level >= 100 ? "Xe đã đến hạn bảo trì" : "Xe sắp đến hạn bảo trì";
        String message = level >= 100
                ? "Xe " + bus.getLicensePlate() + " đã vượt chu kỳ " + interval + " km " + Math.abs(remaining) + " km."
                : "Xe " + bus.getLicensePlate() + " còn " + remaining + " km đến kỳ bảo trì.";
        messagingTemplate.convertAndSend("/topic/admin/maintenance", Map.of(
                "type", "MAINTENANCE_DUE", "level", level, "title", title, "message", message,
                "busId", bus.getId(), "licensePlate", bus.getLicensePlate(), "date", java.time.LocalDateTime.now().toString()));
    }

    private double resolveDistance(Trip trip) {
        if (trip.getDistanceKm() != null && trip.getDistanceKm() > 0) return round(trip.getDistanceKm());
        Matcher matcher = DISTANCE_PATTERN.matcher(String.valueOf(trip.getDuration()));
        if (matcher.find()) return round(Double.parseDouble(matcher.group(1).replace(',', '.')));
        if (trip.getDepartureLat() != null && trip.getDepartureLng() != null && trip.getArrivalLat() != null && trip.getArrivalLng() != null) {
            double earthRadius = 6371;
            double lat = Math.toRadians(trip.getArrivalLat() - trip.getDepartureLat());
            double lng = Math.toRadians(trip.getArrivalLng() - trip.getDepartureLng());
            double a = Math.sin(lat / 2) * Math.sin(lat / 2)
                    + Math.cos(Math.toRadians(trip.getDepartureLat())) * Math.cos(Math.toRadians(trip.getArrivalLat()))
                    * Math.sin(lng / 2) * Math.sin(lng / 2);
            return round(earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)) * 1.2);
        }
        return 0;
    }

    private Bus getBus(Long id) {
        return busRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe."));
    }
    private double safe(Double value) { return value == null ? 0 : value; }
    private double round(double value) { return Math.round(value * 10.0) / 10.0; }
}
