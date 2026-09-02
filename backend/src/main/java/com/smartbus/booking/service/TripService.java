package com.smartbus.booking.service;

import com.smartbus.booking.entity.LeaveRequest;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.dto.TripPairAssignmentRequest;
import com.smartbus.booking.repository.LeaveRequestRepository;
import com.smartbus.booking.repository.BusTypeRepository;
import com.smartbus.booking.repository.BusRepository;
import com.smartbus.booking.repository.TripRepository;
import com.smartbus.booking.repository.VehicleMileageLogRepository;
import com.smartbus.booking.repository.BookingRepository;
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
    private final BookingRepository bookingRepository;
    private final com.smartbus.booking.repository.UserRepository userRepository;

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

        List<Trip> nearbyTrips = tripRepository.findTripsForDriverSchedule(driverUsername, excludeTripId);
        Trip c = findOperationalConflict(
                departureDate, departureTime, arrivalTime, departurePoint, arrivalPoint, nearbyTrips);
        if (c != null) {
                throw new IllegalArgumentException(
                        "⚠️ XUNG ĐỘT LỊCH TÀI XẾ: Tài xế "
                                + (c.getAssignedDriverFullName() != null ? c.getAssignedDriverFullName()
                                        : driverUsername)
                                +
                                " đã có chuyến " + c.getDeparturePoint().split(",")[0] + " ➔ "
                                + c.getArrivalPoint().split(",")[0] +
                                " (" + c.getDepartureTime() + " - " + c.getArrivalTime() + ") ngày "
                                + c.getDepartureDate() +
                                ". Điểm xuất phát chuyến sau phải nối tiếp điểm đến chuyến trước và phải đủ thời gian nghỉ!");
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

        List<Trip> nearbyTrips = tripRepository.findTripsForBusSchedule(licensePlate, excludeTripId);
        Trip c = findOperationalConflict(
                departureDate, departureTime, arrivalTime, departurePoint, arrivalPoint, nearbyTrips);
        if (c != null) {
                throw new IllegalArgumentException(
                        "⚠️ XUNG ĐỘT LỊCH XE: Xe biển số " + licensePlate +
                                " đã được phân công cho chuyến " + c.getDeparturePoint().split(",")[0] + " ➔ "
                                + c.getArrivalPoint().split(",")[0] +
                                " (" + c.getDepartureTime() + " - " + c.getArrivalTime() + ") ngày "
                                + c.getDepartureDate() +
                                ". Xe phải xuất phát từ nơi chuyến trước kết thúc và phải đủ thời gian quay đầu!");
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

        List<Trip> nearbyTrips = tripRepository.findTripsForInspectorSchedule(inspectorId, excludeTripId);
        Trip c = findOperationalConflict(
                departureDate, departureTime, arrivalTime, departurePoint, arrivalPoint, nearbyTrips);
        if (c != null) {
                throw new IllegalArgumentException(
                        "⚠️ XUNG ĐỘT LỊCH LƠ XE: Lơ xe đã được phân công cho chuyến "
                                + c.getDeparturePoint().split(",")[0] + " ➔ " + c.getArrivalPoint().split(",")[0] +
                                " (" + c.getDepartureTime() + " - " + c.getArrivalTime() + ") ngày "
                                + c.getDepartureDate() +
                                ". Lơ xe phải ở đúng điểm xuất phát kế tiếp và phải đủ thời gian nghỉ!");
        }
    }

    private record TripWindow(java.time.LocalDateTime start, java.time.LocalDateTime end) {}

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
        if (!sameStation) return true;

        long requiredRestMinutes = sameStation
                && target.start().toLocalDate().equals(existing.start().toLocalDate()) ? 60 : 720;
        long actualGapMinutes = java.time.Duration.between(earlierEnd, laterStart).toMinutes();
        return actualGapMinutes < requiredRestMinutes;
    }

    /**
     * Chỉ so vị trí với chuyến liền trước/liền sau. Nếu so với mọi chuyến cũ,
     * một chuỗi hợp lệ A→B, B→A, A→C sẽ bị báo sai do A→B không nối thẳng A→C.
     */
    private Trip findOperationalConflict(
            String departureDate,
            String departureTime,
            String arrivalTime,
            String departurePoint,
            String arrivalPoint,
            List<Trip> existingTrips) {
        TripWindow target = tripWindow(departureDate, departureTime, arrivalTime);
        Trip previous = null;
        Trip next = null;
        java.time.LocalDateTime previousEnd = null;
        java.time.LocalDateTime nextStart = null;

        for (Trip existingTrip : existingTrips) {
            TripWindow existing = tripWindow(
                    existingTrip.getDepartureDate(),
                    existingTrip.getDepartureTime(),
                    existingTrip.getArrivalTime());

            if (target.start().isBefore(existing.end()) && existing.start().isBefore(target.end())) {
                return existingTrip;
            }
            if (!existing.end().isAfter(target.start())
                    && (previousEnd == null || existing.end().isAfter(previousEnd))) {
                previous = existingTrip;
                previousEnd = existing.end();
            }
            if (!target.end().isAfter(existing.start())
                    && (nextStart == null || existing.start().isBefore(nextStart))) {
                next = existingTrip;
                nextStart = existing.start();
            }
        }

        if (previous != null && hasOperationalConflict(
                departureDate, departureTime, arrivalTime, departurePoint, arrivalPoint, previous)) {
            return previous;
        }
        if (next != null && hasOperationalConflict(
                departureDate, departureTime, arrivalTime, departurePoint, arrivalPoint, next)) {
            return next;
        }
        return null;
    }

    private java.util.Optional<com.smartbus.booking.entity.User> findDriver(String identity) {
        if (identity == null || identity.isBlank()) return java.util.Optional.empty();
        java.util.Optional<com.smartbus.booking.entity.User> byPhone = userRepository.findByPhone(identity);
        return byPhone.isPresent() ? byPhone : userRepository.findByUsernameIgnoreCase(identity);
    }

    // ============================================================================
    // CRUD OPERATIONS (CÓ TÍCH HỢP CONFLICT DETECTION + NOTIFICATION)
    // ============================================================================

    // Khi lưu chuyến xe mới, tự động sinh ra 24 ghế tương ứng!
    @Transactional
    public Trip saveTrip(Trip trip) {
        synchronizeSeatCapacity(trip);
        validateDriverPair(trip);
        validateSecondaryDriverRequirement(trip);
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
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe với mã ID: " + id));
        if ("COMPLETED".equalsIgnoreCase(trip.getStatus())) {
            throw new IllegalStateException(
                    "Chuyến xe đã hoàn thành nên chỉ được phép xem, không thể chỉnh sửa.");
        }

        validateDriverPair(updatedDetails);
        validateSecondaryDriverRequirement(updatedDetails);

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

        ensureTripCanBeDeleted(trip);

        // Nhật ký kilomet là lịch sử vận hành độc lập. Giữ lại log và chỉ tháo
        // liên kết đến chuyến trước khi xóa để không vi phạm khóa ngoại.
        vehicleMileageLogRepository.detachTrip(id);
        vehicleMileageLogRepository.flush();

        // Ghế và đặt vé thuộc chuyến được xử lý theo cascade trên Trip.
        tripRepository.delete(trip);
        tripRepository.flush();
    }

    @Transactional
    public int deleteTrips(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một chuyến xe để xóa.");
        }

        List<Long> uniqueIds = ids.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (uniqueIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách chuyến xe cần xóa không hợp lệ.");
        }

        List<Trip> selectedTrips = tripRepository.findAllById(uniqueIds);
        if (selectedTrips.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("Một hoặc nhiều chuyến xe không còn tồn tại. Vui lòng tải lại danh sách.");
        }

        // Kiểm tra toàn bộ trước khi thay đổi dữ liệu: xóa tất cả hoặc không xóa gì.
        selectedTrips.forEach(this::ensureTripCanBeDeleted);
        uniqueIds.forEach(vehicleMileageLogRepository::detachTrip);
        vehicleMileageLogRepository.flush();
        tripRepository.deleteAll(selectedTrips);
        tripRepository.flush();
        return selectedTrips.size();
    }

    private void ensureTripCanBeDeleted(Trip trip) {
        if ("COMPLETED".equalsIgnoreCase(trip.getStatus())) {
            throw new IllegalStateException(
                    "Không thể xóa chuyến xe đã hoàn thành vì đây là dữ liệu lịch sử vận hành.");
        }
        if (bookingRepository.existsByTripId(trip.getId())) {
            throw new IllegalArgumentException(
                    "Không thể xóa chuyến #" + trip.getId() + " vì chuyến này đã có dữ liệu đặt vé.");
        }
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

        if ("IN_PROGRESS".equals(normalizedStatus)) {
            updateDriverOperationalState(trip.getAssignedDriverUsername(), trip.getDeparturePoint(), "DRIVING");
            updateDriverOperationalState(trip.getSecondaryDriverUsername(), trip.getDeparturePoint(), "DRIVING");
            updateInspectorStation(trip, trip.getDeparturePoint());
        } else if ("COMPLETED".equals(normalizedStatus)) {
            updateDriverOperationalState(trip.getAssignedDriverUsername(), trip.getArrivalPoint(), "FREE");
            updateDriverOperationalState(trip.getSecondaryDriverUsername(), trip.getArrivalPoint(), "FREE");
            updateInspectorStation(trip, trip.getArrivalPoint());
        }

        vehicleMileageService.recordCompletedTrip(savedTrip);
        return savedTrip;
    }

    private void updateDriverOperationalState(String identity, String station, String driverStatus) {
        findDriver(identity).ifPresent(driver -> {
            driver.setCurrentStation(station);
            driver.setDriverStatus(driverStatus);
            userRepository.save(driver);
        });
    }

    private void updateInspectorStation(Trip trip, String station) {
        if (trip.getInspector() == null || trip.getInspector().getId() == null) return;
        inspectorRepository.findById(trip.getInspector().getId()).ifPresent(inspector -> {
            inspector.setCurrentStation(station);
            inspectorRepository.save(inspector);
        });
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

    private void validateSecondaryDriverRequirement(Trip trip) {
        if (!"ASSIGNED".equalsIgnoreCase(trip.getStatus()) || !requiresSecondaryDriver(trip)) {
            return;
        }
        if (trip.getSecondaryDriverUsername() == null || trip.getSecondaryDriverUsername().isBlank()) {
            throw new IllegalArgumentException(
                    "Chuyến có thời lượng từ 6 giờ trở lên bắt buộc phải có tài xế phụ.");
        }
    }

    private boolean requiresSecondaryDriver(Trip trip) {
        if (trip.getDuration() == null || trip.getDuration().isBlank()) {
            return false;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d+)\\s*h(?:\\s*(\\d+)\\s*m)?", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(trip.getDuration());
        if (!matcher.find()) {
            return false;
        }
        int hours = Integer.parseInt(matcher.group(1));
        int minutes = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
        return (hours * 60) + minutes >= 360;
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
