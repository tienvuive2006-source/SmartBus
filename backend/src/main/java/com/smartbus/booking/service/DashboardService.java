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

    private final com.smartbus.booking.repository.BookingRepository bookingRepository;

    public Map<String, Object> getLiveStats() {
        Map<String, Object> stats = new HashMap<>();

        // Lấy tất cả bookings đã thanh toán hoặc đã lên xe
        List<com.smartbus.booking.entity.Booking> paidBookings = bookingRepository.findAll().stream()
                .filter(b -> "PAID".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()))
                .toList();

        // 1. Tính tổng số vé đã bán thành công
        long totalTicketsSold = paidBookings.stream()
                .mapToLong(b -> b.getSeatNumbers() != null ? b.getSeatNumbers().size() : 0)
                .sum();

        // 2. Tính doanh thu thực tế dựa trên tổng tiền của các booking đã thanh toán
        double realRevenue = paidBookings.stream()
                .mapToDouble(com.smartbus.booking.entity.Booking::getTotalPrice)
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
