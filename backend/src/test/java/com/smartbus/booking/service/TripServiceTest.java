package com.smartbus.booking.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.entity.Inspector;
import com.smartbus.booking.entity.Bus;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.BusRepository;
import com.smartbus.booking.repository.BusTypeRepository;
import com.smartbus.booking.repository.BookingRepository;
import com.smartbus.booking.repository.InspectorRepository;
import com.smartbus.booking.repository.LeaveRequestRepository;
import com.smartbus.booking.repository.TripRepository;
import com.smartbus.booking.repository.VehicleMileageLogRepository;
import com.smartbus.booking.repository.UserRepository;
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
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
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
        when(tripRepository.findTripsForDriverSchedule(eq("0987654321"), eq(100L)))
            .thenReturn(List.of(overnightTrip));

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip(100L, update));
    }

    @Test
    void completedTripIsReadOnly() {
        Trip completed = Trip.builder()
            .id(100L)
            .status("COMPLETED")
            .departureDate("2026-09-02")
            .price(250000.0)
            .build();
        Trip requestedUpdate = Trip.builder()
            .departureDate("2026-09-10")
            .price(100000.0)
            .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(completed));

        assertThrows(IllegalStateException.class,
            () -> tripService.updateTrip(100L, requestedUpdate));
        assertEquals("2026-09-02", completed.getDepartureDate());
        assertEquals(250000.0, completed.getPrice());
    }

    @Test
    void updateTripRejectsDriverWhoIsStillAtPreviousDestination() {
        Trip storedTrip = daNangToQuangNamTrip();
        Trip previousTrip = daNangToHoChiMinhTrip();
        previousTrip.setAssignedDriverUsername("0900000001");
        previousTrip.setAssignedDriverFullName("Tài xế A");

        Trip update = daNangToQuangNamTrip();
        update.setAssignedDriverUsername("0900000001");
        update.setAssignedDriverFullName("Tài xế A");

        when(tripRepository.findById(100L)).thenReturn(Optional.of(storedTrip));
        when(leaveRequestRepository.findApprovedLeaveOnDate("0900000001", "2026-10-06"))
            .thenReturn(List.of());
        when(tripRepository.findTripsForDriverSchedule(eq("0900000001"), eq(100L)))
            .thenReturn(List.of(previousTrip));

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip(100L, update));
    }

    @Test
    void updateTripIgnoresLegacyStationWhenDriverHasNoOperationalTrips() {
        Trip storedTrip = daNangToQuangNamTrip();
        Trip update = daNangToQuangNamTrip();
        update.setAssignedDriverUsername("0900000001");
        update.setAssignedDriverFullName("Tài xế A");

        when(tripRepository.findById(100L)).thenReturn(Optional.of(storedTrip));
        when(leaveRequestRepository.findApprovedLeaveOnDate("0900000001", "2026-10-06"))
            .thenReturn(List.of());
        when(tripRepository.findTripsForDriverSchedule("0900000001", 100L)).thenReturn(List.of());

        assertDoesNotThrow(() -> tripService.updateTrip(100L, update));
    }

    @Test
    void updateTripRejectsBusThatIsStillAtPreviousDestination() {
        Trip storedTrip = daNangToQuangNamTrip();
        Trip previousTrip = daNangToHoChiMinhTrip();
        previousTrip.setAssignedLicensePlate("92B-12345");

        Trip update = daNangToQuangNamTrip();
        update.setAssignedLicensePlate("92B-12345");

        when(tripRepository.findById(100L)).thenReturn(Optional.of(storedTrip));
        when(busRepository.findByLicensePlateIgnoreCase("92B-12345")).thenReturn(Optional.empty());
        when(tripRepository.findTripsForBusSchedule(eq("92B-12345"), eq(100L)))
            .thenReturn(List.of(previousTrip));

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip(100L, update));
    }

    @Test
    void updateTripRejectsInspectorWhoIsStillAtPreviousDestination() {
        Inspector inspector = Inspector.builder().id(7L).build();
        Trip storedTrip = daNangToQuangNamTrip();
        Trip previousTrip = daNangToHoChiMinhTrip();
        previousTrip.setInspector(inspector);

        Trip update = daNangToQuangNamTrip();
        update.setInspector(inspector);

        when(tripRepository.findById(100L)).thenReturn(Optional.of(storedTrip));
        when(tripRepository.findTripsForInspectorSchedule(eq(7L), eq(100L)))
            .thenReturn(List.of(previousTrip));

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip(100L, update));
    }

    @Test
    void tripStatusMovesBusDriversAndInspectorBetweenStations() {
        Bus bus = Bus.builder().licensePlate("92B-12345").status("ĐANG NGHỈ").build();
        User primary = User.builder().phone("0900000001").fullName("Tài chính").password("x").build();
        User secondary = User.builder().phone("0900000002").fullName("Tài phụ").password("x").build();
        Inspector inspector = Inspector.builder().id(7L).phone("0900000003").fullName("Lơ xe").build();
        Trip trip = Trip.builder()
            .id(200L)
            .companyName("Trung Nam")
            .busType("Limousine")
            .assignedLicensePlate("92B-12345")
            .assignedDriverUsername("0900000001")
            .secondaryDriverUsername("0900000002")
            .inspector(inspector)
            .departurePoint("Bến xe trung tâm Đà Nẵng")
            .arrivalPoint("Bến Xe An Sương")
            .departureDate("2026-10-05")
            .departureTime("06:00")
            .arrivalTime("23:00")
            .availableSeats(24)
            .totalSeats(24)
            .status("ASSIGNED")
            .build();

        when(tripRepository.findById(200L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(busRepository.findByLicensePlateIgnoreCase("92B-12345")).thenReturn(Optional.of(bus));
        when(userRepository.findByPhone("0900000001")).thenReturn(Optional.of(primary));
        when(userRepository.findByPhone("0900000002")).thenReturn(Optional.of(secondary));
        when(inspectorRepository.findById(7L)).thenReturn(Optional.of(inspector));

        tripService.updateTripStatus(200L, "IN_PROGRESS");
        assertEquals("Bến xe trung tâm Đà Nẵng", bus.getCurrentStation());
        assertEquals("Bến xe trung tâm Đà Nẵng", primary.getCurrentStation());
        assertEquals("Bến xe trung tâm Đà Nẵng", secondary.getCurrentStation());
        assertEquals("Bến xe trung tâm Đà Nẵng", inspector.getCurrentStation());
        assertEquals("DRIVING", primary.getDriverStatus());

        tripService.updateTripStatus(200L, "COMPLETED");
        assertEquals("Bến Xe An Sương", bus.getCurrentStation());
        assertEquals("Bến Xe An Sương", primary.getCurrentStation());
        assertEquals("Bến Xe An Sương", secondary.getCurrentStation());
        assertEquals("Bến Xe An Sương", inspector.getCurrentStation());
        assertEquals("FREE", primary.getDriverStatus());
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

    private Trip daNangToHoChiMinhTrip() {
        return Trip.builder()
            .id(99L)
            .companyName("Trung Nam")
            .busType("")
            .departurePoint("Bến xe trung tâm Đà Nẵng")
            .arrivalPoint("Bến Xe An Sương")
            .departureDate("2026-10-05")
            .departureTime("06:00")
            .arrivalTime("23:00")
            .availableSeats(24)
            .totalSeats(24)
            .status("ASSIGNED")
            .build();
    }

    private Trip daNangToQuangNamTrip() {
        return Trip.builder()
            .id(100L)
            .companyName("Trung Nam")
            .busType("")
            .departurePoint("Bến xe trung tâm Đà Nẵng")
            .arrivalPoint("Bến xe Quảng Nam")
            .departureDate("2026-10-06")
            .departureTime("14:00")
            .arrivalTime("16:00")
            .availableSeats(24)
            .totalSeats(24)
            .status("ASSIGNED")
            .build();
    }
}
