package com.smartbus.booking.repository;

import com.smartbus.booking.entity.BusTypeSeatLayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusTypeSeatLayoutRepository extends JpaRepository<BusTypeSeatLayout, Long> {
    List<BusTypeSeatLayout> findByBusTypeIdOrderBySeatFloorAscSeatNumberAsc(Long busTypeId);

    void deleteByBusTypeId(Long busTypeId);
}
