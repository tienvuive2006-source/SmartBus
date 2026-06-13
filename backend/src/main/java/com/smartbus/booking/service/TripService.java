package com.smartbus.booking.service;

import com.smartbus.booking.entity.Trip;
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

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public static String stripAccents(String s) {
        if (s == null) return "";
        String normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "D")
                .toLowerCase()
                .trim();
    }

    public List<Trip> searchTrips(String from, String to, String date) {
        if ((from == null || from.isEmpty()) && (to == null || to.isEmpty()) && (date == null || date.isEmpty())) {
            return getAllTrips();
        }
        
        List<Trip> tripsByDate = tripRepository.findByDepartureDateContaining(date != null ? date : "");
        if ((from == null || from.trim().isEmpty()) && (to == null || to.trim().isEmpty())) {
            return tripsByDate;
        }

        String normalizedFrom = stripAccents(from);
        String normalizedTo = stripAccents(to);

        return tripsByDate.stream().filter(t -> {
            String tFrom = stripAccents(t.getDeparturePoint());
            String tTo = stripAccents(t.getArrivalPoint());
            
            boolean matchFrom = normalizedFrom.isEmpty() || tFrom.contains(normalizedFrom) || normalizedFrom.contains(tFrom);
            boolean matchTo = normalizedTo.isEmpty() || tTo.contains(normalizedTo) || normalizedTo.contains(tTo);
            
            return matchFrom && matchTo;
        }).collect(java.util.stream.Collectors.toList());
    }

    // Khi lưu chuyến xe mới, tự động sinh ra 24 ghế tương ứng!
    @Transactional
    public Trip saveTrip(Trip trip) {
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
        
        // 🛡️ FIX LỖI "OVERWRITING SEATS": Frontend gửi "Số ghế" (Total Seats) thông qua field availableSeats.
        Integer requestedTotalSeats = updatedDetails.getAvailableSeats();
        boolean seatCountChanged = false;
        
        if (requestedTotalSeats != null && !requestedTotalSeats.equals(trip.getTotalSeats())) {
            seatCountChanged = true;
        }
        
        trip.setCompanyName(updatedDetails.getCompanyName());
        trip.setBusType(updatedDetails.getBusType());
        trip.setAssignedLicensePlate(updatedDetails.getAssignedLicensePlate());
        trip.setDeparturePoint(updatedDetails.getDeparturePoint());
        trip.setArrivalPoint(updatedDetails.getArrivalPoint());
        trip.setDepartureTime(updatedDetails.getDepartureTime());
        trip.setDepartureDate(updatedDetails.getDepartureDate()); // 📅 CHÈN CHỮ CHỮ KÝ SINH MỆNH: Đồng bộ ngày vào Database!
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

        if (updatedDetails.getIsVisible() != null) {
            trip.setIsVisible(updatedDetails.getIsVisible());
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
        // Nhờ có CascadeType.ALL cấu hình trong Trip.java, lệnh xóa này sẽ tự xóa sạch 24 ghế của nó!
        tripRepository.deleteById(id);
    }

    private final com.smartbus.booking.repository.InspectorRepository inspectorRepository;

    public List<Trip> getTripsByInspectorId(Long userId) {
        java.util.Optional<com.smartbus.booking.entity.Inspector> inspector = inspectorRepository.findByUserAccountId(userId);
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
    public Trip toggleVisibility(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe với mã ID: " + id));
        trip.setIsVisible(trip.getIsVisible() == null ? false : !trip.getIsVisible());
        return tripRepository.save(trip);
    }
}
