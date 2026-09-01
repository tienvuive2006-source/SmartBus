package com.smartbus.booking.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Chuyển dữ liệu ghế FEMALE cũ sang loại CHILD trước khi ứng dụng đọc sơ đồ ghế. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ChildSeatTypeMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public ChildSeatTypeMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                ALTER TABLE bus_type_seat_layout
                DROP CONSTRAINT IF EXISTS bus_type_seat_layout_seat_type_check
                """);
        jdbcTemplate.execute("""
                ALTER TABLE bus_type_seat_layout
                DROP CONSTRAINT IF EXISTS ck_bus_type_seat_layout_type
                """);
        jdbcTemplate.execute("""
                ALTER TABLE seats
                DROP CONSTRAINT IF EXISTS seats_seat_type_check
                """);
        jdbcTemplate.execute("""
                ALTER TABLE seats
                DROP CONSTRAINT IF EXISTS ck_seats_seat_type
                """);
        jdbcTemplate.update("UPDATE bus_type_seat_layout SET seat_type = 'CHILD' WHERE seat_type = 'FEMALE'");
        jdbcTemplate.update("UPDATE seats SET seat_type = 'CHILD' WHERE seat_type = 'FEMALE'");
        jdbcTemplate.execute("""
                ALTER TABLE bus_type_seat_layout
                ADD CONSTRAINT ck_bus_type_seat_layout_type
                CHECK (seat_type IN ('STANDARD', 'PRIORITY', 'CHILD'))
                """);
        jdbcTemplate.execute("""
                ALTER TABLE seats
                ADD CONSTRAINT ck_seats_seat_type
                CHECK (seat_type IN ('STANDARD', 'PRIORITY', 'CHILD'))
                """);
    }
}
