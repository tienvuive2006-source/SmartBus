package com.smartbus.booking.service;

import com.smartbus.booking.entity.FundTransaction;
import com.smartbus.booking.repository.FundTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FundService {

    private final FundTransactionRepository fundTransactionRepository;
    private final com.smartbus.booking.repository.BookingRepository bookingRepository;
    private final com.smartbus.booking.repository.RefundRequestRepository refundRequestRepository;

    /**
     * Ghi nhận giao dịch vào sổ cái
     */
    public FundTransaction recordTransaction(String rawFundType, String transactionType, Double amount, String description, String referenceId, String performedBy) {
        return recordTransaction(rawFundType, transactionType, amount, description, referenceId, performedBy, java.time.LocalDateTime.now());
    }

    /** Ghi giao dịch một lần theo mã tham chiếu, dùng cho thao tác có thể bị gửi lại. */
    public FundTransaction recordTransactionIfAbsent(String rawFundType, String transactionType, Double amount,
            String description, String referenceId, String performedBy) {
        return recordTransactionIfAbsent(rawFundType, transactionType, amount, description, referenceId, performedBy,
                java.time.LocalDateTime.now());
    }

    public FundTransaction recordTransactionIfAbsent(String rawFundType, String transactionType, Double amount,
            String description, String referenceId, String performedBy, java.time.LocalDateTime date) {
        if (referenceId != null && fundTransactionRepository.existsByReferenceIdAndTransactionType(referenceId, transactionType)) return null;
        return recordTransaction(rawFundType, transactionType, amount, description, referenceId, performedBy, date);
    }

    public FundTransaction recordTransaction(String rawFundType, String transactionType, Double amount, String description, String referenceId, String performedBy, java.time.LocalDateTime date) {
        if (amount == null || amount <= 0) return null; // Không ghi nhận giao dịch 0đ
        
        String fundType = "CASH";
        if (rawFundType != null) {
            String upper = rawFundType.toUpperCase();
            if (upper.contains("BANK") || upper.contains("SEPAY") || upper.contains("SEEPAY") || upper.contains("VNPAY") || upper.contains("MOMO") || upper.contains("CHUYEN_KHOAN") || upper.equals("QR")) {
                fundType = "BANK_TRANSFER";
            } else if (upper.contains("WALLET") || upper.contains("VI_NOI_BO")) {
                fundType = "WALLET";
            } else {
                fundType = "CASH";
            }
        }
        
        FundTransaction tx = FundTransaction.builder()
                .fundType(fundType)
                .transactionType(transactionType)
                .amount(amount)
                .description(description)
                .referenceId(referenceId)
                .performedBy(performedBy)
                .transactionDate(date != null ? date : java.time.LocalDateTime.now())
                .build();
                
        return fundTransactionRepository.save(tx);
    }

    /**
     * Lấy thông kê của 1 quỹ cụ thể
     */
    public Map<String, Object> getFundStats(String fundType, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        Double allTimeIncome = fundTransactionRepository.sumIncomeByFundType(fundType);
        Double allTimeExpense = fundTransactionRepository.sumExpenseByFundType(fundType);
        Double balance = allTimeIncome - allTimeExpense;
        
        Double filteredIncome = allTimeIncome;
        Double filteredExpense = allTimeExpense;
        
        if (start != null && end != null) {
            filteredIncome = fundTransactionRepository.sumIncomeByFundTypeAndDate(fundType, start, end);
            filteredExpense = fundTransactionRepository.sumExpenseByFundTypeAndDate(fundType, start, end);
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("fundType", fundType);
        stats.put("balance", balance);
        stats.put("totalIncome", filteredIncome);
        stats.put("totalExpense", filteredExpense);
        return stats;
    }

    /**
     * Lấy thống kê của tất cả các quỹ
     */
    public Map<String, Object> getAllFundsStats(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        Map<String, Object> result = new HashMap<>();
        result.put("CASH", getFundStats("CASH", start, end));
        result.put("BANK_TRANSFER", getFundStats("BANK_TRANSFER", start, end));
        result.put("WALLET", getFundStats("WALLET", start, end));
        return result;
    }

    /**
     * Lấy danh sách giao dịch
     */
    public List<FundTransaction> getTransactions(String fundType, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (fundType == null || fundType.isEmpty() || fundType.equalsIgnoreCase("ALL")) {
            if (start != null && end != null) {
                return fundTransactionRepository.findAllByTransactionDateBetweenOrderByTransactionDateDesc(start, end);
            }
            return fundTransactionRepository.findAllByOrderByTransactionDateDesc();
        }
        if (start != null && end != null) {
            return fundTransactionRepository.findByFundTypeAndTransactionDateBetweenOrderByTransactionDateDesc(fundType, start, end);
        }
        return fundTransactionRepository.findByFundTypeOrderByTransactionDateDesc(fundType);
    }

    /**
     * Đồng bộ lịch sử bán vé cũ vào Sổ cái
     */
    public int syncHistoricalBookings() {
        List<com.smartbus.booking.entity.Booking> allBookings = bookingRepository.findAll();
        int count = 0;
        
        for (com.smartbus.booking.entity.Booking b : allBookings) {
            // Nếu đã thanh toán hoặc đã lên xe
            if ("PAID".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus())) {
                recordTransactionIfAbsent(
                    b.getPaymentMethod(), 
                    "INCOME", 
                    b.getTotalPrice(), 
                    "Thanh toán vé #" + b.getId(), 
                    String.valueOf(b.getId()), 
                    b.getCustomerName() != null ? b.getCustomerName() : "Khách hàng",
                    b.getCreatedAt()
                );
                count++;
            }
            // Nếu bị hủy và có hoàn tiền
            if ("CANCELLED".equals(b.getStatus()) && b.getRefundAmount() != null && b.getRefundAmount() > 0) {
                // Thu ban đầu
                recordTransactionIfAbsent(
                    b.getPaymentMethod(), 
                    "INCOME", 
                    b.getTotalPrice(), 
                    "Thanh toán vé #" + b.getId(), 
                    String.valueOf(b.getId()), 
                    b.getCustomerName() != null ? b.getCustomerName() : "Khách hàng",
                    b.getCreatedAt()
                );
                boolean refundCompleted = refundRequestRepository.findByBookingId(b.getId())
                    .map(refund -> "COMPLETED".equals(refund.getStatus()))
                    .orElse(true);
                if (refundCompleted) {
                    recordTransactionIfAbsent(
                        b.getPaymentMethod(), "EXPENSE", b.getRefundAmount(),
                        "Hoàn tiền hủy vé #" + b.getId(), String.valueOf(b.getId()),
                        b.getCustomerName() != null ? b.getCustomerName() : "Khách hàng", b.getCreatedAt()
                    );
                    count++;
                }
                count++;
            }
        }
        return count;
    }
}
