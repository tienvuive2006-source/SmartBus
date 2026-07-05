package com.smartbus.booking.service;

import com.smartbus.booking.entity.LeaveRequest;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.repository.LeaveRequestRepository;
import com.smartbus.booking.repository.TripRepository;
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
            String arrivalTime, Long excludeTripId) {
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

        // 2. Kiểm tra xung đột lịch trình (buffer_time = 60 phút)
        List<Trip> dailyTrips = tripRepository.findTripsForDriverOnDate(driverUsername, departureDate, excludeTripId);
        java.time.LocalTime newStart = java.time.LocalTime.parse(departureTime);
        java.time.LocalTime newEnd = java.time.LocalTime.parse(arrivalTime).plusMinutes(60);

        for (Trip c : dailyTrips) {
            java.time.LocalTime existingStart = java.time.LocalTime.parse(c.getDepartureTime());
            java.time.LocalTime existingEnd = java.time.LocalTime.parse(c.getArrivalTime()).plusMinutes(60);

            // Công thức giao nhau: Max(start1, start2) < Min(end1, end2)
            if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                throw new IllegalArgumentException(
                        "⚠️ XUNG ĐỘT LỊCH TÀI XẾ: Tài xế "
                                + (c.getAssignedDriverFullName() != null ? c.getAssignedDriverFullName()
                                        : driverUsername)
                                +
                                " đã có chuyến " + c.getDeparturePoint().split(",")[0] + " ➔ "
                                + c.getArrivalPoint().split(",")[0] +
                                " (" + c.getDepartureTime() + " - " + c.getArrivalTime() + ") cùng ngày "
                                + departureDate +
                                ". Hệ thống yêu cầu 60 phút nghỉ ngơi giữa các chuyến!");
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
            Long excludeTripId) {
        if (licensePlate == null || licensePlate.isEmpty())
            return;
        if (departureDate == null || departureTime == null || arrivalTime == null)
            return;

        List<Trip> dailyTrips = tripRepository.findTripsForBusOnDate(licensePlate, departureDate, excludeTripId);
        java.time.LocalTime newStart = java.time.LocalTime.parse(departureTime);
        java.time.LocalTime newEnd = java.time.LocalTime.parse(arrivalTime).plusMinutes(60);

        for (Trip c : dailyTrips) {
            java.time.LocalTime existingStart = java.time.LocalTime.parse(c.getDepartureTime());
            java.time.LocalTime existingEnd = java.time.LocalTime.parse(c.getArrivalTime()).plusMinutes(60);

            if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                throw new IllegalArgumentException(
                        "⚠️ XUNG ĐỘT LỊCH XE: Xe biển số " + licensePlate +
                                " đã được phân công cho chuyến " + c.getDeparturePoint().split(",")[0] + " ➔ "
                                + c.getArrivalPoint().split(",")[0] +
                                " (" + c.getDepartureTime() + " - " + c.getArrivalTime() + ") cùng ngày "
                                + departureDate +
                                ". Hệ thống yêu cầu 60 phút quay đầu xe giữa các chuyến!");
            }
        }
    }

    /**
     * Kiểm tra lơ xe có bị trùng lịch hay không (có tính buffer_time = 60 phút).
     * 
     * @throws RuntimeException nếu phát hiện xung đột
     */
    private void checkInspectorConflict(Long inspectorId, String departureDate, String departureTime,
            String arrivalTime, Long excludeTripId) {
        if (inspectorId == null)
            return;
        if (departureDate == null || departureTime == null || arrivalTime == null)
            return;

        List<Trip> dailyTrips = tripRepository.findTripsForInspectorOnDate(inspectorId, departureDate, excludeTripId);
        java.time.LocalTime newStart = java.time.LocalTime.parse(departureTime);
        java.time.LocalTime newEnd = java.time.LocalTime.parse(arrivalTime).plusMinutes(60);

        for (Trip c : dailyTrips) {
            java.time.LocalTime existingStart = java.time.LocalTime.parse(c.getDepartureTime());
            java.time.LocalTime existingEnd = java.time.LocalTime.parse(c.getArrivalTime()).plusMinutes(60);

            if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                throw new IllegalArgumentException(
                        "⚠️ XUNG ĐỘT LỊCH LƠ XE: Lơ xe đã được phân công cho chuyến "
                                + c.getDeparturePoint().split(",")[0] + " ➔ " + c.getArrivalPoint().split(",")[0] +
                                " (" + c.getDepartureTime() + " - " + c.getArrivalTime() + ") cùng ngày "
                                + departureDate +
                                ". Hệ thống yêu cầu 60 phút nghỉ ngơi giữa các chuyến!");
            }
        }
    }

    // ============================================================================
    // CRUD OPERATIONS (CÓ TÍCH HỢP CONFLICT DETECTION + NOTIFICATION)
    // ============================================================================

    // Khi lưu chuyến xe mới, tự động sinh ra 24 ghế tương ứng!
    @Transactional
    public Trip saveTrip(Trip trip) {
        // 🛡️ KIỂM TRA XUNG ĐỘT TRƯỚC KHI LƯU
        checkDriverConflict(trip.getAssignedDriverUsername(), trip.getDepartureDate(), trip.getDepartureTime(),
                trip.getArrivalTime(), null);
        checkBusConflict(trip.getAssignedLicensePlate(), trip.getDepartureDate(), trip.getDepartureTime(),
                trip.getArrivalTime(), null);
        if (trip.getInspector() != null) {
            checkInspectorConflict(trip.getInspector().getId(), trip.getDepartureDate(), trip.getDepartureTime(),
                    trip.getArrivalTime(), null);
        }

        if (trip.getTotalSeats() == null || trip.getTotalSeats() == 24) {
            trip.setTotalSeats(trip.getAvailableSeats());
        }
        Trip savedTrip = tripRepository.save(trip);
        seatService.createDefaultSeatsForTrip(savedTrip); // Tự sinh sơ đồ ghế tự động cực thông minh

        return savedTrip;
    }

    @Transactional
    public Trip updateTrip(Long id, Trip updatedDetails) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe với mã ID: " + id));

        // 🛡️ KIỂM TRA XUNG ĐỘT TRƯỚC KHI CẬP NHẬT (bỏ qua chính chuyến xe hiện tại)
        checkDriverConflict(updatedDetails.getAssignedDriverUsername(), updatedDetails.getDepartureDate(),
                updatedDetails.getDepartureTime(), updatedDetails.getArrivalTime(), id);
        checkBusConflict(updatedDetails.getAssignedLicensePlate(), updatedDetails.getDepartureDate(),
                updatedDetails.getDepartureTime(), updatedDetails.getArrivalTime(), id);
        if (updatedDetails.getInspector() != null) {
            checkInspectorConflict(updatedDetails.getInspector().getId(), updatedDetails.getDepartureDate(),
                    updatedDetails.getDepartureTime(), updatedDetails.getArrivalTime(), id);
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

        if (requestedTotalSeats != null && !requestedTotalSeats.equals(trip.getTotalSeats())) {
            // 🛡️ BACKEND SECURITY CHECK: Ngăn chặn hack qua API khi đã có khách đặt vé
            if (trip.getAvailableSeats() != null && trip.getAvailableSeats() < trip.getTotalSeats()) {
                throw new RuntimeException(
                        "LỖI BẢO MẬT: Không thể thay đổi loại xe hoặc số ghế vì chuyến xe này đã có khách hàng đặt vé!");
            }
            seatCountChanged = true;
        }

        trip.setCompanyName(updatedDetails.getCompanyName());
        trip.setBusType(updatedDetails.getBusType());
        trip.setAssignedLicensePlate(updatedDetails.getAssignedLicensePlate());
        trip.setAssignedDriverUsername(updatedDetails.getAssignedDriverUsername());
        trip.setAssignedDriverFullName(updatedDetails.getAssignedDriverFullName());
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

        // 🚀 NẾU THAY ĐỔI SỐ GHẾ: Tự động đập đi xây lại sơ đồ ghế cực mượt!
        if (seatCountChanged) {
            seatService.deleteAllSeatsByTripId(id); // Xóa sạch ghế thừa cũ
            seatService.createDefaultSeatsForTrip(savedTrip); // Vẽ lại đúng số ghế mới
        }

        return savedTrip;
    }

    @Transactional
    public void deleteTrip(Long id) {
        Trip trip = tripRepository.findById(id).orElse(null);

        // Nhờ có CascadeType.ALL cấu hình trong Trip.java, lệnh xóa này sẽ tự xóa sạch
        // 24 ghế của nó!
        tripRepository.deleteById(id);
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
        trip.setStatus(status);

        return tripRepository.save(trip);
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
}
