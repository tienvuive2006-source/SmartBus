package com.smartbus.booking.dto;

import com.smartbus.booking.entity.Trip;

public record TripPairAssignmentRequest(
        Long returnTripId,
        Trip outboundTrip,
        Trip returnTrip) {
}
