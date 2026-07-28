package com.smartbus.booking.controller;

import com.smartbus.booking.entity.ExpenseType;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.entity.TripExpense;
import com.smartbus.booking.repository.TripExpenseRepository;
import com.smartbus.booking.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Map;

@RestController
@RequestMapping("/inspector/trips")
public class InspectorExpenseController {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TripExpenseRepository expenseRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{tripId}/expenses")
    public ResponseEntity<?> getTripExpenses(@PathVariable("tripId") Long tripId) {
        return ResponseEntity.ok(expenseRepository.findByTripIdOrderByCreatedAtDesc(tripId));
    }

    @PostMapping("/{tripId}/expenses")
    public ResponseEntity<?> reportExpense(@PathVariable("tripId") Long tripId, @RequestBody Map<String, Object> payload) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new RuntimeException("Trip not found"));

        Double amount = Double.valueOf(payload.get("amount").toString());
        String expenseTypeStr = (String) payload.get("expenseType");
        String description = (String) payload.get("description");
        String imageUrl = (String) payload.get("receiptImageUrl");
        String reportedBy = (String) payload.get("reportedBy");

        TripExpense expense = TripExpense.builder()
                .trip(trip)
                .amount(amount)
                .expenseType(ExpenseType.valueOf(expenseTypeStr))
                .description(description)
                .receiptImageUrl(imageUrl)
                .reportedBy(reportedBy)
                .build();

        TripExpense savedExpense = expenseRepository.save(expense);

        try {
            messagingTemplate.convertAndSend("/topic/admin/expenses/new", "NEW_EXPENSE");
        } catch (Exception e) {
            System.err.println("Failed to send websocket message: " + e.getMessage());
        }

        return ResponseEntity.ok(savedExpense);
    }
}
