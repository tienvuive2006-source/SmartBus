package com.smartbus.booking.config;

import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TripRepository tripRepository;

    @Override
    public void run(String... args) throws Exception {
        // Chỉ nạp dữ liệu nếu Database chưa có chuyến xe nào
        if (tripRepository.count() == 0) {
            System.out.println("🌱 Khởi tạo dữ liệu chuyến xe mẫu vào database...");

            Trip trip1 = Trip.builder()
                    .companyName("Sao Việt Express")
                    .busType("Limousine 21 chỗ VIP")
                    .departurePoint("Hà Nội (Mỹ Đình)")
                    .arrivalPoint("Sapa")
                    .departureTime("06:00")
                    .arrivalTime("11:30")
                    .duration("5h 30m")
                    .price(350000.0)
                    .rating(4.8)
                    .availableSeats(8)
                    .instantConfirmation(true)
                    .build();

            Trip trip2 = Trip.builder()
                    .companyName("Hà Sơn Hải Vân")
                    .busType("Giường nằm 34 chỗ")
                    .departurePoint("Hà Nội (Yên Nghĩa)")
                    .arrivalPoint("Sapa")
                    .departureTime("07:30")
                    .arrivalTime("13:15")
                    .duration("5h 45m")
                    .price(280000.0)
                    .rating(4.5)
                    .availableSeats(15)
                    .instantConfirmation(false)
                    .build();

            Trip trip3 = Trip.builder()
                    .companyName("Hoàng Long Limousine")
                    .busType("Xe VIP 9 Chỗ")
                    .departurePoint("Hải Phòng")
                    .arrivalPoint("Hà Nội")
                    .departureTime("08:30")
                    .arrivalTime("10:30")
                    .duration("2h 00m")
                    .price(250000.0)
                    .rating(4.9)
                    .availableSeats(4)
                    .instantConfirmation(true)
                    .build();

            Trip trip4 = Trip.builder()
                    .companyName("Inter Bus Lines")
                    .busType("Cabin Đôi Tình Yêu")
                    .departurePoint("Hà Nội")
                    .arrivalPoint("Sapa")
                    .departureTime("22:00")
                    .arrivalTime("04:30")
                    .duration("6h 30m")
                    .price(450000.0)
                    .rating(4.7)
                    .availableSeats(6)
                    .instantConfirmation(true)
                    .build();

            tripRepository.saveAll(Arrays.asList(trip1, trip2, trip3, trip4));
            System.out.println("✅ Đã nạp thành công " + tripRepository.count() + " chuyến xe vào SQL Server!");
        } else {
            System.out.println("✅ Database đã có sẵn " + tripRepository.count() + " chuyến xe, bỏ qua tự động nạp.");
        }
    }
}
