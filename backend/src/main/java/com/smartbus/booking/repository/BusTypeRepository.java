package com.smartbus.booking.repository;

import com.smartbus.booking.entity.BusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusTypeRepository extends JpaRepository<BusType, Long> {
    // Tự động sinh hạ tầng CRUD của JPA
}
