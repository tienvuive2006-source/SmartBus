-- Chạy migration này trên PostgreSQL/Supabase trước khi khởi động phiên bản realtime.
ALTER TABLE seat_reservations
    ADD COLUMN IF NOT EXISTS hold_token VARCHAR(64);

-- Dọn bản ghi trùng cũ trước khi tạo khóa chống hai người giữ cùng một ghế.
DELETE FROM seat_reservations older
USING seat_reservations newer
WHERE older.trip_id = newer.trip_id
  AND older.seat_number = newer.seat_number
  AND older.id < newer.id;

CREATE UNIQUE INDEX IF NOT EXISTS uk_seat_reservations_trip_seat
    ON seat_reservations (trip_id, seat_number);

CREATE INDEX IF NOT EXISTS idx_seat_reservations_hold_token
    ON seat_reservations (hold_token);

CREATE INDEX IF NOT EXISTS idx_seat_reservations_expired_at
    ON seat_reservations (expired_at);
