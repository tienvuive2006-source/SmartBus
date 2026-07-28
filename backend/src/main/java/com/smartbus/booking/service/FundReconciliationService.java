package com.smartbus.booking.service;

import com.smartbus.booking.entity.FundReconciliation;
import com.smartbus.booking.entity.FundTransaction;
import com.smartbus.booking.repository.FundReconciliationRepository;
import com.smartbus.booking.repository.FundTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FundReconciliationService {

    private final FundReconciliationRepository reconciliationRepository;
    private final FundTransactionRepository transactionRepository;
    private final FundService fundService;

    public List<FundReconciliation> getHistory(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            return reconciliationRepository.findAllByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        }
        return reconciliationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public FundReconciliation createReconciliation(String fundType, Double actualBalance, String reason, String performedBy) {
        // 1. Get system balance
        Map<String, Object> stats = fundService.getFundStats(fundType, null, null);
        Double systemBalance = 0.0;
        if (stats.get("balance") != null) {
            systemBalance = Double.valueOf(stats.get("balance").toString());
        }

        // 2. Calculate discrepancy
        Double discrepancy = actualBalance - systemBalance;

        // 3. Create record
        FundReconciliation rec = new FundReconciliation();
        rec.setFundType(fundType);
        rec.setSystemBalance(systemBalance);
        rec.setActualBalance(actualBalance);
        rec.setDiscrepancy(discrepancy);
        rec.setReason(reason);
        rec.setPerformedBy(performedBy);
        rec.setStatus("COMPLETED");
        rec.setCreatedAt(LocalDateTime.now());
        
        FundReconciliation saved = reconciliationRepository.save(rec);

        // 4. Create compensating transaction if discrepancy != 0
        if (Math.abs(discrepancy) > 0.01) { // float tolerance
            FundTransaction tx = FundTransaction.builder()
                    .fundType(fundType)
                    .transactionType(discrepancy > 0 ? "INCOME" : "EXPENSE")
                    .amount(Math.abs(discrepancy))
                    .description("Điều chỉnh đối soát quỹ. Nguyên nhân: " + (reason != null ? reason : "Không có"))
                    .referenceId("REC-" + saved.getId())
                    .performedBy(performedBy)
                    .transactionDate(LocalDateTime.now())
                    .build();
            transactionRepository.save(tx);
        }

        return saved;
    }
}
