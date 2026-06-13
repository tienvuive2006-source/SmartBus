package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Inspector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InspectorRepository extends JpaRepository<Inspector, Long> {
    Optional<Inspector> findByPhone(String phone);
    Optional<Inspector> findByUserAccountId(Long userId);
}
