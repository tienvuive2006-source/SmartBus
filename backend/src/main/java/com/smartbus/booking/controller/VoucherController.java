package com.smartbus.booking.controller;

import com.smartbus.booking.entity.User;
import com.smartbus.booking.entity.UserVoucher;
import com.smartbus.booking.entity.Voucher;
import com.smartbus.booking.repository.UserRepository;
import com.smartbus.booking.repository.UserVoucherRepository;
import com.smartbus.booking.repository.VoucherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/vouchers")
@CrossOrigin(origins = "*")
public class VoucherController {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;

    public VoucherController(VoucherRepository voucherRepository, UserVoucherRepository userVoucherRepository, UserRepository userRepository) {
        this.voucherRepository = voucherRepository;
        this.userVoucherRepository = userVoucherRepository;
        this.userRepository = userRepository;
    }

    // --- ADMIN APIs ---

    @GetMapping("/admin")
    public ResponseEntity<List<Voucher>> getAllVouchersAdmin() {
        List<Voucher> vouchers = voucherRepository.findAll();
        for (Voucher v : vouchers) {
            v.setUsageCount(userVoucherRepository.countByVoucherIdAndIsUsedTrue(v.getId()));
        }
        return ResponseEntity.ok(vouchers);
    }

    @PostMapping("/admin")
    public ResponseEntity<?> createVoucher(@RequestBody Voucher voucher) {
        if (voucherRepository.findByCode(voucher.getCode()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã voucher đã tồn tại!"));
        }
        return ResponseEntity.ok(voucherRepository.save(voucher));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateVoucher(@PathVariable("id") Long id, @RequestBody Voucher updates) {
        Optional<Voucher> opt = voucherRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Voucher existing = opt.get();
        
        if (!existing.getCode().equals(updates.getCode()) && voucherRepository.findByCode(updates.getCode()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã voucher đã tồn tại!"));
        }
        
        existing.setCode(updates.getCode());
        existing.setDiscountAmount(updates.getDiscountAmount());
        existing.setPointsCost(updates.getPointsCost());
        existing.setIsActive(updates.getIsActive());
        
        return ResponseEntity.ok(voucherRepository.save(existing));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteVoucher(@PathVariable("id") Long id) {
        voucherRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa voucher!"));
    }

    // --- USER APIs ---

    // Lấy danh sách Voucher đang Active trên hệ thống để Khách đổi
    @GetMapping("/available")
    public ResponseEntity<List<Voucher>> getAvailableVouchers() {
        List<Voucher> activeVouchers = voucherRepository.findAll().stream()
                .filter(Voucher::getIsActive)
                .toList();
        return ResponseEntity.ok(activeVouchers);
    }

    // Khách hàng đổi điểm lấy Voucher
    @PostMapping("/redeem/{voucherId}")
    public ResponseEntity<?> redeemVoucher(@PathVariable("voucherId") Long voucherId) {
        String currentUserIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = Long.parseLong(currentUserIdStr);
        
        Optional<User> userOpt = userRepository.findById(currentUserId);
        Optional<Voucher> voucherOpt = voucherRepository.findById(voucherId);
        
        if (userOpt.isEmpty() || voucherOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy người dùng hoặc Voucher."));
        }
        
        User user = userOpt.get();
        Voucher voucher = voucherOpt.get();
        
        if (!voucher.getIsActive()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Voucher này không còn khả dụng."));
        }
        
        if (user.getLoyaltyPoints() == null || user.getLoyaltyPoints() < voucher.getPointsCost()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bạn không đủ điểm thưởng để đổi Voucher này."));
        }
        
        // Trừ điểm
        user.setLoyaltyPoints(user.getLoyaltyPoints() - voucher.getPointsCost());
        userRepository.save(user);
        
        // Tạo UserVoucher
        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .build();
        userVoucherRepository.save(userVoucher);
        
        return ResponseEntity.ok(Map.of("message", "Đổi Voucher thành công!", "newPoints", user.getLoyaltyPoints()));
    }

    // Lấy danh sách Voucher của Khách hàng
    @GetMapping("/my-vouchers")
    public ResponseEntity<List<UserVoucher>> getMyVouchers() {
        String currentUserIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = Long.parseLong(currentUserIdStr);
        
        List<UserVoucher> myVouchers = userVoucherRepository.findByUserIdOrderByAcquiredAtDesc(currentUserId);
        return ResponseEntity.ok(myVouchers);
    }
}
