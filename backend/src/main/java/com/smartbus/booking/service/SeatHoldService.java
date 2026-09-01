package com.smartbus.booking.service;

import com.smartbus.booking.dto.SeatHoldResponse;
import com.smartbus.booking.dto.SeatHoldSessionResponse;
import com.smartbus.booking.entity.Seat;
import com.smartbus.booking.entity.SeatReservation;
import com.smartbus.booking.repository.SeatRepository;
import com.smartbus.booking.repository.SeatReservationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class SeatHoldService {
    private static final int HOLD_MINUTES = 10;
    private final SeatRepository seatRepository;
    private final SeatReservationRepository reservationRepository;
    private final SeatRealtimePublisher realtimePublisher;

    public SeatHoldService(SeatRepository seatRepository, SeatReservationRepository reservationRepository,
                           SeatRealtimePublisher realtimePublisher) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public SeatHoldResponse hold(Long tripId, String seatNumber, String holdToken) {
        validateInput(tripId, seatNumber, holdToken);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_MINUTES);
        Seat seat = seatRepository.findByTripIdOrderBySeatNumberAsc(tripId).stream()
                .filter(item -> item.getSeatNumber().equalsIgnoreCase(seatNumber)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại trong chuyến xe."));
        if (Boolean.TRUE.equals(seat.getIsBooked())) {
            throw new SeatUnavailableException("Ghế " + seatNumber + " đã được bán.");
        }

        SeatReservation existing = reservationRepository.findByTripIdAndSeatNumber(tripId, seat.getSeatNumber())
                .orElse(null);
        if (existing != null && !existing.getExpiredAt().isAfter(now)) {
            reservationRepository.delete(existing);
            reservationRepository.flush();
            existing = null;
        }
        if (existing != null && !holdToken.equals(existing.getHoldToken())) {
            throw new SeatUnavailableException("Ghế " + seatNumber + " đang được hành khách khác giữ.");
        }

        try {
            if (existing == null) {
                existing = SeatReservation.builder().tripId(tripId).seatNumber(seat.getSeatNumber())
                        .holdToken(holdToken).createdAt(now).expiredAt(expiresAt).build();
            } else {
                existing.setExpiredAt(expiresAt);
            }
            reservationRepository.saveAndFlush(existing);
            for (SeatReservation owned : reservationRepository.findByHoldToken(holdToken)) {
                owned.setExpiredAt(expiresAt);
            }
        } catch (DataIntegrityViolationException conflict) {
            throw new SeatUnavailableException("Ghế " + seatNumber + " vừa được hành khách khác chọn.");
        }

        realtimePublisher.publish(tripId, seat.getSeatNumber(), "HELD", expiresAt);
        return new SeatHoldResponse(tripId, seat.getSeatNumber(), "HELD",
                expiresAt.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Transactional
    public void release(Long tripId, String seatNumber, String holdToken) {
        validateInput(tripId, seatNumber, holdToken);
        reservationRepository.findByTripIdAndSeatNumber(tripId, seatNumber)
                .filter(reservation -> holdToken.equals(reservation.getHoldToken()))
                .ifPresent(reservation -> {
                    reservationRepository.delete(reservation);
                    reservationRepository.flush();
                    realtimePublisher.publish(tripId, seatNumber, "AVAILABLE", null);
                });
    }

    @Transactional
    public void releaseSession(String holdToken) {
        validateToken(holdToken);
        List<SeatReservation> owned = reservationRepository.findByHoldToken(holdToken);
        reservationRepository.deleteAll(owned);
        reservationRepository.flush();
        owned.forEach(item -> realtimePublisher.publish(item.getTripId(), item.getSeatNumber(), "AVAILABLE", null));
    }

    @Transactional(readOnly = true)
    public SeatHoldSessionResponse getSession(String holdToken) {
        validateToken(holdToken);
        LocalDateTime now = LocalDateTime.now();
        List<SeatReservation> active = reservationRepository.findByHoldToken(holdToken).stream()
                .filter(item -> item.getExpiredAt().isAfter(now)).toList();
        LocalDateTime earliest = active.stream().map(SeatReservation::getExpiredAt)
                .min(LocalDateTime::compareTo).orElse(null);
        return new SeatHoldSessionResponse(
                active.stream().map(item -> new SeatHoldSessionResponse.HeldSeat(item.getTripId(), item.getSeatNumber())).toList(),
                earliest == null ? null : earliest.atZone(ZoneId.systemDefault()).toInstant());
    }

    private void validateInput(Long tripId, String seatNumber, String holdToken) {
        if (tripId == null || seatNumber == null || seatNumber.isBlank()) {
            throw new IllegalArgumentException("Thiếu thông tin chuyến hoặc ghế.");
        }
        validateToken(holdToken);
    }

    private void validateToken(String holdToken) {
        if (holdToken == null || !holdToken.matches("[A-Za-z0-9-]{20,64}")) {
            throw new IllegalArgumentException("Phiên giữ ghế không hợp lệ.");
        }
    }

    public static class SeatUnavailableException extends RuntimeException {
        public SeatUnavailableException(String message) { super(message); }
    }
}
