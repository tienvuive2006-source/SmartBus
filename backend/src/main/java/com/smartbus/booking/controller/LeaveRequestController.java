package com.smartbus.booking.controller;

import com.smartbus.booking.entity.LeaveRequest;
import com.smartbus.booking.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    // ADMIN: Lấy tất cả
    @GetMapping
    public ResponseEntity<List<LeaveRequest>> getAllLeaveRequests() {
        return ResponseEntity.ok(leaveRequestService.getAllLeaveRequests());
    }

    // DRIVER: Lấy của mình
    @GetMapping("/my/{username}")
    public ResponseEntity<List<LeaveRequest>> getMyLeaveRequests(@PathVariable("username") String username) {
        return ResponseEntity.ok(leaveRequestService.getMyLeaveRequests(username));
    }

    // DRIVER: Tạo mới
    @PostMapping
    public ResponseEntity<LeaveRequest> createLeaveRequest(@RequestBody LeaveRequest request) {
        return ResponseEntity.ok(leaveRequestService.createLeaveRequest(request));
    }

    // ADMIN: Duyệt hoặc Từ chối
    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_LEAVE_STATUS", entityName = "LeaveRequest")
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaveRequest> updateLeaveStatus(@PathVariable("id") Long id, @RequestBody java.util.Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(leaveRequestService.updateLeaveRequestStatus(id, status));
    }
}
