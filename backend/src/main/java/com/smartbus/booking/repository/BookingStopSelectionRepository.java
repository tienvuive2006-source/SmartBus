package com.smartbus.booking.repository;

import com.smartbus.booking.entity.BookingStopSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingStopSelectionRepository extends JpaRepository<BookingStopSelection, Long> {
    List<BookingStopSelection> findByBookingIdIn(List<Long> bookingIds);
}
