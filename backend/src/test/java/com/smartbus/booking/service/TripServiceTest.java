package com.smartbus.booking.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.repository.BusRepository;
import com.smartbus.booking.repository.BusTypeRepository;
import com.smartbus.booking.repository.InspectorRepository;
import com.smartbus.booking.repository.LeaveRequestRepository;
import com.smartbus.booking.repository.TripRepository;
import com.smartbus.booking.repository.VehicleMileageLogRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock private TripRepository tripRepository;
    @Mock private SeatService seatService;
    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private BusTypeRepository busTypeRepository;
    @Mock private BusRepository busRepository;
    @Mock private VehicleMileageService vehicleMileageService;
    @Mock private VehicleMileageLogRepository vehicleMileageLogRepository;
    @Mock private InspectorRepository inspectorRepository;
    @InjectMocks private TripService tripService;

    @Test
    void updateTripRejectsDriverConflictFromOvernightTrip() {
        Trip storedTrip = Trip.builder()
            .id(100L)
            .companyName("Trung Nam")
            .busType("")
            .departurePoint("Bến xe Quảng Nam")
            .arrivalPoint("Bến xe Đà Nẵng")
            .departureDate("2026-09-01")
            .departureTime("05:00")
            .arrivalTime("07:00")
            .availableSeats(24)
            .totalSeats(24)
            .build();

        Trip overnightTrip = Trip.builder()
            .id(99L)
            .companyName("Trung Nam")
            .busType("Trung Nam VIP")
            .assignedDriverUsername("0987654321")
            .assignedDriverFullName("Tài xế A")
            .departurePoint("Bến xe Đà Nẵng")
            .arrivalPoint("Bến xe Quảng Nam")
            .departureDate("2026-08-31")
            .departureTime("22:00")
            .arrivalTime("06:00")
            .availableSeats(24)
            .totalSeats(24)
            .status("ASSIGNED")
            .build();

        Trip update = Trip.builder()
            .id(100L)
            .companyName("Trung Nam")
            .busType("")
            .assignedDriverUsername("0987654321")
            .assignedDriverFullName("Tài xế A")
            .departurePoint("Bến xe Quảng Nam")
            .arrivalPoint("Bến xe Đà Nẵng")
            .departureDate("2026-09-01")
            .departureTime("05:00")
            .arrivalTime("07:00")
            .availableSeats(24)
            .totalSeats(24)
            .status("ASSIGNED")
            .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(storedTrip));
        when(leaveRequestRepository.findApprovedLeaveOnDate("0987654321", "2026-09-01"))
            .thenReturn(List.of());
        when(tripRepository.findTripsForDriverInDateRange(
            eq("0987654321"), eq("2026-08-31"), eq("2026-09-02"), eq(100L)))
            .thenReturn(List.of(overnightTrip));

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip(100L, update));
    }

    @Test
    void deleteTripDetachesMileageHistoryBeforeDeletingTrip() {
        Trip trip = Trip.builder().id(6L).build();
        when(tripRepository.findById(6L)).thenReturn(Optional.of(trip));

        tripService.deleteTrip(6L);

        InOrder order = inOrder(vehicleMileageLogRepository, tripRepository);
        order.verify(vehicleMileageLogRepository).detachTrip(6L);
        order.verify(vehicleMileageLogRepository).flush();
        order.verify(tripRepository).delete(trip);
        order.verify(tripRepository).flush();
    }
}
