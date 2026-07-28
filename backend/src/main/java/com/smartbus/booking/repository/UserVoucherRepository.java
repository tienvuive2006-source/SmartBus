package com.smartbus.booking.repository;

import com.smartbus.booking.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUserIdOrderByAcquiredAtDesc(Long userId);
    List<UserVoucher> findByUserIdAndIsUsedFalseOrderByAcquiredAtDesc(Long userId);
    long countByVoucherIdAndIsUsedTrue(Long voucherId);
}
