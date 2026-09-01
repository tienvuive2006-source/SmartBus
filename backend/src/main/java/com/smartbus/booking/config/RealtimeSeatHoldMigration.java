package com.smartbus.booking.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migration nhỏ, idempotent cho tính năng giữ ghế realtime.
 * Dự án đang dùng ddl-auto=none và chưa bật Flyway nên cần chạy trước khi API nhận request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RealtimeSeatHoldMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public RealtimeSeatHoldMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                ALTER TABLE seat_reservations
                ADD COLUMN IF NOT EXISTS hold_token VARCHAR(64)
                """);

        jdbcTemplate.execute("""
                DELETE FROM seat_reservations older
                USING seat_reservations newer
                WHERE older.trip_id = newer.trip_id
                  AND older.seat_number = newer.seat_number
                  AND older.id < newer.id
                """);

        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_seat_reservations_trip_seat
                ON seat_reservations (trip_id, seat_number)
                """);

        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_seat_reservations_hold_token
                ON seat_reservations (hold_token)
                """);

        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_seat_reservations_expired_at
                ON seat_reservations (expired_at)
                """);
    }
}
