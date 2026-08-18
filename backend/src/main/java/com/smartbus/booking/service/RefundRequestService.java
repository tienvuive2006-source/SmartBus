package com.smartbus.booking.service;

import com.smartbus.booking.dto.BookingCancellationRequest;
import com.smartbus.booking.dto.RefundActionRequest;
import com.smartbus.booking.dto.RefundRequestResponse;
import com.smartbus.booking.dto.RefundBankUpdateRequest;
import com.smartbus.booking.entity.Booking;
import com.smartbus.booking.entity.RefundRequest;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.RefundRequestRepository;
import com.smartbus.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefundRequestService {

    private final RefundRequestRepository repository;
    private final UserRepository userRepository;
    private final FundService fundService;
    private final SimpMessagingTemplate messagingTemplate;

    public String validateMethod(BookingCancellationRequest request, boolean hasRefund) {
        if (!hasRefund) return "NONE";
        String method = normalize(request.getRefundMethod());
        if (!method.equals("WALLET") && !method.equals("BANK_TRANSFER")) {
            throw new IllegalArgumentException("Vui lòng chọn phương thức nhận tiền hoàn.");
        }
        if (method.equals("BANK_TRANSFER")) {
            if (blank(request.getBankName()) || blank(request.getBankAccountName())) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ ngân hàng và tên chủ tài khoản.");
            }
            String accountNumber = digits(request.getBankAccountNumber());
            if (!accountNumber.matches("\\d{6,30}")) {
                throw new IllegalArgumentException("Số tài khoản ngân hàng không hợp lệ.");
            }
        }
        return method;
    }

    public RefundRequest create(Booking booking, User user, double amount, String method, BookingCancellationRequest request) {
        if (repository.existsByBookingId(booking.getId())) {
            throw new IllegalArgumentException("Vé này đã có yêu cầu hoàn tiền.");
        }
        boolean wallet = "WALLET".equals(method);
        RefundRequest saved = repository.save(RefundRequest.builder()
                .booking(booking)
                .user(user)
                .refundMethod(method)
                .refundAmount(amount)
                .status(wallet ? "COMPLETED" : "PENDING")
                .bankName(wallet ? null : clean(request.getBankName()))
                .bankAccountNumber(wallet ? null : digits(request.getBankAccountNumber()))
                .bankAccountName(wallet ? null : clean(request.getBankAccountName()).toUpperCase())
                .processedBy(wallet ? "SYSTEM" : null)
                .completedAt(wallet ? LocalDateTime.now() : null)
                .build());
        if (!wallet) {
            messagingTemplate.convertAndSend("/topic/admin/refunds/new", "NEW_REFUND_REQUEST");
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<RefundRequestResponse> findMine(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    public boolean existsForBooking(Long bookingId) {
        return repository.existsByBookingId(bookingId);
    }

    @Transactional(readOnly = true)
    public List<RefundRequestResponse> findAdmin(String status) {
        List<RefundRequest> refunds = blank(status) || "ALL".equalsIgnoreCase(status)
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
        return refunds.stream().map(this::toResponse).toList();
    }

    @Transactional
    public RefundRequestResponse approve(Long id, Long adminId, RefundActionRequest request) {
        RefundRequest refund = locked(id);
        requireStatus(refund, "PENDING");
        refund.setStatus("APPROVED");
        refund.setProcessedBy(adminName(adminId));
        refund.setAdminNote(clean(request.getNote()));
        RefundRequest saved = repository.save(refund);
        messagingTemplate.convertAndSend("/topic/refunds/" + saved.getUser().getId(), Map.of(
                "type", "REFUND_APPROVED",
                "status", "APPROVED",
                "refundId", saved.getId(),
                "bookingId", saved.getBooking().getId(),
                "amount", saved.getRefundAmount(),
                "message", "Yêu cầu hoàn tiền vé #" + saved.getBooking().getId() + " đã được duyệt và đang chờ chuyển khoản."
        ));
        return toResponse(saved);
    }

    @Transactional
    public RefundRequestResponse complete(Long id, Long adminId, RefundActionRequest request) {
        RefundRequest refund = locked(id);
        if (!"BANK_TRANSFER".equals(refund.getRefundMethod())) {
            throw new IllegalArgumentException("Khoản hoàn vào ví đã được hệ thống xử lý tự động.");
        }
        if (!List.of("PENDING", "APPROVED").contains(refund.getStatus())) {
            throw new IllegalArgumentException("Yêu cầu này đã được xử lý trước đó.");
        }
        String adminName = adminName(adminId);
        refund.setStatus("COMPLETED");
        refund.setTransactionCode(blank(request.getTransactionCode())
                ? generateTransactionCode(refund.getId())
                : clean(request.getTransactionCode()));
        refund.setProofUrl(clean(request.getProofUrl()));
        refund.setAdminNote(clean(request.getNote()));
        refund.setProcessedBy(adminName);
        refund.setCompletedAt(LocalDateTime.now());
        RefundRequest saved = repository.save(refund);

        fundService.recordTransaction(
                "BANK_TRANSFER", "EXPENSE", saved.getRefundAmount(),
                "Hoàn tiền chuyển khoản vé #" + saved.getBooking().getId(),
                "REFUND-" + saved.getId(), adminName);

        messagingTemplate.convertAndSend("/topic/refunds/" + saved.getUser().getId(), Map.of(
                "type", "REFUND_COMPLETED",
                "status", "COMPLETED",
                "refundId", saved.getId(),
                "bookingId", saved.getBooking().getId(),
                "amount", saved.getRefundAmount(),
                "message", "Khoản hoàn vé #" + saved.getBooking().getId() + " đã được chuyển vào tài khoản ngân hàng của bạn."
        ));
        return toResponse(saved);
    }

    @Transactional
    public RefundRequestResponse requestInformation(Long id, Long adminId, RefundActionRequest request) {
        RefundRequest refund = locked(id);
        if (!List.of("PENDING", "APPROVED").contains(refund.getStatus())) {
            throw new IllegalArgumentException("Yêu cầu này đã được xử lý trước đó.");
        }
        if (blank(request.getNote())) {
            throw new IllegalArgumentException("Vui lòng nhập thông tin khách cần bổ sung.");
        }
        refund.setStatus("NEEDS_INFO");
        refund.setAdminNote(clean(request.getNote()));
        refund.setProcessedBy(adminName(adminId));
        RefundRequest saved = repository.save(refund);
        messagingTemplate.convertAndSend("/topic/refunds/" + saved.getUser().getId(), Map.of(
                "type", "REFUND_NEEDS_INFO",
                "status", "NEEDS_INFO",
                "refundId", saved.getId(),
                "bookingId", saved.getBooking().getId(),
                "amount", saved.getRefundAmount(),
                "reason", saved.getAdminNote(),
                "message", "Yêu cầu hoàn tiền vé #" + saved.getBooking().getId() + " cần bổ sung thông tin. Nội dung: " + saved.getAdminNote()
        ));
        return toResponse(saved);
    }

    @Transactional
    public RefundRequestResponse resubmit(Long id, Long userId, RefundBankUpdateRequest request) {
        RefundRequest refund = locked(id);
        if (!refund.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền cập nhật yêu cầu hoàn tiền này.");
        }
        if (!"BANK_TRANSFER".equals(refund.getRefundMethod())) {
            throw new IllegalArgumentException("Yêu cầu hoàn tiền này không sử dụng chuyển khoản ngân hàng.");
        }
        if (!List.of("NEEDS_INFO", "REJECTED").contains(refund.getStatus())) {
            throw new IllegalArgumentException("Yêu cầu này không ở trạng thái cần bổ sung thông tin.");
        }
        validateBankDetails(request.getBankName(), request.getBankAccountNumber(), request.getBankAccountName());

        refund.setBankName(clean(request.getBankName()));
        refund.setBankAccountNumber(digits(request.getBankAccountNumber()));
        refund.setBankAccountName(clean(request.getBankAccountName()).toUpperCase());
        refund.setStatus("PENDING");
        refund.setAdminNote(null);
        refund.setProcessedBy(null);
        refund.setTransactionCode(null);
        refund.setProofUrl(null);
        refund.setCompletedAt(null);
        RefundRequest saved = repository.save(refund);
        messagingTemplate.convertAndSend("/topic/admin/refunds/new", "NEW_REFUND_REQUEST");
        return toResponse(saved);
    }

    private RefundRequestResponse toResponse(RefundRequest refund) {
        Booking booking = refund.getBooking();
        User user = refund.getUser();
        return RefundRequestResponse.builder()
                .id(refund.getId())
                .booking(RefundRequestResponse.BookingSummary.builder()
                        .id(booking.getId()).customerName(booking.getCustomerName())
                        .customerPhone(booking.getCustomerPhone()).build())
                .user(RefundRequestResponse.UserSummary.builder()
                        .id(user.getId()).fullName(user.getFullName()).phone(user.getPhone()).build())
                .refundMethod(refund.getRefundMethod()).refundAmount(refund.getRefundAmount())
                .status(refund.getStatus()).bankName(refund.getBankName())
                .bankAccountNumber(refund.getBankAccountNumber()).bankAccountName(refund.getBankAccountName())
                .transactionCode(refund.getTransactionCode()).proofUrl(refund.getProofUrl())
                .adminNote(refund.getAdminNote()).processedBy(refund.getProcessedBy())
                .createdAt(refund.getCreatedAt()).updatedAt(refund.getUpdatedAt())
                .completedAt(refund.getCompletedAt()).build();
    }

    private RefundRequest locked(Long id) {
        return repository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hoàn tiền."));
    }

    private void requireStatus(RefundRequest refund, String status) {
        if (!status.equals(refund.getStatus())) {
            throw new IllegalArgumentException("Yêu cầu này đã được xử lý trước đó.");
        }
    }

    private String adminName(Long adminId) {
        return userRepository.findById(adminId).map(User::getFullName).orElse("ADMIN");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String clean(String value) {
        return blank(value) ? null : value.trim();
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }

    private String generateTransactionCode(Long refundId) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RF" + refundId + "-" + timestamp;
    }

    private void validateBankDetails(String bankName, String accountNumberValue, String accountName) {
        if (blank(bankName) || blank(accountName)) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ ngân hàng và tên chủ tài khoản.");
        }
        if (!digits(accountNumberValue).matches("\\d{6,30}")) {
            throw new IllegalArgumentException("Số tài khoản ngân hàng không hợp lệ.");
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
