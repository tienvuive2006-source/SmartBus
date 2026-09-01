CREATE TABLE IF NOT EXISTS route_vehicle_assignments (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    bus_id BIGINT NOT NULL REFERENCES bus(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'PRIMARY',
    CONSTRAINT uk_route_vehicle_assignment UNIQUE (route_id, bus_id),
    CONSTRAINT ck_route_vehicle_role CHECK (role IN ('PRIMARY', 'BACKUP'))
);

CREATE INDEX IF NOT EXISTS idx_route_vehicle_route
    ON route_vehicle_assignments(route_id);

CREATE INDEX IF NOT EXISTS idx_route_vehicle_bus
    ON route_vehicle_assignments(bus_id);
