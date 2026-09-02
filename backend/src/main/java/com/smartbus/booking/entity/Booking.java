package com.smartbus.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã vé công khai, ngẫu nhiên; không dùng ID tăng dần để tra cứu/hủy vé. */
    // Cột cho phép tối đa 19 ký tự để tương thích mã đã phát hành trước đây;
    // mã vé mới vẫn giữ dạng ngắn TN-XXXXXX.
    @Column(name = "ticket_code", unique = true, length = 19)
    private String ticketCode;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerPhone;

    @Column(nullable = false)
    private String customerEmail;

    @ElementCollection
    @CollectionTable(name = "booking_seats", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "seat_number")
    private List<String> seatNumbers;

    @Column(nullable = false)
    private Double totalPrice;

    @Column(name = "discount_amount")
    @Builder.Default
    private Double discountAmount = 0.0;

    @Column(name = "applied_user_voucher_id")
    private Long appliedUserVoucherId;

    @Column(nullable = false)
    private String paymentMethod; // CASH, BANK_TRANSFER, WALLET

    @Column(nullable = false)
    private String status; // PENDING, PAID, CANCELLED, CHECKED_IN

    @Column(name = "refund_amount")
    @Builder.Default
    private Double refundAmount = 0.0;

    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user; // Optional: If the user is logged in

    @Column(name = "round_trip_group_id")
    private String roundTripGroupId;

    @Column(name = "trip_type")
    private String tripType; // OUTBOUND, RETURN

    @Transient
    private boolean isReviewed;

    @Transient
    private Review userReview;

    /** Loại của từng ghế để hiển thị; chỉ dựng khi trả API, không lưu trùng vào bookings. */
    @Transient
    private Map<String, SeatType> seatTypes;

    @PrePersist
    void ensureTicketCode() {
        if (ticketCode == null || ticketCode.isBlank()) {
            ticketCode = "TN-" + UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 6)
                    .toUpperCase();
        }
    }
}
