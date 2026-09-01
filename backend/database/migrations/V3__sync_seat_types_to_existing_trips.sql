BEGIN;

UPDATE seats AS seat
SET seat_type = COALESCE((
    SELECT layout.seat_type
    FROM trips AS trip
    JOIN bus_type AS bus_type
        ON LOWER(bus_type.name) = LOWER(trip.bus_type)
    LEFT JOIN bus_type_seat_layout AS layout
        ON layout.bus_type_id = bus_type.id
        AND UPPER(layout.seat_number) = UPPER(seat.seat_number)
    WHERE trip.id = seat.trip_id
), 'STANDARD')
WHERE EXISTS (
    SELECT 1
    FROM trips AS trip
    JOIN bus_type AS bus_type
        ON LOWER(bus_type.name) = LOWER(trip.bus_type)
    JOIN bus_type_seat_layout AS layout
        ON layout.bus_type_id = bus_type.id
    WHERE trip.id = seat.trip_id
);

COMMIT;
