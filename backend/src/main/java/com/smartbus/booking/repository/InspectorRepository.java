package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Inspector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface InspectorRepository extends JpaRepository<Inspector, Long> {
    Optional<Inspector> findByPhone(String phone);
    Optional<Inspector> findByUserAccountId(Long userId);
    java.util.List<Inspector> findByUserAccountRole(String role);

    @Query("SELECT i FROM Inspector i LEFT JOIN FETCH i.userAccount u WHERE u.role = :role")
    java.util.List<Inspector> findByUserAccountRoleWithUser(@Param("role") String role);
}
