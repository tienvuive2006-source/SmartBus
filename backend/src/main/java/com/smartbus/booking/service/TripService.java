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

    public List<Trip> searchTrips(String from, String to, String date) {
        if ((from == null || from.isEmpty()) && (to == null || to.isEmpty()) && (date == null || date.isEmpty())) {
            return getAllTrips();
        }
        return tripRepository.findByDeparturePointContainingIgnoreCaseAndArrivalPointContainingIgnoreCaseAndDepartureDateContaining(
                from != null ? from : "", 
                to != null ? to : "",
                date != null ? date : ""
        );
    }

    // Khi lưu chuyến xe mới, tự động sinh ra 24 ghế tương ứng!
    @Transactional
    public Trip saveTrip(Trip trip) {
        Trip savedTrip = tripRepository.save(trip);
        seatService.createDefaultSeatsForTrip(savedTrip); // Tự sinh sơ đồ ghế tự động cực thông minh
        return savedTrip;
    }

    @Transactional
    public Trip updateTrip(Long id, Trip updatedDetails) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe với mã ID: " + id));
        
        // 🛡️ CẢM BIẾN PHÁT HIỆN ĐỔI SỐ GHẾ
        boolean seatCountChanged = !updatedDetails.getAvailableSeats().equals(trip.getAvailableSeats());
        
        trip.setCompanyName(updatedDetails.getCompanyName());
        trip.setBusType(updatedDetails.getBusType());
        trip.setDeparturePoint(updatedDetails.getDeparturePoint());
        trip.setArrivalPoint(updatedDetails.getArrivalPoint());
        trip.setDepartureTime(updatedDetails.getDepartureTime());
        trip.setDepartureDate(updatedDetails.getDepartureDate()); // 📅 CHÈN CHỮ CHỮ KÝ SINH MỆNH: Đồng bộ ngày vào Database!
        trip.setArrivalTime(updatedDetails.getArrivalTime());
        trip.setDuration(updatedDetails.getDuration());
        trip.setPrice(updatedDetails.getPrice());
        trip.setRating(updatedDetails.getRating());
        trip.setAvailableSeats(updatedDetails.getAvailableSeats());
        trip.setImageUrl(updatedDetails.getImageUrl()); // 🖼️ CHÈN CHỮ KÝ SINH MỆNH: Lưu hình ảnh vào Database!
        trip.setInstantConfirmation(updatedDetails.getInstantConfirmation());
        trip.setDepartureLat(updatedDetails.getDepartureLat());
        trip.setDepartureLng(updatedDetails.getDepartureLng());
        trip.setArrivalLat(updatedDetails.getArrivalLat());
        trip.setArrivalLng(updatedDetails.getArrivalLng());

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
}
