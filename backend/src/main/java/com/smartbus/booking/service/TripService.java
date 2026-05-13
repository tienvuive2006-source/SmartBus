package com.smartbus.booking.service;

import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public List<Trip> searchTrips(String from, String to) {
        if ((from == null || from.isEmpty()) && (to == null || to.isEmpty())) {
            return getAllTrips();
        }
        return tripRepository.findByDeparturePointContainingIgnoreCaseAndArrivalPointContainingIgnoreCase(
                from != null ? from : "", 
                to != null ? to : ""
        );
    }

    public Trip saveTrip(Trip trip) {
        return tripRepository.save(trip);
    }
}
