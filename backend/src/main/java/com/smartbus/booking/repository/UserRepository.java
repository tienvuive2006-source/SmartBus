package com.smartbus.booking.repository;

import com.smartbus.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    java.util.List<User> findByRole(String role);
}
