package com.smartbus.booking.controller;

import com.smartbus.booking.entity.ExpenseStatus;
import com.smartbus.booking.entity.TripExpense;
import com.smartbus.booking.repository.TripExpenseRepository;
import com.smartbus.booking.service.FundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/expenses")
public class AdminExpenseController {

    @Autowired
    private TripExpenseRepository expenseRepository;

    @Autowired
    private FundService fundService;

    @GetMapping
    public ResponseEntity<?> getAllExpenses() {
        return ResponseEntity.ok(expenseRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveExpense(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        TripExpense expense = expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));
        
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("error", "Expense is already processed"));
        }

        String fundType = payload.getOrDefault("fundType", "CASH");
        
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setApprovedBy(payload.get("approvedBy"));
        TripExpense savedExpense = expenseRepository.save(expense);

        // Deduct from fund
        fundService.recordTransaction(
            fundType, 
            "EXPENSE", 
            expense.getAmount(), 
            "Duyệt chi phí chuyến " + expense.getTrip().getId() + " - " + expense.getExpenseType(), 
            "EXP-" + savedExpense.getId(),
            payload.get("approvedBy")
        );

        return ResponseEntity.ok(savedExpense);
    }

    @Autowired
    private com.smartbus.booking.repository.BookingRepository bookingRepository;

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectExpense(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        TripExpense expense = expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));
        
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("error", "Expense is already processed"));
        }

        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setRejectReason(payload.get("rejectReason"));
        return ResponseEntity.ok(expenseRepository.save(expense));
    }

    @GetMapping("/trips/{tripId}/report")
    public ResponseEntity<?> getTripFinancialReport(@PathVariable("tripId") Long tripId) {
        java.util.List<com.smartbus.booking.entity.Booking> bookings = bookingRepository.findByTripId(tripId);
        double totalRevenue = bookings.stream()
                .filter(b -> "PAID".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                .mapToDouble(com.smartbus.booking.entity.Booking::getTotalPrice)
                .sum();
        
        java.util.List<TripExpense> expenses = expenseRepository.findByTripIdOrderByCreatedAtDesc(tripId);
        double totalExpense = expenses.stream()
                .filter(e -> e.getStatus() == ExpenseStatus.APPROVED)
                .mapToDouble(TripExpense::getAmount)
                .sum();
                
        return ResponseEntity.ok(Map.of(
            "totalRevenue", totalRevenue,
            "totalExpense", totalExpense,
            "netProfit", totalRevenue - totalExpense
        ));
    }
}
