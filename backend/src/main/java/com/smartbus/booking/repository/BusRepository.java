package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    // Kế thừa toàn bộ sức mạnh CRUD từ JpaRepository
}
