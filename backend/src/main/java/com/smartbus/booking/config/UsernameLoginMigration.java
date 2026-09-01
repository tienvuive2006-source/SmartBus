package com.smartbus.booking.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Adds username login without breaking accounts created when phone was the login id. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class UsernameLoginMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public UsernameLoginMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(50)");
        jdbcTemplate.update("""
                UPDATE users target_user
                   SET username = CASE
                       WHEN NOT EXISTS (
                           SELECT 1
                             FROM users other
                            WHERE other.id <> target_user.id
                              AND LOWER(other.username) = LOWER(target_user.phone)
                       ) THEN LOWER(target_user.phone)
                       ELSE 'legacy_user_' || target_user.id
                   END
                 WHERE target_user.username IS NULL
                   AND target_user.phone !~ '^(0[0-9]{9}|\\+84[0-9]{9})$'
                   AND NOT EXISTS (
                       SELECT 1
                         FROM users fallback_owner
                        WHERE fallback_owner.id <> target_user.id
                          AND LOWER(fallback_owner.username) = 'legacy_user_' || target_user.id
                   )
                """);
        jdbcTemplate.update("""
                UPDATE users target_user
                   SET username = 'admin', phone = '0900000000'
                 WHERE UPPER(target_user.role) = 'ADMIN'
                   AND LOWER(target_user.phone) = 'admin'
                   AND NOT EXISTS (
                       SELECT 1 FROM users other
                        WHERE other.id <> target_user.id
                          AND (
                              other.phone = '0900000000'
                              OR LOWER(other.username) = 'admin'
                          )
                   )
                """);
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_users_username_lower
                    ON users (LOWER(username))
                 WHERE username IS NOT NULL
                """);
    }
}
