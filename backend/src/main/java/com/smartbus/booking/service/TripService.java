package com.smartbus.booking.service;

import com.smartbus.booking.entity.LeaveRequest;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.dto.TripPairAssignmentRequest;
import com.smartbus.booking.repository.LeaveRequestRepository;
import com.smartbus.booking.repository.BusTypeRepository;
import com.smartbus.booking.repository.BusRepository;
import com.smartbus.booking.repository.TripRepository;
import com.smartbus.booking.repository.VehicleMileageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final SeatService seatService;
    private final LeaveRequestRepository leaveRequestRepository;
    private final BusTypeRepository busTypeRepository;
    private final BusRepository busRepository;
    private final VehicleMileageService vehicleMileageService;
    private final VehicleMileageLogRepository vehicleMileageLogRepository;

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public static String stripAccents(String s) {
        if (s == null)
            return "";
        String normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "D")
                .toLowerCase()
                .trim();
    }

    public List<Trip> searchTrips(String from, String to, String date) {
        String safeDate = date != null ? date.trim() : "";
        String todayStr = java.time.LocalDate.now().toString();

        // Bước 1: Thu hẹp phễu ngay từ Database (Không cho phép lấy toàn bộ 100,000
        // chuyến trong quá khứ)
        List<Trip> tripsByDate = tripRepository.findTripsSafely(safeDate, todayStr);

        if ((from == null || from.trim().isEmpty()) && (to == null || to.trim().isEmpty())) {
            return tripsByDate;
        }

        String normalizedFrom = stripAccents(from);
        String normalizedTo = stripAccents(to);

        return tripsByDate.stream().filter(t -> {
            String tFrom = stripAccents(t.getDeparturePoint());
            String tTo = stripAccents(t.getArrivalPoint());

            boolean matchFrom = normalizedFrom.isEmpty() || tFrom.contains(normalizedFrom)
                    || normalizedFrom.contains(tFrom);
            boolean matchTo = normalizedTo.isEmpty() || tTo.contains(normalizedTo) || normalizedTo.contains(tTo);

            return matchFrom && matchTo;
        }).collect(java.util.stream.Collectors.toList());
    }

    // ============================================================================
    // 🛡️ HỆ THỐNG KIỂM TRA XUNG ĐỘT (CONFLICT DETECTION)
    // ============================================================================

    /**
     * Kiểm tra tài xế có bị trùng lịch hay không (có tính buffer_time = 60 phút).
     * 
     * @throws RuntimeException nếu phát hiện xung đột
     */
    private void checkDriverConflict(String driverUsername, String departureDate, String departureTime,
            String arrivalTime, String departurePoint, String arrivalPoint, Long excludeTripId) {
        if (driverUsername == null || driverUsername.isEmpty())
            return;
        if (departureDate == null || departureTime == null || arrivalTime == null)
            return;

        // 1. Kiểm tra tài xế có đang nghỉ phép không
        List<LeaveRequest> leaves = leaveRequestRepository.findApprovedLeaveOnDate(driverUsername, departureDate);
        if (!leaves.isEmpty()) {
            LeaveRequest leave = leaves.get(0);
            throw new IllegalArgumentException(
                    "⚠️ TÀI XẾ ĐANG NGHỈ PHÉP: Tài xế " + leave.getDriverFullName() +
                            " đã được duyệt nghỉ phép từ " + leave.getStartDate() + " đến " + leave.getEndDate() +
                            ". Không thể gán chuyến trong thời gian này!");
        }

        DateRange range = conflictDateRange(departureDate);
        List<Trip> nearbyTrips = tripRepository.findTripsForDriverInDateRange(
                driverUsername, range.fromDate(), range.toDate(), excludeTripId);

        for (Trip c : nearbyTrips) {
            if (hasOperationalConflict(departureDate, departureTime, arrivalTime, departurePoint, arrivalPoint, c)) {
                throw new IllegalArgumentException(
                        "⚠️ XUNG ĐỘT LỊCH TÀI XẾ: Tài xế "
                                + (c.getAssignedDriverFullName() != null ? c.getAssignedDriverFullName()
                                        : driverUsername)
                                +
                                " đã có chuyến " + c.getDeparturePoint().split(",")[0] + " ➔ "
                                + c.getArrivalPoint().split(",")[0] +
                                " (" + c.getDepartureTime() + " - " + c.getArrivalTime() + ") ngày "
                                + c.getDepartureDate() +
                                ". Không đủ thời gian nghỉ hoặc di chuyển giữa hai chuyến!");
            }
        }
    }

    /**
     * Kiểm tra xe (biển số) có bị trùng lịch hay không (có tính buffer_time = 60
     * phút).
     * 
     * @throws RuntimeException nếu phát hiện xung đột
     */
    private void checkBusConflict(String licensePlate, String departureDate, String departureTime, String arrivalTime,
            String departurePoint, String arrivalPoint, Long excludeTripId) {
        if (licensePlate == null || licensePlate.isEmpty())
            return;
        busRepository.findByLicensePlateIgnoreCase(licensePlate).ifPresent(bus -> {
            if ("BẢO TRÌ".equalsIgnoreCase(bus.getStatus())) {
                throw new IllegalArgumentException("Xe " + licensePlate + " đang bảo trì và không thể phân công chuyến.");
            }
        });
        if (departureDate == null || departureTime == null || arrivalTime == null)
            return;

        DateRange range = conflictDateRange(departureDate);
        List<Trip> nearbyTrips = tripRepository.findTripsForBusInDateRange(
                licensePlate, range.fromDate(), range.toDate(), excludeTripId);

        for (Trip c : nearbyTrips) {
            if (hasOperationalConflict(departureDate, departureTime, arrivalTime, departurePoint, arrivalPoint, c)) {
                throw new IllegalArgumentException(
                        "⚠️ XUNG ĐỘT LỊCH XE: Xe biển số " + licensePlate +
                                " đã được phân công cho chuyến " + c.getDeparturePoint().split(",")[0] + " ➔ "
                                + c.getArrivalPoint().split(",")[0] +
                                " (" + c.getDepartureTime() + " - " + c.getArrivalTime() + ") ngày "
                                + c.getDepartureDate() +
                                ". Không đủ thời gian quay đầu hoặc di chuyển giữa hai chuyến!");
            }
        }
    }

    /**
     * Kiểm tra lơ xe có bị trùng lịch hay không (có tính buffer_time = 60 phút).
     * 
     * @throws RuntimeException nếu phát hiện xung đột
     */
    private void checkInspectorConflict(Long inspectorId, String departureDate, String departureTime,
            String arrivalTime, String departurePoint, String arrivalPoint, Long excludeTripId) {
        if (inspectorId == null)
            return;
        if (departureDate == null || departureTime == null || arrivalTime == null)
            return;

        DateRange range = conflictDateRange(departureDate);
        List<Trip> nearbyTrips = tripRepository.findTripsForInspectorInDateRange(
                inspectorId, range.fromDate(), range.toDate(), excludeTripId);

        for (Trip c : nearbyTrips) {
            if (hasOperationalConflict(departureDate, departureTime, arrivalTime, departurePoint, arrivalPoint, c)) {
                throw new IllegalArgumentException(
                        "⚠️ XUNG ĐỘT LỊCH LƠ XE: Lơ xe đã được phân công cho chuyến "
                                + c.getDeparturePoint().split(",")[0] + " ➔ " + c.getArrivalPoint().split(",")[0] +
                                " (" + c.getDepartureTime() + " - " + c.getArrivalTime() + ") ngày "
                                + c.getDepartureDate() +
                                ". Không đủ thời gian nghỉ hoặc di chuyển giữa hai chuyến!");
            }
        }
    }

    private record DateRange(String fromDate, String toDate) {}

    private record TripWindow(java.time.LocalDateTime start, java.time.LocalDateTime end) {}

    private DateRange conflictDateRange(String departureDate) {
        java.time.LocalDate date = java.time.LocalDate.parse(departureDate);
        return new DateRange(date.minusDays(1).toString(), date.plusDays(1).toString());
    }

    private TripWindow tripWindow(String date, String departureTime, String arrivalTime) {
        java.time.LocalDate departureDate = java.time.LocalDate.parse(date);
        java.time.LocalDateTime start = java.time.LocalDateTime.of(
                departureDate, java.time.LocalTime.parse(departureTime));
        java.time.LocalDateTime end = java.time.LocalDateTime.of(
                departureDate, java.time.LocalTime.parse(arrivalTime));
        if (!end.isAfter(start)) end = end.plusDays(1);
        return new TripWindow(start, end);
    }

    private String operationalPoint(String value) {
        if (value == null) return "";
        String[] parts = value.split(",");
        return stripAccents(parts[parts.length - 1])
                .replaceAll("\\b(thanh pho|tp|tinh)\\b", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean hasOperationalConflict(
            String departureDate,
            String departureTime,
            String arrivalTime,
            String departurePoint,
            String arrivalPoint,
            Trip existingTrip) {
        TripWindow target = tripWindow(departureDate, departureTime, arrivalTime);
        TripWindow existing = tripWindow(
                existingTrip.getDepartureDate(),
                existingTrip.getDepartureTime(),
                existingTrip.getArrivalTime());

        if (target.start().isBefore(existing.end()) && existing.start().isBefore(target.end())) {
            return true;
        }

        boolean targetBeforeExisting = !target.end().isAfter(existing.start());
        java.time.LocalDateTime earlierEnd = targetBeforeExisting ? target.end() : existing.end();
        java.time.LocalDateTime laterStart = targetBeforeExisting ? existing.start() : target.start();
        String earlierArrival = targetBeforeExisting ? arrivalPoint : existingTrip.getArrivalPoint();
        String laterDeparture = targetBeforeExisting ? existingTrip.getDeparturePoint() : departurePoint;
        boolean sameStation = !operationalPoint(earlierArrival).isBlank()
                && operationalPoint(earlierArrival).equals(operationalPoint(laterDeparture));
        long requiredRestMinutes = sameStation
                && target.start().toLocalDate().equals(existing.start().toLocalDate()) ? 60 : 720;
        long actualGapMinutes = java.time.Duration.between(earlierEnd, laterStart).toMinutes();
        return actualGapMinutes < requiredRestMinutes;
    }

    // ============================================================================
    // CRUD OPERATIONS (CÓ TÍCH HỢP CONFLICT DETECTION + NOTIFICATION)
    // ============================================================================

    // Khi lưu chuyến xe mới, tự động sinh ra 24 ghế tương ứng!
    @Transactional
    public Trip saveTrip(Trip trip) {
        synchronizeSeatCapacity(trip);
        validateDriverPair(trip);
        // 🛡️ KIỂM TRA XUNG ĐỘT TRƯỚC KHI LƯU
        checkDriverConflict(trip.getAssignedDriverUsername(), trip.getDepartureDate(), trip.getDepartureTime(),
                trip.getArrivalTime(), trip.getDeparturePoint(), trip.getArrivalPoint(), null);
        checkDriverConflict(trip.getSecondaryDriverUsername(), trip.getDepartureDate(), trip.getDepartureTime(),
                trip.getArrivalTime(), trip.getDeparturePoint(), trip.getArrivalPoint(), null);
        checkBusConflict(trip.getAssignedLicensePlate(), trip.getDepartureDate(), trip.getDepartureTime(),
                trip.getArrivalTime(), trip.getDeparturePoint(), trip.getArrivalPoint(), null);
        if (trip.getInspector() != null) {
            checkInspectorConflict(trip.getInspector().getId(), trip.getDepartureDate(), trip.getDepartureTime(),
                    trip.getArrivalTime(), trip.getDeparturePoint(), trip.getArrivalPoint(), null);
        }

        if (trip.getTotalSeats() == null || trip.getTotalSeats() == 24) {
            trip.setTotalSeats(trip.getAvailableSeats());
        }
        Trip savedTrip = tripRepository.save(trip);
        seatService.createDefaultSeatsForTrip(savedTrip); // Tự sinh sơ đồ ghế tự động cực thông minh
        vehicleMileageService.recordCompletedTrip(savedTrip);

        return savedTrip;
    }

    @Transactional
    public Trip updateTrip(Long id, Trip updatedDetails) {
        validateDriverPair(updatedDetails);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe với mã ID: " + id));

        synchronizeSeatCapacity(updatedDetails);

        // 🛡️ KIỂM TRA XUNG ĐỘT TRƯỚC KHI CẬP NHẬT (bỏ qua chính chuyến xe hiện tại)
        checkDriverConflict(updatedDetails.getAssignedDriverUsername(), updatedDetails.getDepartureDate(),
                updatedDetails.getDepartureTime(), updatedDetails.getArrivalTime(), updatedDetails.getDeparturePoint(),
                updatedDetails.getArrivalPoint(), id);
        checkDriverConflict(updatedDetails.getSecondaryDriverUsername(), updatedDetails.getDepartureDate(),
                updatedDetails.getDepartureTime(), updatedDetails.getArrivalTime(), updatedDetails.getDeparturePoint(),
                updatedDetails.getArrivalPoint(), id);
        checkBusConflict(updatedDetails.getAssignedLicensePlate(), updatedDetails.getDepartureDate(),
                updatedDetails.getDepartureTime(), updatedDetails.getArrivalTime(), updatedDetails.getDeparturePoint(),
                updatedDetails.getArrivalPoint(), id);
        if (updatedDetails.getInspector() != null) {
            checkInspectorConflict(updatedDetails.getInspector().getId(), updatedDetails.getDepartureDate(),
                    updatedDetails.getDepartureTime(), updatedDetails.getArrivalTime(), updatedDetails.getDeparturePoint(),
                    updatedDetails.getArrivalPoint(), id);
        }

        // Phát hiện thay đổi tài xế / lơ xe / xe để gửi notification
        String oldDriver = trip.getAssignedDriverUsername();
        String newDriver = updatedDetails.getAssignedDriverUsername();
        String oldInspector = trip.getInspector() != null ? trip.getInspector().getPhone() : null;
        String newInspector = updatedDetails.getInspector() != null ? updatedDetails.getInspector().getPhone() : null;
        String oldPlate = trip.getAssignedLicensePlate();
        String newPlate = updatedDetails.getAssignedLicensePlate();

        Integer requestedTotalSeats = updatedDetails.getTotalSeats();
        boolean seatCountChanged = false;
        boolean busTypeChanged = !java.util.Objects.equals(trip.getBusType(), updatedDetails.getBusType());

        if (requestedTotalSeats != null && !requestedTotalSeats.equals(trip.getTotalSeats())) {
            seatCountChanged = true;
        }

        if ((seatCountChanged || busTypeChanged)
                && trip.getAvailableSeats() != null
                && trip.getAvailableSeats() < trip.getTotalSeats()) {
            throw new RuntimeException(
                    "Không thể thay đổi loại xe hoặc số ghế vì chuyến này đã có khách đặt vé.");
        }

        trip.setCompanyName(updatedDetails.getCompanyName());
        trip.setRouteId(updatedDetails.getRouteId());
        trip.setBusType(updatedDetails.getBusType());
        trip.setAssignedLicensePlate(updatedDetails.getAssignedLicensePlate());
        trip.setAssignedDriverUsername(updatedDetails.getAssignedDriverUsername());
        trip.setAssignedDriverFullName(updatedDetails.getAssignedDriverFullName());
        trip.setSecondaryDriverUsername(updatedDetails.getSecondaryDriverUsername());
        trip.setSecondaryDriverFullName(updatedDetails.getSecondaryDriverFullName());
        trip.setDeparturePoint(updatedDetails.getDeparturePoint());
        trip.setArrivalPoint(updatedDetails.getArrivalPoint());
        trip.setDepartureTime(updatedDetails.getDepartureTime());
        trip.setDepartureDate(updatedDetails.getDepartureDate()); // 📅 CHÈN CHỮ CHỮ KÝ SINH MỆNH: Đồng bộ ngày vào
                                                                  // Database!
        trip.setArrivalTime(updatedDetails.getArrivalTime());
        trip.setDuration(updatedDetails.getDuration());
        trip.setPrice(updatedDetails.getPrice());
        trip.setRating(updatedDetails.getRating());

        if (seatCountChanged) {
            trip.setTotalSeats(requestedTotalSeats);
            trip.setAvailableSeats(requestedTotalSeats);
        }

        trip.setImageUrl(updatedDetails.getImageUrl()); // 🖼️ CHÈN CHỮ KÝ SINH MỆNH: Lưu hình ảnh vào Database!
        trip.setInstantConfirmation(updatedDetails.getInstantConfirmation());
        trip.setDepartureLat(updatedDetails.getDepartureLat());
        trip.setDepartureLng(updatedDetails.getDepartureLng());
        trip.setArrivalLat(updatedDetails.getArrivalLat());
        trip.setArrivalLng(updatedDetails.getArrivalLng());

        if (updatedDetails.getStatus() != null) {
            trip.setStatus(updatedDetails.getStatus());
        }

        if (updatedDetails.getIsVisible() != null) {
            trip.setIsVisible(updatedDetails.getIsVisible());
        }

        // Phân công lơ xe (inspector)
        trip.setInspector(updatedDetails.getInspector());

        if (updatedDetails.getRouteData() != null) {
            trip.setRouteData(updatedDetails.getRouteData());
        }

        Trip savedTrip = tripRepository.save(trip);
        vehicleMileageService.recordCompletedTrip(savedTrip);

        // Chỉ dựng lại sơ đồ khi chuyến chưa bán vé.
        if (seatCountChanged || busTypeChanged) {
            seatService.deleteAllSeatsByTripId(id); // Xóa sạch ghế thừa cũ
            seatService.createDefaultSeatsForTrip(savedTrip); // Vẽ lại đúng số ghế mới
        }

        return savedTrip;
    }

    @Transactional
    public List<Trip> assignTripPair(Long outboundTripId, TripPairAssignmentRequest request) {
        if (request == null || request.returnTripId() == null
                || request.outboundTrip() == null || request.returnTrip() == null) {
            throw new IllegalArgumentException("Thiếu thông tin chuyến đi hoặc chuyến về.");
        }
        if (outboundTripId.equals(request.returnTripId())) {
            throw new IllegalArgumentException("Chuyến đi và chuyến về phải khác nhau.");
        }

        Trip outbound = tripRepository.findById(outboundTripId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi mã " + outboundTripId + "."));
        Trip returnTrip = tripRepository.findById(request.returnTripId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến về mã " + request.returnTripId() + "."));

        boolean reverseRoute = stripAccents(outbound.getDeparturePoint())
                .equals(stripAccents(returnTrip.getArrivalPoint()))
                && stripAccents(outbound.getArrivalPoint())
                .equals(stripAccents(returnTrip.getDeparturePoint()));
        if (!reverseRoute || !java.util.Objects.equals(outbound.getBusType(), returnTrip.getBusType())) {
            throw new IllegalArgumentException("Chuyến được chọn không phải chiều về cùng tuyến và cùng dòng xe.");
        }

        boolean returnAssignedToAnotherResource =
                (returnTrip.getAssignedDriverUsername() != null && !returnTrip.getAssignedDriverUsername().isBlank())
                || (returnTrip.getAssignedLicensePlate() != null && !returnTrip.getAssignedLicensePlate().isBlank());
        if (returnAssignedToAnotherResource) {
            throw new IllegalStateException("Chuyến về vừa được người khác phân công. Vui lòng tải lại lịch.");
        }

        Trip savedOutbound = updateTrip(outboundTripId, request.outboundTrip());
        Trip savedReturn = updateTrip(request.returnTripId(), request.returnTrip());
        return List.of(savedOutbound, savedReturn);
    }

    @Transactional
    public void deleteTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến xe với mã ID: " + id));

        // Nhật ký kilomet là lịch sử vận hành độc lập. Giữ lại log và chỉ tháo
        // liên kết đến chuyến trước khi xóa để không vi phạm khóa ngoại.
        vehicleMileageLogRepository.detachTrip(id);
        vehicleMileageLogRepository.flush();

        // Ghế và đặt vé thuộc chuyến được xử lý theo cascade trên Trip.
        tripRepository.delete(trip);
        tripRepository.flush();
    }

    private final com.smartbus.booking.repository.InspectorRepository inspectorRepository;

    public List<Trip> getTripsByInspectorId(Long userId) {
        java.util.Optional<com.smartbus.booking.entity.Inspector> inspector = inspectorRepository
                .findByUserAccountId(userId);
        if (inspector.isPresent()) {
            return tripRepository.findByInspectorId(inspector.get().getId());
        }
        return java.util.Collections.emptyList();
    }

    @Transactional
    public Trip updateTripStatus(Long id, String status) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe với mã ID: " + id));
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        trip.setStatus(normalizedStatus);
        Trip savedTrip = tripRepository.save(trip);

        if (trip.getAssignedLicensePlate() != null && !trip.getAssignedLicensePlate().isBlank()) {
            busRepository.findByLicensePlateIgnoreCase(trip.getAssignedLicensePlate()).ifPresent(bus -> {
                if ("IN_PROGRESS".equals(normalizedStatus)) {
                    bus.setCurrentStation(trip.getDeparturePoint());
                    bus.setStatus("ĐANG CHẠY");
                } else if ("COMPLETED".equals(normalizedStatus)) {
                    bus.setCurrentStation(trip.getArrivalPoint());
                    bus.setStatus("ĐANG NGHỈ");
                }
                busRepository.save(bus);
            });
        }

        vehicleMileageService.recordCompletedTrip(savedTrip);
        return savedTrip;
    }

    @Transactional
    public Trip acceptTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe với mã ID: " + id));
        trip.setDriverAccepted(true);
        return tripRepository.save(trip);
    }

    @Transactional
    public Trip rejectTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe với mã ID: " + id));
        // Reset driver assignment
        trip.setAssignedDriverUsername(null);
        trip.setAssignedDriverFullName(null);
        trip.setDriverAccepted(false);
        return tripRepository.save(trip);
    }

    @Transactional
    public Trip toggleVisibility(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe với mã ID: " + id));
        trip.setIsVisible(trip.getIsVisible() == null ? false : !trip.getIsVisible());
        return tripRepository.save(trip);
    }

    private void validateDriverPair(Trip trip) {
        if (trip.getAssignedDriverUsername() != null
                && trip.getAssignedDriverUsername().equals(trip.getSecondaryDriverUsername())) {
            throw new IllegalArgumentException("Tài xế chính và tài xế phụ phải là hai người khác nhau.");
        }
    }

    private void synchronizeSeatCapacity(Trip trip) {
        if (trip.getBusType() == null || trip.getBusType().isBlank()) {
            return;
        }

        busTypeRepository.findByNameIgnoreCase(trip.getBusType().trim()).ifPresent(busType -> {
            Integer seatCount = busType.getSeatCount();
            if (seatCount != null && seatCount > 0) {
                trip.setAvailableSeats(seatCount);
                trip.setTotalSeats(seatCount);
            }
        });
    }
}
