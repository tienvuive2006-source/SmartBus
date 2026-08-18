package com.smartbus.booking.service;

import com.smartbus.booking.dto.TicketExchangeRequest;
import com.smartbus.booking.entity.Booking;
import com.smartbus.booking.entity.Seat;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.BookingExchangeRepository;
import com.smartbus.booking.repository.BookingRepository;
import com.smartbus.booking.repository.TicketExchangeSeatRepository;
import com.smartbus.booking.repository.TicketExchangeReservationRepository;
import com.smartbus.booking.repository.TicketExchangeTripRepository;
import com.smartbus.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketExchangeServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingExchangeRepository exchangeRepository;
    @Mock private TicketExchangeSeatRepository seatRepository;
    @Mock private TicketExchangeReservationRepository reservationRepository;
    @Mock private TicketExchangeTripRepository tripRepository;
    @Mock private UserRepository userRepository;
    @Mock private FundService fundService;
    @Mock private TicketExchangeEmailService ticketExchangeEmailService;
    @Mock private TicketExchangePaymentVerifier paymentVerifier;

    private TicketExchangeService service;
    private User user;
    private Trip currentTrip;
    private Booking booking;

    @BeforeEach
    void setUp() {
        service = new TicketExchangeService(
                bookingRepository,
                exchangeRepository,
                seatRepository,
                reservationRepository,
                tripRepository,
                userRepository,
                fundService,
                ticketExchangeEmailService,
                paymentVerifier
        );

        user = User.builder()
                .id(7L)
                .fullName("Khách hàng")
                .walletBalance(500_000.0)
                .loyaltyPoints(300)
                .build();

        currentTrip = Trip.builder()
                .id(10L)
                .departurePoint("Đà Nẵng")
                .arrivalPoint("Đà Lạt")
                .departureDate("2099-08-20")
                .departureTime("20:00")
                .price(300_000.0)
                .availableSeats(1)
                .isVisible(true)
                .status("PENDING")
                .build();

        booking = Booking.builder()
                .id(99L)
                .user(user)
                .trip(currentTrip)
                .seatNumbers(new ArrayList<>(List.of("A01")))
                .totalPrice(300_000.0)
                .discountAmount(0.0)
                .status("PAID")
                .build();

        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));
    }

    @Test
    void exchangeSeatUpdatesSeatAndKeepsPrice() {
        currentTrip.setPrice(350_000.0);
        Seat oldSeat = seat(1L, "A01", true);
        Seat newSeat = seat(2L, "A02", false);
        when(exchangeRepository.existsByBookingIdAndStatus(99L, "COMPLETED")).thenReturn(false);
        when(tripRepository.findById(10L)).thenReturn(Optional.of(currentTrip));
        when(seatRepository.findTripSeatsForUpdate(10L)).thenReturn(List.of(oldSeat, newSeat));
        when(reservationRepository.findByTripIdAndExpiredAtAfter(eq(10L), any())).thenReturn(List.of());
        when(seatRepository.countByTripIdAndIsBookedFalse(10L)).thenReturn(1L);

        TicketExchangeRequest request = new TicketExchangeRequest();
        request.setExchangeType("SEAT");
        request.setNewTripId(10L);
        request.setNewSeatNumbers(List.of("A02"));
        request.setReason("Muốn đổi vị trí");

        Map<String, Object> result = service.exchange(99L, 7L, request);

        assertEquals(List.of("A02"), booking.getSeatNumbers());
        assertFalse(oldSeat.getIsBooked());
        assertTrue(newSeat.getIsBooked());
        assertEquals(300_000.0, booking.getTotalPrice());
        assertEquals(0.0, result.get("priceDifference"));
        verify(bookingRepository).save(booking);
        verify(exchangeRepository).save(any());
        verify(ticketExchangeEmailService).sendExchangeConfirmation(
                booking, 10L, "A01", 300_000.0, 300_000.0, 0.0);
        verifyNoInteractions(fundService);
    }

    @Test
    void rejectsBookingThatWasAlreadyExchanged() {
        when(exchangeRepository.existsByBookingIdAndStatus(99L, "COMPLETED")).thenReturn(true);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.getOptions(99L, 7L)
        );

        assertTrue(error.getMessage().contains("đã sử dụng một lần"));
        verifyNoInteractions(seatRepository, tripRepository);
    }

    @Test
    void rejectsTripOnDifferentRoute() {
        Trip invalidTrip = Trip.builder()
                .id(11L)
                .departurePoint("Đà Nẵng")
                .arrivalPoint("Huế")
                .departureDate("2099-08-22")
                .departureTime("20:00")
                .price(350_000.0)
                .isVisible(true)
                .status("PENDING")
                .build();
        when(exchangeRepository.existsByBookingIdAndStatus(99L, "COMPLETED")).thenReturn(false);
        when(tripRepository.findById(11L)).thenReturn(Optional.of(invalidTrip));

        TicketExchangeRequest request = new TicketExchangeRequest();
        request.setExchangeType("TRIP");
        request.setNewTripId(11L);
        request.setNewSeatNumbers(List.of("A01"));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.exchange(99L, 7L, request)
        );

        assertTrue(error.getMessage().contains("cùng tuyến đường"));
        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(seatRepository);
    }

    @Test
    void qrPaymentCreatesPendingSessionWithoutChangingBooking() {
        Trip newTrip = Trip.builder()
                .id(11L)
                .departurePoint("Đà Nẵng")
                .arrivalPoint("Đà Lạt")
                .departureDate("2099-08-22")
                .departureTime("20:00")
                .price(350_000.0)
                .availableSeats(1)
                .isVisible(true)
                .status("PENDING")
                .build();
        Seat newSeat = Seat.builder()
                .id(3L)
                .seatNumber("B03")
                .seatFloor(1)
                .isBooked(false)
                .trip(newTrip)
                .build();

        when(exchangeRepository.existsByBookingIdAndStatus(99L, "COMPLETED")).thenReturn(false);
        when(tripRepository.findById(11L)).thenReturn(Optional.of(newTrip));
        when(seatRepository.findTripSeatsForUpdate(11L)).thenReturn(List.of(newSeat));
        when(reservationRepository.findByTripIdAndExpiredAtAfter(eq(11L), any())).thenReturn(List.of());
        when(exchangeRepository.save(any())).thenAnswer(invocation -> {
            com.smartbus.booking.entity.BookingExchange exchange = invocation.getArgument(0, com.smartbus.booking.entity.BookingExchange.class);
            if (exchange.getId() == null) exchange.setId(55L);
            return exchange;
        });
        when(reservationRepository.save(any())).thenAnswer(invocation -> {
            com.smartbus.booking.entity.SeatReservation reservation = invocation.getArgument(0, com.smartbus.booking.entity.SeatReservation.class);
            reservation.setId(88L);
            return reservation;
        });

        TicketExchangeRequest request = new TicketExchangeRequest();
        request.setExchangeType("TRIP");
        request.setNewTripId(11L);
        request.setNewSeatNumbers(List.of("B03"));
        request.setReason("Đổi lịch");
        request.setPaymentMethod("QR");

        Map<String, Object> result = service.exchange(99L, 7L, request);

        assertEquals(true, result.get("paymentRequired"));
        assertEquals(55L, result.get("exchangeId"));
        assertEquals(List.of("A01"), booking.getSeatNumbers());
        assertEquals(10L, booking.getTrip().getId());
        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(ticketExchangeEmailService, fundService);
    }

    private Seat seat(Long id, String number, boolean booked) {
        return Seat.builder()
                .id(id)
                .seatNumber(number)
                .seatFloor(1)
                .isBooked(booked)
                .trip(currentTrip)
                .build();
    }
}
