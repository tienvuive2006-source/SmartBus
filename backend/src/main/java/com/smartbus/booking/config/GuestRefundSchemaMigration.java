package com.smartbus.booking.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GuestRefundSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    void allowGuestRefundRequests() {
        jdbcTemplate.execute("ALTER TABLE refund_requests ALTER COLUMN user_id DROP NOT NULL");
    }
}
