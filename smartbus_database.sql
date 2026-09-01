-- ===================================================================
-- SMARTBUS BOOKING SYSTEM - DATABASE DDL SCRIPT (POSTGRESQL / SUPABASE)
-- Project: Graduation Thesis FPT Polytechnic
-- Database: PostgreSQL 15+ Managed by Supabase
-- ===================================================================

-- 1. BANG NGUOI DUNG (users)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    wallet_balance NUMERIC(15,2) DEFAULT 0,
    avatar_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. BANG XE KHACH (buses)
CREATE TABLE IF NOT EXISTS buses (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    bus_type VARCHAR(50) NOT NULL, -- Limousine, Giuong Nam 34 phong, etc.
    total_seats INT NOT NULL DEFAULT 34,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    image_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. BANG TUYEN DUONG (routes)
CREATE TABLE IF NOT EXISTS routes (
    id BIGSERIAL PRIMARY KEY,
    departure_location VARCHAR(100) NOT NULL, -- Point A (e.g. Da Nang)
    arrival_location VARCHAR(100) NOT NULL,   -- Point B (e.g. Sai Gon)
    distance_km NUMERIC(8,2),
    estimated_duration_hours NUMERIC(4,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. BANG DIEM DUNG TUYEN DUONG (route_stops)
CREATE TABLE IF NOT EXISTS route_stops (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT REFERENCES routes(id) ON DELETE CASCADE,
    stop_name VARCHAR(150) NOT NULL,
    address TEXT,
    stop_order INT NOT NULL,
    estimated_offset_minutes INT DEFAULT 0
);

-- 5. BANG CHUYEN XE (trips)
CREATE TABLE IF NOT EXISTS trips (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT REFERENCES routes(id),
    bus_id BIGINT REFERENCES buses(id),
    driver_id BIGINT REFERENCES users(id),
    inspector_id BIGINT REFERENCES users(id),
    departure_time TIMESTAMP WITH TIME ZONE NOT NULL,
    arrival_time TIMESTAMP WITH TIME ZONE,
    ticket_price NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. BANG DAT VE (bookings)
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_code VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT REFERENCES users(id),
    trip_id BIGINT REFERENCES trips(id),
    seat_number VARCHAR(10) NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_phone VARCHAR(20) NOT NULL,
    passenger_email VARCHAR(100),
    total_price NUMERIC(12,2) NOT NULL,
    pickup_point TEXT,
    dropoff_point TEXT,
    payment_method VARCHAR(30) DEFAULT 'CASH', -- SEPAY, WALLET, CASH
    payment_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PAID, CANCELLED, REFUNDED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. BANG KHUYEN MAI (vouchers)
CREATE TABLE IF NOT EXISTS vouchers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_percent INT NOT NULL,
    max_discount_amount NUMERIC(12,2),
    min_order_amount NUMERIC(12,2) DEFAULT 0,
    valid_from TIMESTAMP WITH TIME ZONE,
    valid_to TIMESTAMP WITH TIME ZONE,
    usage_limit INT DEFAULT 100,
    used_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- 8. BANG HOAN TIEN (refund_requests)
CREATE TABLE IF NOT EXISTS refund_requests (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id),
    user_id BIGINT REFERENCES users(id),
    refund_amount NUMERIC(12,2) NOT NULL,
    reason TEXT,
    bank_name VARCHAR(100),
    bank_account_number VARCHAR(50),
    bank_account_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. BANG GIAO DICH TAI CHINH (fund_transactions)
CREATE TABLE IF NOT EXISTS fund_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    transaction_type VARCHAR(30) NOT NULL, -- TOPUP, BOOKING_PAYMENT, REFUND
    amount NUMERIC(15,2) NOT NULL,
    description TEXT,
    sepay_reference_code VARCHAR(100),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. BANG HANG HOA KY GUI (cargo_packages)
CREATE TABLE IF NOT EXISTS cargo_packages (
    id BIGSERIAL PRIMARY KEY,
    sender_name VARCHAR(100) NOT NULL,
    sender_phone VARCHAR(20) NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    trip_id BIGINT REFERENCES trips(id),
    package_weight_kg NUMERIC(6,2),
    shipping_fee NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'RECEIVED', -- RECEIVED, IN_TRANSIT, DELIVERED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- DULIEU MAU (ADMIN ACCOUNT DEMO)
INSERT INTO users (full_name, phone, password, email, role, wallet_balance)
VALUES ('Khách Hàng VIP Admin', '0905123456', '$2a$10$e8w.pUqXg4KjQc8E9V.rOO0KjQc8E9V.rOO0KjQc8E9V.rOO', 'admin@smartbus.com', 'ADMIN', 10000000)
ON CONFLICT (phone) DO NOTHING;
