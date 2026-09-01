package com.smartbus.booking.repository;

import com.smartbus.booking.entity.BusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusTypeRepository extends JpaRepository<BusType, Long> {
    Optional<BusType> findByNameIgnoreCase(String name);
    // Tự động sinh hạ tầng CRUD của JPA
}
