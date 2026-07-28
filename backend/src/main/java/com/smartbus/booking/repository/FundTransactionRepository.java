package com.smartbus.booking.repository;

import com.smartbus.booking.entity.FundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {

    List<FundTransaction> findByFundTypeOrderByTransactionDateDesc(String fundType);
    
    boolean existsByReferenceId(String referenceId);

    List<FundTransaction> findByFundTypeAndTransactionDateBetweenOrderByTransactionDateDesc(String fundType, java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    List<FundTransaction> findAllByOrderByTransactionDateDesc();

    List<FundTransaction> findAllByTransactionDateBetweenOrderByTransactionDateDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FundTransaction f WHERE f.fundType = :fundType AND f.transactionType = 'INCOME'")
    Double sumIncomeByFundType(@Param("fundType") String fundType);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FundTransaction f WHERE f.fundType = :fundType AND f.transactionType = 'EXPENSE'")
    Double sumExpenseByFundType(@Param("fundType") String fundType);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FundTransaction f WHERE f.fundType = :fundType AND f.transactionType = 'INCOME' AND f.transactionDate BETWEEN :start AND :end")
    Double sumIncomeByFundTypeAndDate(@Param("fundType") String fundType, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FundTransaction f WHERE f.fundType = :fundType AND f.transactionType = 'EXPENSE' AND f.transactionDate BETWEEN :start AND :end")
    Double sumExpenseByFundTypeAndDate(@Param("fundType") String fundType, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
