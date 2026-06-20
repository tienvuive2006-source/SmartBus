package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    java.util.List<Incident> findByTripIdOrderByCreatedAtDesc(Long tripId);
}
