package com.smartbus.booking.service;

import com.smartbus.booking.entity.LeaveRequest;
import com.smartbus.booking.entity.Trip;
import com.smartbus.booking.repository.LeaveRequestRepository;
import com.smartbus.booking.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final TripRepository tripRepository;

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getMyLeaveRequests(String username) {
        return leaveRequestRepository.findByDriverUsername(username);
    }

    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequest request) {
        request.setStatus("PENDING");
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest updateLeaveRequestStatus(Long id, String status) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn xin nghỉ phép này"));

        // 🛡️ KHI DUYỆT NGHỈ PHÉP: Kiểm tra tài xế có chuyến đã gán trong khoảng ngày
        // nghỉ không
        if ("APPROVED".equals(status)) {
            // Lấy tất cả chuyến trong khoảng ngày nghỉ
            List<Trip> allTrips = tripRepository.findAll();
            List<Trip> conflictingTrips = allTrips.stream()
                    .filter(t -> ((t.getAssignedDriverUsername() != null
                            && t.getAssignedDriverUsername().equals(request.getDriverUsername())) ||
                            (t.getSecondaryDriverUsername() != null
                                    && t.getSecondaryDriverUsername().equals(request.getDriverUsername())) ||
                            (t.getInspector() != null && t.getInspector().getPhone() != null
                                    && t.getInspector().getPhone().equals(request.getDriverUsername())))
                            &&
                            t.getDepartureDate() != null &&
                            t.getDepartureDate().compareTo(request.getStartDate()) >= 0 &&
                            t.getDepartureDate().compareTo(request.getEndDate()) <= 0 &&
                            !"CANCELLED".equals(t.getStatus()) &&
                            !"COMPLETED".equals(t.getStatus()))
                    .collect(java.util.stream.Collectors.toList());

            if (!conflictingTrips.isEmpty()) {
                StringBuilder sb = new StringBuilder("⚠️ KHÔNG THỂ DUYỆT: Tài xế " + request.getDriverFullName() +
                        " đang có " + conflictingTrips.size() + " chuyến xe trong thời gian nghỉ phép:\n");
                for (Trip t : conflictingTrips) {
                    sb.append("• ").append(t.getDeparturePoint().split(",")[0]).append(" ➔ ")
                            .append(t.getArrivalPoint().split(",")[0])
                            .append(" (").append(t.getDepartureTime()).append(" ngày ").append(t.getDepartureDate())
                            .append(")\n");
                }
                sb.append("\nVui lòng đổi tài xế cho các chuyến trên trước khi duyệt nghỉ phép!");
                throw new RuntimeException(sb.toString());
            }

        }

        request.setStatus(status);
        return leaveRequestRepository.save(request);
    }
}
