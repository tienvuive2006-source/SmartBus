ALTER TABLE trips
    ADD COLUMN IF NOT EXISTS route_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_trips_route_id
    ON trips(route_id);
