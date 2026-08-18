package com.smartbus.booking.service;

import com.smartbus.booking.dto.RouteStopRequest;
import com.smartbus.booking.dto.DriverStopPassengerDto;
import com.smartbus.booking.entity.*;
import com.smartbus.booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RouteStopService {
    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final BookingStopSelectionRepository selectionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public List<RouteStop> getRouteStops(Long routeId) {
        routeRepository.findById(routeId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tuyến đường."));
        return routeStopRepository.findByRouteIdOrderByStopOrderAsc(routeId);
    }

    public List<RouteStop> getResolvedTripStops(Long tripId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến xe."));
        List<RouteStop> overrides = routeStopRepository.findByTripIdOrderByStopOrderAsc(tripId);
        if (!overrides.isEmpty()) return overrides;
        return routeRepository.findFirstByDeparturePointIgnoreCaseAndArrivalPointIgnoreCase(
                        trip.getDeparturePoint(), trip.getArrivalPoint())
                .map(route -> routeStopRepository.findByRouteIdOrderByStopOrderAsc(route.getId()))
                .orElseGet(List::of);
    }

    @Transactional
    public List<RouteStop> replaceRouteStops(Long routeId, List<RouteStopRequest> requests) {
        routeRepository.findById(routeId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tuyến đường."));
        routeStopRepository.deleteByRouteId(routeId);
        return routeStopRepository.saveAll(buildStops(routeId, null, requests));
    }

    public void saveBookingSelection(Booking booking, Object pickupValue, Object dropoffValue) {
        if (pickupValue == null && dropoffValue == null) return;
        if (pickupValue == null || dropoffValue == null) throw new IllegalArgumentException("Vui lòng chọn đầy đủ điểm đón và điểm trả.");
        Long pickupId = Long.valueOf(pickupValue.toString());
        Long dropoffId = Long.valueOf(dropoffValue.toString());
        Map<Long, RouteStop> allowed = new HashMap<>();
        getResolvedTripStops(booking.getTrip().getId()).forEach(stop -> allowed.put(stop.getId(), stop));
        RouteStop pickup = allowed.get(pickupId);
        RouteStop dropoff = allowed.get(dropoffId);
        if (pickup == null || dropoff == null) throw new IllegalArgumentException("Điểm đón hoặc điểm trả không thuộc chuyến xe.");
        if (pickup.getStopOrder() >= dropoff.getStopOrder()) throw new IllegalArgumentException("Điểm trả phải nằm sau điểm đón.");
        if (!Set.of("PICKUP", "BOTH").contains(pickup.getStopType()) ||
                !Set.of("DROPOFF", "BOTH").contains(dropoff.getStopType()))
            throw new IllegalArgumentException("Loại điểm đón/trả không hợp lệ.");
        selectionRepository.save(BookingStopSelection.builder()
                .bookingId(booking.getId()).pickupStopId(pickupId).dropoffStopId(dropoffId)
                .pickupName(pickup.getName()).dropoffName(dropoff.getName())
                .pickupAddress(pickup.getAddress()).dropoffAddress(dropoff.getAddress())
                .pickupOffsetMinutes(pickup.getOffsetMinutes()).dropoffOffsetMinutes(dropoff.getOffsetMinutes())
                .build());
    }

    public List<BookingStopSelection> getSelections(List<Long> bookingIds) {
        return bookingIds.isEmpty() ? List.of() : selectionRepository.findByBookingIdIn(bookingIds);
    }

    public List<DriverStopPassengerDto> getDriverStopPassengers(Long tripId, Long userId, boolean admin) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến xe."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (!admin
                && !Objects.equals(trip.getAssignedDriverUsername(), user.getPhone())
                && !Objects.equals(trip.getSecondaryDriverUsername(), user.getPhone())) {
            throw new AccessDeniedException("Bạn không được phân công cho chuyến xe này.");
        }

        List<Booking> bookings = bookingRepository.findByTripId(tripId).stream()
                .filter(booking -> !"CANCELLED".equals(booking.getStatus()))
                .toList();
        List<Long> bookingIds = bookings.stream().map(Booking::getId).toList();
        Map<Long, BookingStopSelection> selectionByBooking = new HashMap<>();
        getSelections(bookingIds).forEach(selection -> selectionByBooking.put(selection.getBookingId(), selection));

        List<RouteStop> routeStops = getResolvedTripStops(tripId);
        Long defaultPickupId = routeStops.stream()
                .filter(stop -> Set.of("PICKUP", "BOTH").contains(stop.getStopType()))
                .map(RouteStop::getId).findFirst().orElse(null);
        Long defaultDropoffId = routeStops.stream()
                .filter(stop -> Set.of("DROPOFF", "BOTH").contains(stop.getStopType()))
                .reduce((first, second) -> second).map(RouteStop::getId).orElse(null);

        return bookings.stream().map(booking -> {
            BookingStopSelection selection = selectionByBooking.get(booking.getId());
            List<String> seats = booking.getSeatNumbers() == null ? List.of() : booking.getSeatNumbers();
            return DriverStopPassengerDto.builder()
                    .bookingId(booking.getId())
                    .pickupStopId(selection == null ? defaultPickupId : selection.getPickupStopId())
                    .dropoffStopId(selection == null ? defaultDropoffId : selection.getDropoffStopId())
                    .customerName(booking.getCustomerName())
                    .customerPhone(booking.getCustomerPhone())
                    .seatNumbers(seats)
                    .passengerCount(seats.size())
                    .build();
        }).toList();
    }

    private List<RouteStop> buildStops(Long routeId, Long tripId, List<RouteStopRequest> requests) {
        if (requests == null || requests.size() < 2) throw new IllegalArgumentException("Tuyến phải có ít nhất điểm đầu và điểm cuối.");
        List<RouteStopRequest> sorted = new ArrayList<>(requests);
        sorted.sort(Comparator.comparing(r -> r.getStopOrder() == null ? Integer.MAX_VALUE : r.getStopOrder()));
        List<RouteStop> result = new ArrayList<>();
        int lastOffset = -1;
        for (int index = 0; index < sorted.size(); index++) {
            RouteStopRequest request = sorted.get(index);
            if (request.getName() == null || request.getName().isBlank()) throw new IllegalArgumentException("Tên điểm dừng không được để trống.");
            int offset = request.getOffsetMinutes() == null ? 0 : request.getOffsetMinutes();
            if (offset < lastOffset) throw new IllegalArgumentException("Thời gian các điểm dừng phải tăng dần.");
            lastOffset = offset;
            String type = Optional.ofNullable(request.getStopType()).orElse("BOTH").toUpperCase();
            if (!Set.of("PICKUP", "DROPOFF", "BOTH", "REST").contains(type)) throw new IllegalArgumentException("Loại điểm dừng không hợp lệ.");
            result.add(RouteStop.builder().routeId(routeId).tripId(tripId).name(request.getName().trim())
                    .address(request.getAddress()).stopType(type).stopOrder(index).offsetMinutes(offset)
                    .latitude(request.getLatitude()).longitude(request.getLongitude()).build());
        }
        return result;
    }
}
