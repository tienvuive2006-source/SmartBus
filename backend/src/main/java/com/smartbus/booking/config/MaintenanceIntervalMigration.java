package com.smartbus.booking.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Đồng bộ chu kỳ bảo trì mặc định cũ từ 1.000 km lên 10.000 km. */
@Component
public class MaintenanceIntervalMigration implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public MaintenanceIntervalMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE bus ALTER COLUMN maintenance_interval_km SET DEFAULT 10000");
        jdbcTemplate.update("UPDATE bus SET maintenance_interval_km = 10000 WHERE maintenance_interval_km = 1000");
        jdbcTemplate.update("""
                UPDATE bus
                SET maintenance_alert_level = CASE
                    WHEN current_mileage - last_maintenance_mileage >= maintenance_interval_km THEN 100
                    WHEN current_mileage - last_maintenance_mileage >= maintenance_interval_km * 0.8 THEN 80
                    ELSE 0
                END
                """);
    }
}
