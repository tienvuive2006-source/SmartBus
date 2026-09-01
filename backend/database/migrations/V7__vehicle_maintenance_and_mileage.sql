ALTER TABLE bus ADD COLUMN IF NOT EXISTS current_mileage DOUBLE PRECISION NOT NULL DEFAULT 0;
ALTER TABLE bus ADD COLUMN IF NOT EXISTS last_maintenance_mileage DOUBLE PRECISION NOT NULL DEFAULT 0;
ALTER TABLE bus ADD COLUMN IF NOT EXISTS maintenance_interval_km INTEGER NOT NULL DEFAULT 10000;
ALTER TABLE bus ADD COLUMN IF NOT EXISTS maintenance_alert_level INTEGER NOT NULL DEFAULT 0;

ALTER TABLE trips ADD COLUMN IF NOT EXISTS distance_km DOUBLE PRECISION;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS mileage_recorded BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS vehicle_mileage_log (
    id BIGSERIAL PRIMARY KEY,
    bus_id BIGINT NOT NULL REFERENCES bus(id),
    trip_id BIGINT REFERENCES trips(id),
    type VARCHAR(30) NOT NULL,
    previous_mileage DOUBLE PRECISION NOT NULL,
    distance_added DOUBLE PRECISION NOT NULL,
    new_mileage DOUBLE PRECISION NOT NULL,
    note VARCHAR(500),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_vehicle_mileage_trip UNIQUE (trip_id)
);

CREATE TABLE IF NOT EXISTS vehicle_maintenance (
    id BIGSERIAL PRIMARY KEY,
    bus_id BIGINT NOT NULL REFERENCES bus(id),
    maintenance_type VARCHAR(60) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    description VARCHAR(1000),
    scheduled_start TIMESTAMP NOT NULL,
    expected_end TIMESTAMP,
    completed_at TIMESTAMP,
    odometer_at_service DOUBLE PRECISION,
    estimated_cost DOUBLE PRECISION,
    actual_cost DOUBLE PRECISION,
    garage_name VARCHAR(200),
    notes VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_vehicle_maintenance_bus_status ON vehicle_maintenance(bus_id, status);
CREATE INDEX IF NOT EXISTS idx_vehicle_mileage_bus_time ON vehicle_mileage_log(bus_id, recorded_at DESC);
