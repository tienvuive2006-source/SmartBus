BEGIN;

ALTER TABLE seats
    ADD COLUMN IF NOT EXISTS seat_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD';

CREATE TABLE IF NOT EXISTS bus_type_seat_layout (
    id BIGSERIAL PRIMARY KEY,
    bus_type_id BIGINT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    seat_floor INTEGER NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    CONSTRAINT fk_bus_type_seat_layout_bus_type
        FOREIGN KEY (bus_type_id) REFERENCES bus_type(id) ON DELETE CASCADE,
    CONSTRAINT uk_bus_type_seat_layout_number
        UNIQUE (bus_type_id, seat_number),
    CONSTRAINT ck_bus_type_seat_layout_floor CHECK (seat_floor IN (1, 2)),
    CONSTRAINT ck_bus_type_seat_layout_type
        CHECK (seat_type IN ('STANDARD', 'PRIORITY', 'CHILD'))
);

CREATE INDEX IF NOT EXISTS idx_bus_type_seat_layout_bus_type
    ON bus_type_seat_layout(bus_type_id);

COMMIT;
