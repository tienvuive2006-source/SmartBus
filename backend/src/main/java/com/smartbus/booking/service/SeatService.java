package com.smartbus.booking.service;

import com.smartbus.booking.entity.Seat;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.repository.SeatRepository;
import com.smartbus.booking.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;

    // 🛡️ CONSTRUCTOR THUẦN TÚY (Pure Vanilla Java): Hoàn hảo 100% không bao giờ sợ lỗi IDE!
    public SeatService(SeatRepository seatRepository, TripRepository tripRepository) {
        this.seatRepository = seatRepository;
        this.tripRepository = tripRepository;
    }

    public List<Seat> getSeatsByTripId(Long tripId) {
        return seatRepository.findByTripIdOrderBySeatNumberAsc(tripId);
    }

    // Xóa toàn bộ ghế cũ khi Admin sửa đổi sơ đồ cấu hình
    @Transactional
    public void deleteAllSeatsByTripId(Long tripId) {
        List<Seat> existingSeats = seatRepository.findByTripIdOrderBySeatNumberAsc(tripId);
        if (!existingSeats.isEmpty()) {
            seatRepository.deleteAll(existingSeats);
        }
    }

    // Sinh sơ đồ ghế động dựa trên tổng số ghế của chuyến xe
    public void createDefaultSeatsForTrip(Trip trip) {
        List<Seat> defaultSeats = new ArrayList<>();

        int totalSeats = trip.getAvailableSeats() != null ? trip.getAvailableSeats() : 24;

        int floor1Count = (int) Math.ceil(totalSeats / 2.0);
        int floor2Count = totalSeats - floor1Count;

        for (int i = 1; i <= floor1Count; i++) {
            defaultSeats.add(Seat.builder()
                    .seatNumber("A" + String.format("%02d", i))
                    .seatFloor(1)
                    .isBooked(false)
                    .trip(trip)
                    .build());
        }

        for (int i = 1; i <= floor2Count; i++) {
            defaultSeats.add(Seat.builder()
                    .seatNumber("B" + String.format("%02d", i))
                    .seatFloor(2)
                    .isBooked(false)
                    .trip(trip)
                    .build());
        }

        seatRepository.saveAll(defaultSeats);
    }

    // 🚀 TRỌNG TÂM GIAO DỊCH CHỐT GHẾ VÀ ĐỒNG BỘ SỐ DƯ GHẾ TRỐNG
    @Transactional
    public void bookSeats(Long tripId, List<String> seatNumbers) {
        // 1. Tải toàn bộ sơ đồ ghế hiện tại của chuyến xe
        List<Seat> seats = seatRepository.findByTripIdOrderBySeatNumberAsc(tripId);
        
        int remainingEmptySeatsCount = 0;
        
        // 2. Duyệt qua sơ đồ để đánh dấu Đã bán và đồng thời ĐẾM DỰ PHÒNG số ghế thực tế
        for (Seat seat : seats) {
            if (seatNumbers.contains(seat.getSeatNumber())) {
                if (Boolean.TRUE.equals(seat.getIsBooked())) {
                    throw new RuntimeException("Ghế " + seat.getSeatNumber() + " đã có người đặt! Vui lòng chọn ghế khác.");
                }
                seat.setIsBooked(true); // Đổi trạng thái thật trong SQL
            }
            
            // Đếm ghế trống còn sót lại sau khi đánh dấu
            if (!seat.getIsBooked()) {
                remainingEmptySeatsCount++;
            }
        }
        
        // 3. Lưu sơ đồ ghế mới cập nhật
        seatRepository.saveAll(seats);
        
        // 4. 📡 PHÉP THUẬT ĐỒNG BỘ: Đồng bộ ngay số ghế trống ĐÍCH THỰC lên Bảng Chuyến Xe!
        int finalCount = remainingEmptySeatsCount;
        tripRepository.findById(tripId).ifPresent(trip -> {
            trip.setAvailableSeats(finalCount);
            tripRepository.save(trip); // Ghi đè trực tiếp vào SQL Server!
        });
    }

    // 🔄 HỦY GHẾ VÀ HOÀN LẠI SỐ DƯ GHẾ TRỐNG
    @Transactional
    public void releaseSeats(Long tripId, List<String> seatNumbers) {
        List<Seat> seats = seatRepository.findByTripIdOrderBySeatNumberAsc(tripId);
        int remainingEmptySeatsCount = 0;
        
        for (Seat seat : seats) {
            if (seatNumbers.contains(seat.getSeatNumber())) {
                seat.setIsBooked(false); // Đổi trạng thái thành trống
            }
            
            if (!seat.getIsBooked()) {
                remainingEmptySeatsCount++;
            }
        }
        
        seatRepository.saveAll(seats);
        
        int finalCount = remainingEmptySeatsCount;
        tripRepository.findById(tripId).ifPresent(trip -> {
            trip.setAvailableSeats(finalCount);
            tripRepository.save(trip);
        });
    }
}
