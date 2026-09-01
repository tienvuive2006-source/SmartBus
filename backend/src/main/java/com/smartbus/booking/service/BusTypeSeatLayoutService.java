package com.smartbus.booking.service;

import com.smartbus.booking.dto.SeatLayoutItemRequest;
import com.smartbus.booking.dto.SeatLayoutItemResponse;
import com.smartbus.booking.dto.SeatLayoutResponse;
import com.smartbus.booking.entity.BusType;
import com.smartbus.booking.entity.BusTypeSeatLayout;
import com.smartbus.booking.entity.SeatType;
import com.smartbus.booking.entity.Seat;
import com.smartbus.booking.repository.BusTypeRepository;
import com.smartbus.booking.repository.BusTypeSeatLayoutRepository;
import com.smartbus.booking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusTypeSeatLayoutService {

    private final BusTypeRepository busTypeRepository;
    private final BusTypeSeatLayoutRepository layoutRepository;
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public SeatLayoutResponse getLayout(Long busTypeId) {
        BusType busType = requireBusType(busTypeId);
        List<BusTypeSeatLayout> stored = layoutRepository
                .findByBusTypeIdOrderBySeatFloorAscSeatNumberAsc(busTypeId);
        Map<String, SeatType> storedTypes = toSeatTypeMap(stored);

        List<SeatLayoutItemResponse> seats = createDefaultLayout(busType.getSeatCount()).stream()
                .map(item -> new SeatLayoutItemResponse(
                        item.seatNumber(),
                        item.seatFloor(),
                        storedTypes.getOrDefault(item.seatNumber(), SeatType.STANDARD)
                ))
                .toList();

        return new SeatLayoutResponse(
                busType.getId(),
                busType.getName(),
                normalizeSeatCount(busType.getSeatCount()),
                !stored.isEmpty(),
                seats
        );
    }

    @Transactional
    public SeatLayoutResponse saveLayout(Long busTypeId, List<SeatLayoutItemRequest> requestedSeats) {
        BusType busType = requireBusType(busTypeId);
        List<SeatLayoutItemResponse> expected = createDefaultLayout(busType.getSeatCount());

        if (requestedSeats == null || requestedSeats.size() != expected.size()) {
            throw new IllegalArgumentException("Sơ đồ phải có đúng " + expected.size() + " ghế.");
        }

        Map<String, SeatLayoutItemRequest> requestedByNumber = new HashMap<>();
        for (SeatLayoutItemRequest item : requestedSeats) {
            if (item == null) {
                throw new IllegalArgumentException("Dữ liệu ghế không hợp lệ.");
            }
            String seatNumber = normalizeSeatNumber(item.seatNumber());
            if (requestedByNumber.putIfAbsent(seatNumber, item) != null) {
                throw new IllegalArgumentException("Sơ đồ có số ghế bị trùng: " + seatNumber + ".");
            }
        }

        layoutRepository.deleteByBusTypeId(busTypeId);
        layoutRepository.flush();

        List<BusTypeSeatLayout> entities = expected.stream().map(position -> {
            SeatLayoutItemRequest request = requestedByNumber.get(position.seatNumber());
            if (request == null) {
                throw new IllegalArgumentException("Thiếu ghế " + position.seatNumber() + " trong sơ đồ.");
            }
            return BusTypeSeatLayout.builder()
                    .busType(busType)
                    .seatNumber(position.seatNumber())
                    .seatFloor(position.seatFloor())
                    .seatType(request.seatType() == null ? SeatType.STANDARD : request.seatType())
                    .build();
        }).toList();

        layoutRepository.saveAll(entities);
        layoutRepository.flush();
        synchronizeExistingTripSeats(busType, entities);
        return getLayout(busTypeId);
    }

    @Transactional(readOnly = true)
    public Map<String, SeatType> getSeatTypesForBusType(String busTypeName) {
        if (busTypeName == null || busTypeName.isBlank()) {
            return Map.of();
        }

        return busTypeRepository.findByNameIgnoreCase(busTypeName.trim())
                .map(busType -> toSeatTypeMap(layoutRepository
                        .findByBusTypeIdOrderBySeatFloorAscSeatNumberAsc(busType.getId())))
                .orElseGet(Map::of);
    }

    private Map<String, SeatType> toSeatTypeMap(List<BusTypeSeatLayout> items) {
        return items.stream().collect(Collectors.toMap(
                item -> item.getSeatNumber().toUpperCase(Locale.ROOT),
                BusTypeSeatLayout::getSeatType,
                (first, ignored) -> first
        ));
    }

    private void synchronizeExistingTripSeats(BusType busType, List<BusTypeSeatLayout> layout) {
        Map<String, SeatType> configuredTypes = toSeatTypeMap(layout);
        List<Seat> existingSeats = seatRepository.findAllByTripBusType(busType.getName());

        for (Seat seat : existingSeats) {
            String seatNumber = seat.getSeatNumber().toUpperCase(Locale.ROOT);
            seat.setSeatType(configuredTypes.getOrDefault(seatNumber, SeatType.STANDARD));
        }

        if (!existingSeats.isEmpty()) {
            seatRepository.saveAll(existingSeats);
        }
    }

    private BusType requireBusType(Long busTypeId) {
        return busTypeRepository.findById(busTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng xe mã " + busTypeId + "."));
    }

    private List<SeatLayoutItemResponse> createDefaultLayout(Integer rawSeatCount) {
        int seatCount = normalizeSeatCount(rawSeatCount);
        int floor1Count = (int) Math.ceil(seatCount / 2.0);
        int floor2Count = seatCount - floor1Count;
        List<SeatLayoutItemResponse> seats = new ArrayList<>(seatCount);

        addFloorSeats(seats, "A", 1, floor1Count);
        addFloorSeats(seats, "B", 2, floor2Count);
        return seats;
    }

    private void addFloorSeats(List<SeatLayoutItemResponse> seats, String prefix, int floor, int count) {
        for (int index = 1; index <= count; index++) {
            seats.add(new SeatLayoutItemResponse(
                    prefix + String.format("%02d", index),
                    floor,
                    SeatType.STANDARD
            ));
        }
    }

    private int normalizeSeatCount(Integer seatCount) {
        return seatCount == null || seatCount < 1 ? 24 : seatCount;
    }

    private String normalizeSeatNumber(String seatNumber) {
        if (seatNumber == null || seatNumber.isBlank()) {
            throw new IllegalArgumentException("Số ghế không được để trống.");
        }
        return seatNumber.trim().toUpperCase(Locale.ROOT);
    }
}
