package com.smartbus.booking.controller;

import com.smartbus.booking.service.FundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/funds")
@CrossOrigin("*")
@RequiredArgsConstructor
public class FundController {

    private final FundService fundService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestParam(name = "startDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {
        return ResponseEntity.ok(fundService.getAllFundsStats(startDate, endDate));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam(name = "fundType", required = false, defaultValue = "ALL") String fundType,
            @RequestParam(name = "startDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {
        return ResponseEntity.ok(fundService.getTransactions(fundType, startDate, endDate));
    }

    @PostMapping("/sync-history")
    public ResponseEntity<?> syncHistory() {
        return ResponseEntity.ok(java.util.Map.of("success", true, "synced", fundService.syncHistoricalBookings()));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam(name = "fundType", required = false, defaultValue = "ALL") String fundType,
            @RequestParam(name = "startDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {
        
        java.util.List<com.smartbus.booking.entity.FundTransaction> transactions = fundService.getTransactions(fundType, startDate, endDate);
        
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Thời Gian,Nguồn Quỹ,Loại Giao Dịch,Số Tiền,Nội Dung,Người Thực Hiện,Mã Tham Chiếu\n");
        
        for (com.smartbus.booking.entity.FundTransaction tx : transactions) {
            String fundTypeStr = tx.getFundType();
            if ("CASH".equals(fundTypeStr)) fundTypeStr = "Tiền mặt";
            else if ("BANK_TRANSFER".equals(fundTypeStr)) fundTypeStr = "Ngân hàng";
            else if ("WALLET".equals(fundTypeStr)) fundTypeStr = "Ví nội bộ";
            
            String txTypeStr = "INCOME".equals(tx.getTransactionType()) ? "THU" : "CHI";
            
            sb.append(tx.getId()).append(",")
              .append(tx.getTransactionDate()).append(",")
              .append(fundTypeStr).append(",")
              .append(txTypeStr).append(",")
              .append(tx.getAmount()).append(",")
              .append("\"").append(tx.getDescription() != null ? tx.getDescription().replace("\"", "\"\"") : "").append("\",")
              .append("\"").append(tx.getPerformedBy() != null ? tx.getPerformedBy() : "Hệ thống").append("\",")
              .append(tx.getReferenceId() != null ? tx.getReferenceId() : "").append("\n");
        }
        
        byte[] csvBytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bom = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
        byte[] finalBytes = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, finalBytes, 0, bom.length);
        System.arraycopy(csvBytes, 0, finalBytes, bom.length, csvBytes.length);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=utf-8"));
        headers.setContentDispositionFormData("attachment", "bao-cao-quy.csv");
        
        return new ResponseEntity<>(finalBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}
