package com.smartbus.booking.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Cấp mã vé công khai ngẫu nhiên cho cả dữ liệu cũ, chạy an toàn nhiều lần. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class BookingTicketCodeMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public BookingTicketCodeMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                ALTER TABLE bookings
                ADD COLUMN IF NOT EXISTS ticket_code VARCHAR(19)
                """);
        jdbcTemplate.execute("""
                UPDATE bookings
                SET ticket_code = 'TN-' || UPPER(SUBSTRING(MD5(
                    RANDOM()::TEXT || CLOCK_TIMESTAMP()::TEXT || id::TEXT
                ), 1, 6))
                WHERE ticket_code IS NULL OR BTRIM(ticket_code) = ''
                """);
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_bookings_ticket_code
                ON bookings (ticket_code)
                """);
        jdbcTemplate.execute("""
                ALTER TABLE bookings
                ALTER COLUMN ticket_code SET NOT NULL
                """);
    }
}
