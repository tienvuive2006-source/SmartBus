package com.smartbus.booking.repository;

import com.smartbus.booking.entity.BookingExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface BookingExchangeRepository extends JpaRepository<BookingExchange, Long> {
    boolean existsByBookingIdAndStatus(Long bookingId, String status);

    List<BookingExchange> findByBookingIdOrderByExchangedAtDesc(Long bookingId);

    List<BookingExchange> findByBookingIdAndStatus(Long bookingId, String status);

    Optional<BookingExchange> findByIdAndBookingUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"booking"})
    List<BookingExchange> findByBookingUserIdAndStatusOrderByExchangedAtDesc(Long userId, String status);

    @Override
    @EntityGraph(attributePaths = {"booking", "booking.user"})
    List<BookingExchange> findAll();
}
