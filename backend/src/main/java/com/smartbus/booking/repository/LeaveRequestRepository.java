package com.smartbus.booking.repository;

import com.smartbus.booking.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByDriverUsername(String driverUsername);

    /**
     * Tìm đơn nghỉ phép APPROVED của tài xế mà overlap với ngày cho trước.
     * Dùng để chặn gán chuyến cho tài xế đang nghỉ phép.
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT lr FROM LeaveRequest lr WHERE lr.driverUsername = :driverUsername " +
        "AND lr.status = 'APPROVED' " +
        "AND lr.startDate <= :date AND lr.endDate >= :date"
    )
    List<LeaveRequest> findApprovedLeaveOnDate(
        @org.springframework.data.repository.query.Param("driverUsername") String driverUsername,
        @org.springframework.data.repository.query.Param("date") String date
    );

    /**
     * Tìm tất cả đơn nghỉ phép APPROVED trong khoảng thời gian (dùng cho lịch Admin)
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT lr FROM LeaveRequest lr WHERE lr.status = 'APPROVED' " +
        "AND lr.startDate <= :endDate AND lr.endDate >= :startDate"
    )
    List<LeaveRequest> findApprovedLeavesInRange(
        @org.springframework.data.repository.query.Param("startDate") String startDate,
        @org.springframework.data.repository.query.Param("endDate") String endDate
    );
}
