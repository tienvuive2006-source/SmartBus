package com.smartbus.booking.service;

import com.smartbus.booking.dto.SePayWebhookRequest;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;
    private final FundService fundService;
    private final com.smartbus.booking.repository.FundTransactionRepository fundTransactionRepository;
    private final com.smartbus.booking.repository.SystemSettingRepository systemSettingRepository;

    @org.springframework.beans.factory.annotation.Value("${sepay.token}")
    private String sepayToken;

    private double calculateBonus(double amount) {
        try {
            com.smartbus.booking.entity.SystemSetting setting = systemSettingRepository.findById("TOPUP_PROMOTIONS").orElse(null);
            if (setting == null || setting.getValue() == null || setting.getValue().isEmpty()) {
                return 0.0;
            }
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> tiers = mapper.readValue(setting.getValue(), 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>(){});
            
            double appliedBonusPercent = 0.0;
            java.time.LocalDate today = java.time.LocalDate.now();
            
            for (java.util.Map<String, Object> tier : tiers) {
                // Kiểm tra khoảng thời gian áp dụng
                if (tier.containsKey("startDate") && tier.get("startDate") != null && !tier.get("startDate").toString().isEmpty()) {
                    try {
                        java.time.LocalDate startDate = java.time.LocalDate.parse(tier.get("startDate").toString());
                        if (today.isBefore(startDate)) {
                            continue; // Bỏ qua nếu chưa tới ngày bắt đầu
                        }
                    } catch (Exception ignored) {}
                }
                
                if (tier.containsKey("endDate") && tier.get("endDate") != null && !tier.get("endDate").toString().isEmpty()) {
                    try {
                        java.time.LocalDate endDate = java.time.LocalDate.parse(tier.get("endDate").toString());
                        if (today.isAfter(endDate)) {
                            continue; // Bỏ qua nếu đã quá hạn
                        }
                    } catch (Exception ignored) {}
                }
                
                double minAmount = Double.parseDouble(tier.get("minAmount").toString());
                double maxAmount = 0.0;
                if (tier.containsKey("maxAmount") && tier.get("maxAmount") != null && !tier.get("maxAmount").toString().isEmpty()) {
                    maxAmount = Double.parseDouble(tier.get("maxAmount").toString());
                }
                
                if (amount >= minAmount && (maxAmount == 0 || amount <= maxAmount)) {
                    appliedBonusPercent = Double.parseDouble(tier.get("bonusPercent").toString());
                    break;
                }
            }
            return amount * appliedBonusPercent / 100.0;
        } catch (Exception e) {
            System.err.println("Error calculating bonus: " + e.getMessage());
            return 0.0;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processTopupWebhook(SePayWebhookRequest request) {
        if (request.getAmountIn() == null || request.getAmountIn() <= 0) {
            return;
        }

        String content = request.getTransactionContent();
        if (content == null) return;
        
        // Loại bỏ dấu tiếng Việt và viết hoa để dễ tìm kiếm (ví dụ: Nạp tiền 123 -> NAP TIEN 123)
        String normalized = java.text.Normalizer.normalize(content, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toUpperCase();

        // Tìm kiếm cú pháp: "NAP <USER_ID>" hoặc "NAP<USER_ID>"
        Pattern pattern = Pattern.compile("NAP\\s*(\\d+)");
        Matcher matcher = pattern.matcher(normalized);
        
        if (matcher.find()) {
            String userIdStr = matcher.group(1);
            try {
                Long userId = Long.valueOf(userIdStr);
                User user = userRepository.findById(userId).orElse(null);
                
                if (user != null) {
                    // Cập nhật số dư ví
                    double amountIn = request.getAmountIn();
                    double bonus = calculateBonus(amountIn);
                    double totalCredit = amountIn + bonus;

                    Double currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
                    user.setWalletBalance(currentBalance + totalCredit);
                    userRepository.save(user);

                    // Ghi nhận dòng tiền vào Quỹ hệ thống (FundTransaction)
                    String description = "Nạp tiền Ví điện tử (SePay) - Ref: " + (request.getReferenceCode() != null ? request.getReferenceCode() : request.getCode());
                    fundService.recordTransaction("BANK_TRANSFER", "INCOME", amountIn, description, userId.toString(), user.getFullName());
                    
                    if (bonus > 0) {
                        fundService.recordTransaction("BANK_TRANSFER", "EXPENSE", bonus, "Chi phí khuyến mãi nạp ví - " + user.getFullName(), "PROMO-" + System.currentTimeMillis(), user.getFullName());
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi xử lý nạp tiền ví (ID không hợp lệ): " + e.getMessage());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndProcessTopupPolling(Long userId, Double expectedAmount, String sessionStartTimeStr) {
        try {
            String urlStr = "https://my.sepay.vn/userapi/transactions/list";
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + sepayToken);
            conn.setRequestProperty("Content-Type", "application/json");

            if (conn.getResponseCode() == 200) {
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(response.toString());
                com.fasterxml.jackson.databind.JsonNode transactions = rootNode.get("transactions");
                
                if (transactions != null && transactions.isArray()) {
                    String expectedCompact = "nap" + userId;
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(^|[^a-z0-9])" + expectedCompact + "([^a-z0-9]|$)");
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    
                    for (com.fasterxml.jackson.databind.JsonNode txn : transactions) {
                        String rawContent = txn.get("transaction_content").asText().toLowerCase();
                        String normalizedContent = java.text.Normalizer.normalize(rawContent, java.text.Normalizer.Form.NFD)
                                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace("đ", "d");
                                
                        double amountIn = Double.parseDouble(txn.get("amount_in").asText());
                        String txnDateStr = txn.get("transaction_date").asText();
                        String txnCode = txn.get("id").asText(); // SePay Transaction ID
                        java.time.LocalDateTime txnDate = java.time.LocalDateTime.parse(txnDateStr, formatter);
                        
                        boolean isContentMatch = pattern.matcher(normalizedContent).find();
                        boolean isTimeValid = false;
                        if (sessionStartTimeStr != null) {
                            try {
                                java.time.Instant sessionStart = java.time.Instant.parse(sessionStartTimeStr);
                                java.time.ZoneId zoneVn = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
                                java.time.Instant txnInstant = txnDate.atZone(zoneVn).toInstant();
                                isTimeValid = txnInstant.isAfter(sessionStart.minus(2, java.time.temporal.ChronoUnit.MINUTES));
                            } catch (Exception e) {
                                isTimeValid = false;
                            }
                        } else {
                            isTimeValid = txnDate.isAfter(LocalDateTime.now().minusMinutes(15));
                        }
                        
                        if (isContentMatch && amountIn == expectedAmount && isTimeValid) {
                            // Check deduplication
                            String refId = "SEPAY-" + txnCode;
                            if (fundTransactionRepository.existsByReferenceId(refId)) {
                                continue; // Already processed
                            }

                            // Add money
                            User user = userRepository.findById(userId).orElse(null);
                            if (user != null) {
                                double bonus = calculateBonus(expectedAmount);
                                double totalCredit = expectedAmount + bonus;

                                Double currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
                                user.setWalletBalance(currentBalance + totalCredit);
                                userRepository.save(user);

                                // Record transaction
                                String description = "Nạp tiền Ví điện tử (SePay Polling) - Ref: " + txnCode;
                                fundService.recordTransaction("BANK_TRANSFER", "INCOME", expectedAmount, description, refId, user.getFullName());
                                
                                if (bonus > 0) {
                                    fundService.recordTransaction("BANK_TRANSFER", "EXPENSE", bonus, "Chi phí khuyến mãi nạp ví - " + user.getFullName(), "PROMO-" + txnCode, user.getFullName());
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
