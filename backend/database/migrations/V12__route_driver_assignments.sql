CREATE TABLE IF NOT EXISTS route_driver_assignments (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    driver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'PRIMARY',
    CONSTRAINT uk_route_driver_assignment UNIQUE (route_id, driver_id),
    CONSTRAINT ck_route_driver_role CHECK (role IN ('PRIMARY', 'BACKUP'))
);

CREATE INDEX IF NOT EXISTS idx_route_driver_route_id
    ON route_driver_assignments(route_id);

CREATE INDEX IF NOT EXISTS idx_route_driver_driver_id
    ON route_driver_assignments(driver_id);
