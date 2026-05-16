package com.smartbus.booking.service;

import com.smartbus.booking.entity.Seat;
import com.smartbus.booking.repository.BusRepository;
import com.smartbus.booking.repository.SeatRepository;
import com.smartbus.booking.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;
    private final BusRepository busRepository;

    public Map<String, Object> getLiveStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Lấy tất cả ghế đã bán để tính toán thật 100%
        List<Seat> allSeats = seatRepository.findAll();
        
        long totalTicketsSold = allSeats.stream()
                .filter(Seat::getIsBooked)
                .count();

        // 2. Doanh thu thật = Tổng (Đơn giá chuyến xe của ghế đã bán)
        double realRevenue = allSeats.stream()
                .filter(Seat::getIsBooked)
                .mapToDouble(s -> s.getTrip() != null ? s.getTrip().getPrice() : 0.0)
                .sum();

        // 3. Thống kê Chuyến xe
        long totalTrips = tripRepository.count();

        // 4. Thống kê Hạm đội xe thực tế
        long totalBuses = busRepository.count();
        long activeBuses = busRepository.findAll().stream()
                .filter(b -> "ĐANG CHẠY".equalsIgnoreCase(b.getStatus()))
                .count();

        // Gói dữ liệu vàng chuyển cho Frontend
        stats.put("totalRevenue", realRevenue);
        stats.put("totalTickets", totalTicketsSold);
        stats.put("totalTrips", totalTrips);
        stats.put("totalBuses", totalBuses);
        stats.put("activeBuses", activeBuses);
        stats.put("lastUpdated", java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));

        return stats;
    }
}
