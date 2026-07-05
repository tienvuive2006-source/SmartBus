package com.smartbus.booking.repository;

import com.smartbus.booking.entity.RoundTripGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoundTripGroupRepository extends JpaRepository<RoundTripGroup, Long> {
    Optional<RoundTripGroup> findByGroupId(String groupId);
}
