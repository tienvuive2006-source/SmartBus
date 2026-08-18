package com.smartbus.booking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class TicketExchangePaymentVerifier {

    private static final DateTimeFormatter SEPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${sepay.token}")
    private String sepayToken;

    public boolean hasMatchingTransfer(String paymentCode, double expectedAmount, LocalDateTime createdAt) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("https://my.sepay.vn/userapi/transactions/list").openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + sepayToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(8000);

            if (connection.getResponseCode() != 200) return false;

            JsonNode transactions = new ObjectMapper().readTree(connection.getInputStream()).get("transactions");
            if (transactions == null || !transactions.isArray()) return false;

            String compactCode = normalize(paymentCode).replaceAll("[^a-z0-9]", "");
            Pattern contentPattern = Pattern.compile("(^|[^a-z0-9])" + Pattern.quote(compactCode) + "([^a-z0-9]|$)");

            for (JsonNode transaction : transactions) {
                String content = normalize(transaction.path("transaction_content").asText());
                double amount = transaction.path("amount_in").asDouble(0);
                LocalDateTime transactionDate = LocalDateTime.parse(transaction.path("transaction_date").asText(), SEPAY_DATE_FORMAT);
                boolean contentMatches = contentPattern.matcher(content).find()
                        || content.replaceAll("[^a-z0-9]", "").contains(compactCode);
                boolean timeMatches = !transactionDate.isBefore(createdAt.minusMinutes(2));
                if (contentMatches && Double.compare(amount, expectedAmount) == 0 && timeMatches) return true;
            }
        } catch (Exception ignored) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
        return false;
    }

    private String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .toLowerCase();
    }
}
