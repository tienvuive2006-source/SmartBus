package com.smartbus.booking.repository;

import com.smartbus.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    java.util.List<User> findByRole(String role);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT u FROM User u WHERE " +
                    "(:role = '' OR UPPER(u.role) = :role) AND " +
                    "(:provider = '' OR UPPER(COALESCE(u.authProvider, 'LOCAL')) = :provider) AND " +
                    "(:locked IS NULL OR u.isLocked = :locked) AND " +
                    "(:search = '' OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR COALESCE(u.phone, '') LIKE CONCAT('%', :search, '%') " +
                    "OR LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(u) FROM User u WHERE " +
                    "(:role = '' OR UPPER(u.role) = :role) AND " +
                    "(:provider = '' OR UPPER(COALESCE(u.authProvider, 'LOCAL')) = :provider) AND " +
                    "(:locked IS NULL OR u.isLocked = :locked) AND " +
                    "(:search = '' OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR COALESCE(u.phone, '') LIKE CONCAT('%', :search, '%') " +
                    "OR LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchAdminUsers(@Param("role") String role,
                                @Param("search") String search,
                                @Param("provider") String provider,
                                @Param("locked") Boolean locked,
                                Pageable pageable);

    long countByRoleIgnoreCase(String role);
    long countByRoleIgnoreCaseAndIsLockedFalse(String role);
    long countByRoleIgnoreCaseAndAuthProviderIgnoreCase(String role, String authProvider);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(u.walletBalance), 0) FROM User u WHERE UPPER(u.role) = :role")
    Double sumWalletBalanceByRole(@Param("role") String role);
}
