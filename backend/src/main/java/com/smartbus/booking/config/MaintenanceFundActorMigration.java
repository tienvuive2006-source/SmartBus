package com.smartbus.booking.config;

import com.smartbus.booking.entity.FundTransaction;
import com.smartbus.booking.repository.FundTransactionRepository;
import com.smartbus.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Đổi user ID cũ trong giao dịch bảo trì thành họ tên người thực hiện. */
@Component
@RequiredArgsConstructor
public class MaintenanceFundActorMigration implements ApplicationRunner {
    private final FundTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<FundTransaction> changed = new ArrayList<>();
        for (FundTransaction transaction : transactionRepository.findByReferenceIdStartingWith("MAINTENANCE-")) {
            String performedBy = transaction.getPerformedBy();
            if (performedBy == null || !performedBy.matches("\\d+")) continue;
            userRepository.findById(Long.valueOf(performedBy)).ifPresent(user -> {
                if (user.getFullName() != null && !user.getFullName().isBlank()) {
                    transaction.setPerformedBy(user.getFullName());
                    changed.add(transaction);
                }
            });
        }
        if (!changed.isEmpty()) transactionRepository.saveAll(changed);
    }
}
