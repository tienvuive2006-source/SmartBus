package com.smartbus.booking.controller;

import com.smartbus.booking.dto.ReviewRequest;
import com.smartbus.booking.entity.Booking;
import com.smartbus.booking.entity.Review;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.BookingRepository;
import com.smartbus.booking.repository.ReviewRepository;
import com.smartbus.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
@CrossOrigin("*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    // Tạo đánh giá mới (chỉ gọi từ client đã login)
    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestHeader("Authorization") String token, @RequestBody ReviewRequest request) {
        try {
            // Xác minh token tạm bỏ qua ở controller, vì bạn dùng AuthController cho login. Ở đây tạm tin client gửi lên hoặc lấy id từ request (bạn có filter JWT không?)
            // Để đơn giản, client nên gửi userId theo body hoặc mình lấy từ token.
            // Sửa lại ReviewRequest để nhận userId:
            return ResponseEntity.badRequest().body("Sử dụng endpoint /create/{userId}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @com.smartbus.booking.annotation.AuditAction(action = "CREATE_REVIEW", entityName = "Review")
    @PostMapping("/create/{userId}")
    public ResponseEntity<?> createReviewWithUserId(@PathVariable("userId") Long userId, @RequestBody ReviewRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mã đặt vé"));

            if (booking.getUser() == null || !booking.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bạn không có quyền đánh giá vé này!"));
            }

            if (!"PAID".equals(booking.getStatus()) && !"CHECKED_IN".equals(booking.getStatus()) && !"COMPLETED".equals(booking.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Chuyến đi chưa hoàn thành, không thể đánh giá!"));
            }

            if (reviewRepository.existsByBookingId(booking.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bạn đã đánh giá chuyến đi này rồi!"));
            }

            Review review = Review.builder()
                    .user(user)
                    .booking(booking)
                    .companyName(booking.getTrip().getCompanyName())
                    .busType(booking.getTrip().getBusType())
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .createdAt(LocalDateTime.now())
                    .build();

            reviewRepository.save(review);
            return ResponseEntity.ok(Map.of("message", "Cảm ơn bạn đã đánh giá chuyến đi!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Lấy tất cả đánh giá của 1 nhà xe (tùy chọn lọc theo loại xe)
    @GetMapping("/company/{companyName}")
    public ResponseEntity<?> getReviewsByCompany(@PathVariable("companyName") String companyName,
                                                 @RequestParam(name = "busType", required = false) String busType) {
        List<Review> reviews = reviewRepository.findByCompanyName(companyName);
        if (busType != null && !busType.isEmpty()) {
            reviews = reviews.stream()
                    .filter(r -> r.getBusType() != null && r.getBusType().equals(busType))
                    .toList();
        }
        return ResponseEntity.ok(reviews);
    }

    // Lấy thông kê (điểm trung bình, tổng số đánh giá) của 1 nhà xe
    @GetMapping("/company/{companyName}/stats")
    public ResponseEntity<?> getCompanyStats(@PathVariable("companyName") String companyName) {
        Double avgRating = reviewRepository.getAverageRatingByCompany(companyName);
        Long count = reviewRepository.getReviewCountByCompany(companyName);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", avgRating != null ? avgRating : 0.0);
        stats.put("totalReviews", count != null ? count : 0);
        return ResponseEntity.ok(stats);
    }

    // Lấy thông kê cho tất cả các nhà xe & loại xe
    @GetMapping("/stats/all")
    public ResponseEntity<?> getAllCompanyStats() {
        List<Review> allReviews = reviewRepository.findAll();
        Map<String, Map<String, Object>> result = new HashMap<>();
        
        for (Review r : allReviews) {
            // Group key: "CompanyName|BusType"
            String key = r.getCompanyName();
            if (r.getBusType() != null) {
                key += "|" + r.getBusType();
            }
            
            result.putIfAbsent(key, new HashMap<>());
            Map<String, Object> stats = result.get(key);
            
            long count = (long) stats.getOrDefault("totalReviews", 0L) + 1;
            double sum = (double) stats.getOrDefault("sumRating", 0.0) + r.getRating();
            
            stats.put("totalReviews", count);
            stats.put("sumRating", sum);
            stats.put("averageRating", sum / count);
        }
        
        return ResponseEntity.ok(result);
    }

    // ==========================================
    // API dành cho Quản trị viên (Admin)
    // ==========================================

    @GetMapping("/all")
    public ResponseEntity<List<Review>> getAllReviewsForAdmin() {
        return ResponseEntity.ok(reviewRepository.findAllWithDetails());
    }

    @com.smartbus.booking.annotation.AuditAction(action = "DELETE_REVIEW", entityName = "Review")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable("id") Long id) {
        if (!reviewRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        reviewRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa đánh giá thành công."));
    }

    @com.smartbus.booking.annotation.AuditAction(action = "REPLY_REVIEW", entityName = "Review")
    @PutMapping("/{id}/reply")
    public ResponseEntity<?> replyToReview(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        Optional<Review> reviewOpt = reviewRepository.findById(id);
        if (reviewOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Review review = reviewOpt.get();
        review.setAdminReply(payload.get("reply"));

        if (payload.containsKey("adminId")) {
            Long adminId = Long.parseLong(payload.get("adminId"));
            Optional<User> adminOpt = userRepository.findById(adminId);
            adminOpt.ifPresent(review::setRepliedBy);
        }

        reviewRepository.save(review);
        return ResponseEntity.ok(Map.of("message", "Đã gửi phản hồi thành công"));
    }
}
