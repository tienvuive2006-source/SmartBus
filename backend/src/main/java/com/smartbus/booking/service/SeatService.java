package com.smartbus.booking.service;

import com.smartbus.booking.entity.Seat;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.entity.SeatReservation;
import com.smartbus.booking.entity.SeatType;
import com.smartbus.booking.repository.SeatRepository;
import com.smartbus.booking.repository.SeatReservationRepository;
import com.smartbus.booking.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final BusTypeSeatLayoutService seatLayoutService;
    private final SeatRealtimePublisher realtimePublisher;

    // 🛡️ CONSTRUCTOR THUẦN TÚY (Pure Vanilla Java): Hoàn hảo 100% không bao giờ sợ lỗi IDE!
    public SeatService(SeatRepository seatRepository, TripRepository tripRepository,
                       SeatReservationRepository seatReservationRepository,
                       BusTypeSeatLayoutService seatLayoutService,
                       SeatRealtimePublisher realtimePublisher) {
        this.seatRepository = seatRepository;
        this.tripRepository = tripRepository;
        this.seatReservationRepository = seatReservationRepository;
        this.seatLayoutService = seatLayoutService;
        this.realtimePublisher = realtimePublisher;
    }

    public List<Seat> getSeatsByTripId(Long tripId) {
        List<Seat> seats = seatRepository.findByTripIdOrderBySeatNumberAsc(tripId);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        // Cập nhật trạng thái giả cho ghế đang bị tạm giữ (hold)
        List<String> heldSeats = seatReservationRepository.findByTripId(tripId).stream()
                .filter(h -> h.getExpiredAt().isAfter(now))
                .map(SeatReservation::getSeatNumber)
                .toList();
                
        seats.forEach(s -> {
            if (!s.getIsBooked() && heldSeats.contains(s.getSeatNumber())) {
                s.setIsBooked(true); // Đánh dấu là đã đặt để Frontend mờ đi
            }
        });
        
        return seats;
    }

    // TẠM GIỮ GHẾ 10 PHÚT TRONG LÚC THANH TOÁN
    @Transactional
    public java.time.LocalDateTime holdSeats(Long tripId, List<String> seatNumbers) {
        return holdSeats(tripId, seatNumbers, "PAYMENT-" + UUID.randomUUID());
    }

    @Transactional
    public java.time.LocalDateTime holdSeats(Long tripId, List<String> seatNumbers, String holdToken) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        seatReservationRepository.deleteExpiredReservations(now); // Dọn rác
        
        List<Seat> seats = seatRepository.findByTripIdOrderBySeatNumberAsc(tripId);
        Set<String> validSeatNumbers = seats.stream().map(Seat::getSeatNumber)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> existingSeatNumbers = new HashSet<>();
        for (String requested : seatNumbers) {
            if (!validSeatNumbers.contains(requested)) {
                throw new RuntimeException("Ghế " + requested + " không tồn tại trong chuyến xe.");
            }
        }

        List<SeatReservation> activeHolds = seatReservationRepository.findByTripId(tripId).stream()
                .filter(h -> h.getExpiredAt().isAfter(now)).toList();
        for (SeatReservation reservation : activeHolds) {
            if (!seatNumbers.contains(reservation.getSeatNumber())) continue;
            if (!holdToken.equals(reservation.getHoldToken())) {
                throw new RuntimeException("Ghế " + reservation.getSeatNumber() + " đang được hành khách khác giữ!");
            }
            existingSeatNumbers.add(reservation.getSeatNumber());
        }
                
        for (Seat s : seats) {
            if (seatNumbers.contains(s.getSeatNumber())) {
                if (s.getIsBooked()) {
                    throw new RuntimeException("Ghế " + s.getSeatNumber() + " đã có người đặt hoặc đang được giữ thanh toán!");
                }
            }
        }
        
        java.time.LocalDateTime expiredAt = now.plusMinutes(10);
        for (String seatNum : seatNumbers) {
            if (existingSeatNumbers.contains(seatNum)) {
                SeatReservation owned = activeHolds.stream()
                        .filter(item -> item.getSeatNumber().equals(seatNum)).findFirst().orElseThrow();
                owned.setExpiredAt(expiredAt);
            } else {
                seatReservationRepository.save(SeatReservation.builder()
                        .tripId(tripId).seatNumber(seatNum).holdToken(holdToken)
                        .createdAt(now).expiredAt(expiredAt).build());
            }
            realtimePublisher.publish(tripId, seatNum, "HELD", expiredAt);
        }

        return expiredAt;
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
        Map<String, SeatType> configuredTypes = seatLayoutService.getSeatTypesForBusType(trip.getBusType());

        int totalSeats = trip.getAvailableSeats() != null ? trip.getAvailableSeats() : 24;

        int floor1Count = (int) Math.ceil(totalSeats / 2.0);
        int floor2Count = totalSeats - floor1Count;

        for (int i = 1; i <= floor1Count; i++) {
            String seatNumber = "A" + String.format("%02d", i);
            defaultSeats.add(Seat.builder()
                    .seatNumber(seatNumber)
                    .seatFloor(1)
                    .isBooked(false)
                    .seatType(configuredTypes.getOrDefault(seatNumber, SeatType.STANDARD))
                    .trip(trip)
                    .build());
        }

        for (int i = 1; i <= floor2Count; i++) {
            String seatNumber = "B" + String.format("%02d", i);
            defaultSeats.add(Seat.builder()
                    .seatNumber(seatNumber)
                    .seatFloor(2)
                    .isBooked(false)
                    .seatType(configuredTypes.getOrDefault(seatNumber, SeatType.STANDARD))
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
        
        // 5. XÓA HOLD TẠM THỜI (Vì đã chốt đơn)
        seatReservationRepository.deleteByTripIdAndSeatNumbers(tripId, seatNumbers);
        seatNumbers.forEach(seatNumber -> realtimePublisher.publish(tripId, seatNumber, "BOOKED", null));
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
        seatNumbers.forEach(seatNumber -> realtimePublisher.publish(tripId, seatNumber, "AVAILABLE", null));
    }
}
