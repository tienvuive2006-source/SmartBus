package com.smartbus.booking.service;

import com.smartbus.booking.entity.SeatReservation;
import com.smartbus.booking.repository.SeatReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatHoldCleanupService {
    private final SeatReservationRepository reservationRepository;
    private final SeatRealtimePublisher realtimePublisher;

    public SeatHoldCleanupService(SeatReservationRepository reservationRepository,
                                  SeatRealtimePublisher realtimePublisher) {
        this.reservationRepository = reservationRepository;
        this.realtimePublisher = realtimePublisher;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void releaseExpiredHolds() {
        List<SeatReservation> expired = reservationRepository.findByExpiredAtLessThanEqual(LocalDateTime.now());
        if (expired.isEmpty()) return;
        reservationRepository.deleteAll(expired);
        reservationRepository.flush();
        expired.forEach(item -> realtimePublisher.publish(item.getTripId(), item.getSeatNumber(), "AVAILABLE", null));
    }
}
