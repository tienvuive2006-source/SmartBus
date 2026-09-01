-- Chuyển loại ghế cũ FEMALE thành CHILD trên dữ liệu hiện có.
ALTER TABLE bus_type_seat_layout
    DROP CONSTRAINT IF EXISTS bus_type_seat_layout_seat_type_check;
ALTER TABLE bus_type_seat_layout
    DROP CONSTRAINT IF EXISTS ck_bus_type_seat_layout_type;
ALTER TABLE seats
    DROP CONSTRAINT IF EXISTS seats_seat_type_check;
ALTER TABLE seats
    DROP CONSTRAINT IF EXISTS ck_seats_seat_type;

UPDATE bus_type_seat_layout SET seat_type = 'CHILD' WHERE seat_type = 'FEMALE';
UPDATE seats SET seat_type = 'CHILD' WHERE seat_type = 'FEMALE';

ALTER TABLE bus_type_seat_layout
    ADD CONSTRAINT ck_bus_type_seat_layout_type
    CHECK (seat_type IN ('STANDARD', 'PRIORITY', 'CHILD'));
ALTER TABLE seats
    ADD CONSTRAINT ck_seats_seat_type
    CHECK (seat_type IN ('STANDARD', 'PRIORITY', 'CHILD'));
