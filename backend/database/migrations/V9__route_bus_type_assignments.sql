CREATE TABLE IF NOT EXISTS route_bus_type_assignments (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    bus_type_id BIGINT NOT NULL REFERENCES bus_type(id) ON DELETE CASCADE,
    default_type BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_route_bus_type_assignment UNIQUE (route_id, bus_type_id)
);

CREATE INDEX IF NOT EXISTS idx_route_bus_type_route
    ON route_bus_type_assignments(route_id);

CREATE INDEX IF NOT EXISTS idx_route_bus_type_bus_type
    ON route_bus_type_assignments(bus_type_id);
